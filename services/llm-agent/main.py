"""
LLM Agent Service - FastAPI application for AI-powered travel planning
"""
import os
import logging
from pathlib import Path
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse

# Load .env file explicitly
env_path = Path(__file__).parent / '.env'
load_dotenv(dotenv_path=env_path)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Log environment configuration
logger.info(f"=== LLM Agent Configuration ===")
logger.info(f"LLM_PROVIDER: {os.getenv('LLM_PROVIDER', 'NOT SET')}")
logger.info(f"GEMINI_MODEL: {os.getenv('GEMINI_MODEL', 'NOT SET')}")
logger.info(f"GOOGLE_API_KEY: {'SET' if os.getenv('GOOGLE_API_KEY') else 'NOT SET'}")
logger.info(f"MOCK_MODE: {os.getenv('MOCK_MODE', 'NOT SET (defaults to false)')}")
logger.info(f"==============================")

from src.routes.langgraph_plans import router as langgraph_plan_router
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

# Include LangGraph plan routes (with iteration and refinement)
app.include_router(langgraph_plan_router, prefix="/api/v1", tags=["plans"])

# Mount static files for test frontend
static_path = Path(__file__).parent / "static"
if static_path.exists():
    app.mount("/static", StaticFiles(directory=str(static_path)), name="static")


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "llm-agent"}


@app.get("/test")
async def test_page():
    """Serve the streaming test page"""
    static_file = Path(__file__).parent / "static" / "streaming-test.html"
    if static_file.exists():
        return FileResponse(static_file)
    return {"error": "Test page not found"}


@app.get("/")
async def root():
    """Root endpoint"""
    return {"message": "Oddiya LLM Agent API", "test_page": "/test"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

