import boto3
import json
import logging
from src.config import Config

logger = logging.getLogger(__name__)

class SnsClient:
    def __init__(self):
        self.client = boto3.client('sns', region_name=Config.AWS_REGION)
        self.topic_arn = Config.SNS_TOPIC_ARN
    
    def send_notification(self, user_id, job_id, video_url, status):
        try:
            message = {
                'userId': user_id,
                'jobId': job_id,
                'videoUrl': video_url,
                'status': status,
                'type': 'VIDEO_COMPLETE'
            }
            
            self.client.publish(
                TopicArn=self.topic_arn,
                Message=json.dumps(message),
                Subject='Video Processing Complete'
            )
            logger.info(f"SNS notification sent for job {job_id}")
        except Exception as e:
            logger.error(f"Failed to send SNS notification: {e}")

