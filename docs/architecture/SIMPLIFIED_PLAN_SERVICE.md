# Simplified Plan Service Architecture

## 핵심 원칙

**✅ 모든 플래닝 로직은 Python에서 처리**
**✅ Java 코드는 단순히 Python 호출만**
**✅ 프롬프트는 별도 YAML 파일에서 관리**

## 아키텍처

```
User Request
    ↓
API Gateway (8080)
    ↓
Java Plan Service (8083) ← 단순 HTTP 프록시, 플래닝 로직 없음
    ↓ HTTP POST
Python LLM Agent (8000) ← 모든 플래닝 로직 처리
    ↓ 프롬프트 로드
prompts/system_prompts.yaml ← 프롬프트 관리
    ↓ Claude 호출
AWS Bedrock Claude Sonnet 3.5
```

## Java 코드 (최소화됨)

### PlanService.java - 단순 프록시만
```java
@Service
public class PlanService {
    private final LlmAgentClient llmAgentClient;

    /**
     * 플래닝 로직 없음 - Python 호출만
     */
    public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
        // 1. Java → Python 형식 변환만
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setLocation(request.getDestination());
        llmRequest.setStartDate(request.getStartDate().toString());
        llmRequest.setEndDate(request.getEndDate().toString());
        llmRequest.setBudget(request.getBudget() != null ?
            String.valueOf(request.getBudget()) : null);

        // 2. Python 호출
        return llmAgentClient.generatePlan(llmRequest)
            .map(llmResponse -> {
                // 3. Python 응답 → Java DTO 매핑만
                PlanResponse response = new PlanResponse();
                response.setUserId(userId);
                response.setTitle(llmResponse.getTitle());
                // ... 단순 매핑
                return response;
            });
    }
}
```

**제거된 것들:**
- ❌ `generateTitle()` - Python이 처리
- ❌ `determineBudgetLevel()` - Python이 처리
- ❌ `planRepository` - DB 저장 안 함
- ❌ 모든 DB 관련 메서드 - stateless

### LlmRequest.java - 최소 필드만
```java
@Data
public class LlmRequest {
    private String location;   // "Seoul", "Busan"
    private String startDate;  // "2025-12-01"
    private String endDate;    // "2025-12-03"
    private String budget;     // "500000" or "medium"
    // title은 없음 - Python이 자동 생성
}
```

## Python 코드 (모든 로직)

### langgraph_plans.py - 자동 Title 생성
```python
class LangGraphPlanRequest(BaseModel):
    location: str  # Required
    startDate: str
    endDate: str
    budget: Optional[str] = "medium"
    title: Optional[str] = None  # Auto-generated

@router.post("/plans/generate")
async def generate_plan_with_langgraph(request: LangGraphPlanRequest):
    # Java에서 title을 보내지 않으면 자동 생성
    if not request.title:
        start = datetime.fromisoformat(request.startDate)
        end = datetime.fromisoformat(request.endDate)
        num_days = (end - start).days + 1
        request.title = f"{request.location} {num_days}-Day Trip"

    # 모든 플래닝 로직은 Python에서
    plan = await planner.generate_plan(
        title=request.title,
        location=request.location,
        start_date=request.startDate,
        end_date=request.endDate,
        budget=request.budget,
        max_iterations=3
    )
    return plan
```

## 프롬프트 관리 (코드 분리)

### prompts/system_prompts.yaml
```yaml
system_message: |
  당신은 한국 여행 전문가입니다.
  실제 존재하는 관광지와 장소를 추천합니다.

planning_prompt_template: |
  "{location}" 지역의 {num_days}일 여행 계획을 생성해주세요.

  여행 정보:
  - 제목: {title}
  - 일정: {start_date} ~ {end_date}
  - 예산: {budget_level}

  요구사항:
  1. {location}의 실제 존재하는 관광지 명시
  2. 구체적인 비용 계산
  3. 날씨 고려한 일정
  ...
```

**✅ 프롬프트만 수정하면 로직 변경 - 코드 수정 불필요!**

## 요청/응답 예시

### Java → Python 요청
```json
POST http://localhost:8000/api/v1/plans/generate
{
  "location": "Seoul",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "budget": "500000"
}
```

### Python → Java 응답
```json
{
  "title": "Seoul 3-Day Trip",
  "days": [
    {
      "day": 1,
      "location": "경복궁 & 북촌한옥마을",
      "activity": "Morning: 경복궁 (₩3,000)..."
    }
  ]
}
```

## 제거된 불필요한 코드

### Java에서 제거
1. **플래닝 로직:**
   - `generateTitle()` 메서드
   - `determineBudgetLevel()` 메서드
   - 날짜 계산 로직

2. **DB 관련:**
   - `@Transactional` 애노테이션
   - `planRepository.save()`
   - `getUserPlans()` 구현
   - `updatePlan()` 구현
   - `deletePlan()` 구현

3. **불필요한 Import:**
   - `TravelPlan` entity
   - `PlanDetail` entity
   - `@Transactional`

### 남은 코드 (최소한)
- `LlmAgentClient` - HTTP 호출만
- `createPlan()` - 요청 전달 & 응답 매핑만
- 단순 DTO 변환 로직

## 테스트

### 1. Python 서비스 직접 테스트
```bash
curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": "medium"
  }'
```

### 2. Java를 통한 전체 플로우
```bash
curl -X POST http://localhost:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "destination": "Seoul",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "budget": 500000
  }'
```

## 서비스 재시작

### Python 서비스 (프롬프트 수정 후)
```bash
lsof -ti:8000 | xargs kill -9
cd /Users/wjs/cursor/oddiya/services/llm-agent
source venv/bin/activate
python main.py > /tmp/llm-agent.log 2>&1 &
```

### Java 서비스 (코드 수정 후)
```bash
cd /Users/wjs/cursor/oddiya/services/plan-service
./gradlew clean build -x test
lsof -ti:8083 | xargs kill -9
/Users/wjs/cursor/oddiya/scripts/start-plan-service.sh
```

## 핵심 이점

✅ **분리된 관심사**
- Java: HTTP 프록시 & API 관리
- Python: AI 로직 & 프롬프트 처리

✅ **프롬프트 관리 용이**
- YAML 파일만 수정
- 코드 재빌드 불필요

✅ **간단한 Java 코드**
- 200+ 줄 → 80줄
- 복잡한 로직 없음
- 유지보수 쉬움

✅ **Stateless**
- DB 저장 안 함
- 매번 새로운 플랜 생성
- 확장 용이

## 향후 DB 저장 추가 시

만약 나중에 DB 저장이 필요하면:
1. `PlanService.createPlan()`에서 `planRepository.save()` 추가
2. `getUserPlans()` 구현
3. 플래닝 로직은 여전히 Python에서만!
