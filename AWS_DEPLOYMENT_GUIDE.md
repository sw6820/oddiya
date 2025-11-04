# 🚀 AWS 배포 가이드 (Phase 1)

**Region:** ap-northeast-2 (Seoul)
**예상 소요 시간:** 30-40분
**월 비용:** ~$5 (프리티어) / ~$26 (프리티어 이후)

---

## 📋 사전 준비 체크리스트

모든 항목이 완료되었는지 확인하세요:

- [x] Gemini API 키 설정 완료
- [x] Google OAuth 설정 완료
- [x] 데이터베이스 비밀번호 생성 완료
- [x] Seoul 리전 설정 완료
- [x] 모든 설정 파일 gitignore 확인 완료
- [ ] AWS CLI 설치 및 설정
- [ ] Terraform 설치
- [ ] SSH 키 페어 생성 (AWS Console)

---

## Step 1: AWS CLI 설정 확인 (2분)

### 1.1 AWS CLI 설치 확인

```bash
# AWS CLI 버전 확인
aws --version

# 설치 안 되어 있으면:
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /
```

### 1.2 AWS 자격 증명 설정

```bash
# AWS 계정 설정
aws configure

# 입력 항목:
# AWS Access Key ID: [Your Access Key]
# AWS Secret Access Key: [Your Secret Key]
# Default region name: ap-northeast-2
# Default output format: json
```

---

## Step 2: SSH 키 페어 생성 (5분)

**중요:** 반드시 **ap-northeast-2 (Seoul)** 리전에서 생성!

### 2.1 AWS Console 열기

```bash
open https://ap-northeast-2.console.aws.amazon.com/ec2/home?region=ap-northeast-2#KeyPairs:
```

### 2.2 키 페어 생성

1. 리전 확인: "아시아 태평양(서울) ap-northeast-2"
2. "키 페어 생성" 클릭
3. 이름: `oddiya-prod`
4. 유형: RSA
5. 형식: .pem
6. "키 페어 생성" 클릭
7. `oddiya-prod.pem` 다운로드

### 2.3 SSH 키 저장

```bash
# .ssh 디렉토리로 이동
mv ~/Downloads/oddiya-prod.pem ~/.ssh/

# 권한 설정 (필수!)
chmod 400 ~/.ssh/oddiya-prod.pem

# 확인
ls -l ~/.ssh/oddiya-prod.pem
```

---

## Step 3: Terraform 배포 (15분)

### 3.1 Terraform 초기화

```bash
cd infrastructure/terraform/phase1

terraform init
```

### 3.2 배포 계획 확인

```bash
terraform plan
```

### 3.3 AWS에 배포

```bash
terraform apply
# 입력: yes

# 10-15분 소요
```

### 3.4 결과 확인

```bash
terraform output > outputs.txt

export APP_IP=$(terraform output -raw app_server_public_ip)
export DB_IP=$(terraform output -raw db_server_private_ip)

echo "App Server: $APP_IP"
echo "DB Server: $DB_IP"
```

---

## Step 4: 데이터베이스 설정 (5분)

```bash
cd ../../../

./scripts/setup-database-phase1.sh
```

---

## Step 5: 애플리케이션 배포 (10분)

### 5.1 빌드

```bash
# Plan Service
cd services/plan-service
./gradlew clean build -x test

# LLM Agent
cd ../llm-agent
tar czf llm-agent.tar.gz src/ requirements.txt main.py .env
```

### 5.2 배포

```bash
cd ../../
./scripts/deploy-phase1.sh
```

---

## Step 6: 검증 (2분)

```bash
APP_IP=$(cd infrastructure/terraform/phase1 && terraform output -raw app_server_public_ip)

# Health checks
curl http://$APP_IP:8000/health
curl http://$APP_IP:8083/actuator/health

# 테스트
curl -X POST http://$APP_IP:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"destination":"Seoul","startDate":"2025-11-10","endDate":"2025-11-12","budget":100000}'
```

---

## 🎉 배포 완료!

### 엔드포인트

- LLM Agent: `http://$APP_IP:8000`
- Plan Service: `http://$APP_IP:8083`

### 다음 단계

1. Android 앱 API URL 업데이트
2. 테스트
3. Google Play 배포

---

## 문제 해결

### SSH 접속 불가

```bash
# 현재 IP 확인
curl ifconfig.me

# terraform.tfvars 업데이트 후 재배포
```

### 서비스 시작 실패

```bash
ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_IP
sudo journalctl -u plan-service -f
sudo journalctl -u llm-agent -f
```

---

## 리소스 정리

```bash
cd infrastructure/terraform/phase1
terraform destroy
# 입력: yes
```
