"""Redis caching service"""
import json
import logging
from typing import Any, Optional

import redis.asyncio as redis

from src.config import settings

logger = logging.getLogger(__name__)


class CacheService:
    """Service for Redis caching"""
    
    def __init__(self):
        self.client: Optional[redis.Redis] = None
    
    async def connect(self):
        """Connect to Redis"""
        if not self.client:
            self.client = redis.from_url(
                f"redis://{settings.redis_host}:{settings.redis_port}",
                password=settings.redis_password or None,
                decode_responses=True
            )
            logger.info(f"Connected to Redis at {settings.redis_host}:{settings.redis_port}")
    
    async def close(self):
        """Close Redis connection"""
        if self.client:
            await self.client.aclose()
            self.client = None
    
    async def get(self, key: str) -> Optional[Any]:
        """Get value from cache"""
        try:
            await self.connect()
            value = await self.client.get(key)
            if value:
                return json.loads(value)
            return None
        except Exception as e:
            logger.error(f"Cache get error: {e}")
            return None
    
    async def set(self, key: str, value: Any, ttl: int = None) -> bool:
        """Set value in cache"""
        try:
            await self.connect()
            serialized = json.dumps(value)
            ttl = ttl or settings.cache_ttl
            await self.client.set(key, serialized, ex=ttl)
            return True
        except Exception as e:
            logger.error(f"Cache set error: {e}")
            return False
    
    async def delete(self, key: str) -> bool:
        """Delete key from cache"""
        try:
            await self.connect()
            await self.client.delete(key)
            return True
        except Exception as e:
            logger.error(f"Cache delete error: {e}")
            return False

