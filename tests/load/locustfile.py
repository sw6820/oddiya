"""
Locust load testing scenarios for Oddiya
Run: locust -f locustfile.py --host=http://localhost:8080
"""

from locust import HttpUser, task, between
import uuid
import random

class OddiyaUser(HttpUser):
    """Simulates a mobile app user"""
    
    wait_time = between(1, 3)  # Wait 1-3 seconds between requests
    
    def on_start(self):
        """Setup - runs once per user"""
        self.user_id = random.randint(1, 100)
        self.headers = {"X-User-Id": str(self.user_id)}
    
    @task(10)
    def get_user_profile(self):
        """Get user profile (common operation)"""
        self.client.get("/api/users/me", headers=self.headers)
    
    @task(5)
    def get_plans(self):
        """List travel plans"""
        self.client.get("/api/plans", headers=self.headers)
    
    @task(2)
    def create_plan(self):
        """Create new travel plan (less frequent)"""
        self.client.post(
            "/api/plans",
            headers={**self.headers, "Content-Type": "application/json"},
            json={
                "title": f"Test Plan {random.randint(1, 1000)}",
                "startDate": "2025-12-01",
                "endDate": "2025-12-03"
            }
        )
    
    @task(1)
    def create_video(self):
        """Create video job (least frequent, most expensive)"""
        self.client.post(
            "/api/videos",
            headers={
                **self.headers,
                "Content-Type": "application/json",
                "Idempotency-Key": str(uuid.uuid4())
            },
            json={
                "photoUrls": [
                    "https://example.com/photo1.jpg",
                    "https://example.com/photo2.jpg"
                ],
                "template": "default"
            }
        )
    
    @task(3)
    def check_video_status(self):
        """Check video job status"""
        video_id = random.randint(1, 10)
        self.client.get(f"/api/videos/{video_id}", headers=self.headers)

class StressTestUser(HttpUser):
    """Stress testing - rapid requests"""
    
    wait_time = between(0.1, 0.5)  # Very short wait time
    
    def on_start(self):
        self.user_id = 1
        self.headers = {"X-User-Id": str(self.user_id)}
    
    @task
    def rapid_health_checks(self):
        """Rapid health check requests"""
        self.client.get("/actuator/health")
    
    @task
    def rapid_user_requests(self):
        """Rapid user profile requests"""
        self.client.get("/api/users/me", headers=self.headers)

class DatabaseStressUser(HttpUser):
    """Stress test database-heavy operations"""
    
    wait_time = between(0.5, 1)
    
    def on_start(self):
        self.user_id = random.randint(1, 50)
        self.headers = {"X-User-Id": str(self.user_id)}
    
    @task(3)
    def list_plans(self):
        """Database read operation"""
        self.client.get("/api/plans", headers=self.headers)
    
    @task(1)
    def create_plan(self):
        """Database write operation"""
        self.client.post(
            "/api/plans",
            headers={**self.headers, "Content-Type": "application/json"},
            json={
                "title": f"Load Test Plan {uuid.uuid4()}",
                "startDate": "2025-12-01",
                "endDate": "2025-12-03"
            }
        )

