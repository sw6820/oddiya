# 🚀 Oddiya 배포 시스템 - 현재 상태

**생성일:** 2025-11-04
**사용자:** sw6820
**리포지토리:** github.com/sw6820/oddiya

---

## ✅ 완료된 작업

### 1. GitHub 환경 설정
- ✅ GitHub CLI 설치 완료 (v2.76.2)
- ✅ GitHub 인증 완료 (account: sw6820)
- ✅ Repository 연결 확인

### 2. 로컬 설정 파일
- ✅ `.env` - Gemini API, Google OAuth, DB password 설정 완료
- ✅ `services/llm-agent/.env` - LLM Agent 설정 완료
- ✅ `infrastructure/terraform/phase1/terraform.tfvars` - Terraform 변수 설정 완료

**검증 완료:**
```
✅ Gemini API Key: AIzaSyDlMv...5Hbk (3개 파일 모두 설정됨)
✅ Google OAuth: Client ID & Secret 설정됨
✅ DB Password: 32자 보안 문자열 설정됨
✅ Seoul Region: ap-northeast-2
✅ Admin IP: 121.162.157.81
```

### 3. GitHub Secrets (6/18 설정 완료)

**✅ 설정 완료 (자동):**
1. ADMIN_IP - 121.162.157.81
2. DB_PASSWORD - PostgreSQL 비밀번호
3. GEMINI_API_KEY - Google Gemini API
4. GOOGLE_CLIENT_ID - Google OAuth Client ID
5. GOOGLE_CLIENT_SECRET - Google OAuth Secret
6. SSH_KEY_NAME - oddiya-prod

**❌ 수동 설정 필요 (AWS 배포용 - 3개):**
1. AWS_ACCESS_KEY_ID - IAM 에서 생성 필요
2. AWS_SECRET_ACCESS_KEY - IAM 에서 생성 필요
3. SSH_PRIVATE_KEY - AWS에서 Key Pair 생성 후 설정

**❌ 수동 설정 필요 (Mobile 배포용 - 9개, 선택사항):**
1. EXPO_TOKEN
2. EAS_PROJECT_ID
3. API_BASE_URL
4. GOOGLE_CLIENT_ID_IOS
5. GOOGLE_SERVICES_JSON
6. ANDROID_KEYSTORE_BASE64
7. KEYSTORE_PASSWORD
8. KEY_ALIAS
9. KEY_PASSWORD

### 4. 헬퍼 스크립트 (모두 실행 가능)
- ✅ `scripts/setup-github-secrets.sh` - 4개 secret 자동 설정 완료
- ✅ `scripts/validate-github-secrets.sh` - 검증 스크립트 준비됨
- ✅ `scripts/verify-api-key.sh` - API key 검증 완료

### 5. GitHub Actions 워크플로우 (3개)
- ✅ `.github/workflows/deploy-aws.yml` - AWS 자동 배포
- ✅ `.github/workflows/deploy-mobile-expo.yml` - Mobile 앱 빌드
- ✅ `.github/workflows/test.yml` - 자동 테스트

---

## 🎯 다음 단계

### Phase 1: AWS 배포 준비 (15분)

#### Step 1: AWS Credentials 생성
```bash
# 1. AWS IAM Console 접속
open https://console.aws.amazon.com/iam/home#/users

# 2. 새 IAM User 생성
#    - User name: github-actions-oddiya
#    - Permissions: AdministratorAccess (또는 제한된 권한)
#    - Access Key 생성 → CLI 선택

# 3. GitHub Secrets 설정
gh secret set AWS_ACCESS_KEY_ID
# → Access Key ID 입력

gh secret set AWS_SECRET_ACCESS_KEY
# → Secret Access Key 입력
```

#### Step 2: SSH Key Pair 생성 (Seoul region!)
```bash
# 1. AWS EC2 Console 접속
open https://ap-northeast-2.console.aws.amazon.com/ec2/home?region=ap-northeast-2#KeyPairs:

# 2. Key Pair 생성
#    - Name: oddiya-prod
#    - Type: RSA
#    - Format: .pem
#    - 다운로드: oddiya-prod.pem

# 3. 로컬에 저장
mv ~/Downloads/oddiya-prod.pem ~/.ssh/
chmod 400 ~/.ssh/oddiya-prod.pem

# 4. GitHub Secret 설정
cat ~/.ssh/oddiya-prod.pem | gh secret set SSH_PRIVATE_KEY
```

#### Step 3: 배포 실행!
```bash
# Option A: Terraform 직접 실행
cd infrastructure/terraform/phase1
terraform init
terraform plan
terraform apply

# Option B: GitHub Actions 자동 배포 (추천)
git add .
git commit -m "feat: ready for deployment"
git push origin main

# → GitHub Actions가 자동으로 AWS 배포 실행
# → 15-20분 후 배포 완료
```

### Phase 2: Mobile 배포 준비 (선택사항)

#### Expo 계정 설정
```bash
# 1. Expo 설치 및 로그인 (무료)
npm install -g eas-cli
eas login

# 2. Access Token 생성
open https://expo.dev/accounts/[account]/settings/access-tokens
gh secret set EXPO_TOKEN

# 3. 프로젝트 초기화
cd mobile
eas init
gh secret set EAS_PROJECT_ID

# 4. API URL 설정 (AWS 배포 후)
gh secret set API_BASE_URL
# → http://[EC2_ELASTIC_IP]:8083
```

#### Android/iOS 설정 (앱스토어 제출시)
```bash
# 상세 가이드 참조:
# - ANDROID_DEPLOYMENT_GUIDE.md
# - docs/development/EXPO_PRICING.md
```

---

## 📊 현재 진행률

```
Overall Progress: ████████░░░░░░░░░░░░ 33%

✅ 완료:
  - GitHub 환경 설정 (100%)
  - 로컬 설정 파일 (100%)
  - GitHub Secrets (33% - 6/18)
  - 헬퍼 스크립트 (100%)
  - GitHub Actions 워크플로우 (100%)

⏳ 진행 중:
  - AWS 배포 (0% - AWS credentials 필요)
  - Mobile 배포 (0% - Expo 설정 필요)

📝 대기 중:
  - 첫 배포 실행
  - 프로덕션 테스트
```

---

## 💰 예상 비용

### 현재 (개발/테스트)
```
AWS: $5/월 (free tier)
Expo: $0/월 (free tier, 30 builds/월)
─────────────────────────────
Total: $5/월 ⭐
```

### 프로덕션 (free tier 만료 후)
```
Option 1 (빌드 적음):
AWS: $26/월 + Expo: $0/월 = $26/월

Option 2 (빌드 많음):
AWS: $26/월 + Expo: $29/월 = $55/월
```

---

## 🔍 현재 상태 확인

```bash
# GitHub Secrets 확인
gh secret list

# 워크플로우 확인
gh workflow list

# 최근 실행 확인
gh run list

# API key 검증
./scripts/verify-api-key.sh

# Secrets 검증
./scripts/validate-github-secrets.sh
```

---

## 🎯 우선순위 작업

### 🔴 High Priority (AWS 배포용)
1. [ ] AWS IAM User 생성 → Access Key 발급
2. [ ] AWS_ACCESS_KEY_ID secret 설정
3. [ ] AWS_SECRET_ACCESS_KEY secret 설정
4. [ ] SSH Key Pair 생성 (Seoul region)
5. [ ] SSH_PRIVATE_KEY secret 설정
6. [ ] Terraform apply 실행 또는 git push

### 🟡 Medium Priority (Mobile 배포용)
7. [ ] Expo 계정 생성
8. [ ] EXPO_TOKEN secret 설정
9. [ ] eas init 실행
10. [ ] EAS_PROJECT_ID secret 설정
11. [ ] API_BASE_URL secret 설정 (AWS 배포 후)

### 🟢 Low Priority (앱스토어 제출용)
12. [ ] Android Keystore 생성
13. [ ] Google Services JSON 설정
14. [ ] iOS OAuth Client ID 설정
15. [ ] Apple Developer 계정 (선택)

---

## 📚 참고 문서

| 문서 | 용도 |
|------|------|
| **QUICK_START.md** | ⚡ 5분 퀵 가이드 |
| **DEPLOYMENT_READY.md** | 📋 전체 배포 가이드 |
| **GITHUB_ACTIONS_SETUP.md** | 🤖 GitHub Actions 상세 설정 |
| **AWS_DEPLOYMENT_GUIDE.md** | ☁️ AWS 배포 가이드 |
| **docs/development/EXPO_PRICING.md** | 📱 Expo 가격 정책 |
| **ANDROID_DEPLOYMENT_GUIDE.md** | 🤖 Android 배포 가이드 |

---

## ✅ 성공 지표

**현재 달성:**
- ✅ GitHub 환경 100% 설정
- ✅ 로컬 설정 100% 완료
- ✅ 자동화 스크립트 100% 준비
- ✅ GitHub Actions 100% 구성
- ✅ GitHub Secrets 33% 설정 (6/18)

**다음 목표:**
- 🎯 AWS credentials 설정 → 배포 가능 상태
- 🎯 첫 번째 배포 성공
- 🎯 Health check 통과
- 🎯 API 응답 확인

---

## 🚀 빠른 배포 (3단계)

```bash
# 1. AWS credentials 설정 (5분)
gh secret set AWS_ACCESS_KEY_ID
gh secret set AWS_SECRET_ACCESS_KEY

# 2. SSH key 생성 및 설정 (5분)
# AWS Console에서 oddiya-prod.pem 다운로드
cat ~/.ssh/oddiya-prod.pem | gh secret set SSH_PRIVATE_KEY

# 3. 배포! (15-20분)
git push origin main
# 또는
cd infrastructure/terraform/phase1 && terraform apply

# 완료! 🎉
open https://github.com/sw6820/oddiya/actions
```

---

**Status:** 🟡 Ready for AWS Deployment (AWS credentials required)
**Next:** Set AWS_ACCESS_KEY_ID & AWS_SECRET_ACCESS_KEY
**Then:** `git push origin main` 🚀
