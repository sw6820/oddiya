"""LangGraph-based plan generation endpoint"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional

from src.services.langgraph_planner import LangGraphPlanner

router = APIRouter()
planner = LangGraphPlanner()

class LangGraphPlanRequest(BaseModel):
    title: str
    startDate: str
    endDate: str
    budget: Optional[str] = "medium"
    location: Optional[str] = "Seoul"
    maxIterations: Optional[int] = 3

@router.post("/plans/generate")
async def generate_plan_with_langgraph(request: LangGraphPlanRequest):
    """
    Generate AI travel plan with LangGraph iterative refinement
    
    Features:
    - Multi-step workflow (gather → draft → validate → refine → finalize)
    - Iterative improvement based on validation
    - Real places from Kakao API
    - Weather data from OpenWeatherMap
    - Budget considerations
    - LangSmith tracing (if configured)
    """
    try:
        plan = await planner.generate_plan(
            title=request.title,
            location=request.location or "Seoul",
            start_date=request.startDate,
            end_date=request.endDate,
            budget=request.budget or "medium",
            max_iterations=request.maxIterations or 3
        )
        
        return plan
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to generate plan: {str(e)}"
        )

@router.get("/health")
async def health():
    """Health check"""
    return {
        "status": "healthy",
        "service": "LangGraph Planner",
        "features": ["LangChain", "LangGraph", "LangSmith"]
    }

