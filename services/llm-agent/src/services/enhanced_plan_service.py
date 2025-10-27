"""Enhanced travel plan generation with weather and real places"""
import logging
from typing import Dict, Any, List
from datetime import datetime, timedelta
import json

from src.services.bedrock_service import BedrockService
from src.services.kakao_service import KakaoService
from src.services.weather_service import WeatherService
from src.services.cache_service import CacheService

logger = logging.getLogger(__name__)

class EnhancedPlanService:
    def __init__(self):
        self.bedrock = BedrockService()
        self.kakao = KakaoService()
        self.weather = WeatherService()
        self.cache = CacheService()
    
    async def generate_complete_plan(
        self,
        title: str,
        start_date: str,
        end_date: str,
        budget: str = "medium",
        location: str = "Seoul"
    ) -> Dict[str, Any]:
        """
        Generate comprehensive travel plan with:
        - Real places from Kakao API
        - Weather forecast from OpenWeatherMap
        - Budget considerations
        - AI recommendations
        """
        
        # Check cache
        cache_key = f"enhanced_plan:{location}:{start_date}:{end_date}:{budget}"
        cached = await self.cache.get(cache_key)
        if cached:
            return cached
        
        # 1. Get weather forecast
        weather_data = await self.weather.get_weather_forecast(location, start_date)
        
        # 2. Get real places from Kakao
        places = await self._get_recommended_places(location, budget)
        
        # 3. Calculate trip duration
        start = datetime.fromisoformat(start_date)
        end = datetime.fromisoformat(end_date)
        num_days = (end - start).days + 1
        
        # 4. Generate AI plan with context
        plan = await self._generate_ai_plan_with_context(
            title=title,
            location=location,
            num_days=num_days,
            budget=budget,
            weather=weather_data,
            places=places
        )
        
        # 5. Cache result
        await self.cache.set(cache_key, plan, ttl=3600)
        
        return plan
    
    async def _get_recommended_places(self, location: str, budget: str) -> List[Dict]:
        """Get real places from Kakao API"""
        try:
            # Get popular tourist attractions
            attractions = await self.kakao.search_places(
                query=f"{location} tourist attractions",
                category="AT4"  # Tourist spots
            )
            
            # Get restaurants based on budget
            restaurants = await self.kakao.search_places(
                query=f"{location} restaurants",
                category="FD6"  # Food
            )
            
            # Get cafes
            cafes = await self.kakao.search_places(
                query=f"{location} cafe",
                category="CE7"  # Cafe
            )
            
            return {
                "attractions": attractions[:5],  # Top 5
                "restaurants": restaurants[:5],
                "cafes": cafes[:3]
            }
        except Exception as e:
            logger.error(f"Kakao API error: {e}")
            return self._get_mock_places(location)
    
    async def _generate_ai_plan_with_context(
        self,
        title: str,
        location: str,
        num_days: int,
        budget: str,
        weather: Dict,
        places: Dict
    ) -> Dict[str, Any]:
        """Generate AI plan with all context"""
        
        # Build context for AI
        context = f"""
Create a {num_days}-day travel itinerary for {location}.

WEATHER FORECAST:
- Temperature: {weather['temperature']['current']}°C ({weather['temperature']['min']}-{weather['temperature']['max']}°C)
- Condition: {weather['description']}
- {weather['recommendation']}

BUDGET: {budget.upper()}
- Low: Budget-friendly options, public transportation
- Medium: Mix of popular spots and local gems
- High: Premium experiences, convenience

REAL PLACES TO CONSIDER:
Attractions: {', '.join([p.get('place_name', '') for p in places.get('attractions', [])[:3]])}
Restaurants: {', '.join([p.get('place_name', '') for p in places.get('restaurants', [])[:3]])}

Create a detailed daily itinerary with:
1. Specific locations (use real places from above)
2. Activities (considering weather)
3. Estimated costs (considering budget)
4. Tips and recommendations
"""
        
        # Call Bedrock (or mock)
        if self.bedrock.mock_mode:
            return self._generate_mock_plan_with_places(num_days, location, places, weather, budget)
        
        # Real Bedrock call
        ai_response = self.bedrock.generate_travel_plan(
            location=location,
            start_date="",  # Included in context
            end_date="",
            preferences={"context": context}
        )
        
        return ai_response
    
    def _generate_mock_plan_with_places(
        self,
        num_days: int,
        location: str,
        places: Dict,
        weather: Dict,
        budget: str
    ) -> Dict[str, Any]:
        """Generate realistic mock plan"""
        
        days = []
        attractions = places.get('attractions', [])
        restaurants = places.get('restaurants', [])
        
        budget_info = {
            "low": {"daily": 50000, "meals": 15000, "transport": 5000},
            "medium": {"daily": 100000, "meals": 30000, "transport": 15000},
            "high": {"daily": 200000, "meals": 60000, "transport": 50000}
        }
        
        budget_kr = budget_info.get(budget, budget_info["medium"])
        
        for day in range(1, num_days + 1):
            attraction = attractions[day - 1] if day - 1 < len(attractions) else {"place_name": f"{location} landmark"}
            restaurant = restaurants[day - 1] if day - 1 < len(restaurants) else {"place_name": "Local restaurant"}
            
            days.append({
                "day": day,
                "location": attraction.get("place_name", f"{location} Area"),
                "activity": f"Visit {attraction.get('place_name', 'attractions')}, lunch at {restaurant.get('place_name', 'local spot')}",
                "details": {
                    "morning": {
                        "time": "09:00-12:00",
                        "activity": f"Explore {attraction.get('place_name', location)}",
                        "location": attraction.get("place_name", location),
                        "address": attraction.get("road_address_name", ""),
                        "cost": budget_kr["transport"]
                    },
                    "afternoon": {
                        "time": "12:00-14:00",
                        "activity": f"Lunch at {restaurant.get('place_name', 'local restaurant')}",
                        "location": restaurant.get("place_name", "Restaurant"),
                        "address": restaurant.get("road_address_name", ""),
                        "cost": budget_kr["meals"]
                    },
                    "evening": {
                        "time": "18:00-20:00",
                        "activity": "Dinner and explore night scenes",
                        "cost": budget_kr["meals"]
                    }
                },
                "estimated_cost": budget_kr["daily"],
                "weather_tip": weather.get("recommendation", "Check weather before going out")
            })
        
        return {
            "title": f"{location} {num_days}-Day Adventure",
            "days": days,
            "total_estimated_cost": budget_kr["daily"] * num_days,
            "currency": "KRW",
            "weather_summary": weather.get("description", "Mild weather"),
            "tips": [
                f"💰 Budget: ₩{budget_kr['daily']:,} per day",
                f"🌤️ {weather.get('recommendation', 'Check weather forecast')}",
                "🚇 Use T-money card for public transport",
                "📱 Download Kakao Map for navigation"
            ]
        }
    
    def _get_mock_places(self, location: str) -> Dict:
        """Mock places data"""
        return {
            "attractions": [
                {"place_name": f"{location} Palace", "road_address_name": f"{location} downtown"},
                {"place_name": f"{location} Tower", "road_address_name": f"{location} central"},
                {"place_name": f"{location} Museum", "road_address_name": f"{location} district"}
            ],
            "restaurants": [
                {"place_name": "Traditional Korean Restaurant", "road_address_name": f"{location}"},
                {"place_name": "Modern Fusion Cuisine", "road_address_name": f"{location}"},
                {"place_name": "Street Food Market", "road_address_name": f"{location}"}
            ],
            "cafes": [
                {"place_name": "Trendy Cafe", "road_address_name": f"{location}"}
            ]
        }

