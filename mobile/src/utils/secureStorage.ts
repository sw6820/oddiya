/**
 * Secure Token Storage using expo-secure-store
 *
 * More secure than AsyncStorage - uses Keychain (iOS) and KeyStore (Android)
 */

import * as SecureStore from 'expo-secure-store';

const KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_ID: 'user_id',
  USER_EMAIL: 'user_email',
} as const;

export const secureStorage = {
  // Tokens
  async setAccessToken(token: string): Promise<void> {
    await SecureStore.setItemAsync(KEYS.ACCESS_TOKEN, token);
  },

  async getAccessToken(): Promise<string | null> {
    return await SecureStore.getItemAsync(KEYS.ACCESS_TOKEN);
  },

  async setRefreshToken(token: string): Promise<void> {
    await SecureStore.setItemAsync(KEYS.REFRESH_TOKEN, token);
  },

  async getRefreshToken(): Promise<string | null> {
    return await SecureStore.getItemAsync(KEYS.REFRESH_TOKEN);
  },

  // User Info
  async setUserId(userId: string): Promise<void> {
    await SecureStore.setItemAsync(KEYS.USER_ID, userId);
  },

  async getUserId(): Promise<string | null> {
    return await SecureStore.getItemAsync(KEYS.USER_ID);
  },

  async setUserEmail(email: string): Promise<void> {
    await SecureStore.setItemAsync(KEYS.USER_EMAIL, email);
  },

  async getUserEmail(): Promise<string | null> {
    return await SecureStore.getItemAsync(KEYS.USER_EMAIL);
  },

  // Batch operations
  async setAuthData(data: {
    accessToken: string;
    refreshToken: string;
    userId: string;
    email?: string;
  }): Promise<void> {
    await Promise.all([
      SecureStore.setItemAsync(KEYS.ACCESS_TOKEN, data.accessToken),
      SecureStore.setItemAsync(KEYS.REFRESH_TOKEN, data.refreshToken),
      SecureStore.setItemAsync(KEYS.USER_ID, data.userId),
      data.email ? SecureStore.setItemAsync(KEYS.USER_EMAIL, data.email) : Promise.resolve(),
    ]);
  },

  async getAuthData(): Promise<{
    accessToken: string | null;
    refreshToken: string | null;
    userId: string | null;
  }> {
    const [accessToken, refreshToken, userId] = await Promise.all([
      SecureStore.getItemAsync(KEYS.ACCESS_TOKEN),
      SecureStore.getItemAsync(KEYS.REFRESH_TOKEN),
      SecureStore.getItemAsync(KEYS.USER_ID),
    ]);

    return { accessToken, refreshToken, userId };
  },

  async clearAll(): Promise<void> {
    await Promise.all([
      SecureStore.deleteItemAsync(KEYS.ACCESS_TOKEN),
      SecureStore.deleteItemAsync(KEYS.REFRESH_TOKEN),
      SecureStore.deleteItemAsync(KEYS.USER_ID),
      SecureStore.deleteItemAsync(KEYS.USER_EMAIL),
    ]);
  },

  // Check if user is logged in
  async isAuthenticated(): Promise<boolean> {
    const accessToken = await SecureStore.getItemAsync(KEYS.ACCESS_TOKEN);
    return !!accessToken;
  },
};
