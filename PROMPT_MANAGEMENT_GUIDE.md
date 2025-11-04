# Prompt Management Guide

**Project:** Oddiya AI Travel Planner
**Last Updated:** 2025-11-04

---

## 📍 Overview

Oddiya uses an **externalized prompt management system** where all AI prompts are stored in a separate YAML file, making them easy to edit without touching any code.

### Architecture

```
services/llm-agent/
├── prompts/
│   └── system_prompts.yaml          # ← All prompts here
├── src/
│   ├── utils/
│   │   └── prompt_loader.py         # Loads prompts from YAML
│   └── services/
│       └── langgraph_planner.py     # Uses prompts
```

---

## 📝 Prompt File Location

**Primary File:** `services/llm-agent/prompts/system_prompts.yaml`

This file contains all prompts used by the AI system:

1. **system_message** - AI persona definition
2. **planning_prompt_template** - Main travel plan generation
3. **refinement_prompt_template** - Plan improvement
4. **validation_criteria** - Quality check guidelines

---

## 🔧 How to Edit Prompts

### Step 1: Edit the YAML File

```bash
cd /Users/wjs/cursor/oddiya
nano services/llm-agent/prompts/system_prompts.yaml
```

### Step 2: Restart the Service

```bash
# If using local Python
cd services/llm-agent
source venv/bin/activate
# Kill the running process and restart
python main.py

# If using Docker
docker-compose restart llm-agent

# If using scripts
./scripts/stop-local-dev.sh
./scripts/start-local-dev.sh
```

### Step 3: Test Your Changes

```bash
curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Seoul",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "budget": "medium"
  }' | python3 -m json.tool
```

---

## 📋 Prompt Structure

### 1. System Message

**Purpose:** Defines the AI's persona and principles

**Current Content:**
```yaml
system_message: |
  당신은 한국 여행 전문가입니다.
  사용자에게 실용적이고 현실적인 여행 계획을 제공합니다.

  원칙:
  - 실제 존재하는 관광지와 장소를 추천합니다
  - 예산에 맞는 활동을 제안합니다
  - 날씨를 고려한 일정을 짭니다
  - 구체적인 비용을 명시합니다
  - 교통편과 이동 시간을 고려합니다
```

**When to Edit:**
- Change AI personality
- Add/remove guiding principles
- Update tone or style

---

### 2. Planning Prompt Template

**Purpose:** Main prompt for generating travel plans

**Variables Available:**
- `{location}` - Destination city/region
- `{num_days}` - Number of days
- `{title}` - Trip title
- `{start_date}`, `{end_date}` - Travel dates
- `{budget_level}` - low/medium/high
- `{temperature}` - Current temperature
- `{temp_min}`, `{temp_max}` - Temperature range
- `{weather_condition}` - Weather description
- `{precipitation}` - Rain probability
- `{weather_recommendation}` - Weather advice

**Structure:**
```yaml
planning_prompt_template: |
  "{location}" 지역의 {num_days}일 여행 계획을 생성해주세요.

  # ... detailed instructions ...

  요구사항:
  1. 정확히 {num_days}일의 일정을 만들어주세요
  2. {location}의 실제 존재하는 관광지, 식당, 카페를 구체적으로 명시하세요
  # ... more requirements ...
```

**When to Edit:**
- Modify output format requirements
- Add/remove planning criteria
- Change budget guidelines
- Update JSON structure requirements

---

### 3. Refinement Prompt Template

**Purpose:** Improve plans based on feedback

**Variables:**
- `{feedback}` - Issues found in current plan

**Structure:**
```yaml
refinement_prompt_template: |
  현재 계획에 다음 문제가 있습니다:
  {feedback}

  이 문제들을 해결한 개선된 버전을 만들어주세요.
  잘 작동하는 부분은 유지하고, 문제가 있는 부분만 수정하세요.
```

**When to Edit:**
- Change refinement strategy
- Add specific improvement guidelines
- Modify problem-solving approach

---

### 4. Validation Criteria

**Purpose:** Quality check guidelines for generated plans

**Structure:**
```yaml
validation_criteria: |
  계획 검증 기준:

  1. 일수 확인: 요청한 일수와 생성된 일수가 정확히 일치해야 함
  2. 예산 확인: 총 비용이 (일일 예산 × 일수 × 1.2) 이하여야 함
  3. 날씨 고려: 강수 확률 70% 이상 시 실내 활동 포함 필수
  # ... more criteria ...
```

**When to Edit:**
- Add new validation rules
- Modify quality standards
- Update budget constraints

---

## 🔄 Variable Substitution

The prompt loader automatically substitutes variables when generating prompts:

```python
# In code:
prompt = prompt_loader.get_planning_prompt(
    location="Seoul",
    num_days=3,
    budget_level="medium",
    temperature=18,
    # ... more variables
)

# Result:
# "Seoul" 지역의 3일 여행 계획을 생성해주세요.
# 예산 수준: medium
# 기온: 18°C
```

### Available Methods

```python
from src.utils.prompt_loader import get_prompt_loader

loader = get_prompt_loader()

# Get system message
system_msg = loader.get_system_message()

# Get planning prompt with variables
planning_prompt = loader.get_planning_prompt(
    location="Seoul",
    num_days=3,
    title="Seoul Winter Trip",
    start_date="2025-12-01",
    end_date="2025-12-03",
    budget_level="medium",
    temperature=18,
    temp_min=15,
    temp_max=22,
    weather_condition="Clear",
    precipitation=10,
    weather_recommendation="Good for outdoor activities"
)

# Get refinement prompt
refinement_prompt = loader.get_refinement_prompt(
    feedback="Budget exceeded by 20%"
)

# Get validation criteria
criteria = loader.get_validation_criteria()

# Reload prompts (hot reload)
loader.reload_prompts()
```

---

## 💡 Best Practices

### 1. Test After Each Change

Always test your prompt changes with real API calls:

```bash
# Test script
cat > /tmp/test.json << 'EOF'
{
  "location": "Seoul",
  "startDate": "2025-11-10",
  "endDate": "2025-11-12",
  "budget": "medium"
}
EOF

curl -X POST http://localhost:8000/api/v1/plans/generate \
  -H "Content-Type: application/json" \
  -d @/tmp/test.json | python3 -m json.tool
```

### 2. Version Control Your Prompts

Commit prompt changes with clear messages:

```bash
git add services/llm-agent/prompts/system_prompts.yaml
git commit -m "prompts: improve budget guidelines for Seoul"
```

### 3. Use A/B Testing for Major Changes

```bash
# Save current version
cp services/llm-agent/prompts/system_prompts.yaml \
   services/llm-agent/prompts/system_prompts_v1.yaml

# Make changes to system_prompts.yaml

# Test both versions and compare results
```

### 4. Document Your Changes

Add comments in the YAML file:

```yaml
# Updated 2025-11-04: Added specific restaurant name requirements
planning_prompt_template: |
  "{location}" 지역의 {num_days}일 여행 계획을 생성해주세요.
  # ...
```

### 5. Keep Prompts DRY (Don't Repeat Yourself)

If you find yourself repeating instructions:
- Use the system_message for general principles
- Use templates for specific requirements
- Use validation_criteria for quality standards

---

## 🐛 Troubleshooting

### Problem: Prompts Not Loading

**Check 1: File exists**
```bash
ls -la services/llm-agent/prompts/system_prompts.yaml
```

**Check 2: Valid YAML syntax**
```bash
python3 -c "import yaml; yaml.safe_load(open('services/llm-agent/prompts/system_prompts.yaml'))"
```

**Check 3: Service has access**
```bash
# Check permissions
chmod 644 services/llm-agent/prompts/system_prompts.yaml
```

### Problem: Variables Not Substituting

**Issue:** Missing curly braces in template

```yaml
# ❌ Wrong
location in {num_days} days

# ✅ Correct
{location} in {num_days} days
```

**Issue:** Variable name mismatch

```python
# Template uses: {budget_level}
# But calling with:
prompt_loader.get_planning_prompt(budget="medium")  # Wrong

# Should be:
prompt_loader.get_planning_prompt(budget_level="medium")  # Correct
```

### Problem: JSON Parsing Errors

**Issue:** LLM not returning valid JSON

**Solution:** Make JSON requirements more explicit in prompt:

```yaml
**중요: 반드시 유효한 JSON만 출력하세요. 다른 텍스트 없이 JSON만 반환하세요.**

출력 형식 (정확한 JSON):
{{
    "title": "...",
    "days": [...]
}}

주의사항:
- 숫자 필드는 따옴표 없이 숫자로만 작성
- 마지막 항목 뒤에 쉼표(,) 붙이지 말것
- JSON 외에 설명이나 다른 텍스트 포함하지 말것
```

---

## 📊 Monitoring Prompt Performance

### Log Analysis

```bash
# Check LLM responses
tail -f logs/llm-agent.log | grep "LLM Response"

# Check for errors
tail -f logs/llm-agent.log | grep -i error

# Check response times
tail -f logs/llm-agent.log | grep "Generated plan"
```

### Quality Metrics

Track these metrics after prompt changes:
- **Response Time:** Should be 2-5 seconds
- **Success Rate:** Should be >95%
- **Budget Accuracy:** Within ±20% of guidelines
- **Location Specificity:** >90% real place names

---

## 🎯 Common Prompt Patterns

### Pattern 1: Adding New Requirements

```yaml
요구사항:
  1. 정확히 {num_days}일의 일정을 만들어주세요
  2. 실제 존재하는 관광지를 명시하세요
  # Add your new requirement:
  3. 각 식당의 영업시간을 포함하세요
```

### Pattern 2: Modifying Budget Guidelines

```yaml
예산 가이드라인:
  - 저예산 (₩50,000/일): 대중교통, 무료 명소 위주, 저렴한 식당
  - 중예산 (₩100,000/일): 택시 가능, 유료 명소 포함, 일반 식당
  # Add new tier:
  - 프리미엄 (₩300,000/일): 프라이빗 투어, 미슐랭 식당, 럭셔리 호텔
```

### Pattern 3: Adding Output Fields

```yaml
출력 형식 (정확한 JSON):
{{
    "title": "{location} {num_days}일 여행",
    "days": [...],
    # Add new field:
    "accessibility": "Wheelchair accessible routes included"
}}
```

---

## 📚 References

### Related Documentation
- **Architecture:** `docs/architecture/overview.md`
- **LLM Service:** `services/llm-agent/README.md`
- **Migration Report:** `BEDROCK_TO_GEMINI_MIGRATION.md`

### Code References
- **Prompt Loader:** `services/llm-agent/src/utils/prompt_loader.py`
- **LangGraph Planner:** `services/llm-agent/src/services/langgraph_planner.py`
- **Main Service:** `services/llm-agent/main.py`

### External Resources
- **Google Gemini Docs:** https://ai.google.dev/docs
- **LangChain Prompting Guide:** https://python.langchain.com/docs/modules/model_io/prompts/
- **YAML Syntax:** https://yaml.org/spec/1.2.2/

---

**Last Updated:** 2025-11-04
**Maintained By:** Oddiya Development Team
