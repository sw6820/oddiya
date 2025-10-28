# Prompt Management: YAML vs Text Files

프롬프트 관리 방식 비교

## 📋 비교 요약

| 특징 | YAML (.yaml) | Text (.txt) |
|------|-------------|-------------|
| **구조화** | ✅ 계층 구조, 키-값 쌍 | ❌ 평문, 구분자 필요 |
| **파싱** | ✅ 라이브러리 자동 | ⚠️ 직접 파싱 필요 |
| **가독성** | ✅ 명확한 구조 | ⚠️ 구분자에 의존 |
| **수정 용이성** | ✅ 키로 쉽게 찾기 | ⚠️ 마커 찾아야 함 |
| **버전 관리** | ✅ Diff 명확 | ⚠️ 변경 추적 어려움 |
| **다국어** | ✅ 쉬움 (키별 분리) | ❌ 어려움 |
| **복잡도** | ⚠️ 문법 있음 | ✅ 단순 |
| **오류 감지** | ✅ 파싱 에러로 즉시 감지 | ❌ 런타임에만 발견 |
| **도구 지원** | ✅ 에디터 지원 좋음 | ⚠️ 기본 텍스트 |
| **성능** | ⚠️ 파싱 오버헤드 | ✅ 빠름 |

---

## 1. YAML 방식 (현재 사용 중)

### 파일 예시: `system_prompts.yaml`

```yaml
system_message: |
  당신은 한국 여행 전문가입니다.
  실용적인 계획을 제공합니다.

planning_prompt_template: |
  {location}의 {num_days}일 여행을 생성하세요.
  
  요구사항:
  - 실제 장소 사용
  - 비용 명시

refinement_prompt_template: |
  문제: {feedback}
  개선하세요.
```

### 로딩 코드:

```python
import yaml

with open('prompts/system_prompts.yaml') as f:
    prompts = yaml.safe_load(f)

system_msg = prompts['system_message']
planning = prompts['planning_prompt_template']
```

### 장점:

✅ **구조화된 데이터**
```yaml
ko:  # 한국어 프롬프트
  system_message: "..."
en:  # 영어 프롬프트
  system_message: "..."
```

✅ **타입 안정성**
```yaml
config:
  max_tokens: 2048  # 숫자로 파싱됨
  temperature: 0.7  # float로 파싱됨
```

✅ **주석 지원**
```yaml
# 이 프롬프트는 서울 여행에 최적화됨
planning_prompt: |
  서울 여행 계획...
```

✅ **중첩 구조**
```yaml
prompts:
  short_trip:  # 2-3일
    template: "..."
  long_trip:   # 7일+
    template: "..."
```

### 단점:

❌ **문법 규칙**
- 들여쓰기 중요 (스페이스 vs 탭)
- 특수문자 이스케이프 필요

❌ **복잡성**
- YAML 문법 이해 필요
- 파싱 라이브러리 의존

---

## 2. Text 방식

### 파일 예시: `planning.txt`

```
===SYSTEM_MESSAGE===
당신은 한국 여행 전문가입니다.
실용적인 계획을 제공합니다.

===PLANNING_PROMPT===
{location}의 {num_days}일 여행을 생성하세요.

요구사항:
- 실제 장소 사용
- 비용 명시

===REFINEMENT_PROMPT===
문제: {feedback}
개선하세요.
```

### 로딩 코드:

```python
def load_text_prompts(file_path):
    with open(file_path) as f:
        content = f.read()
    
    prompts = {}
    sections = content.split('===')
    
    for section in sections:
        if not section.strip():
            continue
        lines = section.strip().split('\n', 1)
        if len(lines) == 2:
            key = lines[0].lower().replace('_', '')
            prompts[key] = lines[1].strip()
    
    return prompts
```

### 장점:

✅ **단순함**
- 특별한 문법 없음
- 누구나 수정 가능

✅ **유연성**
- 원하는 대로 작성
- 제약 없음

✅ **직관적**
- 마크다운처럼 읽기 쉬움
- 구분자만 기억

✅ **빠른 로딩**
- 파싱 오버헤드 최소

### 단점:

❌ **구조 부족**
- 중첩 불가능
- 키-값 쌍만 가능

❌ **파싱 코드 필요**
- 직접 구현해야 함
- 오류 처리 복잡

❌ **타입 없음**
- 모두 문자열
- 숫자 변환 수동

---

## 3. 혼합 방식 (권장)

### 구조:

```
prompts/
├── config.yaml          # 설정 (메타데이터)
├── system.txt           # 시스템 메시지
├── planning.txt         # 계획 생성
├── refinement.txt       # 개선
└── validation.txt       # 검증
```

### config.yaml:

```yaml
version: "1.0"
locale: ko
model:
  name: claude-sonnet
  max_tokens: 2048
  temperature: 0.7

prompts:
  system: system.txt
  planning: planning.txt
  refinement: refinement.txt
```

### 장점:

✅ YAML의 구조 + Text의 단순함
✅ 긴 프롬프트는 별도 파일
✅ 설정은 YAML로 관리

---

## 4. 실제 사용 추천

### 작은 프로젝트 → Text

```
prompts.txt  (하나의 파일에 모두)
```

**이유:**
- 프롬프트 개수 적음 (3-5개)
- 빠른 수정 필요
- 협업자 없음

### 중간 프로젝트 → YAML (현재 Oddiya)

```
system_prompts.yaml  (구조화된 하나의 파일)
```

**이유:**
- 프롬프트 개수 중간 (5-10개)
- 다국어 계획 있음
- 버전 관리 중요

### 큰 프로젝트 → 혼합

```
config.yaml  (설정)
prompts/
  ├── system/*.txt
  ├── planning/*.txt
  └── refinement/*.txt
```

**이유:**
- 프롬프트 많음 (10개+)
- 여러 사람 협업
- A/B 테스팅 필요

---

## 5. Oddiya 현재 상태 분석

### 현재: YAML 사용 중

```yaml
# system_prompts.yaml
system_message: |
  당신은 한국 여행 전문가...

planning_prompt_template: |
  {location}의 여행 계획...
```

### 장점 (현재):
- ✅ 구조화되어 관리 쉬움
- ✅ 다국어 확장 용이 (향후 영어/일어)
- ✅ Git diff 명확
- ✅ 변수 명시적 ({location}, {budget})

### 개선 제안:

**Option A: YAML 유지 + 개선**
```yaml
# 버전 관리 추가
version: "2.0"
last_updated: "2025-01-28"

# 다국어 지원
prompts:
  ko:  # 한국어
    system_message: "..."
    planning: "..."
  en:  # 영어
    system_message: "..."
    planning: "..."
```

**Option B: Text로 전환**
```
# 단순하게
prompts/
  ├── system.txt
  ├── planning.txt
  └── refinement.txt
```

**Option C: 혼합 (권장)**
```
prompts/
  ├── config.yaml       # 설정만
  ├── system.txt        # 짧은 메시지
  ├── planning.txt      # 긴 프롬프트
  └── refinement.txt
```

---

## 6. 변환 예시

### YAML → Text 변환:

```python
# 현재 YAML
prompts['planning_prompt_template']

# Text 파일로 변환
with open('prompts/planning.txt', 'w') as f:
    f.write(prompts['planning_prompt_template'])
```

### Text → YAML 변환:

```python
# Text 파일
with open('prompts/planning.txt') as f:
    planning_prompt = f.read()

# YAML로 변환
yaml_content = {
    'planning_prompt_template': planning_prompt
}

with open('prompts/system_prompts.yaml', 'w') as f:
    yaml.dump(yaml_content, f, allow_unicode=True)
```

---

## 7. 성능 비교

### YAML:
```python
# 로딩: ~5ms
# 파싱: yaml.safe_load()
# 메모리: 약간 더 사용
```

### Text:
```python
# 로딩: ~1ms  
# 파싱: str.split()
# 메모리: 적음
```

**차이:** 무시할 수 있는 수준 (~4ms)

---

## 8. 협업 관점

### YAML:
- ✅ 기획자도 수정 가능 (구조 명확)
- ✅ PR 리뷰 쉬움 (키 변경 확인)
- ⚠️ 문법 오류 가능성

### Text:
- ✅ 누구나 수정 가능
- ⚠️ 구분자 실수 가능
- ⚠️ 어디가 뭔지 찾기 어려움

---

## 9. 최종 추천

### Oddiya의 경우:

**현재 YAML 유지 권장 ✅**

**이유:**
1. 프롬프트 4개 (적당한 수)
2. 향후 다국어 계획
3. 구조화된 관리 필요
4. A/B 테스팅 가능성

**개선 방향:**
```yaml
# Version 2.0
version: "2.0"
locale: ko

prompts:
  system: |
    당신은 여행 전문가...
  
  planning:
    template: |
      {location} 여행...
    variables:
      - location
      - num_days
      - budget
    
  refinement: |
    개선...
```

---

## 10. 실용적 조언

### YAML 사용 시:

✅ **DO:**
- 들여쓰기 일관성 (스페이스 2칸)
- 변수 명시: `{variable}`
- 주석 활용
- 버전 명시

❌ **DON'T:**
- 탭 사용하지 말기
- 특수문자 이스케이프 잊지 말기
- 너무 깊은 중첩 (2-3단계만)

### Text 사용 시:

✅ **DO:**
- 명확한 구분자 (`===SECTION===`)
- 섹션 순서 일관성
- 빈 줄로 구분

❌ **DON'T:**
- 구분자를 프롬프트 내용에 사용
- 파일 너무 크게 (1000줄 이하)

---

## 결론

**Oddiya는 YAML 유지하면서 Text 파일 참고용으로 병행**

```
prompts/
├── system_prompts.yaml  (메인, 코드에서 사용)
└── planning.txt         (참고용, 사람이 읽기 쉬움)
```

**Best of both worlds!** 🎯

