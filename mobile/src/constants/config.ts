import { Platform } from 'react-native';
import { AWS_EC2_IP, BACKEND_ENV } from '@env';

// Base URLs for different services
// IMPORTANT: All configuration is managed via .env file
// Port assignments:
// - Port 8081: Metro Bundler (React Native packager) - RESERVED FOR LOCAL DEV
// - Port 8082: Auth Service (OAuth, JWT tokens) - LOCAL ONLY
// - Port 8083: Plan Service (CRUD operations)
// - Port 8000: LLM Agent (AI plan generation)

// Environment configuration from .env
const USE_LOCAL = BACKEND_ENV === 'local';
const EC2_IP = AWS_EC2_IP;  // Read from .env (Elastic IP - permanent)
const LOCAL_URL = 'http://localhost';

// Port configuration:
// Local: Auth=8082 (Metro uses 8081), Plan=8083, LLM=8000
// AWS: Auth=8081 (no Metro in production), Plan=8083, LLM=8000
export const BASE_URL = USE_LOCAL ? `${LOCAL_URL}:8082` : `http://${EC2_IP}:8081`;  // Auth Service
export const PLAN_SERVICE_URL = USE_LOCAL ? `${LOCAL_URL}:8083` : `http://${EC2_IP}:8083`;  // Plan Service
export const LLM_AGENT_URL = 'http://localhost:8000';  // LLM Agent for plan generation

export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/api/auth/login',
  SIGNUP: '/api/auth/signup',
  GOOGLE_LOGIN: '/api/v1/auth/google/verify',  // Mobile Google Sign-In endpoint
  APPLE_LOGIN: '/api/v1/auth/apple/verify',  // Mobile Apple Sign-In endpoint
  OAUTH_CALLBACK: '/api/auth/oauth2/callback',
  REFRESH_TOKEN: '/api/auth/refresh',

  // User
  USER_PROFILE: '/api/users/me',

  // Plans (on Plan Service, port 8083)
  PLANS: `${PLAN_SERVICE_URL}/api/v1/plans`,
  PLAN_BY_ID: (id: number) => `${PLAN_SERVICE_URL}/api/v1/plans/${id}`,

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

