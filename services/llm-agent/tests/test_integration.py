import pytest
from fastapi.testclient import TestClient
from main import app
import os

# Set test environment
os.environ["MOCK_MODE"] = "true"
os.environ["REDIS_HOST"] = "localhost"

client = TestClient(app)

def test_health_endpoint():
    """Test health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}

def test_generate_plan_with_mock():
    """Test plan generation with mock mode enabled"""
    request_data = {
        "title": "Seoul Weekend Trip",
        "startDate": "2025-12-01",
        "endDate": "2025-12-03"
    }
    
    response = client.post("/api/v1/plans/generate", json=request_data)
    
    # Should succeed with mock response
    assert response.status_code == 200
    data = response.json()
    
    assert "title" in data or "days" in data
    # Mock mode should return data without calling real Bedrock

def test_generate_plan_invalid_dates():
    """Test validation for invalid date range"""
    request_data = {
        "title": "Invalid Trip",
        "startDate": "2025-12-05",
        "endDate": "2025-12-01"  # End before start
    }
    
    response = client.post("/api/v1/plans/generate", json=request_data)
    
    # Should return error
    assert response.status_code in [400, 422, 500]

def test_concurrent_requests():
    """Test handling multiple concurrent requests"""
    import concurrent.futures
    
    def make_request():
        return client.post("/api/v1/plans/generate", json={
            "title": "Test",
            "startDate": "2025-12-01",
            "endDate": "2025-12-03"
        })
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(make_request) for _ in range(5)]
        results = [f.result() for f in futures]
    
    # All requests should complete
    assert len(results) == 5
    successful = sum(1 for r in results if r.status_code == 200)
    assert successful >= 3  # At least 60% success rate

