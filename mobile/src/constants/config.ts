import { Platform } from 'react-native';

export const API_CONFIG = {
  // Base URLs by environment
  LOCAL_SIMULATOR: 'http://localhost:8080',
  LOCAL_ANDROID_EMULATOR: 'http://10.0.2.2:8080',
  LOCAL_DEVICE: 'http://172.16.102.149:8080',
  STAGING: 'https://staging.oddiya.com',
  PRODUCTION: 'https://api.oddiya.com',
};

// Determine which base URL to use
export const getBaseURL = () => {
  if (__DEV__) {
    // Development mode
    if (Platform.OS === 'ios') {
      // iOS Simulator
      return API_CONFIG.LOCAL_SIMULATOR;
    } else {
      // Android Emulator
      return API_CONFIG.LOCAL_ANDROID_EMULATOR;
    }
  }
  // Production
  return API_CONFIG.PRODUCTION;
};

export const BASE_URL = getBaseURL();

export const API_ENDPOINTS = {
  // Auth
  OAUTH_CALLBACK: '/api/auth/oauth2/callback',
  REFRESH_TOKEN: '/api/auth/refresh',
  
  // User
  USER_PROFILE: '/api/users/me',
  
  // Plans
  PLANS: '/api/plans',
  PLAN_BY_ID: (id: number) => `/api/plans/${id}`,
  
  // Videos
  VIDEOS: '/api/videos',
  VIDEO_BY_ID: (id: number) => `/api/videos/${id}`,
  
  // Health
  HEALTH: '/actuator/health',
  OVERVIEW: '/api/overview',
};

export const APP_CONFIG = {
  APP_NAME: 'Oddiya',
  VERSION: '1.0.0',
  TIMEOUT: 30000, // 30 seconds
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000, // 1 second
};

