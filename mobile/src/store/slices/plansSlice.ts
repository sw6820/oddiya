import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { planService } from '@/api/services';
import { PlansState, CreatePlanRequest } from '@/types';

const initialState: PlansState = {
  plans: [],
  currentPlan: null,
  isLoading: false,
  error: null,
};

// Async Thunks
export const fetchPlans = createAsyncThunk('plans/fetchPlans', async () => {
  return await planService.getPlans();
});

export const createPlan = createAsyncThunk(
  'plans/createPlan',
  async (planData: CreatePlanRequest) => {
    return await planService.createPlan(planData);
  },
);

export const fetchPlanById = createAsyncThunk('plans/fetchById', async (id: number) => {
  return await planService.getPlanById(id);
});

export const updatePlan = createAsyncThunk(
  'plans/updatePlan',
  async ({ id, data }: { id: number; data: CreatePlanRequest }) => {
    return await planService.updatePlan(id, data);
  },
);

export const deletePlan = createAsyncThunk('plans/deletePlan', async (id: number) => {
  await planService.deletePlan(id);
  return id;
});

// Slice
const plansSlice = createSlice({
  name: 'plans',
  initialState,
  reducers: {
    clearCurrentPlan: state => {
      state.currentPlan = null;
    },
    clearError: state => {
      state.error = null;
    },
  },
  extraReducers: builder => {
    // Fetch plans
    builder
      .addCase(fetchPlans.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchPlans.fulfilled, (state, action) => {
        state.isLoading = false;
        state.plans = action.payload;
      })
      .addCase(fetchPlans.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch plans';
      });

    // Create plan
    builder
      .addCase(createPlan.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createPlan.fulfilled, (state, action) => {
        state.isLoading = false;
        state.plans.unshift(action.payload);
        state.currentPlan = action.payload;
      })
      .addCase(createPlan.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to create plan';
      });

    // Fetch plan by ID
    builder
      .addCase(fetchPlanById.pending, state => {
        state.isLoading = true;
      })
      .addCase(fetchPlanById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.currentPlan = action.payload;
      })
      .addCase(fetchPlanById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch plan';
      });

    // Update plan
    builder
      .addCase(updatePlan.fulfilled, (state, action) => {
        const index = state.plans.findIndex(p => p.id === action.payload.id);
        if (index !== -1) {
          state.plans[index] = action.payload;
        }
        state.currentPlan = action.payload;
      });

    // Delete plan
    builder.addCase(deletePlan.fulfilled, (state, action) => {
      state.plans = state.plans.filter(p => p.id !== action.payload);
      if (state.currentPlan?.id === action.payload) {
        state.currentPlan = null;
      }
    });
  },
});

export const { clearCurrentPlan, clearError } = plansSlice.actions;
export default plansSlice.reducer;

