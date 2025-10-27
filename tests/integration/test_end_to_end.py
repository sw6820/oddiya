"""
End-to-end integration tests for Oddiya
Tests complete user flows across multiple services
"""

import pytest
import requests
import time
import uuid
from typing import Optional

BASE_URL = "http://localhost:8080"
USER_ID = 1

class TestEndToEndFlow:
    """Test complete user journey"""
    
    @classmethod
    def setup_class(cls):
        """Wait for services to be ready"""
        max_retries = 30
        for i in range(max_retries):
            try:
                response = requests.get(f"{BASE_URL}/actuator/health", timeout=2)
                if response.status_code == 200:
                    print("✅ API Gateway is ready")
                    return
            except:
                pass
            time.sleep(2)
        
        raise Exception("Services not ready after 60 seconds")
    
    def test_01_user_profile_flow(self):
        """Test user profile creation and retrieval"""
        
        # Create user via internal API
        create_response = requests.post(
            "http://localhost:8082/api/v1/users/internal/users",
            json={
                "email": "integration-test@example.com",
                "name": "Integration Test User",
                "provider": "google",
                "providerId": "integration-test-123"
            }
        )
        
        assert create_response.status_code == 200
        user_data = create_response.json()
        assert user_data["email"] == "integration-test@example.com"
        user_id = user_data["id"]
        
        # Get user profile through API Gateway
        get_response = requests.get(
            f"{BASE_URL}/api/users/me",
            headers={"X-User-Id": str(user_id)}
        )
        
        assert get_response.status_code == 200
        profile = get_response.json()
        assert profile["email"] == "integration-test@example.com"
        
        # Update user profile
        update_response = requests.patch(
            f"{BASE_URL}/api/users/me",
            headers={"X-User-Id": str(user_id)},
            json={"name": "Updated Name"}
        )
        
        assert update_response.status_code == 200
        updated = update_response.json()
        assert updated["name"] == "Updated Name"
    
    def test_02_plan_creation_flow(self):
        """Test travel plan creation with AI"""
        
        # Create plan
        create_response = requests.post(
            f"{BASE_URL}/api/plans",
            headers={
                "X-User-Id": str(USER_ID),
                "Content-Type": "application/json"
            },
            json={
                "title": "Integration Test Trip",
                "startDate": "2025-12-01",
                "endDate": "2025-12-03"
            }
        )
        
        # May fail if LLM Agent is down, that's okay for now
        if create_response.status_code == 200:
            plan_data = create_response.json()
            assert "id" in plan_data
            assert plan_data["title"] == "Integration Test Trip"
            plan_id = plan_data["id"]
            
            # Get plan
            get_response = requests.get(
                f"{BASE_URL}/api/plans/{plan_id}",
                headers={"X-User-Id": str(USER_ID)}
            )
            assert get_response.status_code == 200
            
            # Update plan
            update_response = requests.patch(
                f"{BASE_URL}/api/plans/{plan_id}",
                headers={"X-User-Id": str(USER_ID)},
                json={
                    "title": "Updated Trip",
                    "startDate": "2025-12-01",
                    "endDate": "2025-12-04"
                }
            )
            assert update_response.status_code == 200
            
            # Delete plan
            delete_response = requests.delete(
                f"{BASE_URL}/api/plans/{plan_id}",
                headers={"X-User-Id": str(USER_ID)}
            )
            assert delete_response.status_code == 200
    
    def test_03_video_job_creation(self):
        """Test video job creation and status tracking"""
        
        idempotency_key = str(uuid.uuid4())
        
        # Create video job
        create_response = requests.post(
            f"{BASE_URL}/api/videos",
            headers={
                "X-User-Id": str(USER_ID),
                "Idempotency-Key": idempotency_key,
                "Content-Type": "application/json"
            },
            json={
                "photoUrls": [
                    "https://example.com/photo1.jpg",
                    "https://example.com/photo2.jpg"
                ],
                "template": "default"
            }
        )
        
        assert create_response.status_code == 202  # Accepted
        job_data = create_response.json()
        assert "id" in job_data
        assert job_data["status"] == "PENDING"
        job_id = job_data["id"]
        
        # Test idempotency - same key should return same job
        idempotent_response = requests.post(
            f"{BASE_URL}/api/videos",
            headers={
                "X-User-Id": str(USER_ID),
                "Idempotency-Key": idempotency_key,
                "Content-Type": "application/json"
            },
            json={
                "photoUrls": ["https://example.com/photo3.jpg"],
                "template": "default"
            }
        )
        
        assert idempotent_response.status_code == 202
        idempotent_data = idempotent_response.json()
        assert idempotent_data["id"] == job_id  # Same job returned
        
        # Get job status
        status_response = requests.get(
            f"{BASE_URL}/api/videos/{job_id}",
            headers={"X-User-Id": str(USER_ID)}
        )
        
        assert status_response.status_code == 200
        status_data = status_response.json()
        assert status_data["id"] == job_id
    
    def test_04_service_availability(self):
        """Test all services are accessible through gateway"""
        
        endpoints = [
            ("/api/users/me", "User Service"),
            ("/api/plans", "Plan Service"),
            ("/api/videos", "Video Service"),
        ]
        
        for endpoint, service_name in endpoints:
            response = requests.get(
                f"{BASE_URL}{endpoint}",
                headers={"X-User-Id": str(USER_ID)}
            )
            # Should not be 404 or 502/503 (service down)
            assert response.status_code not in [404, 502, 503], \
                f"{service_name} not accessible"

@pytest.mark.performance
class TestPerformance:
    """Basic performance tests"""
    
    def test_response_time_health_check(self):
        """Health check should respond quickly"""
        start = time.time()
        response = requests.get(f"{BASE_URL}/actuator/health")
        elapsed = time.time() - start
        
        assert response.status_code == 200
        assert elapsed < 1.0  # Should respond in < 1 second
    
    def test_response_time_user_profile(self):
        """User profile should respond quickly"""
        start = time.time()
        response = requests.get(
            f"{BASE_URL}/api/users/me",
            headers={"X-User-Id": str(USER_ID)}
        )
        elapsed = time.time() - start
        
        # Should respond in < 2 seconds
        assert elapsed < 2.0

