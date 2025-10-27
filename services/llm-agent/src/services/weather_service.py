"""OpenWeatherMap API integration"""
import os
import httpx
import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)

class WeatherService:
    def __init__(self):
        self.api_key = os.getenv("OPENWEATHER_API_KEY", "")
        self.base_url = "https://api.openweathermap.org/data/2.5"
        
    async def get_weather_forecast(self, location: str, start_date: str) -> Dict[str, Any]:
        """
        Get weather forecast for a location
        
        Args:
            location: City name (e.g., "Seoul", "Busan")
            start_date: Date string (YYYY-MM-DD)
            
        Returns:
            Weather data including temperature, conditions, precipitation
        """
        if not self.api_key or self.api_key == "test-weather-key":
            # Return mock data for development
            return self._get_mock_weather(location)
        
        try:
            # Get coordinates for city
            geo_url = f"http://api.openweathermap.org/geo/1.0/direct"
            async with httpx.AsyncClient() as client:
                geo_response = await client.get(
                    geo_url,
                    params={
                        "q": location,
                        "limit": 1,
                        "appid": self.api_key
                    },
                    timeout=10.0
                )
                
                if geo_response.status_code != 200 or not geo_response.json():
                    return self._get_mock_weather(location)
                
                geo_data = geo_response.json()[0]
                lat, lon = geo_data["lat"], geo_data["lon"]
                
                # Get weather forecast
                forecast_response = await client.get(
                    f"{self.base_url}/forecast",
                    params={
                        "lat": lat,
                        "lon": lon,
                        "appid": self.api_key,
                        "units": "metric",  # Celsius
                        "lang": "en"
                    },
                    timeout=10.0
                )
                
                if forecast_response.status_code == 200:
                    forecast_data = forecast_response.json()
                    return self._parse_weather_data(forecast_data)
                else:
                    return self._get_mock_weather(location)
                    
        except Exception as e:
            logger.error(f"Weather API error: {e}")
            return self._get_mock_weather(location)
    
    def _parse_weather_data(self, data: Dict) -> Dict[str, Any]:
        """Parse OpenWeatherMap response"""
        if not data.get("list"):
            return self._get_mock_weather("Unknown")
        
        # Get first forecast
        forecast = data["list"][0]
        
        return {
            "temperature": {
                "current": round(forecast["main"]["temp"]),
                "min": round(forecast["main"]["temp_min"]),
                "max": round(forecast["main"]["temp_max"]),
                "feels_like": round(forecast["main"]["feels_like"])
            },
            "condition": forecast["weather"][0]["main"],
            "description": forecast["weather"][0]["description"],
            "humidity": forecast["main"]["humidity"],
            "wind_speed": forecast["wind"]["speed"],
            "precipitation_probability": forecast.get("pop", 0) * 100,  # 0-100%
            "recommendation": self._get_weather_recommendation(forecast)
        }
    
    def _get_weather_recommendation(self, forecast: Dict) -> str:
        """Generate weather-based recommendation"""
        temp = forecast["main"]["temp"]
        condition = forecast["weather"][0]["main"]
        pop = forecast.get("pop", 0)
        
        if pop > 0.7:
            return "🌧️ High chance of rain - bring an umbrella!"
        elif temp < 0:
            return "🧥 Very cold - dress warmly!"
        elif temp > 30:
            return "☀️ Very hot - stay hydrated!"
        elif condition in ["Clear", "Clouds"] and 15 <= temp <= 25:
            return "✨ Perfect weather for sightseeing!"
        else:
            return f"☁️ {condition} weather expected"
    
    def _get_mock_weather(self, location: str) -> Dict[str, Any]:
        """Return mock weather data for development"""
        return {
            "temperature": {
                "current": 18,
                "min": 12,
                "max": 22,
                "feels_like": 17
            },
            "condition": "Clear",
            "description": "clear sky",
            "humidity": 60,
            "wind_speed": 3.5,
            "precipitation_probability": 20,
            "recommendation": "✨ Perfect weather for sightseeing!",
            "_mock": True
        }

