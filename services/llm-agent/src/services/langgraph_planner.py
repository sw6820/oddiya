"""
LangGraph-based iterative travel plan generation
Uses state machine for multi-step planning with iteration and refinement
"""
import os
import logging
from typing import TypedDict, List, Dict, Any, Annotated
from datetime import datetime
import operator

# Phase 1: Using Gemini only, Bedrock commented out
# from langchain_aws import ChatBedrock
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langgraph.graph import StateGraph, END
from langsmith import Client, traceable

from src.services.weather_service import WeatherService
from src.utils.prompt_loader import get_prompt_loader

logger = logging.getLogger(__name__)

# Initialize LangSmith (optional, for monitoring)
try:
    langsmith_client = Client(
        api_key=os.getenv("LANGSMITH_API_KEY", ""),
        api_url=os.getenv("LANGSMITH_API_URL", "https://api.smith.langchain.com")
    )
    LANGSMITH_ENABLED = bool(os.getenv("LANGSMITH_API_KEY"))
except Exception as e:
    logger.warning(f"LangSmith not configured: {e}")
    LANGSMITH_ENABLED = False


# State definition for the graph
class PlanState(TypedDict):
    """State passed between nodes in the graph"""
    title: str
    location: str
    start_date: str
    end_date: str
    budget: str
    num_days: int
    
    # External data
    weather_data: Dict[str, Any]
    places_data: Dict[str, Any]
    
    # Planning state
    current_iteration: int
    max_iterations: int
    plan_draft: Dict[str, Any]
    feedback: List[str]
    final_plan: Dict[str, Any]
    
    # Messages for LLM
    messages: Annotated[List, operator.add]


class LangGraphPlanner:
    """Iterative travel planner using LangGraph"""
    
    def __init__(self):
        self.mock_mode = os.getenv("MOCK_MODE", "false").lower() == "true"  # Default: Use real LLM
        llm_provider = os.getenv("LLM_PROVIDER", "bedrock").lower()

        if not self.mock_mode:
            if llm_provider == "gemini":
                # Use Google Gemini
                api_key = os.getenv("GOOGLE_API_KEY")
                if not api_key:
                    logger.error("GOOGLE_API_KEY not set, falling back to mock mode")
                    self.mock_mode = True
                    self.llm = None
                else:
                    self.llm = ChatGoogleGenerativeAI(
                        model=os.getenv("GEMINI_MODEL", "gemini-pro"),
                        google_api_key=api_key,
                        temperature=0.7,
                        max_output_tokens=2048
                    )
                    logger.info(f"Initialized Gemini: {os.getenv('GEMINI_MODEL', 'gemini-pro')}")
            else:
                # Phase 1: Bedrock not installed, use Gemini only
                raise ValueError("Bedrock not available in Phase 1. Set LLM_PROVIDER=gemini and GOOGLE_API_KEY")
        else:
            self.llm = None  # Use mock responses
            logger.info("Mock mode enabled")

        self.weather_service = WeatherService()
        self.prompt_loader = get_prompt_loader()

        # Build the planning graph
        self.graph = self._build_planning_graph()
    
    def _build_planning_graph(self) -> StateGraph:
        """
        Build the LangGraph workflow:
        
        1. gather_context → Collect weather, places
        2. generate_draft → Create initial plan
        3. validate_plan → Check for issues
        4. refine_plan → Improve based on feedback (iterate)
        5. finalize_plan → Prepare final output
        """
        workflow = StateGraph(PlanState)
        
        # Add nodes
        workflow.add_node("gather_context", self.gather_context_node)
        workflow.add_node("generate_draft", self.generate_draft_node)
        workflow.add_node("validate_plan", self.validate_plan_node)
        workflow.add_node("refine_plan", self.refine_plan_node)
        workflow.add_node("finalize_plan", self.finalize_plan_node)
        
        # Define edges (workflow)
        workflow.set_entry_point("gather_context")
        
        workflow.add_edge("gather_context", "generate_draft")
        workflow.add_edge("generate_draft", "validate_plan")
        
        # Conditional edge: iterate or finalize
        workflow.add_conditional_edges(
            "validate_plan",
            self.should_iterate,
            {
                "refine": "refine_plan",
                "finalize": "finalize_plan"
            }
        )
        
        workflow.add_edge("refine_plan", "validate_plan")  # Loop back
        workflow.add_edge("finalize_plan", END)
        
        return workflow.compile()
    
    @traceable(name="gather_context")
    async def gather_context_node(self, state: PlanState) -> PlanState:
        """Node 1: Gather external data (weather, places)"""
        logger.info(f"[LangGraph] Gathering context for {state['location']}")
        
        # Get weather forecast
        weather = await self.weather_service.get_weather_forecast(
            location=state["location"],
            start_date=state["start_date"]
        )
        
        # Use weather data only (no Kakao API)
        state["weather_data"] = weather
        state["places_data"] = {}  # Claude has built-in Korea knowledge

        # Load system message from prompt file
        system_msg = self.prompt_loader.get_system_message()
        state["messages"] = [
            SystemMessage(content=system_msg)
        ]

        logger.info(f"[LangGraph] Context gathered - weather: {weather.get('description', 'N/A')}, temp: {weather.get('temperature', {}).get('current', 'N/A')}°C")
        return state
    
    @traceable(name="generate_draft")
    async def generate_draft_node(self, state: PlanState) -> PlanState:
        """Node 2: Generate initial plan draft"""
        logger.info(f"[LangGraph] Generating initial draft (iteration {state['current_iteration']})")
        
        # Build prompt with all context
        prompt = self._build_planning_prompt(state)
        
        if self.mock_mode or not self.llm:
            # Use mock response
            draft = self._generate_mock_draft(state)
        else:
            # Real LLM call
            messages = state["messages"] + [HumanMessage(content=prompt)]
            response = await self.llm.ainvoke(messages)
            logger.info(f"[LLM Response] Length: {len(response.content)} chars")
            logger.info(f"[LLM Response] Preview (first 500 chars): {response.content[:500]}")
            draft = self._parse_llm_response(response.content, state)
            state["messages"].append(AIMessage(content=response.content))
        
        state["plan_draft"] = draft
        logger.info(f"[LangGraph] Draft generated with {len(draft.get('days', []))} days")
        return state
    
    @traceable(name="validate_plan")
    async def validate_plan_node(self, state: PlanState) -> PlanState:
        """Node 3: Validate plan and generate feedback"""
        logger.info("[LangGraph] Validating plan")
        
        draft = state["plan_draft"]
        feedback = []
        
        # Check 1: Correct number of days
        expected_days = state["num_days"]
        actual_days = len(draft.get("days", []))
        if actual_days != expected_days:
            feedback.append(f"Plan should have {expected_days} days, but has {actual_days}")
        
        # Check 2: Budget alignment
        total_cost = draft.get("total_estimated_cost", 0)
        budget_limits = {"low": 100000, "medium": 200000, "high": 500000}
        max_budget = budget_limits.get(state["budget"], 200000) * state["num_days"]
        
        if total_cost > max_budget * 1.2:
            feedback.append(f"Plan exceeds budget: ₩{total_cost:,} > ₩{max_budget:,}")
        
        # Check 3: Weather considerations
        weather = state["weather_data"]
        if weather.get("precipitation_probability", 0) > 70:
            has_indoor = any("museum" in day.get("activity", "").lower() or
                            "mall" in day.get("activity", "").lower()
                            for day in draft.get("days", []))
            if not has_indoor:
                feedback.append("High chance of rain - add indoor activities")
        
        # Check 4: Activity format (Morning/Afternoon/Evening)
        for day in draft.get("days", []):
            activity = day.get("activity", "")
            if "Morning:" not in activity or "Afternoon:" not in activity or "Evening:" not in activity:
                feedback.append(f"Day {day.get('day')} missing proper activity format (Morning/Afternoon/Evening)")
                break  # Only report once

        state["feedback"] = feedback
        state["current_iteration"] += 1
        
        logger.info(f"[LangGraph] Validation complete: {len(feedback)} issues found")
        return state
    
    def should_iterate(self, state: PlanState) -> str:
        """Decision: continue iterating or finalize?"""
        has_feedback = len(state["feedback"]) > 0
        under_max = state["current_iteration"] < state["max_iterations"]
        
        if has_feedback and under_max:
            logger.info(f"[LangGraph] Decision: REFINE (iteration {state['current_iteration']})")
            return "refine"
        else:
            logger.info(f"[LangGraph] Decision: FINALIZE (iterations: {state['current_iteration']})")
            return "finalize"
    
    @traceable(name="refine_plan")
    async def refine_plan_node(self, state: PlanState) -> PlanState:
        """Node 4: Refine plan based on feedback"""
        logger.info(f"[LangGraph] Refining plan based on {len(state['feedback'])} feedback items")
        
        # Build refinement prompt using template
        feedback_text = "\n".join(f"- {fb}" for fb in state["feedback"])
        refinement_prompt = self.prompt_loader.get_refinement_prompt(
            feedback=feedback_text
        )
        
        if self.mock_mode or not self.llm:
            # Improve mock draft
            draft = state["plan_draft"]
            # Apply simple improvements
            if "exceeds budget" in feedback_text:
                for day in draft.get("days", []):
                    day["estimatedCost"] = int(day.get("estimatedCost", 100000) * 0.8)
            draft["_refined"] = True
            state["plan_draft"] = draft
        else:
            # Real LLM refinement
            messages = state["messages"] + [HumanMessage(content=refinement_prompt)]
            response = await self.llm.ainvoke(messages)
            refined = self._parse_llm_response(response.content, state)
            state["plan_draft"] = refined
            state["messages"].append(AIMessage(content=response.content))
        
        logger.info("[LangGraph] Plan refined")
        return state
    
    @traceable(name="finalize_plan")
    async def finalize_plan_node(self, state: PlanState) -> PlanState:
        """Node 5: Finalize and format plan"""
        logger.info("[LangGraph] Finalizing plan")
        
        final_plan = state["plan_draft"]
        
        # Add metadata
        llm_provider = os.getenv("LLM_PROVIDER", "bedrock").lower()
        if self.mock_mode:
            model_name = "Mock"
            architecture = "LangChain + LangGraph (Mock Mode)"
        elif llm_provider == "gemini":
            model_name = f"Google Gemini {os.getenv('GEMINI_MODEL', 'gemini-2.5-flash')}"
            architecture = "LangChain + LangGraph + Google Gemini"
        else:
            model_name = "Claude Sonnet 3.5"
            architecture = "LangChain + LangGraph + AWS Bedrock"

        final_plan["metadata"] = {
            "generated_at": datetime.now().isoformat(),
            "iterations": state["current_iteration"],
            "location": state["location"],
            "budget": state["budget"],
            "ai_model": model_name,
            "external_apis": ["OpenWeatherMap"],  # LLM-only for travel content
            "architecture": architecture
        }
        
        state["final_plan"] = final_plan
        logger.info(f"[LangGraph] Plan finalized after {state['current_iteration']} iterations")
        return state
    
    def _build_planning_prompt(self, state: PlanState) -> str:
        """Build comprehensive prompt using template from file"""
        weather = state["weather_data"]
        temp_data = weather.get('temperature', {})
        
        # Budget level in Korean
        budget_korean = {
            'low': '저예산',
            'medium': '중예산',
            'high': '고예산'
        }.get(state['budget'], '중예산')
        
        # Fill template with variables
        prompt = self.prompt_loader.get_planning_prompt(
            num_days=state['num_days'],
            location=state['location'],
            title=state['title'],
            start_date=state['start_date'],
            end_date=state['end_date'],
            budget_level=budget_korean,
            temperature=temp_data.get('current', 20),
            temp_min=temp_data.get('min', 15),
            temp_max=temp_data.get('max', 25),
            weather_condition=weather.get('description', 'Mild'),
            precipitation=weather.get('precipitation_probability', 0),
            weather_recommendation=weather.get('recommendation', '날씨 확인 필요')
        )
        
        return prompt
    
    def _parse_llm_response(self, content: str, state: PlanState) -> Dict[str, Any]:
        """Parse LLM response into structured plan"""
        import json
        import re

        try:
            # Try multiple parsing strategies

            # Strategy 1: Extract from markdown code block
            json_match = re.search(r'```(?:json)?\s*(\{.*?\})\s*```', content, re.DOTALL)
            if json_match:
                logger.info("[Parser] Found JSON in markdown code block")
                return json.loads(json_match.group(1))

            # Strategy 2: Find JSON object (greedy)
            json_match = re.search(r'\{(?:[^{}]|(?:\{[^{}]*\}))*\}', content, re.DOTALL)
            if json_match:
                json_str = json_match.group()
                logger.info(f"[Parser] Found JSON object, length: {len(json_str)}")
                try:
                    return json.loads(json_str)
                except json.JSONDecodeError as je:
                    # Try to fix common JSON issues
                    logger.warning(f"[Parser] JSON decode error: {je}, attempting fixes")
                    # Remove trailing commas
                    json_str = re.sub(r',\s*}', '}', json_str)
                    json_str = re.sub(r',\s*]', ']', json_str)
                    return json.loads(json_str)

            # Strategy 3: Use structured output hint
            logger.warning("[Parser] No JSON found, requesting structured output")
            # Fallback to mock if parsing fails
            return self._generate_mock_draft(state)
        except Exception as e:
            logger.error(f"Failed to parse LLM response: {e}")
            logger.error(f"Response preview (first 500 chars): {content[:500]}")
            return self._generate_mock_draft(state)
    
    def _generate_mock_draft(self, state: PlanState) -> Dict[str, Any]:
        """Generate mock plan draft"""
        places = state["places_data"]
        weather = state["weather_data"]
        
        budget_costs = {
            "low": {"daily": 50000, "meal": 15000, "transport": 5000},
            "medium": {"daily": 100000, "meal": 30000, "transport": 15000},
            "high": {"daily": 200000, "meal": 60000, "transport": 50000}
        }
        costs = budget_costs.get(state["budget"], budget_costs["medium"])
        
        days = []
        for day_num in range(1, state["num_days"] + 1):
            attraction_idx = (day_num - 1) % len(places.get("attractions", [{"place_name": "City Center"}]))
            restaurant_idx = (day_num - 1) % len(places.get("restaurants", [{"place_name": "Local Restaurant"}]))
            
            attraction = places.get("attractions", [{"place_name": "City Center"}])[attraction_idx]
            restaurant = places.get("restaurants", [{"place_name": "Local Restaurant"}])[restaurant_idx]
            
            days.append({
                "day": day_num,
                "location": attraction.get("place_name", "City Center"),
                "activity": f"Explore {attraction.get('place_name')}, lunch at {restaurant.get('place_name')}",
                "details": {
                    "morning": {
                        "time": "09:00-12:00",
                        "activity": f"Visit {attraction.get('place_name', 'attractions')}",
                        "location": attraction.get("place_name", "City"),
                        "cost": costs["transport"]
                    },
                    "afternoon": {
                        "time": "13:00-17:00",
                        "activity": f"Lunch and explore {restaurant.get('place_name', 'area')}",
                        "location": restaurant.get("place_name", "Restaurant"),
                        "cost": costs["meal"]
                    },
                    "evening": {
                        "time": "18:00-21:00",
                        "activity": "Dinner and night scene",
                        "cost": costs["meal"]
                    }
                },
                "estimatedCost": costs["daily"],
                "weatherTip": weather.get("recommendation", "Check weather before going")
            })
        
        return {
            "title": f"{state['location']} {state['num_days']}-Day Adventure",
            "days": days,
            "total_estimated_cost": costs["daily"] * state["num_days"],
            "currency": "KRW",
            "weather_summary": weather.get("description", "Pleasant weather"),
            "tips": [
                f"💰 Budget: ₩{costs['daily']:,} per day",
                f"🌤️ {weather.get('recommendation', 'Check weather forecast')}",
                "🚇 Use T-money card for public transport",
                "📱 Download Kakao Map app for navigation"
            ]
        }

    @traceable(name="generate_travel_plan")
    async def generate_plan(
        self,
        title: str,
        location: str,
        start_date: str,
        end_date: str,
        budget: str = "medium",
        max_iterations: int = 3
    ) -> Dict[str, Any]:
        """
        Main entry point: Generate travel plan with iterative refinement

        Args:
            title: Trip title
            location: City name
            start_date: Start date (YYYY-MM-DD)
            end_date: End date (YYYY-MM-DD)
            budget: Budget level (low/medium/high)
            max_iterations: Maximum refinement iterations

        Returns:
            Complete travel plan with AI recommendations
        """
        # Calculate number of days
        start = datetime.fromisoformat(start_date)
        end = datetime.fromisoformat(end_date)
        num_days = (end - start).days + 1

        # Initialize state
        initial_state: PlanState = {
            "title": title,
            "location": location,
            "start_date": start_date,
            "end_date": end_date,
            "budget": budget,
            "num_days": num_days,
            "weather_data": {},
            "places_data": {},
            "current_iteration": 0,
            "max_iterations": max_iterations,
            "plan_draft": {},
            "feedback": [],
            "final_plan": {},
            "messages": []
        }

        # Run the graph
        logger.info(f"[LangGraph] Starting plan generation for {location}, {num_days} days")

        try:
            result = await self.graph.ainvoke(initial_state)
            final_plan = result["final_plan"]

            logger.info(f"[LangGraph] Plan generated successfully after {result['current_iteration']} iterations")

            if LANGSMITH_ENABLED:
                logger.info("[LangSmith] Trace available in LangSmith dashboard")

            return final_plan

        except Exception as e:
            logger.error(f"[LangGraph] Error generating plan: {e}")
            # Return simple fallback
            return self._generate_mock_draft(initial_state)

    async def generate_plan_streaming(
        self,
        title: str,
        location: str,
        start_date: str,
        end_date: str,
        budget: str = "medium",
        max_iterations: int = 3
    ):
        """
        Streaming version of generate_plan - yields progress updates

        Yields events in format:
        {
            "type": "status|progress|chunk|complete|error",
            "message": "...",  # For status updates
            "progress": 0-100,  # For progress tracking
            "step": "gather_context|generate_draft|...",  # Current step
            "content": "...",  # For LLM chunks
            "plan": {...}  # Final plan for complete event
        }
        """
        from typing import AsyncGenerator

        # Calculate number of days
        start = datetime.fromisoformat(start_date)
        end = datetime.fromisoformat(end_date)
        num_days = (end - start).days + 1

        # Initialize state
        initial_state: PlanState = {
            "title": title,
            "location": location,
            "start_date": start_date,
            "end_date": end_date,
            "budget": budget,
            "num_days": num_days,
            "weather_data": {},
            "places_data": {},
            "current_iteration": 0,
            "max_iterations": max_iterations,
            "plan_draft": {},
            "feedback": [],
            "final_plan": {},
            "messages": []
        }

        logger.info(f"[LangGraph Streaming] Starting plan generation for {location}, {num_days} days")

        try:
            # Step 1: Gather context (20% progress)
            yield {
                "type": "status",
                "message": f"{location}의 날씨 정보를 수집하고 있습니다...",
                "progress": 10,
                "step": "gather_context"
            }

            state = await self.gather_context_node(initial_state)

            yield {
                "type": "progress",
                "message": "날씨 정보 수집 완료",
                "progress": 20,
                "step": "gather_context"
            }

            # Step 2: Generate draft (20-60% progress)
            yield {
                "type": "status",
                "message": "AI가 여행 계획을 생성하고 있습니다...",
                "progress": 30,
                "step": "generate_draft"
            }

            # For mock mode, use simple generation
            if self.mock_mode or not self.llm:
                draft = self._generate_mock_draft(state)
                state["plan_draft"] = draft
                state["messages"].append(AIMessage(content="Mock plan generated"))

                yield {
                    "type": "progress",
                    "message": "초안 생성 완료",
                    "progress": 60,
                    "step": "generate_draft"
                }
            else:
                # Real LLM streaming
                prompt = self._build_planning_prompt(state)
                messages = state["messages"] + [HumanMessage(content=prompt)]

                # Accumulate streamed content
                full_content = ""
                chunk_count = 0
                min_chunk_size = 10  # Minimum chars before yielding

                async for chunk in self.llm.astream(messages):
                    if hasattr(chunk, 'content') and chunk.content:
                        full_content += chunk.content
                        chunk_count += 1

                        # Yield chunks periodically (every 10 chunks or when min_chunk_size reached)
                        if chunk_count >= 10 and len(chunk.content) >= min_chunk_size:
                            yield {
                                "type": "chunk",
                                "content": chunk.content,
                                "progress": min(30 + (chunk_count // 10), 55),
                                "step": "generate_draft"
                            }
                            chunk_count = 0

                # Parse complete response
                draft = self._parse_llm_response(full_content, state)
                state["plan_draft"] = draft
                state["messages"].append(AIMessage(content=full_content))

                yield {
                    "type": "progress",
                    "message": f"{len(draft.get('days', []))}일 일정 초안 생성 완료",
                    "progress": 60,
                    "step": "generate_draft"
                }

            # Step 3: Validate plan (60-70%)
            yield {
                "type": "status",
                "message": "계획을 검증하고 있습니다...",
                "progress": 65,
                "step": "validate_plan"
            }

            state = await self.validate_plan_node(state)

            feedback_count = len(state["feedback"])
            if feedback_count > 0:
                yield {
                    "type": "progress",
                    "message": f"개선할 점 {feedback_count}개 발견",
                    "progress": 70,
                    "step": "validate_plan"
                }
            else:
                yield {
                    "type": "progress",
                    "message": "검증 완료 - 문제 없음",
                    "progress": 70,
                    "step": "validate_plan"
                }

            # Step 4: Refinement loop (70-90%)
            while self.should_iterate(state) == "refine":
                iteration = state["current_iteration"]
                yield {
                    "type": "status",
                    "message": f"계획을 개선하고 있습니다... (반복 {iteration})",
                    "progress": 70 + (iteration * 5),
                    "step": "refine_plan"
                }

                state = await self.refine_plan_node(state)
                state = await self.validate_plan_node(state)

                yield {
                    "type": "progress",
                    "message": f"개선 완료 (반복 {iteration})",
                    "progress": 75 + (iteration * 5),
                    "step": "refine_plan"
                }

            # Step 5: Finalize (90-100%)
            yield {
                "type": "status",
                "message": "최종 계획을 완성하고 있습니다...",
                "progress": 95,
                "step": "finalize_plan"
            }

            state = await self.finalize_plan_node(state)
            final_plan = state["final_plan"]

            logger.info(f"[LangGraph Streaming] Plan generated successfully after {state['current_iteration']} iterations")

            # Final complete event
            yield {
                "type": "complete",
                "message": "여행 계획 생성 완료!",
                "progress": 100,
                "step": "finalize_plan",
                "plan": final_plan
            }

        except Exception as e:
            logger.error(f"[LangGraph Streaming] Error: {e}")
            yield {
                "type": "error",
                "message": f"계획 생성 중 오류 발생: {str(e)}",
                "error": str(e)
            }

