"""Tests for Bedrock service"""
import pytest
from unittest.mock import Mock, patch

from src.services.bedrock_service import BedrockService


class TestBedrockService:
    """Test Bedrock service functionality"""
    
    def test_init_mock_mode(self):
        """Test Bedrock service initializes in mock mode"""
        service = BedrockService()
        assert service.bedrock_runtime is None
    
    @patch('src.services.bedrock_service.settings')
    def test_init_real_mode(self, mock_settings):
        """Test Bedrock service initializes in real mode"""
        mock_settings.use_bedrock_mock = False
        mock_settings.aws_region = "ap-northeast-2"
        
        with patch('src.services.bedrock_service.boto3'):
            service = BedrockService()
            # In real mode, bedrock_runtime would be set
            # For now just verify no exception is raised
    
    def test_generate_mock_plan(self):
        """Test mock plan generation"""
        service = BedrockService()
        
        plan = service._generate_mock_plan(
            location="Seoul",
            start_date="2025-01-01",
            end_date="2025-01-02",
            preferences=None
        )
        
        assert plan is not None
        assert "daily_plans" in plan
        assert len(plan["daily_plans"]) > 0
        assert "summary" in plan
    
    def test_generate_travel_plan_mock(self):
        """Test generate travel plan in mock mode"""
        service = BedrockService()
        
        plan = service.generate_travel_plan(
            location="Seoul",
            start_date="2025-01-01",
            end_date="2025-01-03"
        )
        
        assert plan is not None
        assert "daily_plans" in plan
        assert isinstance(plan["daily_plans"], list)
    
    def test_build_prompt(self):
        """Test prompt building"""
        service = BedrockService()
        
        prompt = service._build_prompt(
            location="Seoul",
            start_date="2025-01-01",
            end_date="2025-01-02",
            preferences={"budget": "medium"}
        )
        
        assert prompt is not None
        assert "Seoul" in prompt
        assert "2025-01-01" in prompt
        assert "budget" in prompt

