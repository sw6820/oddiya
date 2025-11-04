import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authService, userService } from '@/api/services';
import { AuthState, TokenResponse, User } from '@/types';
import { secureStorage } from '@/utils/secureStorage';
import { googleSignInService } from '@/services/googleSignInService';

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

    // Store tokens securely
    await secureStorage.setAuthData({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      userId: String(tokenResponse.userId),
      email: googleUser.email,
    });

    // Fetch user profile
    const user = await userService.getProfile();

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
  const { accessToken, refreshToken, userId } = await secureStorage.getAuthData();

  if (!accessToken || !userId) {
    throw new Error('No stored authentication');
  }

  // Fetch user profile
  const user = await userService.getProfile();

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

