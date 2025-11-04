// API Response Types

export interface User {
  id: number;
  email: string;
  name: string;
  provider: string;
  createdAt: string;
  updatedAt: string;
}

export interface TravelPlan {
  id?: number;
  userId?: number;
  title: string;
  startDate?: string;
  endDate?: string;
  details?: PlanDetail[];
  days?: PlanDay[];  // From LLM service
  totalEstimatedCost?: number;
  currency?: string;
  weatherSummary?: string;
  tips?: string[];
  metadata?: PlanMetadata;
  createdAt?: string;
  updatedAt?: string;
}

export interface PlanDetail {
  id: number;
  day: number;
  location: string;
  activity: string;
}

export interface PlanDay {
  day: number;
  location: string;
  activity: string;
  estimatedCost: number;
  weatherTip?: string;
}

export interface PlanMetadata {
  generatedAt: string;
  iterations: number;
  location: string;
  budget: string;
  aiModel: string;
  externalApis: string[];
  architecture: string;
}

export interface VideoJob {
  id: number;
  userId: number;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  photoUrls: string[];
  template: string;
  videoUrl?: string;
  idempotencyKey: string;
  createdAt: string;
  updatedAt: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
}

// API Request Types

export interface CreatePlanRequest {
  title?: string;
  location: string;
  startDate: string;
  endDate: string;
  budget?: 'low' | 'medium' | 'high';
  maxIterations?: number;
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
}

export interface CreateVideoRequest {
  photoUrls: string[];
  template?: string;
}

// Redux State Types

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export interface PlansState {
  plans: TravelPlan[];
  currentPlan: TravelPlan | null;
  isLoading: boolean;
  error: string | null;
}

export interface VideosState {
  videos: VideoJob[];
  isLoading: boolean;
  error: string | null;
}

export interface UIState {
  isOffline: boolean;
  toast: {
    visible: boolean;
    message: string;
    type: 'success' | 'error' | 'info';
  } | null;
}

// Component Props Types

export interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline';
  disabled?: boolean;
  loading?: boolean;
  icon?: string;
}

export interface CardProps {
  children: React.ReactNode;
  onPress?: () => void;
  style?: object;
}

export interface InputProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  label?: string;
  error?: string;
  secureTextEntry?: boolean;
  keyboardType?: 'default' | 'email-address' | 'numeric';
}

