# Streaming Implementation Guide

**목적:** ChatGPT처럼 여행 계획을 실시간으로 조금씩 스트리밍하면서 보여주기

**현재 상태:** 완료 후 한 번에 전체 응답 반환
**목표 상태:** 실시간 스트리밍으로 점진적 응답 (Server-Sent Events)

---

## 📊 현재 vs 목표 아키텍처

### 현재 (Non-Streaming)
```
Client → POST /api/v1/plans/generate
  ↓ (loading... 2-5초 대기)
Server → {"title": "...", "days": [...]} (전체 JSON 한 번에)
```

### 목표 (Streaming with SSE)
```
Client → POST /api/v1/plans/generate/stream
  ↓
Server → data: {"type": "status", "message": "날씨 정보 수집 중..."}\n\n
Server → data: {"type": "status", "message": "AI 계획 생성 중..."}\n\n
Server → data: {"type": "chunk", "content": "Day 1: 경복궁..."}\n\n
Server → data: {"type": "chunk", "content": " - Morning: ..."}\n\n
Server → data: {"type": "complete", "plan": {...}}\n\n
```

---

## 🛠️ 구현 방안

### Option 1: Server-Sent Events (SSE) - **추천**

**장점:**
- ✅ HTTP/1.1 기반 (기존 인프라 사용)
- ✅ 자동 재연결 지원
- ✅ 브라우저 네이티브 지원 (`EventSource`)
- ✅ 단방향 통신에 적합 (서버 → 클라이언트)

**단점:**
- ❌ 클라이언트 → 서버는 별도 요청 필요

### Option 2: WebSocket

**장점:**
- ✅ 양방향 통신
- ✅ 실시간성 높음

**단점:**
- ❌ 인프라 변경 필요 (WebSocket 지원)
- ❌ 추가 복잡도

**결론:** SSE 추천 (여행 계획은 서버 → 클라이언트만 필요)

---

## 📝 구현 단계

### Step 1: Backend - 스트리밍 엔드포인트 추가

**파일:** `services/llm-agent/src/routes/langgraph_plans.py`

```python
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Optional, AsyncGenerator
import json
import asyncio

router = APIRouter()
planner = LangGraphPlanner()

class LangGraphPlanRequest(BaseModel):
    location: str
    startDate: str
    endDate: str
    budget: Optional[str] = "medium"
    title: Optional[str] = None
    maxIterations: Optional[int] = 3


# 기존 엔드포인트 (호환성 유지)
@router.post("/plans/generate")
async def generate_plan_with_langgraph(request: LangGraphPlanRequest):
    """기존 방식: 완료 후 한 번에 응답"""
    # ... 기존 코드 유지 ...


# 새로운 스트리밍 엔드포인트
@router.post("/plans/generate/stream")
async def generate_plan_streaming(request: LangGraphPlanRequest):
    """
    실시간 스트리밍 방식: ChatGPT처럼 점진적 응답

    Response Format (Server-Sent Events):
    data: {"type": "status", "message": "날씨 정보 수집 중..."}\n\n
    data: {"type": "progress", "step": "generate_draft", "progress": 50}\n\n
    data: {"type": "chunk", "content": "Day 1: 경복궁..."}\n\n
    data: {"type": "complete", "plan": {...}}\n\n
    """

    async def event_generator() -> AsyncGenerator[str, None]:
        """SSE 이벤트 생성기"""
        try:
            # Auto-generate title
            from datetime import datetime
            if not request.title:
                start = datetime.fromisoformat(request.startDate)
                end = datetime.fromisoformat(request.endDate)
                num_days = (end - start).days + 1
                request.title = f"{request.location} {num_days}-Day Trip"

            # 1. 날씨 정보 수집 시작
            yield f"data: {json.dumps({'type': 'status', 'message': f'{request.location} 날씨 정보 수집 중...'}, ensure_ascii=False)}\n\n"
            await asyncio.sleep(0.1)  # 시각적 피드백

            # 2. AI 계획 생성 (스트리밍)
            yield f"data: {json.dumps({'type': 'status', 'message': 'AI 여행 계획 생성 중...'}, ensure_ascii=False)}\n\n"

            # LangGraph 플래너 호출 (스트리밍 버전)
            async for event in planner.generate_plan_streaming(
                title=request.title,
                location=request.location,
                start_date=request.startDate,
                end_date=request.endDate,
                budget=request.budget or "medium",
                max_iterations=request.maxIterations or 3
            ):
                # 각 이벤트를 SSE 형식으로 전송
                yield f"data: {json.dumps(event, ensure_ascii=False)}\n\n"

            # 3. 완료
            yield f"data: {json.dumps({'type': 'done'}, ensure_ascii=False)}\n\n"

        except Exception as e:
            error_event = {
                'type': 'error',
                'message': f'계획 생성 실패: {str(e)}'
            }
            yield f"data: {json.dumps(error_event, ensure_ascii=False)}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no"  # Nginx buffering 비활성화
        }
    )
```

---

### Step 2: LangGraph Planner - 스트리밍 메서드 추가

**파일:** `services/llm-agent/src/services/langgraph_planner.py`

```python
from typing import AsyncGenerator, Dict, Any
import json

class LangGraphPlanner:
    # ... 기존 코드 ...

    async def generate_plan_streaming(
        self,
        title: str,
        location: str,
        start_date: str,
        end_date: str,
        budget: str = "medium",
        max_iterations: int = 3
    ) -> AsyncGenerator[Dict[str, Any], None]:
        """
        실시간 스트리밍 방식으로 여행 계획 생성

        Yields:
            dict: 이벤트 객체
                - {"type": "status", "message": "..."}
                - {"type": "progress", "step": "...", "progress": 0-100}
                - {"type": "chunk", "content": "..."}
                - {"type": "complete", "plan": {...}}
        """
        from datetime import datetime

        # 날짜 계산
        start = datetime.fromisoformat(start_date)
        end = datetime.fromisoformat(end_date)
        num_days = (end - start).days + 1

        # 초기 상태
        initial_state: PlanState = {
            "title": title or f"{location} {num_days}일 여행",
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

        # Step 1: 날씨 정보 수집
        yield {
            "type": "progress",
            "step": "gather_context",
            "message": f"{location} 날씨 정보 확인 중...",
            "progress": 20
        }

        state = await self.gather_context_node(initial_state)

        yield {
            "type": "status",
            "message": f"날씨: {state['weather_data'].get('description', 'N/A')} {state['weather_data'].get('temperature', {}).get('current', '')}°C",
        }

        # Step 2: AI 초안 생성 (스트리밍)
        yield {
            "type": "progress",
            "step": "generate_draft",
            "message": "AI 여행 계획 생성 중...",
            "progress": 40
        }

        # LLM 스트리밍 호출
        prompt = self._build_planning_prompt(state)

        if self.mock_mode or not self.llm:
            # Mock 모드: 점진적으로 전송
            draft = self._generate_mock_draft(state)

            # Days를 하나씩 스트리밍
            for day in draft.get('days', []):
                yield {
                    "type": "chunk",
                    "content": f"Day {day['day']}: {day['location']}",
                    "day": day['day']
                }
                await asyncio.sleep(0.3)  # 시각적 효과

            state["plan_draft"] = draft

        else:
            # 실제 LLM 스트리밍
            messages = state["messages"] + [HumanMessage(content=prompt)]

            full_response = ""
            async for chunk in self.llm.astream(messages):
                # 청크를 실시간으로 전송
                content = chunk.content if hasattr(chunk, 'content') else str(chunk)
                full_response += content

                yield {
                    "type": "chunk",
                    "content": content
                }

            # 전체 응답 파싱
            draft = self._parse_llm_response(full_response, state)
            state["plan_draft"] = draft
            state["messages"].append(AIMessage(content=full_response))

        # Step 3: 검증
        yield {
            "type": "progress",
            "step": "validate",
            "message": "계획 검증 중...",
            "progress": 70
        }

        state = await self.validate_plan_node(state)

        # Step 4: 필요시 개선 (iteration)
        while state["feedback"] and state["current_iteration"] < state["max_iterations"]:
            yield {
                "type": "progress",
                "step": "refine",
                "message": f"계획 개선 중 ({state['current_iteration'] + 1}/{state['max_iterations']})...",
                "progress": 80
            }

            state = await self.refine_plan_node(state)
            state["current_iteration"] += 1
            state = await self.validate_plan_node(state)

        # Step 5: 최종 완료
        yield {
            "type": "progress",
            "step": "finalize",
            "message": "최종 계획 완성 중...",
            "progress": 95
        }

        state = await self.finalize_plan_node(state)

        # 최종 결과 전송
        yield {
            "type": "complete",
            "plan": state["final_plan"],
            "progress": 100
        }
```

---

### Step 3: Frontend - EventSource 사용

#### React/React Native 예시

```typescript
// src/services/planService.ts

export async function generatePlanStreaming(
  request: PlanRequest,
  onProgress: (event: StreamEvent) => void,
  onComplete: (plan: TravelPlan) => void,
  onError: (error: string) => void
) {
  const url = `${API_BASE_URL}/api/v1/plans/generate/stream`;

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    const decoder = new TextDecoder();

    if (!reader) {
      throw new Error('No response body');
    }

    while (true) {
      const { done, value } = await reader.read();

      if (done) break;

      // SSE 데이터 파싱
      const chunk = decoder.decode(value);
      const lines = chunk.split('\n');

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6);

          try {
            const event = JSON.parse(data);

            switch (event.type) {
              case 'status':
              case 'progress':
              case 'chunk':
                onProgress(event);
                break;

              case 'complete':
                onComplete(event.plan);
                break;

              case 'error':
                onError(event.message);
                break;
            }
          } catch (e) {
            console.error('Failed to parse SSE event:', e);
          }
        }
      }
    }
  } catch (error) {
    onError(error instanceof Error ? error.message : String(error));
  }
}
```

#### React Component 예시

```typescript
// src/components/PlanGenerator.tsx

import React, { useState } from 'react';
import { generatePlanStreaming } from '../services/planService';

export function PlanGenerator() {
  const [status, setStatus] = useState<string>('');
  const [progress, setProgress] = useState<number>(0);
  const [chunks, setChunks] = useState<string[]>([]);
  const [plan, setPlan] = useState<TravelPlan | null>(null);
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    setChunks([]);
    setPlan(null);

    await generatePlanStreaming(
      {
        location: 'Seoul',
        startDate: '2025-11-10',
        endDate: '2025-11-12',
        budget: 'medium',
      },
      // onProgress
      (event) => {
        if (event.type === 'status') {
          setStatus(event.message);
        } else if (event.type === 'progress') {
          setStatus(event.message);
          setProgress(event.progress);
        } else if (event.type === 'chunk') {
          setChunks(prev => [...prev, event.content]);
        }
      },
      // onComplete
      (completePlan) => {
        setPlan(completePlan);
        setLoading(false);
        setStatus('완료!');
      },
      // onError
      (error) => {
        console.error('Error:', error);
        setStatus(`오류: ${error}`);
        setLoading(false);
      }
    );
  };

  return (
    <div>
      <button onClick={handleGenerate} disabled={loading}>
        여행 계획 생성
      </button>

      {loading && (
        <div>
          <div className="progress-bar">
            <div style={{ width: `${progress}%` }} />
          </div>
          <p>{status}</p>
        </div>
      )}

      {/* 실시간 청크 표시 (ChatGPT 스타일) */}
      {chunks.length > 0 && (
        <div className="streaming-content">
          {chunks.map((chunk, i) => (
            <span key={i}>{chunk}</span>
          ))}
          <span className="cursor-blink">|</span>
        </div>
      )}

      {/* 최종 계획 표시 */}
      {plan && (
        <div className="final-plan">
          <h2>{plan.title}</h2>
          {plan.days.map(day => (
            <div key={day.day}>
              <h3>Day {day.day}: {day.location}</h3>
              <p>{day.activity}</p>
              <p>Cost: ₩{day.estimatedCost.toLocaleString()}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

---

## 🎨 UI/UX 개선 포인트

### 1. 진행 단계별 시각화

```typescript
const STEPS = [
  { key: 'gather_context', label: '날씨 정보 수집', icon: '🌤️' },
  { key: 'generate_draft', label: 'AI 계획 생성', icon: '🤖' },
  { key: 'validate', label: '계획 검증', icon: '✅' },
  { key: 'refine', label: '계획 개선', icon: '🔧' },
  { key: 'finalize', label: '최종 완료', icon: '🎉' },
];

{STEPS.map((step, i) => (
  <div key={step.key} className={currentStep === step.key ? 'active' : ''}>
    {step.icon} {step.label}
  </div>
))}
```

### 2. 타이핑 효과 (ChatGPT 스타일)

```css
.streaming-content {
  font-family: monospace;
  white-space: pre-wrap;
}

.cursor-blink {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
```

### 3. 프로그레스 바

```tsx
<div className="progress-container">
  <div className="progress-bar" style={{ width: `${progress}%` }}>
    {progress}%
  </div>
  <p className="status-text">{status}</p>
</div>
```

---

## 📊 성능 고려사항

### 1. 청크 크기 조정

```python
# 너무 작은 청크는 오버헤드 증가
MIN_CHUNK_SIZE = 10  # 최소 10자씩 전송

buffer = ""
async for chunk in self.llm.astream(messages):
    buffer += chunk.content

    if len(buffer) >= MIN_CHUNK_SIZE:
        yield {"type": "chunk", "content": buffer}
        buffer = ""

# 남은 버퍼 전송
if buffer:
    yield {"type": "chunk", "content": buffer}
```

### 2. 타임아웃 설정

```python
@router.post("/plans/generate/stream")
async def generate_plan_streaming(
    request: LangGraphPlanRequest,
    timeout: int = 60  # 60초 타임아웃
):
    async def event_generator():
        try:
            async with asyncio.timeout(timeout):
                # 스트리밍 로직
                ...
        except asyncio.TimeoutError:
            yield f"data: {json.dumps({'type': 'error', 'message': '응답 시간 초과'})}\n\n"
```

### 3. 에러 핸들링

```python
try:
    async for event in planner.generate_plan_streaming(...):
        yield f"data: {json.dumps(event, ensure_ascii=False)}\n\n"
except Exception as e:
    logger.error(f"Streaming error: {e}")
    yield f"data: {json.dumps({'type': 'error', 'message': str(e)})}\n\n"
finally:
    # 정리 작업
    logger.info("Streaming completed")
```

---

## 🧪 테스트 방법

### 1. cURL로 테스트

```bash
curl -N -X POST http://localhost:8000/api/v1/plans/generate/stream \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Seoul",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "budget": "medium"
  }'

# Expected Output:
# data: {"type":"status","message":"Seoul 날씨 정보 수집 중..."}
#
# data: {"type":"progress","step":"generate_draft","message":"AI 여행 계획 생성 중...","progress":40}
#
# data: {"type":"chunk","content":"Day 1: 경복궁"}
#
# ...
```

### 2. Python 테스트 스크립트

```python
import requests
import json

def test_streaming():
    url = "http://localhost:8000/api/v1/plans/generate/stream"
    payload = {
        "location": "Seoul",
        "startDate": "2025-11-10",
        "endDate": "2025-11-12",
        "budget": "medium"
    }

    with requests.post(url, json=payload, stream=True) as response:
        for line in response.iter_lines():
            if line:
                decoded = line.decode('utf-8')
                if decoded.startswith('data: '):
                    event = json.loads(decoded[6:])
                    print(f"[{event['type']}] {event.get('message', event.get('content', ''))}")

if __name__ == "__main__":
    test_streaming()
```

---

## 🚀 배포 시 주의사항

### 1. Nginx 설정 (Buffering 비활성화)

```nginx
location /api/v1/plans/generate/stream {
    proxy_pass http://llm-agent:8000;

    # SSE를 위한 설정
    proxy_buffering off;
    proxy_cache off;
    proxy_set_header Connection '';
    proxy_http_version 1.1;
    chunked_transfer_encoding off;

    # 타임아웃 연장
    proxy_read_timeout 300s;
    proxy_connect_timeout 75s;
}
```

### 2. API Gateway 설정 (Spring Cloud Gateway)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: llm-agent-stream
          uri: http://llm-agent:8000
          predicates:
            - Path=/api/v1/plans/generate/stream
          filters:
            - name: RequestTimeout
              args:
                timeout: 60s
```

### 3. 모바일 앱 (React Native)

```typescript
// React Native는 EventSource 미지원 → Polyfill 필요
import EventSource from 'react-native-sse';

const es = new EventSource(url, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(request),
});

es.addEventListener('message', (event) => {
  const data = JSON.parse(event.data);
  // 처리 로직
});

es.addEventListener('error', (error) => {
  console.error('SSE Error:', error);
  es.close();
});
```

---

## 📈 장단점 비교

### 스트리밍 방식

**장점:**
- ✅ **UX 개선:** 즉각적인 피드백, 사용자 이탈률 감소
- ✅ **체감 속도:** 첫 응답이 빠르게 보임 (TTFB 개선)
- ✅ **진행 상황:** 실시간 진행도 표시 가능
- ✅ **투명성:** 어떤 작업이 진행 중인지 명확

**단점:**
- ❌ **복잡도:** 구현 및 디버깅 어려움
- ❌ **에러 처리:** 중간에 실패 시 처리 복잡
- ❌ **캐싱:** 스트리밍 응답은 캐싱 어려움
- ❌ **인프라:** Nginx, 로드밸런서 설정 필요

### 기존 방식 (한 번에)

**장점:**
- ✅ **단순함:** 구현 및 디버깅 쉬움
- ✅ **안정성:** 에러 처리 간단
- ✅ **캐싱:** Redis 캐싱 가능

**단점:**
- ❌ **UX:** 로딩 시간 동안 대기
- ❌ **피드백 없음:** 진행 상황 모름

---

## 🎯 권장 사항

### Phase 1: 하이브리드 접근 (추천)

1. **기존 엔드포인트 유지** (`/plans/generate`)
   - 캐싱 가능
   - 안정적
   - 기존 클라이언트 호환

2. **새로운 스트리밍 엔드포인트 추가** (`/plans/generate/stream`)
   - 새로운 UX
   - 점진적 도입
   - A/B 테스트 가능

### Phase 2: 선택적 적용

**스트리밍 추천 상황:**
- 긴 여행 (5일 이상)
- 복잡한 요구사항
- 첫 방문 사용자 (UX 중요)

**기존 방식 유지 상황:**
- 짧은 여행 (1-2일)
- 반복 요청 (캐싱 효과)
- 빠른 응답 필요

---

## 📚 참고 자료

### LangChain Streaming
- https://python.langchain.com/docs/expression_language/streaming

### FastAPI StreamingResponse
- https://fastapi.tiangolo.com/advanced/custom-response/#streamingresponse

### Server-Sent Events (SSE)
- https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events

### Google Gemini Streaming
- https://ai.google.dev/tutorials/python_quickstart#streaming

---

**구현 완료 후 예상 효과:**
- 🚀 체감 속도 50% 향상
- 😊 사용자 만족도 상승
- 📉 이탈률 감소
- 🎯 차별화된 UX

**다음 단계:** Step 1부터 구현 시작하시겠습니까?
