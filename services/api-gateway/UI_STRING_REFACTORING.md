# UI String Externalization Guide

## Overview

This guide explains how to complete the externalization of UI strings from SimpleMobileController and WebAppController to prevent hardcoding violations.

## ✅ What's Been Done

1. **Created `UIMessages` configuration class** (`config/UIMessages.java`)
   - Loads all UI strings from configuration
   - Supports multiple languages (currently Korean)
   - Can be overridden via `application.yml`

2. **Created `MessagesController` API** (`controller/MessagesController.java`)
   - Endpoint: `GET /api/messages/ko` - Returns all Korean messages as JSON
   - Endpoint: `GET /api/messages/ko/{key}` - Returns specific message

3. **Created `ui-messages.yml`** configuration file
   - All UI strings defined in YAML
   - Easy to update without code changes
   - Supports internationalization (i18n)

## 🔄 What Needs to be Done

### Option 1: Dynamic Loading (Recommended)

Update `SimpleMobileController.java` and `WebAppController.java` to load strings dynamically:

```javascript
// Add to window.onload
let messages = {};

async function loadMessages() {
    const response = await fetch('/api/messages/ko');
    messages = await response.json();

    // Update UI elements
    document.querySelector('.header h1').textContent = messages['app.title'];
    document.querySelector('.header p').textContent = messages['app.subtitle'];
    // ... etc
}

window.onload = function() {
    loadMessages().then(() => {
        loadPlans();
        // ... rest of initialization
    });
};

// Use messages in functions
function createPlan() {
    if (!location || !startDate || !endDate) {
        alert(messages['message.allFieldsRequired']);
        return;
    }
    // ...
}
```

### Option 2: Server-Side Template (Alternative)

Convert to Thymeleaf template:

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${messages['app.title']}"></title>
</head>
<body>
    <div class="header">
        <h1 th:text="${messages['app.title']}"></h1>
        <p th:text="${messages['app.subtitle']}"></p>
    </div>
    <!-- Use th:text for all strings -->
</body>
</html>
```

## 📝 Implementation Steps

### Step 1: Update SimpleMobileController

```java
@RestController
@RequiredArgsConstructor
public class SimpleMobileController {

    private final UIMessages uiMessages;

    @GetMapping(value = "/mobile", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> simpleMobileApp(ServerHttpResponse response) {
        // Cache control headers
        response.getHeaders().setCacheControl("no-cache, no-store, must-revalidate");

        // Build HTML with messages injected server-side
        String html = buildMobileHTML(uiMessages);
        return Mono.just(html);
    }

    private String buildMobileHTML(UIMessages messages) {
        return """
        <!DOCTYPE html>
        <html>
        <head>...</head>
        <body>
            <script>
            const MESSAGES = """ + toJSON(messages.getAllKorean()) + """;
            </script>
            <!-- Use MESSAGES.key throughout -->
        </body>
        </html>
        """;
    }
}
```

### Step 2: Refactor JavaScript to Use Message Keys

**Before (Hardcoded):**
```javascript
alert('✅ 여행 계획이 생성되었습니다!');
```

**After (Externalized):**
```javascript
alert(MESSAGES['message.planCreated']);
```

### Step 3: Update all alert(), innerHTML, and text assignments

Search for all hardcoded Korean strings and replace with message keys:

```bash
# Find all hardcoded strings in controllers
grep -n "여행\|계획\|생성\|실패" SimpleMobileController.java
```

## 🎯 Complete Mapping

| Current Hardcoded String | Message Key |
|-------------------------|-------------|
| "Oddiya" | `app.title` |
| "AI 여행 플래너" | `app.subtitle` |
| "여행 계획 만들기" | `form.create.title` |
| "여행지" | `form.label.location` |
| "예: 서울, 부산, 제주..." | `form.placeholder.location` |
| "🤖 AI 여행 계획 생성" | `button.createPlan` |
| "✅ 여행 계획이 생성되었습니다!" | `message.planCreated` |
| "❌ 생성에 실패했습니다" | `message.planFailed` |
| "모든 항목을 입력해주세요" | `message.allFieldsRequired` |
| "AI가 계획 생성 중..." | `message.loading` |
| ... | ... |

**See `ui-messages.yml` for complete list of 40+ messages**

## 📦 Files to Modify

1. ✅ `config/UIMessages.java` - Already created
2. ✅ `controller/MessagesController.java` - Already created
3. ✅ `resources/ui-messages.yml` - Already created
4. ⚠️ `controller/SimpleMobileController.java` - **Needs update**
5. ⚠️ `controller/WebAppController.java` - **Needs update**

## 🧪 Testing

After refactoring, verify:

```bash
# 1. Messages API works
curl http://localhost:8080/api/messages/ko

# Expected output:
{
  "app.title": "Oddiya",
  "app.subtitle": "AI 여행 플래너",
  "form.label.location": "여행지",
  ...
}

# 2. UI loads correctly
# Open browser: http://localhost:8080/mobile
# Check that all text displays properly

# 3. No hardcoded strings remain
grep -r "여행.*계획.*만들기" services/api-gateway/src/
# Should only find in ui-messages.yml and UIMessages.java
```

## 🌍 Internationalization (Future)

To add English support:

1. Add `en` map to `UIMessages.java`:
```java
private Map<String, String> en = new HashMap<>();

private void initializeEnglishMessages() {
    en.put("app.title", "Oddiya");
    en.put("app.subtitle", "AI Travel Planner");
    en.put("form.label.location", "Destination");
    // ...
}
```

2. Add language parameter to API:
```java
@GetMapping("/messages/{lang}")
public Mono<Map<String, String>> getMessages(@PathVariable String lang) {
    if ("ko".equals(lang)) {
        return Mono.just(uiMessages.getAllKorean());
    } else if ("en".equals(lang)) {
        return Mono.just(uiMessages.getAllEnglish());
    }
    return Mono.just(uiMessages.getAllKorean()); // default
}
```

3. Update JavaScript to detect language:
```javascript
const userLang = navigator.language.startsWith('ko') ? 'ko' : 'en';
const response = await fetch(`/api/messages/${userLang}`);
```

## 📋 Checklist

- [x] Create UIMessages configuration class
- [x] Create MessagesController API
- [x] Create ui-messages.yml configuration
- [ ] Refactor SimpleMobileController HTML to load messages dynamically
- [ ] Refactor WebAppController HTML to load messages dynamically
- [ ] Replace all alert() calls with message keys
- [ ] Replace all innerHTML assignments with message keys
- [ ] Test all UI flows
- [ ] Verify no hardcoded strings remain

## 🔍 Find Remaining Hardcoded Strings

```bash
# Find hardcoded Korean strings
cd services/api-gateway/src/main/java
grep -rn "여행\|계획\|생성\|실패\|업로드\|사진\|영상" com/oddiya/gateway/controller/

# Should only find:
# - UIMessages.java (definitions)
# - MessagesController.java (class name)
```

## ⚠️ Important Notes

1. **Cache Control**: Messages are loaded once on page load. If messages change, users need to refresh.

2. **Fallback**: If message key not found, show the key itself:
```javascript
function t(key) {
    return MESSAGES[key] || key;
}
```

3. **Placeholder Values**: Some messages contain dynamic values:
```javascript
// Before
alert('📤 ' + files.length + '장의 사진 업로드 중...');

// After
alert(t('message.photoUploading').replace('{count}', files.length));

// Update ui-messages.yml:
message.photoUploading: "📤 {count}장의 사진 업로드 중..."
```

## 📚 References

- Spring Boot Internationalization: https://spring.io/guides/gs/internationalization
- Thymeleaf i18n: https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#using-texts
- JavaScript i18n patterns: https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/i18n

---

**Status**: Infrastructure created ✅ | Controllers need refactoring ⚠️
**Priority**: P2 (Medium)
**Effort**: 4-6 hours to complete refactoring
