#!/usr/bin/env python3
"""Mock LLM Agent for testing - returns sample travel plan data"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse
import uvicorn
from datetime import datetime, timedelta

app = FastAPI(title="Mock LLM Agent")

@app.post("/api/v1/plans/generate")
async def generate_plan(request: dict):
    """Generate a mock travel plan"""

    title = request.get("title", "여행")
    location = request.get("location", title.split()[0] if title else "서울")
    start_date = request.get("startDate", "2025-01-01")
    end_date = request.get("endDate", "2025-01-03")

    # Calculate number of days
    try:
        start = datetime.fromisoformat(start_date)
        end = datetime.fromisoformat(end_date)
        num_days = (end - start).days + 1
    except:
        num_days = 3

    # Generate mock days
    days = []
    for i in range(num_days):
        days.append({
            "day": i + 1,
            "location": f"{location} - Day {i+1}",
            "activity": f"오전: {location} 관광지 방문\n오후: {location} 맛집 투어\n저녁: {location} 야경 감상"
        })

    response = {
        "title": title,
        "days": days,
        "totalCost": 300000,
        "budget": request.get("budget", "medium")
    }

    return JSONResponse(content=response)

@app.get("/health")
async def health():
    return {"status": "ok", "service": "mock-llm-agent"}

if __name__ == "__main__":
    print("🚀 Starting Mock LLM Agent on http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)
