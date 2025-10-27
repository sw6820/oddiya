# Oddiya Mobile App

React Native mobile application for Oddiya AI-powered travel planner.

## Features

- ✅ OAuth authentication (Google, Apple)
- ✅ AI-powered travel plan generation
- ✅ Video creation from travel photos
- ✅ Push notifications
- ✅ Offline support
- ✅ Reusable component library
- ✅ Comprehensive testing

## Project Structure

```
mobile/
├── src/
│   ├── api/                  # API client and services
│   │   ├── client.ts        # Axios setup with interceptors
│   │   └── services.ts      # API service methods
│   ├── components/           # Reusable components
│   │   ├── atoms/           # Basic components (Button, Input)
│   │   ├── molecules/       # Composed components (PlanCard, VideoCard)
│   │   └── organisms/       # Complex components
│   ├── screens/              # Screen components
│   │   ├── PlansScreen.tsx
│   │   ├── VideosScreen.tsx
│   │   └── ProfileScreen.tsx
│   ├── navigation/           # Navigation setup
│   ├── store/                # Redux store
│   │   ├── index.ts
│   │   └── slices/          # Redux slices
│   │       ├── authSlice.ts
│   │       ├── plansSlice.ts
│   │       ├── videosSlice.ts
│   │       └── uiSlice.ts
│   ├── hooks/                # Custom hooks
│   ├── utils/                # Utility functions
│   ├── types/                # TypeScript types
│   ├── constants/            # Constants and config
│   └── assets/               # Images, fonts
├── __tests__/                # Tests
├── ios/                      # iOS native code
├── android/                  # Android native code
└── package.json
```

## Setup

```bash
cd mobile

# Install dependencies
npm install

# iOS
cd ios && pod install && cd ..
npx react-native run-ios

# Android
npx react-native run-android
```

## Configuration

### API Base URL

Edit `src/constants/config.ts`:

```typescript
export const API_CONFIG = {
  LOCAL_SIMULATOR: 'http://localhost:8080',
  LOCAL_DEVICE: 'http://172.16.102.149:8080',
  PRODUCTION: 'https://api.oddiya.com',
};
```

## Testing

```bash
# Run all tests
npm test

# Watch mode
npm run test:watch

# Coverage
npm run test:coverage
```

## Component Library

### Atoms (Basic Components)

```typescript
import Button from '@/components/atoms/Button';
import Input from '@/components/atoms/Input';

<Button title="Click Me" onPress={() => {}} variant="primary" />
<Input value={text} onChangeText={setText} label="Name" />
```

### Molecules (Composed Components)

```typescript
import PlanCard from '@/components/molecules/PlanCard';
import VideoCard from '@/components/molecules/VideoCard';

<PlanCard plan={planData} onPress={handlePress} />
<VideoCard video={videoData} onPress={handlePress} />
```

## Redux Store

### Usage

```typescript
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchPlans, createPlan } from '@/store/slices/plansSlice';

// In component
const dispatch = useAppDispatch();
const { plans, isLoading } = useAppSelector(state => state.plans);

// Dispatch actions
dispatch(fetchPlans());
dispatch(createPlan({ title, startDate, endDate }));
```

## API Integration

### Services Available

```typescript
import { planService, videoService, userService } from '@/api/services';

// Fetch plans
const plans = await planService.getPlans();

// Create plan
const newPlan = await planService.createPlan({
  title: 'Seoul Trip',
  startDate: '2025-12-01',
  endDate: '2025-12-03',
});

// Create video
const video = await videoService.createVideo({
  photoUrls: ['url1', 'url2'],
  template: 'default',
}, idempotencyKey);
```

## Backend Integration

### Local Development

**iOS Simulator:**
- Base URL: `http://localhost:8080`
- Automatically configured

**Android Emulator:**
- Base URL: `http://10.0.2.2:8080`
- Automatically configured

**Physical Device:**
- Base URL: `http://172.16.102.149:8080`
- Must be on same WiFi network

### Start Backend

```bash
cd ..
./scripts/start-for-mobile-testing.sh
```

## Testing Strategy

### Unit Tests

- Redux reducers and actions
- Utility functions
- API client methods

### Component Tests

- Render tests
- User interaction tests
- Prop validation

### Integration Tests

- API integration
- Redux integration
- Navigation flows

### Coverage Target

- 80% code coverage
- All critical paths tested

## Code Quality

### Linting

```bash
npm run lint
```

### Formatting

```bash
npm run format
```

## Architecture Decisions

### Why Redux Toolkit?

- Type-safe with TypeScript
- Built-in async handling (createAsyncThunk)
- Reduced boilerplate
- DevTools integration

### Why Axios?

- Interceptors for auth
- Request/response transformation
- Automatic retry logic
- Better error handling than fetch

### Why Atomic Design?

- Reusable components
- Consistent UI
- Easy testing
- Scalable structure

## Performance Optimizations

- React.memo for expensive components
- useMemo/useCallback for heavy computations
- FlatList for long lists
- Image optimization
- Code splitting

## Security

- Tokens stored in SecureStore (encrypted)
- SSL pinning (production)
- Input validation
- No sensitive data in logs (production)
- Biometric authentication

## Next Steps

1. Complete navigation setup
2. Add push notifications
3. Implement offline sync
4. Add analytics
5. Build and test on devices
6. Submit to App Store / Play Store

## Resources

- [React Native Docs](https://reactnative.dev/)
- [Redux Toolkit Docs](https://redux-toolkit.js.org/)
- [Testing Library](https://callstack.github.io/react-native-testing-library/)
- [Backend API Docs](../docs/api/MOBILE_API_TESTING.md)

