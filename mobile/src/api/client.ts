import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { BASE_URL, APP_CONFIG } from '@/constants/config';

class ApiClient {
  private client: AxiosInstance;
  private isRefreshing = false;
  private failedQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (reason?: unknown) => void;
  }> = [];

  constructor() {
    this.client = axios.create({
      baseURL: BASE_URL,
      timeout: APP_CONFIG.TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - Add auth token
    this.client.interceptors.request.use(
      async (config: InternalAxiosRequestConfig) => {
        const token = await AsyncStorage.getItem('accessToken');
        const userId = await AsyncStorage.getItem('userId');

        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        if (userId) {
          config.headers['X-User-Id'] = userId;
        }

        if (__DEV__) {
          console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`);
        }

        return config;
      },
      error => {
        return Promise.reject(error);
      },
    );

    // Response interceptor - Handle errors and token refresh
    this.client.interceptors.response.use(
      response => {
        if (__DEV__) {
          console.log(`[API] Response:`, response.status);
        }
        return response;
      },
      async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
          _retry?: boolean;
        };

        // Handle 401 Unauthorized - Token expired
        if (error.response?.status === 401 && !originalRequest._retry) {
          if (this.isRefreshing) {
            // Wait for token refresh
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ resolve, reject });
            })
              .then(() => {
                return this.client(originalRequest);
              })
              .catch(err => {
                return Promise.reject(err);
              });
          }

          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            const refreshToken = await AsyncStorage.getItem('refreshToken');
            if (!refreshToken) {
              throw new Error('No refresh token available');
            }

            // Call refresh token endpoint
            const response = await this.client.post('/api/auth/refresh', {
              refreshToken,
            });

            const { accessToken, userId } = response.data;

            // Store new token
            await AsyncStorage.setItem('accessToken', accessToken);
            await AsyncStorage.setItem('userId', String(userId));

            // Retry all failed requests
            this.failedQueue.forEach(promise => promise.resolve());
            this.failedQueue = [];

            // Retry original request
            return this.client(originalRequest);
          } catch (refreshError) {
            // Refresh failed - logout user
            this.failedQueue.forEach(promise => promise.reject(refreshError));
            this.failedQueue = [];

            await AsyncStorage.multiRemove(['accessToken', 'refreshToken', 'userId']);
            
            throw refreshError;
          } finally {
            this.isRefreshing = false;
          }
        }

        // Handle other errors
        if (__DEV__) {
          console.error('[API] Error:', error.response?.data || error.message);
        }

        return Promise.reject(this.formatError(error));
      },
    );
  }

  private formatError(error: AxiosError): Error {
    if (error.response) {
      // Server responded with error
      const data = error.response.data as any;
      return new Error(data?.message || `Server error: ${error.response.status}`);
    } else if (error.request) {
      // Request made but no response
      return new Error('Network error - please check your connection');
    } else {
      // Error setting up request
      return new Error(error.message || 'Unknown error occurred');
    }
  }

  public getInstance(): AxiosInstance {
    return this.client;
  }

  // Convenience methods
  public async get<T>(url: string, config?: any): Promise<T> {
    const response = await this.client.get<T>(url, config);
    return response.data;
  }

  public async post<T>(url: string, data?: any, config?: any): Promise<T> {
    const response = await this.client.post<T>(url, data, config);
    return response.data;
  }

  public async patch<T>(url: string, data?: any, config?: any): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  public async delete<T>(url: string, config?: any): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }
}

export const apiClient = new ApiClient();
export default apiClient.getInstance();

