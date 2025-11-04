# Travel Plan Generation Flow

## Architecture Overview

```
Mobile App / Web Browser
        ↓
   API Gateway (8080)
        ↓
   Plan Service (8083) - Java Spring Boot
        ↓ HTTP REST Call
   LLM Agent (8000) - Python FastAPI
        ↓ Uses prompts from
   prompts/system_prompts.yaml
        ↓ Calls
   AWS Bedrock - Claude Sonnet 3.5
```

## Components

### 1. Plan Service (Java)
**Location:** `/services/plan-service/`
**Port:** 8083
**Role:** API endpoint handler, calls Python LLM service

**Key File:** `src/main/java/com/oddiya/plan/service/PlanService.java`

```java
public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
    // 1. Generate title from destination
    String title = generateTitle(request.getDestination(), ...);

    // 2. Build request for Python LLM service
    LlmRequest llmRequest = new LlmRequest();
    llmRequest.setTitle(title);
    llmRequest.setLocation(request.getDestination());
    llmRequest.setStartDate(request.getStartDate().toString());
    llmRequest.setEndDate(request.getEndDate().toString());
    llmRequest.setBudget(determineBudgetLevel(request.getBudget()));

    // 3. Call Python LLM Agent
    return llmAgentClient.generatePlan(llmRequest)
        .map(llmResponse -> {
            // 4. Return plan directly WITHOUT saving to database
            PlanResponse response = new PlanResponse();
            // ... map fields from llmResponse
            return response;
        });
}
```

**Important:**
- ❌ **Does NOT save to database**
- ✅ **Returns plan directly** from Python service
- ✅ **Stateless** - no persistence

### 2. LLM Agent (Python)
**Location:** `/services/llm-agent/`
**Port:** 8000
**Role:** AI travel plan generation using Claude Sonnet

**Key Files:**
- `main.py` - FastAPI application
- `src/routes/langgraph_plans.py` - Plan generation endpoint
- `src/services/langgraph_planner.py` - LangGraph workflow

**API Endpoint:**
```
POST http://localhost:8000/api/v1/plans/generate
Content-Type: application/json

{
    "title": "Seoul 3-Day Adventure",
    "location": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": "medium"
}
```

### 3. Prompt Management
**Location:** `/services/llm-agent/prompts/system_prompts.yaml`

**✅ Prompts are managed separately from code!**

```yaml
system_message: |
  당신은 한국 여행 전문가입니다.
  사용자에게 실용적이고 현실적인 여행 계획을 제공합니다.

  원칙:
  - 실제 존재하는 관광지와 장소를 추천합니다
  - 예산에 맞는 활동을 제안합니다
  - 날씨를 고려한 일정을 짭니다

planning_prompt_template: |
  "{location}" 지역의 {num_days}일 여행 계획을 생성해주세요.

  여행 정보:
  - 제목: {title}
  - 일정: {start_date} ~ {end_date}
  - 예산 수준: {budget_level}

  요구사항:
  1. 정확히 {num_days}일의 일정을 만들어주세요
  2. {location}의 실제 존재하는 관광지, 식당, 카페를 구체적으로 명시하세요
  3. 각 활동마다 구체적인 시간을 명시하세요
  ...
```

**To Update Prompts:**
1. Edit `/services/llm-agent/prompts/system_prompts.yaml`
2. Restart LLM Agent: `lsof -ti:8000 | xargs kill; cd services/llm-agent && python main.py`
3. ✅ **No code changes needed!**

## Request Flow

### 1. User Creates Plan (Web/Mobile)
```javascript
// Web UI: index.html
const planRequest = {
    destination: "Seoul",
    startDate: "2025-12-01",
    endDate: "2025-12-03",
    budget: 500000,
    interests: ["food", "culture"]
};

fetch('http://localhost:8080/api/v1/plans', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + accessToken,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(planRequest)
});
```

### 2. API Gateway Routes Request
```yaml
# services/api-gateway/src/main/resources/application.yml
routes:
  - id: plan-service
    uri: http://localhost:8083
    predicates:
      - Path=/api/v1/plans/**
```

### 3. Plan Service Calls Python LLM Agent
```java
// Plan Service
llmAgentClient.generatePlan(llmRequest)
    .map(llmResponse -> convertToResponse(llmResponse));
```

**HTTP Request:**
```http
POST http://localhost:8000/api/v1/plans/generate
Content-Type: application/json

{
    "title": "Seoul 3-Day Trip",
    "location": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": "medium",
    "maxIterations": 3
}
```

### 4. Python LLM Agent Processing
```python
# src/services/langgraph_planner.py
async def generate_plan(self, title, location, start_date, end_date, budget):
    # 1. Load prompts from system_prompts.yaml
    system_message = prompt_loader.get_system_message()
    planning_prompt = prompt_loader.get_planning_prompt(...)

    # 2. Call AWS Bedrock Claude Sonnet 3.5
    response = bedrock.invoke_model(
        modelId="anthropic.claude-3-5-sonnet-20241022-v2:0",
        body={
            "messages": [
                {"role": "system", "content": system_message},
                {"role": "user", "content": planning_prompt}
            ]
        }
    )

    # 3. Parse and validate response
    plan = parse_json_response(response)

    # 4. Return structured plan
    return {
        "title": "Seoul 3-Day Adventure",
        "days": [
            {
                "day": 1,
                "location": "경복궁 & 북촌한옥마을",
                "activity": "Morning: 경복궁 (₩3,000), Afternoon: 북촌한옥마을 (무료)...",
                "estimatedCost": 50000
            },
            ...
        ]
    }
```

### 5. Response Returns to User
```json
{
    "userId": 1,
    "title": "Seoul 3-Day Adventure",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "details": [
        {
            "day": 1,
            "location": "경복궁 & 북촌한옥마을",
            "activity": "Morning: 경복궁 (₩3,000), Afternoon: 북촌한옥마을 (무료), Evening: 명동교자 (₩15,000)"
        }
    ],
    "createdAt": "2025-11-03T19:46:00",
    "updatedAt": "2025-11-03T19:46:00"
}
```

## How to Manage Prompts

### 1. View Current Prompts
```bash
cat /Users/wjs/cursor/oddiya/services/llm-agent/prompts/system_prompts.yaml
```

### 2. Edit Prompts
```bash
# Open in editor
vi /Users/wjs/cursor/oddiya/services/llm-agent/prompts/system_prompts.yaml

# Or use any text editor
open /Users/wjs/cursor/oddiya/services/llm-agent/prompts/system_prompts.yaml
```

### 3. Available Prompt Templates

**system_message:**
- Defines the AI's role and principles
- Example: "당신은 한국 여행 전문가입니다."

**planning_prompt_template:**
- Main prompt for generating travel plans
- Variables: `{location}`, `{num_days}`, `{title}`, `{budget_level}`
- Includes output format requirements

**refinement_prompt_template:**
- Used when iteratively improving plans
- Takes feedback and generates improved version

**validation_criteria:**
- Rules for validating generated plans
- Budget limits, day count, weather considerations

### 4. Restart Services After Prompt Changes
```bash
# Kill and restart LLM Agent
lsof -ti:8000 | xargs kill -9
cd /Users/wjs/cursor/oddiya/services/llm-agent
python main.py > /tmp/llm-agent.log 2>&1 &

# Plan Service doesn't need restart (it calls Python service)
```

## Testing the Flow

### 1. Check Services Are Running
```bash
# API Gateway (8080)
curl http://localhost:8080/actuator/health

# Plan Service (8083)
curl http://localhost:8083/actuator/health

# LLM Agent (8000)
curl http://localhost:8000/health
```

### 2. Test Plan Generation Directly (Python Service)
```bash
curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Seoul 3-Day Trip",
    "location": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": "medium"
  }'
```

### 3. Test Full Flow (Through Java Service)
```bash
curl -X POST http://localhost:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "destination": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": 500000,
    "interests": ["food", "culture"]
  }'
```

## Important Notes

### ✅ What's Working
- Java Plan Service calls Python LLM Agent via HTTP REST
- Prompts are managed in `prompts/system_prompts.yaml`
- Plans are generated by Claude Sonnet 3.5 via AWS Bedrock
- **Plans are NOT saved to database** (stateless)
- Full authentication flow with JWT tokens

### ⚠️ Current Limitations
- No database persistence (by design for now)
- User cannot view previously generated plans
- Each request generates a new plan (no caching)

### 🔧 Future Enhancements
When you want to add database persistence:
1. Remove `@Transactional` comment in `PlanService.createPlan()`
2. Add back `planRepository.save(plan)` call
3. Update `getUserPlans()` to fetch from database

## Troubleshooting

### Python Service Not Responding
```bash
# Check if running
lsof -ti:8000

# Check logs
tail -f /tmp/llm-agent.log

# Restart
cd /Users/wjs/cursor/oddiya/services/llm-agent
python main.py > /tmp/llm-agent.log 2>&1 &
```

### Java Service Can't Connect to Python
```bash
# Verify URL in application.yml
cat /Users/wjs/cursor/oddiya/services/plan-service/src/main/resources/application.yml | grep -A 5 "llm:"

# Should show:
# llm:
#   agent:
#     base-url: ${LLM_AGENT_URL:http://localhost:8000}
```

### AWS Bedrock Authentication Issues
```bash
# Check AWS credentials
aws sts get-caller-identity

# Verify Bedrock model access
aws bedrock list-foundation-models --region us-east-1
```
