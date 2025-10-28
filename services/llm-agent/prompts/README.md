# Prompt Management

프롬프트를 코드에서 분리하여 관리하는 시스템

## 구조

```
prompts/
├── system_prompts.yaml  # 메인 프롬프트 파일
├── README.md            # 이 파일
└── examples/            # 예제 프롬프트들
```

## 프롬프트 파일 형식

### system_prompts.yaml

```yaml
system_message: |
  시스템 메시지 (LLM의 역할 정의)

planning_prompt_template: |
  여행 계획 생성 프롬프트
  {변수}를 사용하여 동적 값 주입

refinement_prompt_template: |
  계획 개선 프롬프트
  
validation_criteria: |
  검증 기준 설명
```

## 변수 사용

### Planning Prompt에서 사용 가능한 변수:

- `{num_days}` - 여행 일수
- `{location}` - 여행지 (Seoul, Busan, Jeju)
- `{title}` - 여행 제목
- `{start_date}` - 시작일
- `{end_date}` - 종료일
- `{budget_level}` - 예산 수준 (저예산/중예산/고예산)
- `{temperature}` - 현재 기온
- `{temp_min}` - 최저 기온
- `{temp_max}` - 최고 기온
- `{weather_condition}` - 날씨 상태
- `{precipitation}` - 강수 확률
- `{weather_recommendation}` - 날씨 추천사항

### Refinement Prompt에서 사용 가능한 변수:

- `{feedback}` - 검증 피드백 내용

## 프롬프트 수정 방법

### 1. 파일 직접 수정

```bash
# 프롬프트 파일 편집
nano services/llm-agent/prompts/system_prompts.yaml

# 서비스 재시작 (자동 반영)
docker-compose -f docker-compose.local.yml restart llm-agent
```

### 2. 핫 리로딩 (개발 중)

```python
# Python 코드에서
from src.utils.prompt_loader import get_prompt_loader

loader = get_prompt_loader()
loader.reload_prompts()  # 파일 다시 로드
```

### 3. 환경별 프롬프트

```yaml
# prompts/system_prompts.dev.yaml (개발용)
# prompts/system_prompts.prod.yaml (프로덕션용)
```

## 프롬프트 작성 가이드

### 좋은 프롬프트:

✅ **구체적:**
```
"서울의 실제 관광지를 사용하세요 (경복궁, 북촌한옥마을 등)"
```

✅ **측정 가능:**
```
"정확히 {num_days}일의 일정을 만들어주세요"
```

✅ **예시 포함:**
```
"출력 형식 (JSON): {{ ... }}"
```

✅ **제약사항 명확:**
```
"예산: {budget_level} - ₩50,000/일 이하"
```

### 나쁜 프롬프트:

❌ **모호함:**
```
"좋은 여행 계획을 만들어주세요"
```

❌ **측정 불가:**
```
"적당한 비용으로 계획을 짜주세요"
```

❌ **형식 미지정:**
```
"여행 계획을 알려주세요"
```

## 프롬프트 테스트

### 변경 사항 테스트:

```bash
# 1. 프롬프트 수정
nano prompts/system_prompts.yaml

# 2. 서비스 재시작
docker-compose -f docker-compose.local.yml restart llm-agent

# 3. 테스트 API 호출
curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "location": "Seoul",
    "budget": "medium"
  }'

# 4. 결과 확인
```

## 프롬프트 버전 관리

### Git으로 관리:

```bash
# 프롬프트 변경 사항 커밋
git add prompts/system_prompts.yaml
git commit -m "prompt: improve planning prompt with better examples"

# 프롬프트 변경 이력 확인
git log -- prompts/system_prompts.yaml
```

## A/B 테스팅

### 다른 프롬프트 비교:

```yaml
# prompts/variants/planning_v1.yaml
# prompts/variants/planning_v2.yaml
# prompts/variants/planning_v3.yaml
```

```python
# 버전별 테스트
loader = PromptLoader(prompts_dir="prompts/variants")
```

## 프롬프트 최적화 팁

### 1. 명확한 지시사항

```yaml
요구사항:
  1. 정확히 {num_days}일의 일정
  2. 실제 장소 이름 사용
  3. 시간대별 활동 구분
```

### 2. 예시 포함

```yaml
예시:
  Day 1:
    location: "경복궁 & 북촌한옥마을"
    activity: "오전: 궁궐 투어..."
```

### 3. 출력 형식 지정

```yaml
OUTPUT FORMAT (JSON):
{
  "title": "...",
  "days": [...]
}
```

### 4. 제약사항 명시

```yaml
제약사항:
  - 예산: {budget_level}
  - 날씨: {weather_condition}
  - 실재 장소만 사용
```

## LangSmith 통합

프롬프트 성능 모니터링:

```python
@traceable(name="planning_prompt")
def get_planning_prompt(**kwargs):
    # LangSmith가 자동으로 추적:
    # - 사용된 프롬프트
    # - 입력 변수
    # - 생성 시간
    pass
```

## 프롬프트 개선 워크플로우

```
1. 현재 프롬프트로 계획 생성
2. 결과 품질 평가
3. 문제점 파악
4. 프롬프트 수정
5. 재테스트
6. 개선 확인
7. Git 커밋
8. 배포
```

---

**프롬프트를 코드에서 분리하여:**
- ✅ 쉬운 수정 (코드 변경 없이)
- ✅ 버전 관리 용이
- ✅ A/B 테스팅 가능
- ✅ 다국어 지원 쉬움
- ✅ 협업 편리 (기획자도 수정 가능)

