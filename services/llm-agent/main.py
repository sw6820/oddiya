"""
LLM Agent Service - FastAPI application for AI-powered travel planning
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.routes.enhanced_plans import router as enhanced_plan_router
from src.config import settings

app = FastAPI(
    title="Oddiya LLM Agent",
    description="AI-powered travel plan generation using Bedrock and Kakao API",
    version="0.1.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure based on your needs
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include enhanced plan routes
app.include_router(enhanced_plan_router, prefix="/api/v1", tags=["plans"])


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "llm-agent"}


@app.get("/")
async def root():
    """Root endpoint"""
    return {"message": "Oddiya LLM Agent API"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

