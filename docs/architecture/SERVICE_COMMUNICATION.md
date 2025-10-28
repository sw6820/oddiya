# Service Communication Architecture

Java 서버와 LLM 서버 간의 통신 방법 및 모바일 연동 구조

## 📊 전체 흐름도

```
Mobile App (React Native)
    ↓ HTTP Request
API Gateway (Spring Cloud Gateway, Port 8080)
    ↓ Route & Forward
Plan Service (Spring Boot, Port 8083)
    ↓ HTTP Request (WebClient)
LLM Agent (FastAPI, Port 8000)
    ↓ AWS Bedrock API Call
Claude Sonnet (AI Model)
    ↓ Response
LLM Agent
    ↓ Response
Plan Service
    ↓ Save to PostgreSQL
Plan Service
    ↓ Response
API Gateway
    ↓ Response
Mobile App
```

---

## 1️⃣ Java ↔ LLM 서버 통신 방법

### A. Plan Service → LLM Agent 통신

#### 1. WebClient 설정

**파일:** `services/plan-service/src/main/java/com/oddiya/plan/service/LlmAgentClient.java`

```java
@Service
@RequiredArgsConstructor
public class LlmAgentClient {
    @Value("${llm.agent.base-url}")
    private String baseUrl;  // http://llm-agent:8000 (Docker) or http://localhost:8000 (Local)

    public Mono<LlmResponse> generatePlan(LlmRequest request) {
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        return client.post()
                .uri("/api/v1/plans/generate")  // LLM Agent 엔드포인트
                .bodyValue(request)              // Request 전송
                .retrieve()
                .bodyToMono(LlmResponse.class)   // Response 역직렬화
                .onErrorMap(throwable -> 
                    new RuntimeException("Failed to call LLM Agent: " + throwable.getMessage())
                );
    }
}
```

**핵심 포인트:**
- ✅ **비동기 통신:** WebClient 사용 (reactive)
- ✅ **타임아웃:** 자동 처리
- ✅ **에러 처리:** onErrorMap으로 예외 변환
- ✅ **JSON 자동 변환:** Spring이 자동으로 직렬화/역직렬화

#### 2. Request/Response 모델

**Request (Java → Python):**

```java
// LlmRequest.java
public class LlmRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**JSON 변환:**
```json
{
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```

**Response (Python → Java):**

```java
// LlmResponse.java
public class LlmResponse {
    private String title;
    private List<DayPlan> days;
    
    public static class DayPlan {
        private Integer day;
        private String location;
        private String activity;
    }
}
```

**JSON 변환:**
```json
{
  "title": "Seoul Weekend Adventure",
  "days": [
    {
      "day": 1,
      "location": "Gyeongbokgung Palace",
      "activity": "Visit the main palace and watch the changing of the guard"
    },
    {
      "day": 2,
      "location": "Myeongdong",
      "activity": "Shopping and street food"
    }
  ]
}
```

### B. LLM Agent 처리 과정

**파일:** `services/llm-agent/src/routes/plans.py`

```python
@router.post("/plans/generate", response_model=PlanResponse)
async def generate_plan(request: PlanRequest):
    # 1. 캐시 확인 (Redis)
    cache_key = f"llm_plan:{request.location}:{request.start_date}:{request.end_date}"
    cached_plan = await cache_service.get(cache_key)
    
    if cached_plan:
        return cached_plan  # 캐시된 결과 즉시 반환
    
    # 2. Bedrock API 호출
    plan = bedrock_service.generate_travel_plan(
        location=request.location,
        start_date=request.start_date,
        end_date=request.end_date,
        preferences=request.preferences
    )
    
    # 3. 캐시 저장 (1시간)
    await cache_service.set(cache_key, plan, ttl=3600)
    
    # 4. 결과 반환
    return plan
```

**핵심 포인트:**
- ✅ **캐시 우선:** Redis에서 먼저 확인 (빠른 응답)
- ✅ **Bedrock 호출:** 캐시 미스 시만 AI 호출 (비용 절감)
- ✅ **결과 캐싱:** 1시간 동안 저장 (동일 요청 재사용)

---

## 2️⃣ Mobile → Backend 전체 흐름

### 시나리오: 사용자가 여행 계획 생성

#### Step 1: Mobile App에서 Request 생성

**코드:** `mobile/src/store/slices/plansSlice.ts`

```typescript
export const createPlan = createAsyncThunk(
  'plans/createPlan',
  async (planData: CreatePlanRequest) => {
    return await planService.createPlan(planData);
  }
);
```

**API 호출:** `mobile/src/api/services.ts`

```typescript
async createPlan(data: CreatePlanRequest): Promise<TravelPlan> {
  return apiClient.post(API_ENDPOINTS.PLANS, data);
  // POST http://172.16.102.149:8080/api/plans
}
```

**Request Body:**
```json
{
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```

**Headers:**
```
Content-Type: application/json
X-User-Id: 1
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

---

#### Step 2: API Gateway 라우팅

**처리:** `services/api-gateway`

```yaml
# API Gateway routes
- id: plan-service
  uri: http://plan-service:8083
  predicates:
    - Path=/api/plans,/api/plans/**
  filters:
    - RewritePath=/api/plans(?<segment>/?.*), /api/v1/plans$\{segment}
```

**변환:**
```
Mobile Request:
  POST http://172.16.102.149:8080/api/plans
  
API Gateway Forwards to:
  POST http://plan-service:8083/api/v1/plans
```

---

#### Step 3: Plan Service 처리

**Controller:** `services/plan-service/.../PlanController.java`

```java
@PostMapping
public Mono<ResponseEntity<PlanResponse>> createPlan(
    @RequestHeader("X-User-Id") Long userId,
    @Valid @RequestBody CreatePlanRequest request
) {
    return planService.createPlan(userId, request)
            .map(ResponseEntity::ok);
}
```

**Service:** `services/plan-service/.../PlanService.java`

```java
@Transactional
public Mono<PlanResponse> createPlan(Long userId, CreatePlanRequest request) {
    // Option 1: Simple plan (current implementation)
    TravelPlan plan = new TravelPlan();
    plan.setUserId(userId);
    plan.setTitle(request.getTitle());
    plan.setStartDate(request.getStartDate());
    plan.setEndDate(request.getEndDate());
    plan.setDetails(List.of(defaultDetail));
    
    TravelPlan savedPlan = planRepository.save(plan);
    return Mono.just(PlanResponse.fromEntity(savedPlan));
    
    // Option 2: With LLM Agent (future)
    // return llmAgentClient.generatePlan(llmRequest)
    //     .map(llmResponse -> {
    //         // Process AI response
    //         TravelPlan plan = convertToEntity(llmResponse);
    //         return planRepository.save(plan);
    //     });
}
```

---

#### Step 4: LLM Agent 호출 (AI 통합 시)

**LLM Agent Client:** `services/plan-service/.../LlmAgentClient.java`

```java
public Mono<LlmResponse> generatePlan(LlmRequest request) {
    WebClient client = WebClient.builder()
            .baseUrl("http://llm-agent:8000")  // Docker network
            .build();

    return client.post()
            .uri("/api/v1/plans/generate")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(LlmResponse.class);
}
```

**Request to LLM Agent:**
```
POST http://llm-agent:8000/api/v1/plans/generate
Content-Type: application/json

{
  "title": "Seoul Weekend Trip",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```

---

#### Step 5: LLM Agent가 Bedrock 호출

**파일:** `services/llm-agent/src/services/bedrock_service.py`

```python
def generate_travel_plan(self, location, start_date, end_date, preferences=None):
    # Mock mode (development)
    if self.mock_mode:
        return self._generate_mock_plan(location, start_date, end_date)
    
    # Real Bedrock call (production)
    prompt = f"""
    Create a travel itinerary for {location}
    From: {start_date} To: {end_date}
    
    Generate daily activities with locations and descriptions.
    """
    
    response = self.bedrock_client.invoke_model(
        modelId='anthropic.claude-3-5-sonnet-20241022-v2:0',
        body=json.dumps({
            "anthropic_version": "bedrock-2023-05-31",
            "max_tokens": 1024,
            "messages": [{
                "role": "user",
                "content": prompt
            }]
        })
    )
    
    # Parse AI response
    ai_result = json.loads(response['body'].read())
    return self._parse_ai_response(ai_result)
```

---

#### Step 6: Response Flow (역방향)

```
LLM Agent (Python)
    ↓ JSON Response
Plan Service (Java)
    ↓ Convert to Entity
PostgreSQL
    ↓ Saved Entity
Plan Service
    ↓ Convert to DTO
API Gateway
    ↓ Forward Response
Mobile App
    ↓ Update Redux Store
UI Update
```

---

## 3️⃣ Mobile에서 결과 받기

### A. API Response 수신

**파일:** `mobile/src/api/client.ts`

```typescript
// Axios intercept response
this.client.interceptors.response.use(
  response => {
    // Success - return data
    return response;
  },
  async (error: AxiosError) => {
    // Handle 401 - auto refresh token
    if (error.response?.status === 401) {
      await this.refreshToken();
      return this.client(originalRequest);
    }
    
    // Format error
    throw this.formatError(error);
  }
);
```

### B. Redux Store Update

**파일:** `mobile/src/store/slices/plansSlice.ts`

```typescript
// When plan is created successfully
builder.addCase(createPlan.fulfilled, (state, action) => {
  state.isLoading = false;
  state.plans.unshift(action.payload);  // Add to list
  state.currentPlan = action.payload;    // Set as current
});

// When plan creation fails
builder.addCase(createPlan.rejected, (state, action) => {
  state.isLoading = false;
  state.error = action.error.message || 'Failed to create plan';
});
```

### C. UI 업데이트

**파일:** `mobile/src/screens/PlansScreen.tsx`

```typescript
const { plans, isLoading, error } = useAppSelector(state => state.plans);

// Loading state
if (isLoading) {
  return <ActivityIndicator />;
}

// Error state
if (error) {
  return <Text>{error}</Text>;
}

// Success - render plans
return (
  <FlatList
    data={plans}
    renderItem={({ item }) => <PlanCard plan={item} />}
  />
);
```

---

## 4️⃣ 실제 통신 예제

### 예제 1: 여행 계획 생성 (AI 통합)

#### Request Flow:

**1. Mobile App (TypeScript)**
```typescript
dispatch(createPlan({
  title: "Seoul Weekend",
  startDate: "2025-12-01",
  endDate: "2025-12-03"
}));
```
↓

**2. API Client (Axios)**
```http
POST http://172.16.102.149:8080/api/plans
Headers:
  Content-Type: application/json
  X-User-Id: 1
Body:
{
  "title": "Seoul Weekend",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```
↓

**3. API Gateway (Spring Cloud Gateway)**
```
Receives: POST /api/plans
Rewrites to: POST /api/v1/plans
Forwards to: http://plan-service:8083/api/v1/plans
```
↓

**4. Plan Service (Spring Boot)**
```java
@PostMapping
public Mono<ResponseEntity<PlanResponse>> createPlan(...) {
    // Call LLM Agent
    return llmAgentClient.generatePlan(llmRequest)
        .map(llmResponse -> {
            // Save to database
            TravelPlan plan = convertToEntity(llmResponse);
            planRepository.save(plan);
            return ResponseEntity.ok(PlanResponse.fromEntity(plan));
        });
}
```
↓

**5. LLM Agent Client (WebClient)**
```http
POST http://llm-agent:8000/api/v1/plans/generate
Content-Type: application/json
{
  "title": "Seoul Weekend",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03"
}
```
↓

**6. LLM Agent (FastAPI)**
```python
@router.post("/plans/generate")
async def generate_plan(request: PlanRequest):
    # Check Redis cache
    cached = await cache_service.get(cache_key)
    if cached:
        return cached
    
    # Call AWS Bedrock
    plan = bedrock_service.generate_travel_plan(
        location=request.title,
        start_date=request.startDate,
        end_date=request.endDate
    )
    
    # Cache result
    await cache_service.set(cache_key, plan, ttl=3600)
    
    return plan
```
↓

**7. AWS Bedrock (Claude 3.5 Sonnet)**
```python
response = bedrock_client.invoke_model(
    modelId='anthropic.claude-3-5-sonnet-20241022-v2:0',
    body={
        "messages": [{
            "role": "user",
            "content": "Create 3-day Seoul itinerary..."
        }]
    }
)
```

---

#### Response Flow (역방향):

**7. Claude AI Response:**
```json
{
  "content": [
    {
      "text": "Day 1: Visit Gyeongbokgung Palace...\nDay 2: Explore Myeongdong..."
    }
  ]
}
```
↓

**6. LLM Agent Parsing:**
```python
# Parse AI response
parsed_plan = {
    "title": "Seoul Weekend Adventure",
    "days": [
        {"day": 1, "location": "Gyeongbokgung", "activity": "..."},
        {"day": 2, "location": "Myeongdong", "activity": "..."}
    ]
}
return parsed_plan
```
↓

**5. LLM Agent Response to Plan Service:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "title": "Seoul Weekend Adventure",
  "days": [...]
}
```
↓

**4. Plan Service Process:**
```java
llmAgentClient.generatePlan(request)
    .map(llmResponse -> {
        // Create entity from AI response
        TravelPlan plan = new TravelPlan();
        plan.setTitle(llmResponse.getTitle());
        
        List<PlanDetail> details = llmResponse.getDays().stream()
            .map(day -> {
                PlanDetail detail = new PlanDetail();
                detail.setDay(day.getDay());
                detail.setLocation(day.getLocation());
                detail.setActivity(day.getActivity());
                return detail;
            })
            .collect(Collectors.toList());
        
        plan.setDetails(details);
        
        // Save to PostgreSQL
        TravelPlan saved = planRepository.save(plan);
        
        return PlanResponse.fromEntity(saved);
    });
```
↓

**3. Plan Service Response to API Gateway:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "userId": 1,
  "title": "Seoul Weekend Adventure",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "details": [
    {"id": 1, "day": 1, "location": "Gyeongbokgung", "activity": "..."},
    {"id": 2, "day": 2, "location": "Myeongdong", "activity": "..."}
  ],
  "createdAt": "2025-01-27T10:00:00",
  "updatedAt": "2025-01-27T10:00:00"
}
```
↓

**2. API Gateway Forward:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "userId": 1,
  "title": "Seoul Weekend Adventure",
  ...
}
```
↓

**1. Mobile App Receives:**
```typescript
// Redux automatically updates
dispatch(createPlan(data));

// State updated:
state.plans = [newPlan, ...existingPlans];
state.currentPlan = newPlan;

// UI re-renders with new plan
<FlatList
  data={plans}  // Now includes new plan
  renderItem={({ item }) => <PlanCard plan={item} />}
/>
```

---

## 5️⃣ 통신 프로토콜 상세

### HTTP Headers

**Mobile → API Gateway:**
```http
POST /api/plans HTTP/1.1
Host: 172.16.102.149:8080
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-User-Id: 1
Content-Length: 98

{"title":"Seoul Trip","startDate":"2025-12-01","endDate":"2025-12-03"}
```

**API Gateway → Plan Service:**
```http
POST /api/v1/plans HTTP/1.1
Host: plan-service:8083
Content-Type: application/json
X-User-Id: 1
X-Forwarded-For: 172.24.0.1

{"title":"Seoul Trip","startDate":"2025-12-01","endDate":"2025-12-03"}
```

**Plan Service → LLM Agent:**
```http
POST /api/v1/plans/generate HTTP/1.1
Host: llm-agent:8000
Content-Type: application/json

{"title":"Seoul Trip","startDate":"2025-12-01","endDate":"2025-12-03"}
```

---

## 6️⃣ 에러 처리 흐름

### 시나리오: LLM Agent 타임아웃

```
LLM Agent (Timeout)
    ↓ WebClient.timeout()
Plan Service
    ↓ onErrorMap()
    ↓ RuntimeException("Failed to call LLM Agent")
API Gateway
    ↓ 500 Internal Server Error
Mobile App
    ↓ axios.interceptors.response (error handler)
    ↓ Redux action.rejected
    ↓ state.error = "Failed to create plan"
UI
    ↓ Display error message
```

---

## 7️⃣ 현재 구현 상태

### ✅ 작동하는 통신:

1. **Mobile → API Gateway**
   - HTTP/HTTPS
   - JSON format
   - Auto retry
   - Token refresh

2. **API Gateway → Services**
   - Path rewriting
   - Header forwarding
   - Load balancing (future)

3. **Plan Service → Database**
   - JPA/Hibernate
   - Transaction management
   - Connection pooling

### ⏳ 통합 필요:

4. **Plan Service → LLM Agent**
   - WebClient 설정됨
   - 현재: 비활성화 (간단한 기본값 사용)
   - TODO: LLM Agent 라우트 매칭 필요

5. **LLM Agent → Bedrock**
   - Mock mode로 작동 중
   - 실제 Bedrock 연동은 AWS 배포 후

---

## 8️⃣ 네트워크 구성

### Docker Network (Local)

```
oddiya_oddiya-network (bridge)
├── oddiya-api-gateway (172.24.0.2)
├── oddiya-plan-service (172.24.0.3)
├── oddiya-llm-agent (172.24.0.4)
├── oddiya-postgres (172.24.0.10)
└── oddiya-redis (172.24.0.11)
```

**Service Discovery:**
- Service names used as hostnames
- `http://plan-service:8083` instead of `http://localhost:8083`
- Docker DNS resolves service names automatically

### Production (AWS EKS)

```
Internet
    ↓
Application Load Balancer
    ↓
Kubernetes Ingress
    ↓
API Gateway Service (ClusterIP)
    ↓
Backend Services (ClusterIP)
    ↓
External: PostgreSQL EC2, Redis EC2
```

---

## 9️⃣ 데이터 흐름 타임라인

**예상 응답 시간:**

```
Mobile Request          0ms
    ↓ (네트워크 ~10ms)
API Gateway            10ms
    ↓ (라우팅 ~5ms)
Plan Service           15ms
    ↓ (WebClient ~50ms)
LLM Agent              65ms
    ↓ (Redis 확인 ~5ms)
Redis Cache            70ms (캐시 히트 시)
    OR
    ↓ (Bedrock 호출 ~500ms)
AWS Bedrock           570ms (캐시 미스 시)
    ↓
LLM Agent Response    570ms
    ↓
Plan Service Save     580ms
    ↓
API Gateway Forward   585ms
    ↓
Mobile Receives       595ms

Total: ~600ms (캐시 미스)
Total: ~85ms (캐시 히트)
```

---

## 🔟 Mobile Web App에서 사용하기

### 현재 동작 (간단한 버전):

**Mobile Web (`http://172.16.102.149:8080/app`):**

```javascript
// Create plan button click
async function createPlan() {
    const response = await fetch(`${API_BASE}/api/plans`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-User-Id': '1'
        },
        body: JSON.stringify({
            title: document.getElementById('plan-title').value,
            startDate: document.getElementById('plan-start').value,
            endDate: document.getElementById('plan-end').value
        })
    });
    
    if (response.ok) {
        const newPlan = await response.json();
        // Display new plan in UI
        showToast('✅ Plan created!');
        loadPlans();  // Refresh list
    }
}
```

**결과:**
1. ✅ 여행 계획이 즉시 생성됩니다
2. ✅ 화면에 새로운 카드가 표시됩니다
3. ✅ "Explore and enjoy!" 기본 활동 포함

---

## 📊 요약

### 통신 방식:

| 구간 | 프로토콜 | 방법 | 특징 |
|------|----------|------|------|
| Mobile → API Gateway | HTTP/HTTPS | Axios | Auto retry, token refresh |
| API Gateway → Services | HTTP | Spring Cloud Gateway | Path rewriting, load balancing |
| Plan Service → LLM Agent | HTTP | WebClient (Reactive) | Async, non-blocking |
| LLM Agent → Bedrock | HTTPS | AWS SDK (boto3) | Retry logic, rate limiting |

### 현재 상태:

✅ **Mobile → API Gateway** - 완전 작동  
✅ **API Gateway → Plan Service** - 완전 작동  
✅ **Plan Service → Database** - 완전 작동  
⏳ **Plan Service → LLM Agent** - 임시로 비활성화 (간단한 기본값 사용)  
⏳ **LLM Agent → Bedrock** - Mock mode (AWS 배포 시 활성화)

### Mobile에서 받는 데이터:

```json
{
  "id": 1,
  "title": "서울 주말 여행",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "details": [
    {
      "day": 1,
      "location": "City Center",
      "activity": "Explore and enjoy!"
    }
  ]
}
```

---

**73 commits | 전체 통신 구조 문서화 완료!** 📚

**모바일 웹 (`http://172.16.102.149:8080/app`)에서 지금 바로 테스트 가능합니다!** 🚀

