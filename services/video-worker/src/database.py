import psycopg2
from psycopg2.extras import RealDictCursor
from src.config import Config
import logging

logger = logging.getLogger(__name__)

class Database:
    def __init__(self):
        self.connection = None
    
    def connect(self):
        try:
            self.connection = psycopg2.connect(
                host=Config.DB_HOST,
                port=Config.DB_PORT,
                database=Config.DB_NAME,
                user=Config.DB_USER,
                password=Config.DB_PASSWORD
            )
            logger.info("Database connected")
        except Exception as e:
            logger.error(f"Database connection failed: {e}")
            raise
    
    def disconnect(self):
        if self.connection:
            self.connection.close()
            logger.info("Database disconnected")
    
    def get_job(self, job_id):
        cursor = self.connection.cursor(cursor_factory=RealDictCursor)
        cursor.execute(
            "SELECT * FROM video_service.video_jobs WHERE id = %s",
            (job_id,)
        )
        return cursor.fetchone()
    
    def update_job_status(self, job_id, status, video_url=None):
        cursor = self.connection.cursor()
        if video_url:
            cursor.execute(
                """
                UPDATE video_service.video_jobs 
                SET status = %s, video_url = %s, updated_at = NOW()
                WHERE id = %s
                """,
                (status, video_url, job_id)
            )
        else:
            cursor.execute(
                """
                UPDATE video_service.video_jobs 
                SET status = %s, updated_at = NOW()
                WHERE id = %s
                """,
                (status, job_id)
            )
        self.connection.commit()
        logger.info(f"Job {job_id} status updated to {status}")

