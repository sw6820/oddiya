"""LangGraph-based plan generation endpoint"""
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Optional
import json
import os
import logging

from src.services.langgraph_planner import LangGraphPlanner

# Initialize Redis for caching
try:
    from redis.asyncio import Redis
    redis_client = Redis(
        host=os.getenv("REDIS_HOST", "localhost"),
        port=int(os.getenv("REDIS_PORT", 6379)),
        decode_responses=True
    )
    REDIS_ENABLED = True
except Exception as e:
    logging.warning(f"Redis not available for caching: {e}")
    redis_client = None
    REDIS_ENABLED = False

router = APIRouter()
planner = LangGraphPlanner()

class LangGraphPlanRequest(BaseModel):
    location: str  # Destination city (required)
    startDate: str  # ISO format: "2025-12-01"
    endDate: str    # ISO format: "2025-12-03"
    budget: Optional[str] = "medium"  # Budget level or amount
    title: Optional[str] = None  # Auto-generated if not provided
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
    - Auto-generates title from location and dates if not provided
    """
    try:
        # Auto-generate title if not provided (Java service doesn't send it)
        from datetime import datetime
        if not request.title:
            start = datetime.fromisoformat(request.startDate)
            end = datetime.fromisoformat(request.endDate)
            num_days = (end - start).days + 1
            request.title = f"{request.location} {num_days}-Day Trip"

        plan = await planner.generate_plan(
            title=request.title,
            location=request.location,
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

@router.post("/plans/generate/stream")
async def generate_plan_streaming(request: LangGraphPlanRequest):
    """
    Generate AI travel plan with real-time streaming updates (ChatGPT-style)

    Returns Server-Sent Events (SSE) stream with progress updates:
    - Status messages (날씨 정보 수집 중...)
    - Progress percentage (0-100)
    - LLM content chunks (as plan is being generated)
    - Final complete plan

    Event types:
    - status: Status message with progress
    - progress: Progress milestone reached
    - chunk: Partial LLM output (like ChatGPT typing)
    - complete: Final plan ready
    - error: Error occurred

    Example client usage:
    ```javascript
    const response = await fetch('/api/v1/plans/generate/stream', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({location: 'Seoul', startDate: '2025-11-10', endDate: '2025-11-12'})
    });

    const reader = response.body.getReader();
    while (true) {
        const {done, value} = await reader.read();
        if (done) break;
        // Parse SSE format: "data: {...}\n\n"
        const event = JSON.parse(line.substring(6));
        if (event.type === 'complete') {
            console.log('Plan ready:', event.plan);
        }
    }
    ```
    """
    try:
        # Auto-generate title if not provided
        from datetime import datetime
        if not request.title:
            start = datetime.fromisoformat(request.startDate)
            end = datetime.fromisoformat(request.endDate)
            num_days = (end - start).days + 1
            request.title = f"{request.location} {num_days}-Day Trip"

        async def event_generator():
            """Generate SSE events from streaming planner"""
            try:
                # Check Redis cache first
                cache_key = f"plan:{request.location}:{request.startDate}:{request.endDate}:{request.budget}"

                if REDIS_ENABLED and redis_client:
                    try:
                        cached_plan = await redis_client.get(cache_key)
                        if cached_plan:
                            # Cache hit - return immediately with streaming format
                            cached_plan_json = json.loads(cached_plan)

                            yield f"data: {json.dumps({'type': 'status', 'message': '💾 저장된 계획을 불러오는 중...', 'progress': 50, 'cached': True})}\n\n"
                            yield f"data: {json.dumps({'type': 'complete', 'message': '✅ 저장된 계획 로드 완료!', 'progress': 100, 'plan': cached_plan_json, 'cached': True})}\n\n"
                            yield f"data: {json.dumps({'type': 'done'})}\n\n"
                            return
                    except Exception as cache_error:
                        logging.warning(f"Redis cache check failed: {cache_error}")

                # Cache miss - generate new plan with streaming
                final_plan = None
                async for event in planner.generate_plan_streaming(
                    title=request.title,
                    location=request.location,
                    start_date=request.startDate,
                    end_date=request.endDate,
                    budget=request.budget or "medium",
                    max_iterations=request.maxIterations or 3
                ):
                    # Format as SSE: "data: {json}\n\n"
                    event_json = json.dumps(event, ensure_ascii=False)
                    yield f"data: {event_json}\n\n"

                    # Save to cache when complete
                    if event.get('type') == 'complete' and event.get('plan'):
                        final_plan = event['plan']
                        if REDIS_ENABLED and redis_client:
                            try:
                                await redis_client.setex(
                                    cache_key,
                                    3600,  # 1 hour TTL
                                    json.dumps(final_plan, ensure_ascii=False)
                                )
                                logging.info(f"Plan cached: {cache_key}")
                            except Exception as cache_error:
                                logging.warning(f"Failed to cache plan: {cache_error}")

                # Send final "done" event to signal completion
                yield f"data: {json.dumps({'type': 'done'})}\n\n"

            except Exception as e:
                # Send error event
                error_event = {
                    "type": "error",
                    "message": f"계획 생성 중 오류 발생: {str(e)}",
                    "error": str(e)
                }
                yield f"data: {json.dumps(error_event, ensure_ascii=False)}\n\n"

        return StreamingResponse(
            event_generator(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no"  # Disable nginx buffering
            }
        )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to start streaming: {str(e)}"
        )

@router.get("/health")
async def health():
    """Health check"""
    return {
        "status": "healthy",
        "service": "LangGraph Planner",
        "features": ["LangChain", "LangGraph", "LangSmith"]
    }

