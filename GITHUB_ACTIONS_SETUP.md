# 🤖 GitHub Actions 자동 배포 설정

**작성일:** 2025-11-04
**소요 시간:** 30분
**결과:** 자동 AWS + Mobile (Android/iOS) 배포

---

## 🚀 Quick Start (추천)

**가장 빠른 방법: 헬퍼 스크립트 사용**

```bash
# 1. GitHub CLI 설치 및 로그인
brew install gh
gh auth login

# 2. 로컬 설정 파일에서 자동으로 Secrets 설정
./scripts/setup-github-secrets.sh

# 3. 설정 확인
./scripts/validate-github-secrets.sh

# 4. 수동 설정이 필요한 항목들 추가 (스크립트가 안내해줌)
# - AWS credentials
# - Expo token
# - Android keystore
# 등...

# 완료! 이제 배포 가능
```

**수동 설정을 원하면**: 아래 상세 가이드를 따라 진행하세요.

---

## ✅ 생성된 워크플로우

### 1. AWS 배포 (`deploy-aws.yml`)
- **트리거:** main 브랜치 push 또는 수동 실행
- **작업:**
  - Terraform으로 인프라 배포
  - Plan Service 빌드 및 배포
  - LLM Agent 배포
  - Health check

### 2. Mobile 빌드 (`deploy-mobile-expo.yml`) - Expo/EAS
- **트리거:** main 브랜치 push 또는 수동 실행
- **플랫폼:** Android + iOS (크로스 플랫폼)
- **작업:**
  - Android: AAB (Play Store) / APK (테스트)
  - iOS: IPA (TestFlight/App Store)
  - 자동 서명 및 배포
  - OTA 업데이트 지원

### 3. 테스트 (`test.yml`)
- **트리거:** Pull Request 또는 push
- **작업:**
  - Backend 테스트
  - Mobile 테스트
  - 보안 스캔

---

## 📋 필수 GitHub Secrets 설정

### Step 1: GitHub Repository Settings 이동

```bash
# Repository Settings 열기
open https://github.com/YOUR_USERNAME/oddiya/settings/secrets/actions
```

### Step 2: AWS 관련 Secrets (9개)

| Secret 이름 | 값 | 설명 |
|-------------|-----|------|
| `AWS_ACCESS_KEY_ID` | AKIA... | AWS Access Key |
| `AWS_SECRET_ACCESS_KEY` | wJa... | AWS Secret Key |
| `SSH_KEY_NAME` | oddiya-prod | SSH 키 이름 |
| `SSH_PRIVATE_KEY` | -----BEGIN RSA... | SSH 프라이빗 키 전체 내용 |
| `ADMIN_IP` | 121.162.157.81 | 관리자 IP |
| `DB_PASSWORD` | +K7fcEtcWcmz0o9P1+wRsSkqT1LexI1K | DB 비밀번호 |
| `GEMINI_API_KEY` | AIzaSyDlMvCLa... | Gemini API 키 |
| `GOOGLE_CLIENT_ID` | 201806680568... | Google OAuth ID |
| `GOOGLE_CLIENT_SECRET` | GOCSPX-dFqboaHuzm... | Google OAuth Secret |

#### AWS Access Key 생성

```bash
# AWS IAM Console 열기
open https://console.aws.amazon.com/iam/home#/users

# 단계:
# 1. IAM → Users → Create user
# 2. User name: github-actions-oddiya
# 3. Permissions: AdministratorAccess (또는 제한된 권한)
# 4. Create access key → CLI
# 5. Access Key ID와 Secret 복사
```

#### SSH Private Key 준비

```bash
# SSH 프라이빗 키 내용 복사
cat ~/.ssh/oddiya-prod.pem | pbcopy

# GitHub Secret에 전체 내용 붙여넣기
```

### Step 3: Mobile (Expo/EAS) 관련 Secrets (9개)

| Secret 이름 | 값 | 설명 |
|-------------|-----|------|
| `EXPO_TOKEN` | expo_token_... | Expo 인증 토큰 |
| `EAS_PROJECT_ID` | abc123... | EAS 프로젝트 ID |
| `API_BASE_URL` | http://43.200.123.45:8083 | API 서버 URL (EC2 IP) |
| `GOOGLE_CLIENT_ID_IOS` | ...apps.googleusercontent.com | iOS OAuth Client ID |
| `GOOGLE_SERVICES_JSON` | {...} | Android google-services.json |
| `ANDROID_KEYSTORE_BASE64` | /Td6WFoAA... | Keystore base64 인코딩 |
| `KEYSTORE_PASSWORD` | your-password | Keystore 비밀번호 |
| `KEY_ALIAS` | oddiya-release | Key alias |
| `KEY_PASSWORD` | your-key-password | Key 비밀번호 |

#### Keystore Base64 인코딩

```bash
# Keystore를 base64로 인코딩
base64 ~/.android/oddiya-release-key.jks | tr -d '\n' | pbcopy

# GitHub Secret에 붙여넣기
```

#### Google Play Service Account 생성

1. **Google Play Console 이동**
   ```bash
   open https://play.google.com/console
   ```

2. **API Access 설정**
   - Setup → API access
   - Create new service account
   - Grant access (Admin 또는 Release manager)

3. **Service Account Key 다운로드**
   - Google Cloud Console → IAM → Service Accounts
   - 생성한 계정 선택 → Keys → Add Key
   - JSON 형식 다운로드

4. **JSON 내용을 GitHub Secret에 추가**
   ```bash
   cat ~/Downloads/service-account-*.json | pbcopy
   ```

#### Expo Token 및 EAS 설정

1. **Expo 계정 생성 및 로그인**
   ```bash
   npm install -g eas-cli
   eas login
   ```

2. **Access Token 생성**
   - https://expo.dev/accounts/[account]/settings/access-tokens
   - "Create Token" 클릭
   - 토큰 복사 후 GitHub Secret에 추가:
   ```bash
   gh secret set EXPO_TOKEN
   ```

3. **EAS 프로젝트 초기화**
   ```bash
   cd mobile
   eas init
   # app.json에서 extra.eas.projectId 확인
   ```

4. **Project ID를 GitHub Secret에 추가**
   ```bash
   # mobile/app.json에서 projectId 복사
   gh secret set EAS_PROJECT_ID
   ```

### Step 4: 선택적 Secrets (Apple OAuth)

| Secret 이름 | 값 | 설명 |
|-------------|-----|------|
| `APPLE_CLIENT_ID` | com.oddiya.service | Apple Service ID |
| `APPLE_TEAM_ID` | ABC123DEFG | Apple Team ID |
| `APPLE_KEY_ID` | XYZ789 | Apple Key ID |
| `APPLE_PRIVATE_KEY` | -----BEGIN PRIVATE... | Apple Private Key |

---

## 🚀 자동 배포 실행

### 방법 1: 코드 Push로 자동 실행

```bash
# 변경사항 커밋 및 푸시
git add .
git commit -m "Update services"
git push origin main

# GitHub Actions 자동 실행됨
# https://github.com/YOUR_USERNAME/oddiya/actions 에서 확인
```

### 방법 2: 수동 실행

1. **GitHub Actions 페이지 이동**
   ```bash
   open https://github.com/YOUR_USERNAME/oddiya/actions
   ```

2. **워크플로우 선택**
   - "Deploy to AWS" 또는 "Build Android App" 선택

3. **"Run workflow" 클릭**
   - Branch: main
   - 환경 선택 (prod/staging)
   - "Run workflow" 클릭

### 방법 3: GitHub CLI 사용

```bash
# GitHub CLI 설치
brew install gh

# 로그인
gh auth login

# AWS 배포 실행
gh workflow run deploy-aws.yml

# Mobile (Android + iOS) 빌드 실행
gh workflow run deploy-mobile-expo.yml \
  -f platform=all \
  -f profile=preview

# 상태 확인
gh run list
gh run view
```

---

## 📊 워크플로우 모니터링

### 실시간 로그 확인

```bash
# 최신 실행 확인
gh run list

# 특정 실행 로그 보기
gh run view <RUN_ID> --log

# 실패한 실행만 보기
gh run list --status failure
```

### Actions 페이지에서 확인

1. **실행 상태 대시보드**
   - 녹색 체크: 성공 ✅
   - 빨간 X: 실패 ❌
   - 노란 점: 진행 중 🟡

2. **각 Step 상세 확인**
   - 클릭하여 로그 확인
   - 실패 원인 파악

---

## 🔧 문제 해결

### 문제 1: Terraform Apply 실패

**증상:**
```
Error: Error creating EC2 Instance: InvalidKeyPair.NotFound
```

**해결:**
1. SSH 키가 Seoul 리전에 생성되었는지 확인
2. `SSH_KEY_NAME` Secret 값 확인
3. AWS 권한 확인

### 문제 2: Android 빌드 실패

**증상:**
```
Execution failed for task ':app:packageRelease'
```

**해결:**
1. Keystore base64 인코딩 확인
2. 비밀번호 정확성 확인
3. Key alias 확인

### 문제 3: SSH 연결 실패

**증상:**
```
Permission denied (publickey)
```

**해결:**
```bash
# SSH 키 형식 확인
cat ~/.ssh/oddiya-prod.pem

# BEGIN과 END 포함 전체 내용이 Secret에 있는지 확인
# 줄바꿈도 포함되어야 함
```

### 문제 4: AWS 자격 증명 오류

**증상:**
```
Error: The security token included in the request is invalid
```

**해결:**
1. AWS Access Key 재생성
2. GitHub Secrets 업데이트
3. 리전 확인 (ap-northeast-2)

---

## 🔐 보안 Best Practices

### 1. Secret 관리
- ✅ 절대 코드에 하드코딩하지 않기
- ✅ GitHub Secrets 사용
- ✅ 정기적으로 교체 (90일)
- ✅ 최소 권한 원칙

### 2. AWS IAM 권한 제한
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*",
        "s3:*",
        "rds:*"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "aws:RequestedRegion": "ap-northeast-2"
        }
      }
    }
  ]
}
```

### 3. Environment Protection
```yaml
# .github/workflows/deploy-aws.yml
environment: production  # 승인 필요
```

GitHub Settings에서:
- Settings → Environments → production
- Required reviewers 추가
- 배포 전 수동 승인 필요

---

## 📈 배포 프로세스

### 자동 배포 플로우

```
┌─────────────────────────────────────────────────────────┐
│              Automated Deployment Flow                  │
└─────────────────────────────────────────────────────────┘

1. Developer: git push origin main
   ↓
2. GitHub Actions Triggered
   ↓
3. Run Tests (test.yml)
   ├─ Backend tests
   ├─ Mobile tests
   └─ Security scan
   ↓
4. Deploy AWS (deploy-aws.yml) - if tests pass
   ├─ Terraform apply
   ├─ Build services
   ├─ Deploy to EC2
   └─ Health check
   ↓
5. Build Android (build-android.yml) - if tests pass
   ├─ Build AAB/APK
   ├─ Sign
   └─ Upload to Play Console
   ↓
6. Notifications
   ├─ Email
   ├─ Slack (optional)
   └─ GitHub commit status

Total time: 15-20분
```

---

## 🎯 고급 설정

### Slack 알림 추가

```yaml
# .github/workflows/deploy-aws.yml 마지막에 추가
- name: Slack Notification
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'AWS Deployment ${{ job.status }}'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 단계별 배포 (Canary)

```yaml
# 10% → 50% → 100% 단계적 배포
- name: Deploy 10%
  run: terraform apply -target=aws_instance.app_server[0]

- name: Wait and Monitor
  run: sleep 600  # 10분 대기

- name: Deploy 100%
  if: success()
  run: terraform apply
```

### 롤백 기능

```yaml
# .github/workflows/rollback.yml
name: Rollback Deployment

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to rollback to'
        required: true

jobs:
  rollback:
    runs-on: ubuntu-latest
    steps:
      - name: Rollback to ${{ github.event.inputs.version }}
        run: |
          # Previous version 배포 로직
```

---

## ✅ 배포 완료 체크리스트

### GitHub Secrets 설정 (총 18개)

**AWS 배포 (9개):**
- [ ] AWS_ACCESS_KEY_ID
- [ ] AWS_SECRET_ACCESS_KEY
- [ ] SSH_KEY_NAME
- [ ] SSH_PRIVATE_KEY
- [ ] ADMIN_IP
- [ ] DB_PASSWORD
- [ ] GEMINI_API_KEY
- [ ] GOOGLE_CLIENT_ID
- [ ] GOOGLE_CLIENT_SECRET

**Mobile 배포 (9개):**
- [ ] EXPO_TOKEN
- [ ] EAS_PROJECT_ID
- [ ] API_BASE_URL
- [ ] GOOGLE_CLIENT_ID_IOS
- [ ] GOOGLE_SERVICES_JSON
- [ ] ANDROID_KEYSTORE_BASE64
- [ ] KEYSTORE_PASSWORD
- [ ] KEY_ALIAS
- [ ] KEY_PASSWORD

### 워크플로우 테스트
- [ ] Test workflow 실행 성공
- [ ] AWS deployment workflow 실행 성공
- [ ] Mobile (Expo) build workflow 실행 성공

### 배포 검증
- [ ] EC2 인스턴스 running
- [ ] Health check 통과
- [ ] Android AAB/APK 파일 생성 확인
- [ ] iOS IPA 파일 생성 확인
- [ ] Play Console 업로드 확인 (optional)
- [ ] TestFlight 업로드 확인 (optional)

---

## 🎉 자동 배포 완료!

### 이제 할 수 있는 것:

1. **코드 Push → 자동 배포**
   ```bash
   git push origin main
   # 15-20분 후 자동으로 AWS와 Play Store에 배포됨
   ```

2. **Pull Request → 자동 테스트**
   ```bash
   git checkout -b feature/new-feature
   git push origin feature/new-feature
   # PR 생성 → 자동 테스트 실행
   ```

3. **수동 배포도 가능**
   - GitHub Actions 페이지에서 클릭 한 번

### 배포 모니터링:
```bash
# GitHub Actions 페이지
open https://github.com/YOUR_USERNAME/oddiya/actions

# 또는 CLI로
gh run list
gh run watch
```

---

**Status:** GitHub Actions 자동 배포 설정 완료 ✅
**다음:** Secrets 설정 → 첫 자동 배포 실행! 🚀
