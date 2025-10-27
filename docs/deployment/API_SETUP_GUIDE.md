# Real API Setup Guide

How to enable real Bedrock, Kakao, and OpenWeatherMap APIs for AI-generated travel plans.

## Current Status

**현재 상태:** Mock mode (테스트 데이터 사용)  
**변경할 상태:** Real APIs (실제 AI 및 데이터 사용)

## Required API Keys (Only 2!)

### 1. AWS Bedrock (Claude Sonnet)

**Why:** Claude has built-in knowledge of Korea's tourist destinations, no external place API needed!

**Get Access:**
```bash
# 1. AWS 계정 필요
# 2. Bedrock 접근 권한 요청
#    https://console.aws.amazon.com/bedrock
#    → Model access → Request access for Claude 3 Sonnet
#    (승인까지 몇 시간~1일 소요)

# 3. IAM 사용자 생성 또는 Access Key 발급
#    https://console.aws.amazon.com/iam
#    → Users → Create user
#    → Attach policies: AmazonBedrockFullAccess
#    → Security credentials → Create access key

# 4. Key 저장
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_REGION=ap-northeast-2
```

**Cost:**
- Claude 3 Sonnet: ~$3 per 1M input tokens, ~$15 per 1M output tokens
- Average plan: ~2,000 tokens = $0.006 per plan
- 100 plans/day = ~$0.60/day

### 2. Kakao Local API

**Get API Key:**
```bash
# 1. Kakao Developers 가입
#    https://developers.kakao.com

# 2. 애플리케이션 생성
#    내 애플리케이션 → 애플리케이션 추가하기
#    이름: "Oddiya Travel Planner"

# 3. REST API 키 복사
#    내 애플리케이션 → 앱 키 → REST API 키

# 4. Key 저장
KAKAO_LOCAL_API_KEY=your-kakao-rest-api-key-here
```

**Cost:** FREE (월 300,000회 무료)

**Documentation:** https://developers.kakao.com/docs/latest/ko/local/dev-guide

### 3. OpenWeatherMap API

**Get API Key:**
```bash
# 1. 회원가입
#    https://openweathermap.org/api

# 2. API key 생성
#    My API keys → Create key

# 3. Key 저장
OPENWEATHER_API_KEY=your-openweather-api-key
```

**Cost:** FREE tier (60 calls/minute, 1,000,000 calls/month)

**Documentation:** https://openweathermap.org/api

### 3. LangSmith (Optional - Monitoring)

**Get API Key:**
```bash
# 1. 회원가입
#    https://smith.langchain.com

# 2. Create API key
#    Settings → API Keys → Create API Key

# 3. Key 저장
LANGSMITH_API_KEY=your-langsmith-api-key
LANGSMITH_PROJECT=oddiya-travel-planner
```

**Cost:** FREE tier (5,000 traces/month)

---

## Setup Instructions

### Method 1: Using .env File (Recommended)

**Step 1: Create environment file**

```bash
cd /Users/wjs/cursor/oddiya

# Copy template
cp env.example .env.local

# Edit file
nano .env.local
```

**Step 2: Add your API keys**

```bash
# .env.local
ENVIRONMENT=development

# AWS Bedrock
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY
AWS_REGION=ap-northeast-2
BEDROCK_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0

# External APIs
KAKAO_LOCAL_API_KEY=YOUR_KAKAO_API_KEY
OPENWEATHER_API_KEY=YOUR_OPENWEATHER_API_KEY

# LangSmith (Optional)
LANGSMITH_API_KEY=YOUR_LANGSMITH_KEY
LANGSMITH_PROJECT=oddiya-travel-planner

# IMPORTANT: Disable mock mode
MOCK_MODE=false

# Database & Redis (keep as is)
DB_HOST=postgres
DB_PORT=5432
REDIS_HOST=redis
REDIS_PORT=6379
```

**Step 3: Update docker-compose.local.yml**

```yaml
services:
  llm-agent:
    environment:
      # Load from .env.local
      AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
      AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
      AWS_REGION: ${AWS_REGION}
      BEDROCK_MODEL_ID: ${BEDROCK_MODEL_ID}
      KAKAO_LOCAL_API_KEY: ${KAKAO_LOCAL_API_KEY}
      OPENWEATHER_API_KEY: ${OPENWEATHER_API_KEY}
      LANGSMITH_API_KEY: ${LANGSMITH_API_KEY}
      MOCK_MODE: ${MOCK_MODE:-false}
      REDIS_HOST: redis
```

**Step 4: Restart services**

```bash
# Stop current services
docker-compose -f docker-compose.local.yml down

# Start with new environment
docker-compose -f docker-compose.local.yml --env-file .env.local up -d

# Check logs
docker logs -f oddiya-llm-agent
```

---

### Method 2: Quick Test (Temporary)

**For quick testing without editing files:**

```bash
# Set environment variables
export AWS_ACCESS_KEY_ID=YOUR_KEY
export AWS_SECRET_ACCESS_KEY=YOUR_SECRET
export AWS_REGION=ap-northeast-2
export KAKAO_LOCAL_API_KEY=YOUR_KAKAO_KEY
export OPENWEATHER_API_KEY=YOUR_WEATHER_KEY
export MOCK_MODE=false

# Restart LLM Agent with real APIs
docker-compose -f docker-compose.local.yml up -d llm-agent
```

---

## Verification

### Test Real APIs Are Working

**1. Check LLM Agent logs:**

```bash
docker logs oddiya-llm-agent

# Should see:
# "Bedrock client initialized"
# "Weather API key configured"
# "Kakao API key configured"
# NOT: "Using mock mode"
```

**2. Test OpenWeatherMap:**

```bash
curl "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=YOUR_KEY&units=metric"

# Should return real weather data
```

**3. Test Kakao API:**

```bash
curl -H "Authorization: KakaoAK YOUR_KAKAO_KEY" \
  "https://dapi.kakao.com/v2/local/search/keyword.json?query=서울 관광지"

# Should return real places
```

**4. Create a plan in mobile web:**

```
http://172.16.102.149:8080/app

→ Plans tab
→ Create New Plan
→ Title: "서울 3일 여행"
→ Generate

# Should show:
# - Real Seoul attractions (from Kakao)
# - Real weather forecast (from OpenWeather)
# - AI-generated detailed itinerary (from Bedrock)
```

---

## Expected Output with Real APIs

### Before (Mock Mode):

```json
{
  "title": "Seoul 3-Day Adventure",
  "days": [
    {
      "day": 1,
      "location": "City Center",
      "activity": "Explore and enjoy!"
    }
  ],
  "_mock": true
}
```

### After (Real APIs):

```json
{
  "title": "Seoul 3-Day Cultural Journey",
  "days": [
    {
      "day": 1,
      "location": "Gyeongbokgung Palace",  ← Real from Kakao
      "activity": "Morning palace tour, afternoon at Bukchon Hanok Village, evening at Insadong",
      "details": {
        "morning": {
          "time": "09:00-12:00",
          "activity": "Explore Gyeongbokgung Palace and watch the changing of the guard ceremony",
          "location": "Gyeongbokgung Palace, 161 Sajik-ro, Jongno-gu",
          "cost": 3000
        },
        "afternoon": {
          "time": "13:00-17:00",
          "activity": "Walk through traditional Hanok houses in Bukchon",
          "location": "Bukchon Hanok Village, Gahoe-dong, Jongno-gu",
          "cost": 0
        },
        "evening": {
          "time": "18:00-21:00",
          "activity": "Dinner at Insadong and shop for traditional crafts",
          "location": "Insadong-gil, Jongno-gu",
          "cost": 35000
        }
      },
      "estimatedCost": 95000,
      "weatherTip": "☀️ Clear skies, 18°C - perfect for walking!"  ← Real from OpenWeather
    },
    {
      "day": 2,
      "location": "Myeongdong & N Seoul Tower",
      "activity": "Shopping and panoramic city views",
      ...
    },
    {
      "day": 3,
      "location": "Gangnam & COEX",
      "activity": "Modern Seoul exploration",
      ...
    }
  ],
  "total_estimated_cost": 285000,
  "weather_summary": "Clear skies, mild temperatures (12-22°C)",  ← Real forecast
  "tips": [
    "💰 Budget: ₩95,000 per day",
    "🌤️ Perfect weather - no umbrella needed!",
    "🚇 Use Line 3 to Gyeongbokgung Station",
    "📱 Buy T-money card at convenience store"
  ],
  "metadata": {
    "iterations": 2,  ← Refined twice
    "ai_model": "Claude Sonnet",
    "external_apis": ["OpenWeatherMap", "Kakao Local API"]
  }
}
```

---

## Quick Setup Script

Create this file: `scripts/enable-real-apis.sh`

```bash
#!/bin/bash

echo "🔧 Setting up Real APIs"
echo ""

# Check if API keys are provided
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: ./enable-real-apis.sh <AWS_ACCESS_KEY> <AWS_SECRET_KEY>"
    echo ""
    echo "Also set these environment variables:"
    echo "  export KAKAO_LOCAL_API_KEY=your-key"
    echo "  export OPENWEATHER_API_KEY=your-key"
    exit 1
fi

# Update .env.local
cat > .env.local << EOF
AWS_ACCESS_KEY_ID=$1
AWS_SECRET_ACCESS_KEY=$2
AWS_REGION=ap-northeast-2
BEDROCK_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0

KAKAO_LOCAL_API_KEY=${KAKAO_LOCAL_API_KEY:-your-kakao-key}
OPENWEATHER_API_KEY=${OPENWEATHER_API_KEY:-your-openweather-key}

MOCK_MODE=false

DB_HOST=postgres
REDIS_HOST=redis
EOF

echo "✅ .env.local created"
echo ""
echo "Restarting LLM Agent with real APIs..."

# Restart with new environment
docker-compose -f docker-compose.local.yml --env-file .env.local up -d llm-agent

echo "✅ LLM Agent restarted"
echo ""
echo "Test at: http://172.16.102.149:8080/app"
```

---

## Troubleshooting

### Issue: "Bedrock access denied"

**Solution:**
1. Check IAM permissions include `AmazonBedrockFullAccess`
2. Verify model access is approved in Bedrock console
3. Check AWS region is correct (`ap-northeast-2` for Korea)

### Issue: "Kakao API 401 Unauthorized"

**Solution:**
1. Verify REST API key (not JavaScript key)
2. Check key is active in Kakao Developers console
3. No spaces in key

### Issue: "OpenWeather 401"

**Solution:**
1. Wait 10-15 minutes after creating key (activation delay)
2. Verify free tier hasn't exceeded limits
3. Check API key in account settings

### Issue: Still seeing mock data

**Solution:**
```bash
# Check environment
docker exec oddiya-llm-agent env | grep MOCK_MODE
# Should show: MOCK_MODE=false

# Check logs
docker logs oddiya-llm-agent | grep -i "mock\|bedrock"

# Restart service
docker-compose -f docker-compose.local.yml restart llm-agent
```

---

## Cost Estimation

### With Real APIs:

**Bedrock (Claude Sonnet):**
- Input: $3 / 1M tokens
- Output: $15 / 1M tokens
- Per plan: ~2,000 tokens = $0.006
- 100 plans: $0.60

**OpenWeatherMap:**
- FREE (월 1,000,000 calls)

**LangSmith (Optional):**
- FREE tier: 5,000 traces/month

**Total Monthly Cost (100 plans/day):**
- Bedrock: ~$18/month
- Others: $0/month
- **Total: ~$18/month**

---

## Environment File Template

Create `.env.production` for real APIs:

```bash
# .env.production
ENVIRONMENT=production

# AWS Bedrock (REAL)
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_REGION=ap-northeast-2
BEDROCK_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0

# External APIs (REAL)
OPENWEATHER_API_KEY=1234567890abcdef1234567890abcdef

# LangSmith (REAL - Optional)
LANGSMITH_API_KEY=ls_123456789
LANGSMITH_PROJECT=oddiya-production

# IMPORTANT: Disable mock mode
MOCK_MODE=false

# Database & Cache
DB_HOST=postgres
DB_PORT=5432
DB_NAME=oddiya
DB_USER=oddiya_user
DB_PASSWORD=oddiya_password_dev
REDIS_HOST=redis
REDIS_PORT=6379
```

---

## Quick Enable Real APIs

**Run this:**

```bash
cd /Users/wjs/cursor/oddiya

# 1. Set your API keys
export AWS_ACCESS_KEY_ID=your-aws-key
export AWS_SECRET_ACCESS_KEY=your-aws-secret
export KAKAO_LOCAL_API_KEY=your-kakao-key  
export OPENWEATHER_API_KEY=your-weather-key
export MOCK_MODE=false

# 2. Restart LLM Agent with environment
docker-compose -f docker-compose.local.yml down llm-agent
docker-compose -f docker-compose.local.yml up -d llm-agent

# 3. Test in mobile web
# http://172.16.102.149:8080/app
# Create a new plan → Should get real AI-generated content!
```

---

## Verification Checklist

- [ ] AWS Access Key ID configured
- [ ] AWS Secret Access Key configured
- [ ] Bedrock model access approved (Claude 3 Sonnet)
- [ ] Kakao Local API key obtained
- [ ] OpenWeatherMap API key obtained
- [ ] MOCK_MODE=false set
- [ ] LLM Agent restarted
- [ ] Test plan creation shows real data
- [ ] Real places appear (not "City Center")
- [ ] Real weather data shown
- [ ] AI-generated activities (not "Explore and enjoy!")

---

## Test Real APIs

```bash
# After setup, create a plan:

curl -X POST http://localhost:8080/api/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "서울 주말 여행",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03"
  }' | jq '.days[0]'

# Should show:
# - Real Seoul attractions (경복궁, 북촌한옥마을, etc.)
# - Real weather forecast
# - Detailed time-based activities
# - Realistic costs
# NOT: "City Center" or "Explore and enjoy!"
```

---

## Next Steps

1. **Get API Keys** (links above)
2. **Update .env.local** with real keys
3. **Set MOCK_MODE=false**
4. **Restart services:** `./scripts/run-with-env.sh local`
5. **Test:** Create plan at `http://172.16.102.149:8080/app`
6. **Verify:** See real places, weather, and AI recommendations!

---

**Once configured, every plan will use:**
- ✅ Real weather forecasts
- ✅ Real Seoul tourist spots
- ✅ Claude AI recommendations
- ✅ Actual cost estimates
- ✅ Practical travel tips

**All for ~$0.006 per plan!** 💰

