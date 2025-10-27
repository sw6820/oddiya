"""Plan generation routes"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any

from src.services.bedrock_service import BedrockService
from src.services.cache_service import CacheService

router = APIRouter()
bedrock_service = BedrockService()
cache_service = CacheService()


class PlanRequest(BaseModel):
    """Request model for plan generation"""
    location: str
    start_date: str
    end_date: str
    preferences: Optional[Dict[str, Any]] = None


class PlanResponse(BaseModel):
    """Response model for generated plan"""
    daily_plans: list
    summary: str


@router.post("/plans/generate", response_model=PlanResponse)
async def generate_plan(request: PlanRequest):
    """
    Generate AI-powered travel plan
    
    Args:
        request: Plan generation request
    
    Returns:
        Generated travel plan with daily itinerary
    """
    try:
        # Check cache first
        cache_key = f"llm_plan:{request.location}:{request.start_date}:{request.end_date}"
        cached_plan = await cache_service.get(cache_key)
        
        if cached_plan:
            return cached_plan
        
        # Generate plan using Bedrock
        plan = bedrock_service.generate_travel_plan(
            location=request.location,
            start_date=request.start_date,
            end_date=request.end_date,
            preferences=request.preferences
        )
        
        # Cache the result
        await cache_service.set(cache_key, plan, ttl=3600)
        
        return plan
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to generate plan: {str(e)}")


@router.get("/health")
async def health():
    """Health check for plan service"""
    return {"status": "ok"}

