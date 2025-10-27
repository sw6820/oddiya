import boto3
import json
import logging
from src.config import Config

logger = logging.getLogger(__name__)

class SqsClient:
    def __init__(self):
        self.client = boto3.client('sqs', region_name=Config.AWS_REGION)
        self.queue_url = Config.SQS_QUEUE_URL
    
    def receive_messages(self):
        try:
            response = self.client.receive_message(
                QueueUrl=self.queue_url,
                MaxNumberOfMessages=Config.MAX_MESSAGES,
                WaitTimeSeconds=Config.POLL_WAIT_TIME,
                VisibilityTimeout=Config.VISIBILITY_TIMEOUT
            )
            
            messages = response.get('Messages', [])
            logger.info(f"Received {len(messages)} messages from SQS")
            return messages
        except Exception as e:
            logger.error(f"Failed to receive SQS messages: {e}")
            return []
    
    def delete_message(self, receipt_handle):
        try:
            self.client.delete_message(
                QueueUrl=self.queue_url,
                ReceiptHandle=receipt_handle
            )
            logger.info("Message deleted from SQS")
        except Exception as e:
            logger.error(f"Failed to delete SQS message: {e}")
    
    def parse_message(self, message):
        try:
            body = json.loads(message['Body'])
            return {
                'job_id': body['jobId'],
                'user_id': body['userId'],
                'photo_urls': body['photoUrls'],
                'template': body['template'],
                'receipt_handle': message['ReceiptHandle']
            }
        except Exception as e:
            logger.error(f"Failed to parse message: {e}")
            return None

