import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # AWS
    AWS_REGION = os.getenv("AWS_REGION", "ap-northeast-2")
    SQS_QUEUE_URL = os.getenv("SQS_QUEUE_URL", "")
    SNS_TOPIC_ARN = os.getenv("SNS_TOPIC_ARN", "")
    S3_BUCKET = os.getenv("S3_BUCKET", "oddiya-storage")
    
    # Database
    DB_HOST = os.getenv("DB_HOST", "localhost")
    DB_PORT = os.getenv("DB_PORT", "5432")
    DB_NAME = os.getenv("DB_NAME", "oddiya")
    DB_USER = os.getenv("DB_USER", "oddiya_user")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "oddiya_password_dev")
    
    # Worker
    POLL_WAIT_TIME = int(os.getenv("POLL_WAIT_TIME", "20"))  # Long polling
    MAX_MESSAGES = int(os.getenv("MAX_MESSAGES", "1"))
    VISIBILITY_TIMEOUT = int(os.getenv("VISIBILITY_TIMEOUT", "300"))  # 5 minutes
    
    @classmethod
    def get_db_uri(cls):
        return f"postgresql://{cls.DB_USER}:{cls.DB_PASSWORD}@{cls.DB_HOST}:{cls.DB_PORT}/{cls.DB_NAME}"

