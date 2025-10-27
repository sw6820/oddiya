import os
import time
import logging
from src.config import Config
from src.database import Database
from src.sqs_client import SqsClient
from src.s3_client import S3Client
from src.video_generator import VideoGenerator
from src.sns_client import SnsClient

logger = logging.getLogger(__name__)

class VideoWorker:
    def __init__(self):
        self.db = Database()
        self.sqs = SqsClient()
        self.s3 = S3Client()
        self.video_gen = VideoGenerator()
        self.sns = SnsClient()
        self.temp_dir = "/tmp/oddiya-videos"
        os.makedirs(self.temp_dir, exist_ok=True)
    
    def start(self):
        logger.info("Video Worker started, polling SQS...")
        self.db.connect()
        
        while True:
            try:
                messages = self.sqs.receive_messages()
                
                for message in messages:
                    self.process_message(message)
                
                if not messages:
                    logger.debug("No messages, continuing long polling...")
                    
            except Exception as e:
                logger.error(f"Error in main loop: {e}")
                time.sleep(5)  # Brief pause before retrying
    
    def process_message(self, message):
        parsed = self.sqs.parse_message(message)
        if not parsed:
            return
        
        job_id = parsed['job_id']
        user_id = parsed['user_id']
        photo_urls = parsed['photo_urls']
        template = parsed['template']
        receipt_handle = parsed['receipt_handle']
        
        logger.info(f"Processing job {job_id}")
        
        try:
            # Check if job already processed (idempotency)
            job = self.db.get_job(job_id)
            if not job:
                logger.warning(f"Job {job_id} not found in database")
                self.sqs.delete_message(receipt_handle)
                return
            
            if job['status'] in ['COMPLETED', 'PROCESSING']:
                logger.info(f"Job {job_id} already {job['status']}, skipping")
                self.sqs.delete_message(receipt_handle)
                return
            
            # Update status to PROCESSING
            self.db.update_job_status(job_id, 'PROCESSING')
            
            # Download photos from S3
            photo_paths = []
            for i, url in enumerate(photo_urls):
                # Extract S3 key from URL
                s3_key = url.split(f"{Config.S3_BUCKET}/")[-1] if Config.S3_BUCKET in url else url
                local_path = os.path.join(self.temp_dir, f"job_{job_id}_photo_{i}.jpg")
                
                if self.s3.download_file(s3_key, local_path):
                    photo_paths.append(local_path)
                else:
                    raise Exception(f"Failed to download photo {i}")
            
            # Generate video
            output_path = os.path.join(self.temp_dir, f"job_{job_id}_video.mp4")
            if not self.video_gen.generate_video(photo_paths, output_path, template):
                raise Exception("Video generation failed")
            
            # Upload video to S3
            video_s3_key = f"videos/{job_id}/output.mp4"
            video_url = self.s3.upload_file(output_path, video_s3_key)
            if not video_url:
                raise Exception("Video upload failed")
            
            # Update job status to COMPLETED
            self.db.update_job_status(job_id, 'COMPLETED', video_url)
            
            # Send SNS notification
            self.sns.send_notification(user_id, job_id, video_url, 'COMPLETED')
            
            # Cleanup temp files
            self.video_gen.cleanup(output_path, *photo_paths)
            
            # Delete message from SQS
            self.sqs.delete_message(receipt_handle)
            
            logger.info(f"Job {job_id} completed successfully")
            
        except Exception as e:
            logger.error(f"Job {job_id} failed: {e}")
            self.db.update_job_status(job_id, 'FAILED')
            # Delete message so it doesn't retry indefinitely
            self.sqs.delete_message(receipt_handle)

