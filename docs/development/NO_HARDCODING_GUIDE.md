# No Hardcoding Guide

Oddiya 프로젝트의 핵심 원칙: **절대 하드코딩하지 않기**

## 🚫 원칙

**코드에 데이터를 넣지 마세요. 설정 파일에 넣으세요.**

---

## ❌ 하드코딩이란?

코드 안에 데이터, 설정, 콘텐츠를 직접 작성하는 것:

### 나쁜 예시들:

**1. 여행지 데이터**
```java
// ❌ BAD
if (city.equals("Seoul")) {
    return "경복궁, 명동, 강남";
} else if (city.equals("Busan")) {
    return "해운대, 광안리";
}
```

**2. 프롬프트**
```python
# ❌ BAD
prompt = """
Create a {days}-day travel plan for {city}.
Include these attractions: Gyeongbokgung, Myeongdong...
"""
```

**3. UI 메시지**
```javascript
// ❌ BAD
if (error) {
    alert("여행 계획 생성에 실패했습니다");
}
```

**4. 설정 값**
```java
// ❌ BAD
int maxPhotos = 10;
int timeout = 30000;
```

---

## ✅ 올바른 방법

### 1. YAML 파일 사용

**데이터 분리:**
```yaml
# default-activities.yaml
Seoul:
  day1:
    location: "경복궁 & 북촌한옥마을"
    activity: "Morning: 경복궁..."

Busan:
  day1:
    location: "해운대"
    activity: "Morning: 해변 산책..."
```

**코드:**
```java
// ✅ GOOD
@Component
public class ActivityLoader {
    private Map<String, Object> activities;
    
    public ActivityLoader() {
        Yaml yaml = new Yaml();
        activities = yaml.load(
            new ClassPathResource("default-activities.yaml").getInputStream()
        );
    }
    
    public Activity getActivity(String location, int day) {
        return activities.get(location).get("day" + day);
    }
}
```

### 2. Properties 파일 사용

**설정 분리:**
```properties
# application.yml
app:
  photos:
    max-count: 10
  timeouts:
    api-call: 30000
  messages:
    error:
      plan-failed: "여행 계획 생성에 실패했습니다"
```

**코드:**
```java
// ✅ GOOD
@Value("${app.photos.max-count}")
private int maxPhotos;

@Value("${app.messages.error.plan-failed}")
private String errorMessage;
```

### 3. 데이터베이스 사용

**동적 데이터:**
```sql
CREATE TABLE config_messages (
    key VARCHAR PRIMARY KEY,
    value TEXT,
    locale VARCHAR
);

INSERT INTO config_messages VALUES
('error.plan.failed', '여행 계획 생성에 실패했습니다', 'ko'),
('error.plan.failed', 'Failed to create plan', 'en');
```

**코드:**
```java
// ✅ GOOD
String message = messageRepository.findByKeyAndLocale(
    "error.plan.failed", "ko"
).getValue();
```

### 4. 별도 프롬프트 파일

**프롬프트 분리:**
```yaml
# prompts/system_prompts.yaml
planning_prompt: |
  {location}의 여행 계획을 생성하세요.
  
  요구사항:
  - 실제 장소 사용
  - 비용 명시
```

**코드:**
```python
# ✅ GOOD
loader = PromptLoader()
prompt = loader.get_planning_prompt(
    location=user_location
)
```

---

## 🎯 Oddiya에서의 적용

### Before (하드코딩):

```java
// ❌ 100+ lines of hardcoded activities
if (location.equals("Seoul")) {
    switch (day) {
        case 1:
            detail.setLocation("경복궁");
            detail.setActivity("Morning: ...");
            break;
        case 2:
            detail.setLocation("명동");
            ...
    }
} else if (location.equals("Busan")) {
    ...
}
```

### After (설정 파일):

```yaml
# default-activities.yaml
Seoul:
  day1:
    location: "경복궁 & 북촌한옥마을"
    activity: "Morning: 경복궁 궁궐 투어..."
```

```java
// ✅ Clean code
Map<String, String> activity = activityLoader.getActivityForDay(location, day);
detail.setLocation(activity.get("location"));
detail.setActivity(activity.get("activity"));
```

---

## 📋 체크리스트

**코드 작성 시 확인:**

- [ ] 이 데이터가 변경될 가능성이 있는가?
  - YES → 설정 파일로 분리
  
- [ ] 다국어 지원이 필요한가?
  - YES → YAML/JSON으로 분리
  
- [ ] 비개발자도 수정할 수 있어야 하는가?
  - YES → 외부 파일로 분리
  
- [ ] 환경마다 다른 값인가?
  - YES → properties/환경변수 사용
  
- [ ] 프롬프트인가?
  - YES → prompts/*.yaml로 분리

---

## 🔍 리뷰 기준

**Pull Request 시 확인:**

```java
// 🚨 Red flag - 하드코딩 발견!
if (city.equals("Seoul")) {
    return "강남, 명동, 홍대";
}

// ✅ Approve - 설정 파일 사용
return cityConfig.getAttractions(city);
```

**리뷰어 체크:**
- switch/case로 데이터 처리? → 🚫 Reject
- if/else로 콘텐츠 분기? → 🚫 Reject
- YAML/JSON 파일 사용? → ✅ Approve

---

## 📁 Oddiya 설정 파일 위치

```
services/
├── plan-service/
│   └── src/main/resources/
│       ├── default-activities.yaml  ← 여행지 데이터
│       └── application.yml          ← 설정
│
└── llm-agent/
    └── prompts/
        ├── system_prompts.yaml      ← 프롬프트
        └── planning.txt             ← 참고용
```

---

## 🎯 예외 경우

**하드코딩 허용:**

✅ **상수 (Constants):**
```java
public static final String DEFAULT_STATUS = "DRAFT";
public static final int MAX_RETRY = 3;
```

✅ **열거형 (Enums):**
```java
public enum PlanStatus {
    DRAFT, CONFIRMED, COMPLETED
}
```

✅ **에러 코드:**
```java
throw new IllegalArgumentException("Invalid plan ID");
```

**하드코딩 금지:**

❌ **비즈니스 데이터:**
- 여행지 목록
- 관광지 이름
- 식당 정보

❌ **UI 콘텐츠:**
- 버튼 텍스트
- 에러 메시지
- 안내 문구

❌ **프롬프트:**
- LLM 지시사항
- AI 역할 정의

---

## 🚀 마이그레이션 가이드

### 기존 하드코딩 찾기:

```bash
# Switch/case 문 찾기
grep -r "switch.*location" services/

# If/else chains 찾기
grep -r "if.*equals.*Seoul" services/

# 인라인 문자열 찾기
grep -r '"경복궁"' services/
```

### YAML로 추출:

```bash
# 1. 하드코딩된 데이터 식별
# 2. YAML 파일 생성
# 3. Loader 클래스 작성
# 4. 코드에서 제거
# 5. 테스트
```

---

## 📊 이점

### 유지보수성:
- ✅ 코드 변경 없이 데이터 수정
- ✅ 재컴파일 불필요
- ✅ 배포 없이 업데이트 (설정 파일만)

### 협업:
- ✅ 기획자도 YAML 수정 가능
- ✅ 개발자는 로직만 집중
- ✅ 변경 이력 추적 용이

### 확장성:
- ✅ 새 도시 추가 쉬움
- ✅ 다국어 지원 준비
- ✅ A/B 테스팅 용이

### 품질:
- ✅ 코드 간결
- ✅ 버그 감소
- ✅ 테스트 쉬움

---

## 🎓 Best Practices

### 1. 데이터는 항상 외부에

```
Code (Logic) ←→ Config File (Data)
```

### 2. 계층별 분리

```
- Prompts → prompts/*.yaml
- Activities → default-activities.yaml
- Messages → messages.properties
- Settings → application.yml
- Secrets → .env / AWS Secrets
```

### 3. 환경별 분리

```
- application-dev.yml
- application-staging.yml
- application-prod.yml
```

---

## ✅ Oddiya 적용 현황

**완전히 제거됨:**
- ✅ Plan activities → `default-activities.yaml`
- ✅ LLM prompts → `prompts/system_prompts.yaml`
- ✅ Database config → `application.yml`

**아직 남아있음 (개선 필요):**
- ⏳ UI messages (계획: `messages.properties`)
- ⏳ Error messages (계획: database 또는 i18n)

---

**Remember: Configuration, not Code!** 📋✨

**When in doubt, externalize!** 🎯

