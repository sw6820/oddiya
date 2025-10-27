"""AWS Bedrock service for LLM integration"""
import json
import logging
from typing import Dict, Any, Optional

import boto3
from botocore.exceptions import ClientError

from src.config import settings

logger = logging.getLogger(__name__)


class BedrockService:
    """Service for interacting with AWS Bedrock Claude models"""
    
    def __init__(self):
        if not settings.use_bedrock_mock:
            self.bedrock_runtime = boto3.client(
                service_name='bedrock-runtime',
                region_name=settings.aws_region
            )
        else:
            self.bedrock_runtime = None
            logger.info("Running in Bedrock Mock Mode")
    
    def generate_travel_plan(
        self,
        location: str,
        start_date: str,
        end_date: str,
        preferences: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Generate travel plan using Bedrock Claude
        
        Args:
            location: Destination (e.g., "Seoul, Korea")
            start_date: Trip start date (YYYY-MM-DD)
            end_date: Trip end date (YYYY-MM-DD)
            preferences: User preferences
        
        Returns:
            Generated travel plan
        """
        if settings.use_bedrock_mock:
            return self._generate_mock_plan(location, start_date, end_date, preferences)
        
        try:
            # Prepare Bedrock request
            prompt = self._build_prompt(location, start_date, end_date, preferences)
            
            body = json.dumps({
                "anthropic_version": "bedrock-2023-05-31",
                "max_tokens": 2000,
                "messages": [
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            })
            
            # Call Bedrock
            response = self.bedrock_runtime.invoke_model(
                modelId=settings.bedrock_model_id,
                body=body
            )
            
            response_body = json.loads(response['body'].read())
            content = response_body['content'][0]['text']
            
            # Parse JSON response
            return json.loads(content)
            
        except ClientError as e:
            logger.error(f"Bedrock API error: {e}")
            raise Exception("Failed to generate travel plan")
    
    def _generate_mock_plan(
        self,
        location: str,
        start_date: str,
        end_date: str,
        preferences: Optional[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """Mock travel plan for development (avoids Bedrock costs)"""
        logger.info(f"Generating mock plan for {location}")
        
        return {
            "daily_plans": [
                {
                    "day": 1,
                    "date": start_date,
                    "locations": [
                        {"name": f"{location} Main Attraction", "type": "sightseeing"},
                        {"name": f"Traditional {location} Restaurant", "type": "restaurant"}
                    ],
                    "activities": [
                        "Explore historical sites",
                        "Visit local markets",
                        "Enjoy traditional cuisine"
                    ],
                    "notes": "First day - light schedule to adjust"
                },
                {
                    "day": 2,
                    "locations": [
                        {"name": f"{location} Museum", "type": "sightseeing"},
                        {"name": f"{location} Night Market", "type": "restaurant"}
                    ],
                    "activities": [
                        "Museum visit",
                        "Shopping",
                        "Street food experience"
                    ],
                    "notes": "Full day of exploration"
                }
            ],
            "summary": f"2-day itinerary for {location}"
        }
    
    def _build_prompt(
        self,
        location: str,
        start_date: str,
        end_date: str,
        preferences: Optional[Dict[str, Any]]
    ) -> str:
        """Build prompt for Bedrock"""
        pref_text = ""
        if preferences:
            pref_text = f"\nUser preferences: {json.dumps(preferences)}"
        
        return f"""Generate a detailed travel plan for {location} from {start_date} to {end_date}.

Requirements:
- Return as valid JSON only
- Include day-by-day itinerary
- For each day: locations, activities, restaurants, and notes
- Consider user preferences
- Suggest authentic local experiences

{pref_text}

Return format (JSON):
{{
  "daily_plans": [
    {{
      "day": 1,
      "date": "{start_date}",
      "locations": [{{"name": "...", "type": "sightseeing"}}],
      "activities": ["..."],
      "restaurants": [{{"name": "...", "type": "..."}}],
      "notes": "..."
    }}
  ],
  "summary": "..."
}}
"""

