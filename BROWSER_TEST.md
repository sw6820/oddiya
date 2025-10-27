# 모바일 브라우저에서 테스트하기

## ⚠️ 중요: 루트 경로는 404입니다!

이것은 **정상**입니다. 

### ❌ 404 에러가 나는 URL (정상):

```
http://172.16.102.149:8080/
http://172.16.102.149:8080/index.html
http://172.16.102.149:8080/home
```

**이유:** 이것은 API 서버입니다. HTML 웹 페이지가 없습니다!

---

## ✅ 올바른 테스트 URL

### iPhone Safari에서:

**1단계:** Safari 열기

**2단계:** 주소창에 정확히 입력:
```
http://172.16.102.149:8080/actuator/health
```

**3단계:** Enter 누르기

**4단계:** 화면에 표시되어야 함:
```json
{"status":"UP"}
```

✅ **이것이 정상입니다!**

---

## 📱 모바일 브라우저 제한사항

모바일 브라우저(Safari, Chrome)로는:

✅ **가능:**
- Health check 조회
- `http://172.16.102.149:8080/actuator/health`

❌ **불가능:**
- 사용자 API (`/api/users/me`)
- 여행 계획 API (`/api/plans`)
- 비디오 API (`/api/videos`)

**이유:** 이 API들은 커스텀 헤더(`X-User-Id`)가 필요한데, 일반 브라우저로는 헤더를 보낼 수 없습니다.

---

## 🚀 실제 모바일 앱에서는

Swift/Kotlin 코드로 헤더를 추가할 수 있으므로 모든 API를 사용할 수 있습니다:

### iOS (Swift)
```swift
var request = URLRequest(url: URL(string: "http://172.16.102.149:8080/api/users/me")!)
request.setValue("1", forHTTPHeaderField: "X-User-Id")

URLSession.shared.dataTask(with: request) { data, response, error in
    // 정상 작동
}
```

### Android (Kotlin)
```kotlin
val request = Request.Builder()
    .url("http://172.16.102.149:8080/api/plans")
    .addHeader("X-User-Id", "1")
    .build()
    
// 정상 작동
```

---

## 📊 요약

| URL | 브라우저 | 모바일 앱 | 결과 |
|-----|----------|-----------|------|
| `/` | ❌ 404 | ❌ 404 | 정상 (없음) |
| `/actuator/health` | ✅ 작동 | ✅ 작동 | `{"status":"UP"}` |
| `/api/users/me` | ❌ 헤더 불가 | ✅ 작동 | 사용자 정보 |
| `/api/plans` | ❌ 헤더 불가 | ✅ 작동 | 여행 계획 목록 |

---

## 지금 테스트하세요!

**iPhone Safari에서:**
1. Safari 앱 열기
2. 주소창:
```
http://172.16.102.149:8080/actuator/health
```
3. Enter
4. `{"status":"UP"}` 확인 ✅

**이것이 성공입니다!**

---

**모든 API 문서:** `MOBILE_ENDPOINTS.md` 또는 `docs/api/MOBILE_API_TESTING.md`

