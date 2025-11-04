# Mobile Streaming Integration Guide

**Date:** 2025-11-04
**Status:** ✅ Code Complete - Ready for Navigation Setup
**Platform:** React Native (iOS & Android)

---

## 📋 Summary

Implemented ChatGPT-style streaming for travel plan generation in the React Native mobile app with:
- ✅ Real-time progress updates (0-100%)
- ✅ Status messages in Korean
- ✅ LLM chunks display
- ✅ Cache indicators
- ✅ Beautiful UI with progress bars

---

## 📁 Files Created/Modified

### New Files:

1. **`mobile/src/api/streaming.ts`** (170 lines)
   - `generatePlanStreaming()` function
   - SSE event parsing
   - Stream callbacks interface
   - Korean status messages

2. **`mobile/src/screens/CreatePlanScreen.tsx`** (450 lines)
   - Complete create plan form
   - Real-time streaming UI
   - Progress bar, status box, chunks display
   - Plan preview after generation

### Modified Files:

3. **`mobile/src/types/index.ts`**
   - Updated `TravelPlan` interface with LLM fields
   - Added `PlanDay`, `PlanMetadata` interfaces
   - Updated `CreatePlanRequest` with `location` and `budget`

---

## 🎯 Features Implemented

### 1. Streaming Service (`streaming.ts`)

```typescript
import { generatePlanStreaming } from '@/api/streaming';

await generatePlanStreaming(request, {
  onStatus: (message, progress) => {
    // "날씨 정보를 수집하고 있습니다..." at 10%
  },
  onProgress: (message, progress) => {
    // "날씨 정보 수집 완료" at 20%
  },
  onChunk: (content) => {
    // "Morning: 경복궁 (₩3,000)..."
  },
  onComplete: (plan, cached) => {
    // Final plan with cached flag
  },
  onError: (error) => {
    // Error message
  }
});
```

**Features:**
- ✅ Fetch API with ReadableStream
- ✅ SSE format parsing
- ✅ Progress tracking (0-100%)
- ✅ Cache detection
- ✅ Error handling

### 2. CreatePlanScreen UI

**Form Section:**
```
📍 Destination: [Seoul         ]
📅 Start Date:  [2025-11-10    ]
📅 End Date:    [2025-11-12    ]
💰 Budget:      [Low] [Medium*] [High]
[Generate Travel Plan ✨]
```

**Progress Section (During Generation):**
```
[████████████░░░░░░░░] 60%

⏳ AI가 여행 계획을 생성하고 있습니다...        60%

AI Output:
Morning: 경복궁 (₩3,000)...
Afternoon: 북촌 한옥마을...

Time: 3.2s
```

**Plan Preview (After Complete):**
```
┌────────────────────────────────────┐
│ Seoul 3-Day Trip          ₩94,000 │
│ 💾 Cached                          │
│                                    │
│ 3 days • Generated in 0.8s        │
│                                    │
│ [View Full Plan →]                │
└────────────────────────────────────┘
```

**Visual Features:**
- 🟣 Progress bar with smooth animation
- 💬 LLM chunks in scrollable container
- ⏱️ Real-time elapsed timer
- 💾 Green background for cached plans
- 📊 Budget selection with visual feedback

---

## 🔌 Integration Steps

### Step 1: Register Screen in Navigation

**For React Navigation:**

```typescript
// App.tsx or navigation setup
import CreatePlanScreen from '@/screens/CreatePlanScreen';

const Stack = createStackNavigator();

function AppNavigator() {
  return (
    <Stack.Navigator>
      {/* ... existing screens ... */}
      <Stack.Screen
        name="CreatePlan"
        component={CreatePlanScreen}
        options={{ title: 'Create Travel Plan' }}
      />
    </Stack.Navigator>
  );
}
```

**Navigation from PlansScreen:**
Already implemented at `PlansScreen.tsx:22-24`:
```typescript
const handleCreatePlan = () => {
  navigation.navigate('CreatePlan');  // ✅ Already wired up
};
```

### Step 2: Update API Config (if needed)

**Current config** (`mobile/src/constants/config.ts`):
```typescript
LOCAL_SIMULATOR: 'http://localhost:8080',
```

**LLM Agent endpoint:**
```
http://localhost:8000/api/v1/plans/generate/stream
```

**Options:**

**Option A: Keep separate ports** (current setup)
- API Gateway: `localhost:8080` (Java services)
- LLM Agent: `localhost:8000` (Python)
- Streaming service directly calls `localhost:8000`

**Option B: Route through API Gateway**
```typescript
// streaming.ts
const url = `${BASE_URL}/api/v1/plans/generate/stream`;
// API Gateway proxies to LLM Agent on port 8000
```

### Step 3: Test on Simulator/Device

**iOS Simulator:**
```bash
cd mobile
npm install
npx pod-install  # iOS only
npm start
# Press 'i' for iOS
```

**Android Emulator:**
```bash
cd mobile
npm install
npm start
# Press 'a' for Android
```

**Update API URLs if testing on device:**
```typescript
// mobile/src/api/streaming.ts
// For device testing, replace localhost with your machine's IP
const url = 'http://192.168.1.XXX:8000/api/v1/plans/generate/stream';
```

---

## 🎨 UI Components Breakdown

### Progress Bar
```typescript
<View style={styles.progressBarContainer}>
  <View style={[styles.progressBar, { width: `${progress}%` }]} />
</View>
```
- Height: 8px
- Background: #E0E0E0
- Fill: Purple gradient (#667eea)
- Smooth transitions

### Status Box
```typescript
<View style={[styles.statusBox, isCached && styles.statusBoxCached]}>
  <Text style={styles.statusIcon}>{isCached ? '💾' : '⏳'}</Text>
  <Text style={styles.statusText}>{statusMessage}</Text>
  <Text style={styles.progressText}>{progress}%</Text>
</View>
```
- Background: #F5F5F5 (normal) / #E8F5E9 (cached)
- Icon: Emoji status indicator
- Message: Korean status text
- Progress: Purple percentage

### LLM Chunks Display
```typescript
<View style={styles.chunksContainer}>
  <Text style={styles.chunksTitle}>AI Output:</Text>
  <ScrollView style={styles.chunksScroll}>
    {chunks.map((chunk, index) => (
      <Text key={index} style={styles.chunk}>{chunk}</Text>
    ))}
  </ScrollView>
</View>
```
- Max height: 150px
- Scrollable
- Monospace font
- Auto-scroll to bottom

### Budget Selection
```typescript
{budgetOptions.map(option => (
  <TouchableOpacity
    style={[
      styles.budgetOption,
      budget === option.value && styles.budgetOptionActive
    ]}
    onPress={() => setBudget(option.value)}>
    <Text style={styles.budgetLabel}>{option.label}</Text>
    <Text style={styles.budgetDesc}>{option.description}</Text>
  </TouchableOpacity>
))}
```
- Border style changes on selection
- Purple accent when active
- Shows daily budget estimate

---

## 🔄 Data Flow

```
User fills form
    ↓
Clicks "Generate"
    ↓
generatePlanStreaming() called
    ↓
Fetch POST to /api/v1/plans/generate/stream
    ↓
Read stream with getReader()
    ↓
Parse SSE events: "data: {...}\n\n"
    ↓
Callbacks trigger UI updates:
├─ onStatus → setProgress(10%), setStatusMessage("...")
├─ onProgress → setProgress(60%)
├─ onChunk → setChunks([...prev, chunk])
└─ onComplete → setGeneratedPlan(plan)
    ↓
Display plan preview
    ↓
User clicks "View Full Plan"
    ↓
Navigate to PlanDetail screen
```

---

## 📊 Performance Expectations

### Fresh Generation (Cache Miss)
```
Duration: 5-7 seconds
Events: ~15-20 events
Progress: 0% → 10% → 20% → 30% → 60% → 70% → 95% → 100%
Chunks: 5-10 visible chunks
UI: Smooth progress updates
```

### Cached Retrieval (Cache Hit)
```
Duration: <1 second
Events: 3 events (status, complete, done)
Progress: 50% → 100%
Chunks: None (no LLM call)
UI: Green status box, "💾 Cached" badge
```

---

## 🧪 Testing Checklist

### Functional Tests

- [ ] Form validation (empty fields)
- [ ] Date picker working
- [ ] Budget selection working
- [ ] Generate button disabled during generation
- [ ] Progress bar animates smoothly
- [ ] Status messages update in Korean
- [ ] LLM chunks appear progressively
- [ ] Timer shows elapsed time
- [ ] Cache detection working (same request twice)
- [ ] Plan preview displays correctly
- [ ] "View Full Plan" navigates correctly
- [ ] Error handling shows alert

### UI Tests

- [ ] Layout looks good on iPhone SE (small screen)
- [ ] Layout looks good on iPhone 14 Pro Max (large screen)
- [ ] Layout looks good on Android (different screen sizes)
- [ ] Progress bar fills entire width
- [ ] Chunks container scrolls properly
- [ ] Status box turns green for cached plans
- [ ] Budget options highlight correctly
- [ ] Keyboard doesn't cover inputs

### Performance Tests

- [ ] No memory leaks during generation
- [ ] Smooth scrolling in chunks container
- [ ] No lag when updating progress
- [ ] App doesn't freeze during streaming
- [ ] Timer updates without jank

---

## 🐛 Common Issues & Solutions

### Issue: "Response body is not readable"
**Cause:** Fetch API ReadableStream not supported
**Solution:** Use polyfill or update React Native version
```bash
npm install react-native-fetch-api
```

### Issue: Streaming slow on Android emulator
**Cause:** Emulator performance
**Solution:** Test on physical device or use `10.0.2.2` for emulator
```typescript
const url = 'http://10.0.2.2:8000/api/v1/plans/generate/stream';
```

### Issue: Progress bar not animating
**Cause:** Missing animated style
**Solution:** Already using `width: ${progress}%` - check CSS transition

### Issue: Korean text showing as boxes
**Cause:** Font doesn't support Korean characters
**Solution:** Use system font or install Noto Sans KR
```typescript
fontFamily: Platform.OS === 'ios' ? 'System' : 'Roboto'
```

### Issue: Cache always shows "Newly Generated"
**Cause:** Redis not running or wrong host
**Solution:**
```bash
# Check Redis
redis-cli ping  # Should return PONG

# Check REDIS_HOST in .env
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 📱 Screenshot Mock (What Users See)

```
┌─────────────────────────────────────┐
│  ← Back        Create Plan          │
├─────────────────────────────────────┤
│                                     │
│  🚀 AI Travel Planner               │
│  Create your personalized travel    │
│  plan                               │
│                                     │
│  📍 Destination                     │
│  ┌───────────────────────────────┐ │
│  │ Seoul                         │ │
│  └───────────────────────────────┘ │
│                                     │
│  📅 Start Date    📅 End Date      │
│  ┌─────────────┐ ┌──────────────┐ │
│  │ 2025-11-10  │ │ 2025-11-12   │ │
│  └─────────────┘ └──────────────┘ │
│                                     │
│  💰 Budget Level                   │
│  ┌─────────────────────────────┐  │
│  │  Low  •  ₩50,000/day        │  │
│  └─────────────────────────────┘  │
│  ┌─────────────────────────────┐  │
│  │  Medium  •  ₩100,000/day    │◄─ Selected
│  └─────────────────────────────┘  │
│  ┌─────────────────────────────┐  │
│  │  High  •  ₩200,000+/day     │  │
│  └─────────────────────────────┘  │
│                                     │
│  ┌─────────────────────────────┐  │
│  │  Generate Travel Plan ✨     │  │
│  └─────────────────────────────┘  │
│                                     │
│  ╔═══════════════════════════════╗ │
│  ║ [████████████░░░░░░░░] 60%   ║ │
│  ║                               ║ │
│  ║ ⏳ AI가 여행 계획을 생성하고  ║ │
│  ║    있습니다...          60%   ║ │
│  ║                               ║ │
│  ║ AI Output:                    ║ │
│  ║ ┌──────────────────────────┐ ║ │
│  ║ │Morning: 경복궁 (₩3,000)  │ ║ │
│  ║ │Afternoon: 북촌 한옥마을  │ ║ │
│  ║ └──────────────────────────┘ ║ │
│  ║                               ║ │
│  ║ Time: 3.2s                   ║ │
│  ╚═══════════════════════════════╝ │
│                                     │
└─────────────────────────────────────┘
```

---

## 🚀 Next Steps

1. **Register CreatePlanScreen in navigation** (5 minutes)
   ```typescript
   <Stack.Screen name="CreatePlan" component={CreatePlanScreen} />
   ```

2. **Test on simulator** (10 minutes)
   - Generate fresh plan
   - Generate same plan (cached)
   - Verify UI updates

3. **Test on physical device** (optional, 15 minutes)
   - Update API URL to machine IP
   - Test network performance

4. **Integrate with PlanDetail screen** (if not exists)
   - Create screen to show full plan details
   - Handle navigation from preview

---

## 📚 Related Documentation

- **Backend:** `STREAMING_IMPLEMENTATION_COMPLETE.md`
- **Testing:** `STREAMING_TEST_GUIDE.md` (HTML test page)
- **API:** `services/llm-agent/src/routes/langgraph_plans.py`

---

## ✅ Status Summary

| Component | Status | Notes |
|-----------|--------|-------|
| **Streaming Service** | ✅ Complete | `mobile/src/api/streaming.ts` |
| **CreatePlanScreen** | ✅ Complete | `mobile/src/screens/CreatePlanScreen.tsx` |
| **Type Definitions** | ✅ Updated | `mobile/src/types/index.ts` |
| **Navigation Setup** | ⚠️ Pending | Add to navigation stack |
| **Testing** | ⚠️ Pending | Run on simulator/device |

---

**Ready for Integration!** 🎉

Just add CreatePlanScreen to your navigation and test on simulator.

**Test command:**
```bash
cd mobile
npm start
# Press 'i' for iOS or 'a' for Android
# Navigate to "Create Plan" screen
```
