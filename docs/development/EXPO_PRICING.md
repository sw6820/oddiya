# 📱 Expo/EAS 가격 정책 및 선택 가이드

**업데이트:** 2025-11-04

---

## ✅ 무료로 사용 가능! (Free Tier)

### Expo Free Tier 포함 사항:

| 기능 | Free Tier | 설명 |
|------|-----------|------|
| **EAS Build** | 30 builds/월 | Android + iOS 합산 |
| **EAS Submit** | 무제한 | 앱스토어 제출 |
| **EAS Update** | 제한된 bandwidth | OTA 업데이트 |
| **Expo Go** | 무제한 | 개발용 앱 |
| **로컬 개발** | 무제한 | - |

### 30 빌드면 충분할까?

**MVP/초기 개발 단계:**
```
주 1회 테스트 빌드 (Android + iOS) = 8 빌드/월
월 1회 프로덕션 빌드 = 2 빌드/월
버그 수정 긴급 빌드 = 4-6 빌드/월
--------------------------------------------
총 14-16 빌드/월 ✅ Free Tier로 충분
```

**활발한 개발 단계:**
```
주 2-3회 테스트 빌드 = 16-24 빌드/월
월 2회 프로덕션 빌드 = 4 빌드/월
버그 수정 = 6-10 빌드/월
--------------------------------------------
총 26-38 빌드/월 ⚠️ Free Tier 거의 한계
```

---

## 💰 Paid Tier ($29/월)

### Production Plan 장점:

| 기능 | Free | Paid ($29) |
|------|------|------------|
| Builds | 30/월 | ♾️ Unlimited |
| Build Priority | Normal | ⚡ High |
| OTA Bandwidth | 제한 | 많음 |
| Team Seats | 1 | 5 |
| Build Concurrency | 1 | 2 |

### 언제 업그레이드가 필요한가?

✅ **지금은 Free Tier 사용:**
- MVP 개발 단계
- 테스트 사용자 < 100명
- 월 빌드 < 30회

⏫ **Paid Tier 고려:**
- 정식 출시 후
- 활발한 업데이트 (주 3회 이상)
- 빠른 빌드 필요 (대기 시간 단축)
- 팀 협업 필요

---

## 🎯 Oddiya 프로젝트 추천

### Phase 1-2 (MVP 개발): Free Tier ✅

```bash
# 예상 빌드 횟수
- 주간 테스트: 주 1-2회 × 4주 = 4-8 빌드
- 프로덕션: 월 1-2회 = 2 빌드
- 긴급 수정: 예비 10 빌드
---------------------------------
총: 16-20 빌드/월 → Free Tier 충분!
```

**비용:**
- AWS: $5/월 (free tier)
- Expo: $0/월 (free tier)
- **Total: $5/월** 🎉

### Phase 3+ (정식 출시 후): 상황에 따라

**Option A: 계속 Free Tier** (빌드 관리)
```
- 월 30 빌드 한도 준수
- 불필요한 빌드 최소화
- Local build 활용
```

**Option B: Paid Tier 업그레이드**
```
- 무제한 빌드
- 더 빠른 빌드 속도
- 팀 협업 기능
- 비용: +$29/월
```

---

## 📊 비용 시나리오

### 시나리오 1: MVP 개발 (추천) ⭐
```
AWS: $5/월 (free tier)
Expo: $0/월 (free tier)
-----------------------
Total: $5/월
```

### 시나리오 2: 정식 출시 (빌드 적음)
```
AWS: $26/월 (free tier 만료 후)
Expo: $0/월 (free tier 계속 사용)
---------------------------------
Total: $26/월
```

### 시나리오 3: 정식 출시 (빌드 많음)
```
AWS: $26/월
Expo: $29/월 (unlimited builds)
-------------------------------
Total: $55/월
```

---

## 🚀 시작하기

### Free Tier로 시작하는 방법:

```bash
# 1. Expo 계정 생성 (무료)
npm install -g eas-cli
eas login

# 2. 프로젝트 초기화
cd mobile
eas init

# 3. 빌드 (Free Tier)
eas build --platform all --profile preview

# 4. 빌드 사용량 확인
eas build:list
# → https://expo.dev/accounts/[account]/projects/oddiya/builds
```

### 빌드 최적화 팁:

**Local Build 활용:**
```bash
# Android는 로컬에서도 빌드 가능
cd mobile/android
./gradlew assembleRelease

# iOS는 Mac에서만 가능
cd mobile/ios
xcodebuild -workspace ...
```

**불필요한 빌드 방지:**
- 로컬에서 충분히 테스트 후 빌드
- OTA Update 활용 (JS 변경 사항만)
- Development 빌드 재사용

---

## 🔄 업그레이드/다운그레이드

**언제든 변경 가능:**
- Free → Paid: 즉시 적용
- Paid → Free: 다음 청구 주기부터
- 위약금 없음

**업그레이드 방법:**
```
1. https://expo.dev/accounts/[account]/settings/billing
2. Select "Production" plan
3. 카드 정보 입력
4. 즉시 무제한 빌드 사용 가능
```

---

## 📚 추가 정보

**공식 가격 정책:**
- https://expo.dev/pricing

**빌드 사용량 확인:**
- https://expo.dev/accounts/[account]/usage

**지원:**
- Discord: https://chat.expo.dev/
- Forums: https://forums.expo.dev/

---

## ✅ 요약

**질문:** Expo를 무료로 사용할 수 있나요?

**답변:** ✅ **예!** Free Tier로 충분합니다.
- 30 builds/월 (Android + iOS)
- MVP 개발 단계에 적합
- 나중에 필요시 업그레이드

**Oddiya 프로젝트:**
- **지금 사용:** Free Tier
- **비용:** $0/월
- **나중에 고려:** 정식 출시 후 필요시 $29/월

**시작:** `eas login` → 무료 계정으로 바로 시작! 🚀
