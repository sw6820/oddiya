import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authService, userService } from '@/api/services';
import { AuthState, TokenResponse, User } from '@/types';
import { secureStorage } from '@/utils/secureStorage';
import { googleSignInService } from '@/services/googleSignInService';
import { appleSignInService } from '@/services/appleSignInService';

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Async Thunks

// Email/Password Login
export const loginWithEmail = createAsyncThunk(
  'auth/loginWithEmail',
  async ({ email, password }: { email: string; password: string }) => {
    const tokenResponse = await authService.loginWithEmail(email, password);

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email,
    });

    // Fetch user profile
    const user = await userService.getProfile();

    return { tokenResponse, user };
  },
);

// Email/Password Signup
export const signupWithEmail = createAsyncThunk(
  'auth/signupWithEmail',
  async ({ name, email, password }: { name: string; email: string; password: string }) => {
    const tokenResponse = await authService.signupWithEmail(name, email, password);

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email,
    });

    // Fetch user profile
    const user = await userService.getProfile();

    return { tokenResponse, user };
  },
);

// Google OAuth Login
export const loginWithGoogle = createAsyncThunk(
  'auth/loginWithGoogle',
  async () => {
    // Sign in with Google and get ID token
    const googleUser = await googleSignInService.signIn();

    // Send ID token to backend for verification and JWT generation
    const tokenResponse = await authService.googleLogin(googleUser.idToken);

    console.log('[Auth] Google login response:', {
      userId: tokenResponse.userId,
      userIdType: typeof tokenResponse.userId,
      userIdString: String(tokenResponse.userId),
    });

    // Validate userId before storing
    const userId = tokenResponse.userId;
    if (typeof userId !== 'number' || userId <= 0) {
      console.error('[Auth] Invalid userId from server:', userId);
      throw new Error('서버에서 잘못된 사용자 ID를 반환했습니다');
    }

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email: googleUser.email,
    });

    // Create user object from Google data and token response
    const user: User = {
      id: tokenResponse.userId,
      name: googleUser.name || '',
      email: googleUser.email,
      profileImage: googleUser.photoUrl || null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return { tokenResponse, user };
  },
);

// Apple OAuth Login
export const loginWithApple = createAsyncThunk(
  'auth/loginWithApple',
  async () => {
    // Sign in with Apple and get identity token
    const appleUser = await appleSignInService.signIn();

    // Send identity token and auth code to backend for verification
    const tokenResponse = await authService.appleLogin(
      appleUser.identityToken,
      appleUser.authorizationCode,
    );

    console.log('[Auth] Apple login response:', {
      userId: tokenResponse.userId,
      userIdType: typeof tokenResponse.userId,
      userIdString: String(tokenResponse.userId),
    });

    // Validate userId before storing
    const userId = tokenResponse.userId;
    if (typeof userId !== 'number' || userId <= 0) {
      console.error('[Auth] Invalid userId from server:', userId);
      throw new Error('서버에서 잘못된 사용자 ID를 반환했습니다');
    }

    // Extract name from fullName
    const givenName = appleUser.fullName?.givenName || '';
    const familyName = appleUser.fullName?.familyName || '';
    const name = `${givenName} ${familyName}`.trim() || 'Apple User';

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email: appleUser.email || '',
    });

    // Create user object from Apple data and token response
    const user: User = {
      id: tokenResponse.userId,
      name,
      email: appleUser.email || '',
      profileImage: null, // Apple doesn't provide profile image
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return { tokenResponse, user };
  },
);

// OAuth Callback (for deep linking)
export const loginWithOAuth = createAsyncThunk(
  'auth/loginWithOAuth',
  async ({ provider, code }: { provider: string; code: string }) => {
    const tokenResponse = await authService.oauthCallback(provider, code);

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
    });

    // Fetch user profile
    const user = await userService.getProfile();

    return { tokenResponse, user };
  },
);

// Refresh Token
export const refreshAuthToken = createAsyncThunk(
  'auth/refreshToken',
  async (refreshToken: string) => {
    const tokenResponse = await authService.refreshToken(refreshToken);

    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
    });

    return tokenResponse;
  },
);

// Load Stored Authentication
export const loadStoredAuth = createAsyncThunk('auth/loadStored', async () => {
  const authData = await secureStorage.getAuthData();
  const { accessToken, refreshToken, userId } = authData;

  // Validate stored tokens
  if (!accessToken || !userId || accessToken.trim() === '') {
    throw new Error('No stored authentication');
  }

  // Get email from storage
  const email = await secureStorage.getUserEmail();

  // Create user object from stored data (no API call needed)
  const user: User = {
    id: Number(userId),
    name: email?.split('@')[0] || 'User',
    email: email || '',
    profileImage: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  return {
    accessToken,
    refreshToken,
    user,
  };
});

// Logout
export const logout = createAsyncThunk('auth/logout', async () => {
  await secureStorage.clearAll();
});

// Slice
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: state => {
      state.error = null;
    },
  },
  extraReducers: builder => {
    // Email Login
    builder
      .addCase(loginWithEmail.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginWithEmail.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.tokenResponse.accessToken;
        state.refreshToken = action.payload.tokenResponse.refreshToken;
        state.isAuthenticated = true;
      })
      .addCase(loginWithEmail.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Login failed';
      });

    // Email Signup
    builder
      .addCase(signupWithEmail.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(signupWithEmail.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.tokenResponse.accessToken;
        state.refreshToken = action.payload.tokenResponse.refreshToken;
        state.isAuthenticated = true;
      })
      .addCase(signupWithEmail.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Signup failed';
      });

    // Google Login
    builder
      .addCase(loginWithGoogle.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginWithGoogle.fulfilled, (state, action: any) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.tokenResponse.accessToken;
        state.refreshToken = action.payload.tokenResponse.refreshToken;
        state.isAuthenticated = true;
      })
      .addCase(loginWithGoogle.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Google login failed';
      });

    // Apple Login
    builder
      .addCase(loginWithApple.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginWithApple.fulfilled, (state, action: any) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.tokenResponse.accessToken;
        state.refreshToken = action.payload.tokenResponse.refreshToken;
        state.isAuthenticated = true;
      })
      .addCase(loginWithApple.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Apple login failed';
      });

    // OAuth Callback
    builder
      .addCase(loginWithOAuth.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginWithOAuth.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.tokenResponse.accessToken;
        state.refreshToken = action.payload.tokenResponse.refreshToken;
        state.isAuthenticated = true;
      })
      .addCase(loginWithOAuth.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'OAuth login failed';
      });

    // Load stored auth
    builder
      .addCase(loadStoredAuth.pending, state => {
        state.isLoading = true;
      })
      .addCase(loadStoredAuth.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken || null;
        state.isAuthenticated = true;
      })
      .addCase(loadStoredAuth.rejected, state => {
        state.isLoading = false;
        state.isAuthenticated = false;
      });

    // Logout
    builder.addCase(logout.fulfilled, state => {
      return initialState;
    });

    // Refresh token
    builder
      .addCase(refreshAuthToken.fulfilled, (state, action) => {
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
      })
      .addCase(refreshAuthToken.rejected, state => {
        // Token refresh failed - logout
        return initialState;
      });
  },
});

export const { clearError } = authSlice.actions;
export default authSlice.reducer;

