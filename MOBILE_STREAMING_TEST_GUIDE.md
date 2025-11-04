# Mobile Streaming - Testing Guide 📱

**Date:** 2025-11-04
**Status:** ✅ Ready to Test
**Latest Update:** Fixed timer bug + plan saving complete

---

## ✅ What Was Fixed

### Issue 1: Plans Not Being Saved ✅ FIXED
**Problem:** Generated plans were shown in the app but not saved to the database.

**Solution:** Added automatic save to Plan Service after generation:
```typescript
// After plan generation completes:
1. Convert budget level (low/medium/high) to amount
2. POST to Plan Service: /api/v1/plans
3. Refresh plans list
4. Show saved plan in the list
```

### Issue 2: Timer Not Working ✅ FIXED
**Problem:** Timer was showing 0.0s instead of actual elapsed time.

**Cause:** Bug in CreatePlanScreen line 56: `(Date.now() - Date.now())` always equals 0

**Solution:**
```typescript
// Before (WRONG):
const timer = setInterval(() => {
  setElapsedTime((Date.now() - Date.now()) / 1000);
}, 100);

// After (CORRECT):
const startTimestamp = Date.now();
const timer = setInterval(() => {
  setElapsedTime((Date.now() - startTimestamp) / 1000);
}, 100);
```

### Issue 3: Streaming Not Working ⚠️ NEEDS TESTING
**Status:** Code is complete, needs verification on device/simulator.

**Added:**
- Console logging for debugging
- Better error handling
- Direct connection to LLM Agent (port 8000)

---

## 🧪 Testing Steps

### Prerequisites
```bash
# 1. Backend services running
ps aux | grep "python.*main.py"  # LLM Agent on 8000
redis-cli ping                   # Redis should return PONG

# 2. Mobile dependencies installed
cd /Users/wjs/cursor/oddiya/mobile
npm install  # Already done

# 3. Metro bundler clean start
npm start -- --reset-cache
```

### Step 1: Start iOS Simulator
```bash
# Terminal 1: Metro Bundler
cd /Users/wjs/cursor/oddiya/mobile
npm start

# Terminal 2: iOS Simulator
cd /Users/wjs/cursor/oddiya/mobile
npm run ios
```

### Step 2: Navigate to Create Plan Screen

**In the app:**
1. You'll see Welcome/Login screen
2. For testing without auth, you can:
   - **Option A:** Complete OAuth setup
   - **Option B:** Temporarily bypass auth (see below)

**Quick Auth Bypass for Testing** (optional):
```typescript
// In mobile/src/store/slices/authSlice.ts
// Temporarily set:
isAuthenticated: true  // For testing only
```

3. Once logged in, tap **Plans** tab (bottom navigation)
4. Tap **"+ New Plan"** button
5. CreatePlanScreen opens

### Step 3: Test Streaming + Save

**Test Case 1: Fresh Generation**

1. Fill form:
   - Destination: `Seoul`
   - Start Date: `2025-11-10`
   - End Date: `2025-11-12`
   - Budget: `Medium`

2. Tap **"Generate Travel Plan ✨"**

3. Watch for:
   - ✅ Progress bar moves: 0% → 100%
   - ✅ Status messages update in Korean
   - ✅ Timer shows elapsed time
   - ✅ Button disabled during generation

4. Check **Metro Console** for logs:
   ```
   [Streaming] Connecting to: http://localhost:8000/api/v1/plans/generate/stream
   [Streaming] Request: {location: "Seoul", ...}
   [Streaming] Response status: 200
   [Streaming] Stream opened, reading events...
   [Streaming] Status: Seoul의 날씨 정보를 수집하고 있습니다... 10
   [Streaming] Progress: 날씨 정보 수집 완료 20
   [Streaming] Status: AI가 여행 계획을 생성하고 있습니다... 30
   [Streaming] Chunk: Morning: 경복궁...
   [Streaming] Complete! Plan: Seoul 3-Day Trip
   [Streaming] Done signal received
   ```

5. After completion:
   - ✅ Plan preview appears
   - ✅ Shows: "Seoul 3-Day Trip" with cost
   - ✅ Badge: "✨ Newly Generated" (green)
   - ✅ Timer shows ~5-7 seconds

6. **Check if plan was saved:**
   - Go back to Plans tab
   - ✅ Should see the new plan in the list
   - ✅ Plan persists after app reload

**Test Case 2: Cached Generation**

1. Use **EXACT same** parameters:
   - Destination: `Seoul`
   - Start Date: `2025-11-10`
   - End Date: `2025-11-12`
   - Budget: `Medium`

2. Tap **"Generate Travel Plan ✨"** again

3. Watch for:
   - ✅ Progress bar jumps: 50% → 100%
   - ✅ Status: "💾 저장된 계획을 불러오는 중..."
   - ✅ Status box turns green
   - ✅ Timer shows <1 second
   - ✅ Badge: "💾 Cached" (blue)

4. Check **Metro Console**:
   ```
   [Streaming] Status: 💾 저장된 계획을 불러오는 중... 50
   [Streaming] Complete! Plan: Seoul 3-Day Trip
   [Streaming] Done signal received
   ```

5. **Verify NO duplicate in database:**
   - Go to Plans tab
   - ✅ Should NOT create duplicate plan
   - ✅ Same plan count as before

---

## 🐛 Troubleshooting

### Issue: "Response body is not readable"

**Cause:** React Native Fetch doesn't support ReadableStream

**Solution 1: Check React Native Version**
```bash
# Should be 0.75.0 or higher (which you have)
grep "react-native" mobile/package.json
```

**Solution 2: Test on Physical Device Instead**
- iOS Simulator should work fine
- Android Emulator might have issues
- Physical device is best for testing

**Solution 3: Enable Hermes Engine** (if not already)
```javascript
// metro.config.js or app.json
// Hermes has better stream support
```

### Issue: "Network request failed"

**Cause:** Can't connect to localhost:8000

**For iOS Simulator:**
```typescript
// Should work with localhost
http://localhost:8000
```

**For Android Emulator:**
```typescript
// In mobile/src/api/streaming.ts, change:
const llmAgentUrl = 'http://10.0.2.2:8000';
```

**For Physical Device:**
```typescript
// In mobile/src/api/streaming.ts, use your machine's IP:
const llmAgentUrl = 'http://192.168.1.XXX:8000';
```

### Issue: No logs appearing

**Check Metro Console:**
```bash
# Look for console.log output in Metro bundler terminal
# Should see [Streaming] logs
```

**Enable Remote Debugging:**
- Shake device/simulator
- Select "Debug"
- Open Chrome DevTools
- Check Console tab

### Issue: Plan generates but doesn't save

**Check Backend Logs:**
```bash
# Plan Service should receive POST
tail -f services/plan-service/logs/*.log

# Should see:
POST /api/v1/plans - 200 OK
```

**Check Database:**
```bash
# Check if plan was saved
psql -h localhost -U oddiya_user -d oddiya
SELECT * FROM plan_service.travel_plans ORDER BY created_at DESC LIMIT 5;
```

**Common Causes:**
1. User not authenticated (missing X-User-Id header)
2. Budget conversion failed
3. Network error saving to Plan Service

---

## 📊 Expected Results

### Console Output (Success)
```
[Streaming] Connecting to: http://localhost:8000/api/v1/plans/generate/stream
[Streaming] Request: {location: "Seoul", startDate: "2025-11-10", endDate: "2025-11-12", budget: "medium"}
[Streaming] Response status: 200
[Streaming] Stream opened, reading events...
[Streaming] Status: Seoul의 날씨 정보를 수집하고 있습니다... 10
[Streaming] Progress: 날씨 정보 수집 완료 20
[Streaming] Status: AI가 여행 계획을 생성하고 있습니다... 30
[Streaming] Chunk: Morning: 경복궁 (₩3,000)...
[Streaming] Chunk: Afternoon: 북촌 한옥마을...
[Streaming] Progress: 3일 일정 초안 생성 완료 60
[Streaming] Progress: 검증 완료 - 문제 없음 70
[Streaming] Status: 최종 계획을 완성하고 있습니다... 95
[Streaming] Complete! Plan: Seoul 3-Day Trip
[Streaming] Done signal received

Saving plan to database...
Plan saved successfully!
Plans list refreshed.
```

### UI Behavior (Success)

**During Generation:**
```
╔═══════════════════════════╗
║ [███████████░░░] 60%     ║
║                           ║
║ ⏳ AI가 여행 계획을       ║
║    생성하고 있습니다 60%  ║
║                           ║
║ AI Output:                ║
║ Morning: 경복궁 (₩3,000) ║
║ Afternoon: 북촌...        ║
║                           ║
║ Time: 3.2s                ║
╚═══════════════════════════╝
```

**After Completion:**
```
╔═══════════════════════════╗
║ Seoul 3-Day Trip          ║
║ ✨ Newly Generated        ║
║                           ║
║ ₩94,000                   ║
║ 3 days • Generated in 6.2s║
║                           ║
║ [View Full Plan →]        ║
╚═══════════════════════════╝
```

**In Plans List:**
```
┌─────────────────────────┐
│ 🗺️ Seoul 3-Day Trip     │
│ Nov 10-12, 2025         │
│ ₩94,000                 │
└─────────────────────────┘
```

---

## ✅ Success Checklist

### Streaming Tests
- [ ] Metro bundler starts without errors
- [ ] iOS simulator launches app
- [ ] Navigate to CreatePlan screen works
- [ ] Form fields are editable
- [ ] Budget selection buttons work
- [ ] Generate button triggers request
- [ ] Console shows [Streaming] logs
- [ ] Progress bar animates 0% → 100%
- [ ] Status messages update in Korean
- [ ] LLM chunks appear (first generation)
- [ ] Timer updates in real-time
- [ ] Plan preview appears after completion
- [ ] Badge shows "✨ Newly Generated"

### Caching Tests
- [ ] Generate same plan twice
- [ ] Second generation is instant (<1s)
- [ ] Status box turns green
- [ ] Badge shows "💾 Cached"
- [ ] No LLM chunks appear (cached)
- [ ] Console shows cached: true

### Database Save Tests
- [ ] Plan appears in Plans list after generation
- [ ] Plan has correct title, dates, budget
- [ ] Plan persists after app restart
- [ ] No duplicate plans created
- [ ] "View Full Plan" navigates correctly

---

## 🚨 Known Issues

### 1. ReadableStream Not Supported (Rare)
**Symptoms:** Error "Response body is not readable"

**Workaround:** Test on physical device or use fallback non-streaming endpoint

**Future Fix:** Add polyfill or fallback to non-streaming mode

### 2. Timer Not Stopping
**Symptoms:** Timer keeps incrementing after completion

**Fix Already Applied:** Using clearInterval in multiple places

### 3. Duplicate Plans on Cached Generation
**Status:** Should be fixed - verify during testing

**Expected:** Cached generation should NOT save duplicate

---

## 📱 Device-Specific Testing

### iOS Simulator
```bash
npm run ios
# Should work out of the box
# Uses: http://localhost:8000
```

### Android Emulator
```bash
# 1. Change streaming.ts URL to:
const llmAgentUrl = 'http://10.0.2.2:8000';

# 2. Run:
npm run android
```

### Physical Device (iOS)
```bash
# 1. Get your machine's IP
ipconfig getifaddr en0  # macOS

# 2. Update streaming.ts:
const llmAgentUrl = 'http://192.168.1.XXX:8000';

# 3. Make sure device on same network
# 4. Run from Xcode or:
npm run ios --device
```

---

## 🎯 Quick Test Script

```bash
# 1. Start services
# LLM Agent already running ✅
# Redis already running ✅

# 2. Start mobile app
cd /Users/wjs/cursor/oddiya/mobile
npm start  # Terminal 1
npm run ios  # Terminal 2

# 3. In app:
# - Navigate to CreatePlan
# - Fill: Seoul, 2025-11-10, 2025-11-12, Medium
# - Tap Generate
# - Watch streaming progress
# - Verify plan saved in Plans list

# 4. Test caching:
# - Generate same plan again
# - Should be instant with 💾 badge
# - Should NOT create duplicate in list
```

---

## 📝 Test Report Template

```
## Streaming Test Results

**Date:** 2025-11-04
**Device:** iOS Simulator / Android Emulator / Physical Device
**React Native Version:** 0.75.0

### Streaming Functionality
- [ ] PASS / [ ] FAIL - Streaming connection established
- [ ] PASS / [ ] FAIL - Progress updates visible
- [ ] PASS / [ ] FAIL - LLM chunks appear
- [ ] PASS / [ ] FAIL - Completes successfully
- [ ] PASS / [ ] FAIL - Cached result instant

### Database Save
- [ ] PASS / [ ] FAIL - Plan saved after generation
- [ ] PASS / [ ] FAIL - Plan appears in list
- [ ] PASS / [ ] FAIL - No duplicates on cache hit
- [ ] PASS / [ ] FAIL - Plan persists after restart

### Issues Found:
(List any issues with console logs/screenshots)

### Console Logs:
(Paste relevant [Streaming] logs)
```

---

## 🆘 Need Help?

**Check these files:**
1. `mobile/src/api/streaming.ts` - Streaming implementation
2. `mobile/src/screens/CreatePlanScreen.tsx` - UI + save logic
3. `services/llm-agent/src/routes/langgraph_plans.py` - Backend endpoint

**Check logs:**
```bash
# LLM Agent
tail -f /tmp/llm-agent.log

# Metro bundler
# Console output in terminal

# Backend Plan Service
docker logs plan-service  # If using Docker
```

---

**Ready to test! 🚀**

Start with: `cd mobile && npm run ios`
