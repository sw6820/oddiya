import { apiClient } from './client';
import { API_ENDPOINTS } from '@/constants/config';
import {
  User,
  TravelPlan,
  VideoJob,
  TokenResponse,
  CreatePlanRequest,
  UpdateUserRequest,
  CreateVideoRequest,
} from '@/types';

// Auth Service
export const authService = {
  async oauthCallback(provider: string, code: string): Promise<TokenResponse> {
    return apiClient.post(`${API_ENDPOINTS.OAUTH_CALLBACK}/${provider}`, { code });
  },

  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    return apiClient.post(API_ENDPOINTS.REFRESH_TOKEN, { refreshToken });
  },
};

// User Service
export const userService = {
  async getProfile(): Promise<User> {
    return apiClient.get(API_ENDPOINTS.USER_PROFILE);
  },

  async updateProfile(data: UpdateUserRequest): Promise<User> {
    return apiClient.patch(API_ENDPOINTS.USER_PROFILE, data);
  },
};

// Plan Service
export const planService = {
  async getPlans(): Promise<TravelPlan[]> {
    return apiClient.get(API_ENDPOINTS.PLANS);
  },

  async getPlanById(id: number): Promise<TravelPlan> {
    return apiClient.get(API_ENDPOINTS.PLAN_BY_ID(id));
  },

  async createPlan(data: CreatePlanRequest): Promise<TravelPlan> {
    return apiClient.post(API_ENDPOINTS.PLANS, data);
  },

  async updatePlan(id: number, data: CreatePlanRequest): Promise<TravelPlan> {
    return apiClient.patch(API_ENDPOINTS.PLAN_BY_ID(id), data);
  },

  async deletePlan(id: number): Promise<void> {
    return apiClient.delete(API_ENDPOINTS.PLAN_BY_ID(id));
  },
};

// Video Service
export const videoService = {
  async getVideos(): Promise<VideoJob[]> {
    return apiClient.get(API_ENDPOINTS.VIDEOS);
  },

  async getVideoById(id: number): Promise<VideoJob> {
    return apiClient.get(API_ENDPOINTS.VIDEO_BY_ID(id));
  },

  async createVideo(data: CreateVideoRequest, idempotencyKey: string): Promise<VideoJob> {
    return apiClient.post(API_ENDPOINTS.VIDEOS, data, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    });
  },
};

// Health Check
export const healthService = {
  async checkHealth(): Promise<{ status: string }> {
    return apiClient.get(API_ENDPOINTS.HEALTH);
  },

  async getOverview(): Promise<any> {
    return apiClient.get(API_ENDPOINTS.OVERVIEW);
  },
};

