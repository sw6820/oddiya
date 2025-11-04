# ⚡ Oddiya 배포 Quick Start

## 🚀 5분 만에 배포 준비하기

### 1️⃣ GitHub CLI 설정
```bash
brew install gh
gh auth login
```

### 2️⃣ Secrets 자동 설정
```bash
./scripts/setup-github-secrets.sh
./scripts/validate-github-secrets.sh
```

### 3️⃣ 수동 설정 (필수 2개)
```bash
# AWS Credentials
gh secret set AWS_ACCESS_KEY_ID
gh secret set AWS_SECRET_ACCESS_KEY

# Expo Token (mobile 배포시)
gh secret set EXPO_TOKEN
```

### 4️⃣ 배포!
```bash
git push origin main
```

✅ GitHub Actions가 자동으로:
- AWS 인프라 배포 (15분)
- Mobile 앱 빌드 (20분)
- 테스트 + 보안 스캔

---

## 📱 Mobile 배포 (Expo/EAS)

```bash
# Preview 빌드 (테스트용)
gh workflow run deploy-mobile-expo.yml -f platform=all -f profile=preview

# Production 빌드 (앱스토어)
gh workflow run deploy-mobile-expo.yml -f platform=all -f profile=production
```

---

## 🔍 상태 확인

```bash
# GitHub Actions 보기
gh run list
gh run watch

# 브라우저에서
open https://github.com/YOUR_USERNAME/oddiya/actions
```

---

## 📊 비용

- **개발/테스트:** **$5/월** (AWS free tier + Expo free tier 30 builds) ⭐
- **프로덕션:** $26-34/월 (AWS + Expo free/paid tier)

---

## 📚 상세 문서

| 파일 | 내용 |
|------|------|
| DEPLOYMENT_READY.md | 완전한 배포 가이드 |
| GITHUB_ACTIONS_SETUP.md | GitHub Actions 설정 |
| AWS_DEPLOYMENT_GUIDE.md | AWS 배포 |
| ANDROID_DEPLOYMENT_GUIDE.md | Android 앱 배포 |

---

## 🆘 도움말

```bash
# Secrets 검증
./scripts/validate-github-secrets.sh

# API Key 확인
./scripts/verify-api-key.sh

# 워크플로우 목록
gh workflow list

# 최근 실행 로그
gh run view --log
```

---

**🟢 Ready!** 지금 바로 `./scripts/setup-github-secrets.sh` 실행하세요!
