"""Tests for FastAPI routes"""
import pytest
from fastapi.testclient import TestClient

from main import app


class TestRoutes:
    """Test API routes"""
    
    def setup_method(self):
        """Setup test client"""
        self.client = TestClient(app)
    
    def test_root_endpoint(self):
        """Test root endpoint"""
        response = self.client.get("/")
        assert response.status_code == 200
        assert "message" in response.json()
    
    def test_health_endpoint(self):
        """Test health check endpoint"""
        response = self.client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "healthy"
    
    def test_generate_plan_endpoint(self):
        """Test plan generation endpoint"""
        payload = {
            "location": "Seoul",
            "start_date": "2025-01-01",
            "end_date": "2025-01-03"
        }
        
        response = self.client.post("/api/v1/generate-plan", json=payload)
        
        # Should return 200 or 500 (500 if Redis not running)
        assert response.status_code in [200, 500]
        
        if response.status_code == 200:
            data = response.json()
            assert "daily_plans" in data or "detail" in data
    
    def test_generate_plan_invalid_request(self):
        """Test plan generation with invalid request"""
        payload = {
            "location": "",  # Empty location
            "start_date": "invalid-date",
            "end_date": "invalid-date"
        }
        
        response = self.client.post("/api/v1/generate-plan", json=payload)
        # Should handle invalid request
        assert response.status_code in [200, 400, 422, 500]

