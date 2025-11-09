# iOS 무선 디버깅 설정 가이드

**요구사항:** 처음 한 번은 USB 연결 필요

---

## 📋 무선 디버깅 설정 단계

### Step 1: USB로 디바이스 연결 (처음 한 번)

```
1. iPhone/iPad를 USB 케이블로 Mac에 연결
2. 디바이스 잠금 해제
3. "이 컴퓨터 신뢰" 탭
4. Xcode에서 디바이스 인식 확인
```

### Step 2: 무선 연결 활성화

**Xcode에서:**

```
1. Xcode 상단 메뉴 → Window → Devices and Simulators (⇧⌘2)

2. "Devices" 탭 선택 (왼쪽)

3. 연결된 iPhone/iPad 선택

4. "Connect via network" 체크박스 ✅ 활성화

5. 디바이스 옆에 네트워크 아이콘 (🌐) 표시됨

6. USB 케이블 제거해도 됨!
```

### Step 3: 무선으로 빌드 및 실행

```bash
# USB 케이블 없이도 실행 가능!
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios --device
```

**또는 Xcode에서:**
```
1. 상단 디바이스 선택 드롭다운
2. 네트워크 아이콘 있는 디바이스 선택
3. Run (⌘R)
```

---

## ⚠️ 무선 디버깅 요구사항

### 필수 조건:

1. **같은 Wi-Fi 네트워크**
   ```
   Mac과 iPhone/iPad가 같은 Wi-Fi에 연결되어야 함

   확인 방법:
   - Mac: Wi-Fi 설정 확인
   - iPhone: 설정 → Wi-Fi → 네트워크 이름 확인
   - 동일해야 함!
   ```

2. **처음 한 번은 USB 연결**
   ```
   최초 설정 시 USB 케이블로 연결 필수
   이후부터 무선 가능
   ```

3. **Xcode에서 "Connect via network" 활성화**
   ```
   체크박스 활성화 후 사용 가능
   ```

### 선택 사항:

- **방화벽 허용:** Mac 방화벽이 Xcode 차단하지 않도록 설정
- **블루투스 ON:** 더 빠른 초기 연결 (선택)

---

## 🔧 무선 연결 문제 해결

### 문제 1: 디바이스가 안 보임

**해결:**
```
1. 같은 Wi-Fi에 연결되어 있는지 확인
2. iPhone/iPad 재시작
3. Mac 재시작
4. Xcode 재시작
5. USB로 다시 연결 후 "Connect via network" 재설정
```

### 문제 2: 연결 느림 또는 불안정

**원인:**
- Wi-Fi 신호 약함
- 네트워크 혼잡
- VPN 사용 중

**해결:**
```
1. Mac과 iPhone을 가까이 배치
2. 라우터 근처로 이동
3. VPN 끄기
4. 5GHz Wi-Fi 사용 (2.4GHz보다 빠름)
```

### 문제 3: "Waiting for [Device Name]"

**해결:**
```bash
# USB로 다시 연결
# Xcode → Devices and Simulators
# "Unpair Device" → "Pair" 다시 하기
# "Connect via network" 다시 체크
```

---

## 💡 무선 vs USB 비교

| 항목 | USB 연결 | 무선 연결 |
|------|---------|----------|
| **빌드 속도** | ⚡ 빠름 | ⚠️ 느림 (Wi-Fi 속도 의존) |
| **안정성** | ✅ 매우 안정적 | ⚠️ 네트워크 환경 의존 |
| **편의성** | ❌ 케이블 필요 | ✅ 케이블 불필요 |
| **디버깅** | ✅ 로그 빠름 | ⚠️ 로그 느릴 수 있음 |
| **충전** | ✅ 동시 충전 가능 | ❌ 별도 충전 필요 |
| **이동성** | ❌ 케이블 범위 내 | ✅ Wi-Fi 범위 내 |

---

## 🎯 권장 사용 시나리오

### USB 연결 추천:

```
✅ 처음 디바이스 등록할 때 (필수)
✅ 큰 앱 빌드할 때 (속도 중요)
✅ 디버깅 집중할 때 (안정성 중요)
✅ 네트워크 불안정할 때
```

### 무선 연결 추천:

```
✅ 자주 테스트할 때 (편의성)
✅ 여러 디바이스 테스트할 때
✅ 책상 정리하고 싶을 때
✅ 디바이스를 들고 테스트할 때 (UI 터치 테스트 등)
```

---

## 📱 여러 디바이스 관리

### 무선으로 여러 디바이스 동시 연결:

```
1. 각 디바이스를 한 번씩 USB로 연결
2. 각각 "Connect via network" 활성화
3. 모두 같은 Wi-Fi에 연결
4. Xcode에서 여러 디바이스 동시 표시
5. 원하는 디바이스 선택해서 빌드
```

**장점:**
- iPhone, iPad 동시 테스트 가능
- 케이블 여러 개 불필요
- 빠른 디바이스 전환

---

## 🚀 빠른 시작 가이드

### 처음 사용:

```bash
1. iPhone을 USB로 Mac에 연결
2. Xcode → Window → Devices and Simulators (⇧⌘2)
3. iPhone 선택 → "Connect via network" ✅
4. 네트워크 아이콘 (🌐) 나타날 때까지 대기
5. USB 케이블 제거
6. npx react-native run-ios --device
7. 완료!
```

### 이후 사용:

```bash
# 그냥 실행하면 됨 (USB 불필요)
cd /Users/wjs/cursor/oddiya/mobile
npx react-native run-ios --device
```

---

## 🔍 무선 연결 상태 확인

### Xcode에서 확인:

```
Window → Devices and Simulators (⇧⌘2)

디바이스 상태 아이콘:
🌐 = 무선 연결됨
⚡ = USB 연결됨
⚠️ = 연결 문제
```

### 터미널에서 확인:

```bash
# 연결된 디바이스 목록
xcrun xctrace list devices

# 출력 예시:
# iPhone (15.0) (wireless)  # 무선 연결
# iPad (15.0) (wired)       # USB 연결
```

---

## ⚡ 성능 최적화 팁

### 무선 연결 속도 향상:

```
1. 5GHz Wi-Fi 사용 (2.4GHz보다 2-3배 빠름)
2. 라우터와 가까운 위치
3. 다른 네트워크 트래픽 최소화
4. Mac과 iPhone 블루투스 ON (초기 연결 빠름)
```

### 대용량 앱 빌드:

```
첫 빌드: USB 권장 (빠름)
이후 수정: 무선 가능 (증분 빌드는 빠름)
```

---

## 📋 체크리스트

### 무선 디버깅 설정 완료:

- [ ] iPhone/iPad를 USB로 한 번 연결
- [ ] "이 컴퓨터 신뢰" 탭
- [ ] Xcode에서 디바이스 인식 확인
- [ ] "Connect via network" 체크박스 활성화
- [ ] 네트워크 아이콘 (🌐) 표시 확인
- [ ] USB 케이블 제거
- [ ] 무선으로 빌드 테스트
- [ ] 성공! 🎉

### 무선 연결 사용 중:

- [ ] Mac과 iPhone이 같은 Wi-Fi에 연결됨
- [ ] Wi-Fi 신호 강함
- [ ] 빌드 및 실행 정상 작동
- [ ] 로그 확인 가능

---

## 🆘 긴급 상황

### 무선이 안 되면:

```bash
# 항상 USB로 돌아갈 수 있음!
1. USB 케이블로 다시 연결
2. 즉시 작동
3. 무선 재설정 시도
```

---

**결론:** 처음 한 번만 USB 연결, 이후 무선으로 편하게 개발! 🚀

**Last Updated:** 2025-11-09
