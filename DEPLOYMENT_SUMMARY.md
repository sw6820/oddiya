# 🚀 배포 종합 요약

**작성일:** 2025-11-04
**대상:** AWS + Android
**총 소요 시간:** 2-4시간

---

## ✅ 완료된 설정

### 1. 환경 설정 ✅
- [x] Gemini API 키 설정
- [x] Google OAuth 설정
- [x] Apple OAuth 설정 (선택사항)
- [x] 데이터베이스 비밀번호 생성
- [x] Seoul 리전 설정
- [x] 모든 시크릿 보안 처리

### 2. 인프라 구성 ✅
- [x] Terraform 설정 완료
- [x] VPC 및 서브넷 설정
- [x] 보안 그룹 설정
- [x] EC2 인스턴스 설정
- [x] 비용 최적화 ($5/월)

---

## 📋 배포 체크리스트

### Phase 1: AWS 배포

#### 1. SSH 키 생성 (5분)
```bash
# AWS Console에서 생성
open https://ap-northeast-2.console.aws.amazon.com/ec2/home?region=ap-northeast-2#KeyPairs:

# 다운로드 후:
mv ~/Downloads/oddiya-prod.pem ~/.ssh/
chmod 400 ~/.ssh/oddiya-prod.pem
```

**가이드:** `AWS_DEPLOYMENT_GUIDE.md`

#### 2. Terraform 배포 (15분)
```bash
cd infrastructure/terraform/phase1
terraform init
terraform apply
```

#### 3. 데이터베이스 설정 (5분)
```bash
./scripts/setup-database-phase1.sh
```

#### 4. 애플리케이션 배포 (10분)
```bash
./scripts/deploy-phase1.sh
```

#### 5. 검증 (2분)
```bash
APP_IP=$(cd infrastructure/terraform/phase1 && terraform output -raw app_server_public_ip)
curl http://$APP_IP:8000/health
curl http://$APP_IP:8083/actuator/health
```

**총 소요 시간:** 30-40분

---

### Phase 2: Android 배포

#### 1. Google Play Developer 계정 (30분)
- 비용: $25 (일회성)
- URL: https://play.google.com/console

#### 2. 프로젝트 설정 (15분)
```typescript
// mobile/src/constants/config.ts
export const API_BASE_URL = 'http://YOUR_EC2_IP:8083';
```

#### 3. 앱 서명 키 생성 (10분)
```bash
keytool -genkey -v \
  -keystore oddiya-release-key.jks \
  -alias oddiya-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

#### 4. 릴리스 빌드 (15분)
```bash
cd mobile/android
./gradlew bundleRelease
```

#### 5. Play Console 업로드 (30분)
- 스토어 등록 정보 작성
- AAB 파일 업로드
- 내부 테스트 설정

#### 6. 프로덕션 출시 (1-7일)
- Google 심사 대기
- 승인 후 자동 배포

**총 소요 시간:** 2-3시간 (첫 배포)
**심사 시간:** 1-7일

**가이드:** `ANDROID_DEPLOYMENT_GUIDE.md`

---

## 🎯 빠른 시작 명령어

### AWS 배포 (올인원)

```bash
# 1. SSH 키 생성 (AWS Console에서 수동)

# 2. 인프라 배포
cd infrastructure/terraform/phase1
terraform init && terraform apply -auto-approve

# 3. 데이터베이스 + 애플리케이션 배포
cd ../../../
./scripts/setup-database-phase1.sh
./scripts/deploy-phase1.sh

# 4. 검증
APP_IP=$(cd infrastructure/terraform/phase1 && terraform output -raw app_server_public_ip)
echo "App Server: http://$APP_IP:8083"
curl http://$APP_IP:8000/health
curl http://$APP_IP:8083/actuator/health
```

### Android 빌드 (올인원)

```bash
# 1. API URL 업데이트 (config.ts에서)

# 2. 빌드
cd mobile/android
./gradlew clean bundleRelease

# 3. 출력 확인
ls -lh app/build/outputs/bundle/release/app-release.aab
```

---

## 📊 배포 상태

| 단계 | 상태 | 소요 시간 | 비용 |
|------|------|-----------|------|
| **환경 설정** | ✅ 완료 | - | Free |
| **AWS 인프라** | ⏳ 대기 | 30-40분 | $5/월 |
| **Android 앱** | ⏳ 대기 | 2-3시간 | $25 (일회성) |

---

## 💰 예상 비용

### 초기 비용
- Google Play Developer: $25 (일회성)
- AWS 프리티어: $0 (12개월)
- **총 초기 비용: $25**

### 월별 비용 (프리티어)
- EC2 t2.micro x2: $0
- EBS 50GB: $1.60
- 데이터 전송: ~$3
- **총 월 비용: ~$5**

### 월별 비용 (프리티어 이후)
- EC2 t2.micro x2: $17
- EBS 50GB: $4
- 데이터 전송: ~$5
- **총 월 비용: ~$26**

### 연간 비용 (첫 해)
- 초기: $25
- 월별 (12개월): $60
- **첫 해 총 비용: $85**

---

## 📚 문서 참조

### 설정 가이드
- `SETUP_COMPLETE.md` - 초기 설정 완료
- `SECRETS_SETUP_COMPLETE.md` - 시크릿 설정
- `OAUTH_STATUS.md` - OAuth 설정 상태
- `APPLE_OAUTH_SETUP.md` - Apple OAuth (선택)

### 배포 가이드
- `AWS_DEPLOYMENT_GUIDE.md` - AWS 배포 (한국어)
- `ANDROID_DEPLOYMENT_GUIDE.md` - Android 배포 (한국어)
- `DEPLOYMENT_READY.md` - 배포 준비 완료

### 기술 문서
- `docs/deployment/PHASE1_DEPLOYMENT_PLAN.md` - 상세 계획
- `docs/deployment/PHASE1_QUICK_START.md` - 빠른 시작

---

## 🔧 유용한 명령어

### AWS 관리

```bash
# 서버 상태 확인
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP
sudo systemctl status llm-agent plan-service

# 로그 확인
sudo journalctl -u llm-agent -f
sudo journalctl -u plan-service -f

# 서비스 재시작
sudo systemctl restart llm-agent
sudo systemctl restart plan-service

# 리소스 삭제 (주의!)
cd infrastructure/terraform/phase1
terraform destroy
```

### Android 관리

```bash
# 새 버전 빌드
cd mobile/android

# 버전 업데이트 (build.gradle)
# versionCode++
# versionName "1.0.1"

./gradlew bundleRelease

# Play Console 업로드
# https://play.google.com/console
```

---

## 🎊 배포 완료 체크리스트

### AWS 배포
- [ ] SSH 키 생성 완료
- [ ] Terraform apply 성공
- [ ] 데이터베이스 설정 완료
- [ ] 애플리케이션 배포 완료
- [ ] Health check 통과
- [ ] API 테스트 성공

### Android 배포
- [ ] Play Developer 계정 생성
- [ ] API URL 업데이트
- [ ] Keystore 생성 및 백업
- [ ] AAB 빌드 성공
- [ ] Play Console 업로드
- [ ] 내부 테스트 완료
- [ ] 프로덕션 출시 승인

---

## 🚀 다음 단계

### 즉시 (AWS 배포 후)
1. ✅ EC2 IP 주소 기록
2. ✅ Android 앱 config.ts 업데이트
3. ✅ 로컬에서 앱 테스트
4. ✅ Play Store 업로드 준비

### 1주일 내
1. 📱 내부 테스트 진행
2. 🐛 버그 수정
3. 📊 피드백 수집
4. 🚀 프로덕션 출시

### 1개월 내
1. 📈 사용자 모니터링
2. ⭐ 리뷰 관리
3. 🆕 기능 추가
4. 🍎 iOS 버전 개발

---

## ⚠️ 중요 참고사항

### AWS
- **프리티어 12개월 후 비용 증가** ($5 → $26/월)
- **IP 주소 변경 시** config.ts와 terraform.tfvars 업데이트
- **백업 설정** (데이터베이스 스냅샷)

### Android
- **Keystore 분실 시 앱 업데이트 불가능**
- **매 배포마다 versionCode 증가**
- **심사 거절 시 수정 후 재제출**

### 보안
- **시크릿은 절대 커밋하지 않기**
- **정기적인 비밀번호 변경 (90일)**
- **AWS 자격 증명 보안**

---

## 📞 지원

### 문제 발생 시
1. 해당 가이드의 "문제 해결" 섹션 확인
2. 로그 확인 (AWS: journalctl, Android: logcat)
3. GitHub Issues 작성

### 유용한 링크
- **AWS Console:** https://ap-northeast-2.console.aws.amazon.com
- **Google Play Console:** https://play.google.com/console
- **Google Cloud Console:** https://console.cloud.google.com

---

## 🎉 성공!

**모든 단계를 완료하면:**
- ✅ AWS 서버 운영 중
- ✅ Android 앱 배포 완료
- ✅ 사용자가 다운로드 및 사용 가능
- ✅ AI 기반 여행 계획 서비스 제공

**축하합니다! Oddiya가 라이브되었습니다!** 🎊

---

**Status:** 배포 가이드 준비 완료
**다음:** SSH 키 생성 → AWS 배포 → Android 빌드 → Play Store 업로드
