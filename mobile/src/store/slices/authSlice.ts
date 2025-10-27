import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authService, userService } from '@/api/services';
import { AuthState, TokenResponse, User } from '@/types';

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Async Thunks
export const loginWithOAuth = createAsyncThunk(
  'auth/loginWithOAuth',
  async ({ provider, code }: { provider: string; code: string }) => {
    const tokenResponse = await authService.oauthCallback(provider, code);
    
    // Store tokens
    await AsyncStorage.multiSet([
      ['accessToken', tokenResponse.accessToken],
      ['refreshToken', tokenResponse.refreshToken],
      ['userId', String(tokenResponse.userId)],
    ]);
    
    // Fetch user profile
    const user = await userService.getProfile();
    
    return { tokenResponse, user };
  },
);

export const refreshAuthToken = createAsyncThunk(
  'auth/refreshToken',
  async (refreshToken: string) => {
    const tokenResponse = await authService.refreshToken(refreshToken);
    
    await AsyncStorage.multiSet([
      ['accessToken', tokenResponse.accessToken],
      ['refreshToken', tokenResponse.refreshToken],
      ['userId', String(tokenResponse.userId)],
    ]);
    
    return tokenResponse;
  },
);

export const loadStoredAuth = createAsyncThunk('auth/loadStored', async () => {
  const [[, accessToken], [, refreshToken], [, userId]] = await AsyncStorage.multiGet([
    'accessToken',
    'refreshToken',
    'userId',
  ]);

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

export const logout = createAsyncThunk('auth/logout', async () => {
  await AsyncStorage.multiRemove(['accessToken', 'refreshToken', 'userId']);
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
    // Login
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
        state.error = action.error.message || 'Login failed';
      });

    // Load stored
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

