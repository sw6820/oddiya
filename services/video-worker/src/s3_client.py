import boto3
import os
import logging
from src.config import Config

logger = logging.getLogger(__name__)

class S3Client:
    def __init__(self):
        self.client = boto3.client('s3', region_name=Config.AWS_REGION)
        self.bucket = Config.S3_BUCKET
    
    def download_file(self, s3_key, local_path):
        try:
            self.client.download_file(self.bucket, s3_key, local_path)
            logger.info(f"Downloaded {s3_key} to {local_path}")
            return True
        except Exception as e:
            logger.error(f"Failed to download {s3_key}: {e}")
            return False
    
    def upload_file(self, local_path, s3_key):
        try:
            self.client.upload_file(local_path, self.bucket, s3_key)
            logger.info(f"Uploaded {local_path} to {s3_key}")
            
            # Return public URL
            return f"https://{self.bucket}.s3.{Config.AWS_REGION}.amazonaws.com/{s3_key}"
        except Exception as e:
            logger.error(f"Failed to upload {local_path}: {e}")
            return None

