"""Enhanced plan generation with external APIs"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional

from src.services.enhanced_plan_service import EnhancedPlanService

router = APIRouter()
enhanced_service = EnhancedPlanService()

class EnhancedPlanRequest(BaseModel):
    title: str
    startDate: str  # Changed from start_date to match Java naming
    endDate: str    # Changed from end_date to match Java naming
    budget: Optional[str] = "medium"  # low, medium, high
    location: Optional[str] = "Seoul"

@router.post("/plans/generate")
async def generate_enhanced_plan(request: EnhancedPlanRequest):
    """
    Generate AI-powered travel plan with:
    - Real places from Kakao Local API
    - Weather forecast from OpenWeatherMap
    - Budget considerations
    - Detailed daily itinerary
    """
    try:
        plan = await enhanced_service.generate_complete_plan(
            title=request.title,
            start_date=request.startDate,
            end_date=request.endDate,
            budget=request.budget,
            location=request.location or "Seoul"
        )
        
        return plan
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to generate plan: {str(e)}"
        )

