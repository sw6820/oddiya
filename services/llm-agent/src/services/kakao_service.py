"""Kakao Local API service"""
import logging
from typing import List, Dict, Any, Optional

import httpx

from src.config import settings

logger = logging.getLogger(__name__)


class KakaoService:
    """Service for Kakao Local API integration"""
    
    def __init__(self):
        self.api_key = settings.kakao_rest_api_key
        self.base_url = "https://dapi.kakao.com/v2/local"
    
    async def search_places(
        self,
        query: str,
        category: Optional[str] = None,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        radius: int = 2000
    ) -> List[Dict[str, Any]]:
        """
        Search for places using Kakao Local API
        
        Args:
            query: Search query (place name, keyword)
            category: Category code (e.g., "FD6" for restaurants)
            latitude: Latitude for location-based search
            longitude: Longitude for location-based search
            radius: Search radius in meters
        
        Returns:
            List of place information
        """
        # TODO: Implement actual Kakao API call
        # For now, return mock data
        logger.info(f"Searching Kakao API for: {query}")
        
        return [
            {
                "id": "mock_1",
                "place_name": f"Mock {query} Place",
                "address_name": "Mock Address",
                "phone": "02-0000-0000",
                "x": longitude or 127.0276,
                "y": latitude or 37.4979,
                "category_group_name": category or "restaurant"
            }
        ]

