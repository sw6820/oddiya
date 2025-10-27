# React Native Mobile App - Chain of Thought Planning

## 🎯 Goal

Create a React Native mobile app for Oddiya that integrates with the existing 7 microservices backend.

## 📊 Chain of Thought (CoT) Planning

### Step 1: Requirements Analysis

**What we need:**
- OAuth login (Google, Apple)
- User profile management
- AI-powered travel plan creation
- Photo upload and video generation
- Push notifications for video completion

**Technical constraints:**
- Backend API: http://172.16.102.149:8080 (local) → https://api.oddiya.com (production)
- Authentication: RS256 JWT tokens
- Video processing: Async with push notifications
- Offline support: Cache user data

**User flows:**
1. **Login Flow:** OAuth → JWT → Store token
2. **Plan Flow:** Input dates → AI generates → View/Edit → Save
3. **Video Flow:** Select photos → Upload to S3 → Create job → Wait for notification → View video

### Step 2: Architecture Design

**Technology Stack:**
- **Framework:** React Native (iOS + Android)
- **State Management:** Redux Toolkit
- **Navigation:** React Navigation v6
- **API Client:** Axios with interceptors
- **Storage:** AsyncStorage + SecureStore
- **UI Components:** Custom + React Native Paper
- **Testing:** Jest + React Native Testing Library
- **Push Notifications:** React Native Firebase

**Folder Structure:**
```
mobile/
├── src/
│   ├── api/              # API client
│   ├── components/       # Reusable components
│   ├── screens/          # Screen components
│   ├── navigation/       # Navigation setup
│   ├── store/            # Redux store
│   ├── hooks/            # Custom hooks
│   ├── utils/            # Utilities
│   ├── types/            # TypeScript types
│   └── constants/        # Constants
├── __tests__/            # Tests
├── ios/                  # iOS native code
├── android/              # Android native code
└── package.json
```

### Step 3: Component Architecture

**Reusable Components:**

1. **Atoms** (Basic building blocks)
   - Button, Input, Text, Icon, Avatar, Badge, Spinner

2. **Molecules** (Simple combinations)
   - FormField, Card, ListItem, SearchBar, DatePicker

3. **Organisms** (Complex components)
   - PlanCard, VideoCard, Header, BottomNav, PhotoGrid

4. **Templates** (Page layouts)
   - AuthLayout, MainLayout, EmptyState, ErrorBoundary

5. **Screens** (Complete pages)
   - LoginScreen, HomeScreen, PlansScreen, VideosScreen, ProfileScreen

### Step 4: State Management Strategy

**Redux Slices:**
- `authSlice` - User authentication & tokens
- `userSlice` - User profile data
- `plansSlice` - Travel plans
- `videosSlice` - Video jobs
- `uiSlice` - Loading states, modals, toasts

**Async Thunks:**
- `loginWithGoogle`, `refreshToken`, `logout`
- `fetchUserProfile`, `updateProfile`
- `fetchPlans`, `createPlan`, `updatePlan`, `deletePlan`
- `createVideo`, `fetchVideos`, `checkVideoStatus`

### Step 5: API Integration Strategy

**API Client Features:**
- Base URL configuration (local vs production)
- Request interceptor (add auth token)
- Response interceptor (handle 401, refresh token)
- Retry logic with exponential backoff
- Request/response logging (dev only)
- Error handling with user-friendly messages

**Endpoints to Integrate:**
- `POST /api/auth/oauth2/callback/{provider}`
- `POST /api/auth/refresh`
- `GET /api/users/me`, `PATCH /api/users/me`
- `GET /api/plans`, `POST /api/plans`, `GET /api/plans/{id}`
- `GET /api/videos`, `POST /api/videos`, `GET /api/videos/{id}`

### Step 6: Testing Strategy

**Unit Tests:**
- Redux reducers and actions
- Utility functions
- Custom hooks
- API client methods

**Component Tests:**
- Render tests for all components
- User interaction tests
- Accessibility tests

**Integration Tests:**
- Complete user flows
- API integration with mock server
- Navigation flows

**E2E Tests:** (Optional)
- Detox for full app testing

**Test Coverage Target:** >80%

### Step 7: Performance Optimization

**Strategies:**
- Lazy load screens (React.lazy)
- Memoize expensive components (React.memo)
- Optimize images (react-native-fast-image)
- Cache API responses (React Query)
- Virtual lists for long content (FlatList optimization)
- Code splitting by route

### Step 8: Offline Support

**Features:**
- Cache user profile locally
- Cache travel plans for offline viewing
- Queue photo uploads when offline
- Sync when back online
- Show offline indicator

### Step 9: Security Considerations

**Implementation:**
- Store tokens in SecureStore (encrypted)
- Never log sensitive data
- SSL pinning for API calls (production)
- Validate all user inputs
- Sanitize data before display
- Implement biometric auth (Touch ID, Face ID)

### Step 10: Development Phases

**Phase 1: Foundation** (1-2 days)
- Project setup
- Navigation structure
- API client
- Redux store
- Reusable components library

**Phase 2: Authentication** (1 day)
- OAuth integration
- Token management
- Login/Logout flows

**Phase 3: Core Features** (2-3 days)
- User profile screen
- Travel plans (list, create, edit)
- AI plan generation integration

**Phase 4: Video Features** (1-2 days)
- Photo upload
- Video job creation
- Push notifications
- Video playback

**Phase 5: Polish** (1-2 days)
- UI/UX improvements
- Error handling
- Loading states
- Animations

**Phase 6: Testing** (1-2 days)
- Write comprehensive tests
- Fix bugs found
- Performance optimization

**Total Estimated Time:** 8-12 days

---

## 📋 Implementation Checklist

### Foundation
- [ ] Initialize React Native project
- [ ] Set up TypeScript
- [ ] Configure ESLint + Prettier
- [ ] Set up folder structure
- [ ] Install core dependencies
- [ ] Configure environment variables
- [ ] Set up Redux store
- [ ] Configure navigation
- [ ] Create API client

### Components Library
- [ ] Design system (colors, typography, spacing)
- [ ] Button component with variants
- [ ] Input component with validation
- [ ] Card components
- [ ] List components
- [ ] Modal components
- [ ] Loading indicators
- [ ] Error boundaries

### Testing Setup
- [ ] Jest configuration
- [ ] Testing Library setup
- [ ] Mock API responses
- [ ] Test utilities
- [ ] Coverage reporting

### Authentication
- [ ] OAuth integration
- [ ] Token storage
- [ ] Auto token refresh
- [ ] Secure storage
- [ ] Biometric auth

### Features
- [ ] User profile
- [ ] Travel plans CRUD
- [ ] AI plan generation
- [ ] Photo picker
- [ ] S3 upload
- [ ] Video jobs
- [ ] Push notifications
- [ ] Video player

### Quality
- [ ] Error handling
- [ ] Loading states
- [ ] Empty states
- [ ] Offline support
- [ ] Accessibility
- [ ] Animations
- [ ] Performance optimization

---

**Next:** Create the React Native project structure with all planned components

