import plansReducer, {
  fetchPlans,
  createPlan,
  deletePlan,
  clearError,
} from '@/store/slices/plansSlice';
import { PlansState } from '@/types';

describe('plansSlice', () => {
  const initialState: PlansState = {
    plans: [],
    currentPlan: null,
    isLoading: false,
    error: null,
  };

  it('should return the initial state', () => {
    expect(plansReducer(undefined, { type: 'unknown' })).toEqual(initialState);
  });

  it('should handle fetchPlans.pending', () => {
    const action = { type: fetchPlans.pending.type };
    const state = plansReducer(initialState, action);
    
    expect(state.isLoading).toBe(true);
    expect(state.error).toBeNull();
  });

  it('should handle fetchPlans.fulfilled', () => {
    const mockPlans = [
      {
        id: 1,
        userId: 1,
        title: 'Seoul Trip',
        startDate: '2025-12-01',
        endDate: '2025-12-03',
        details: [],
        createdAt: '2025-01-27',
        updatedAt: '2025-01-27',
      },
    ];

    const action = {
      type: fetchPlans.fulfilled.type,
      payload: mockPlans,
    };
    const state = plansReducer(initialState, action);

    expect(state.isLoading).toBe(false);
    expect(state.plans).toEqual(mockPlans);
  });

  it('should handle fetchPlans.rejected', () => {
    const action = {
      type: fetchPlans.rejected.type,
      error: { message: 'Network error' },
    };
    const state = plansReducer(initialState, action);

    expect(state.isLoading).toBe(false);
    expect(state.error).toBe('Network error');
  });

  it('should handle createPlan.fulfilled', () => {
    const newPlan = {
      id: 2,
      userId: 1,
      title: 'Busan Trip',
      startDate: '2025-12-10',
      endDate: '2025-12-15',
      details: [],
      createdAt: '2025-01-27',
      updatedAt: '2025-01-27',
    };

    const action = {
      type: createPlan.fulfilled.type,
      payload: newPlan,
    };
    const state = plansReducer(initialState, action);

    expect(state.plans).toHaveLength(1);
    expect(state.plans[0]).toEqual(newPlan);
    expect(state.currentPlan).toEqual(newPlan);
  });

  it('should handle deletePlan.fulfilled', () => {
    const stateWithPlan: PlansState = {
      ...initialState,
      plans: [
        {
          id: 1,
          userId: 1,
          title: 'Seoul Trip',
          startDate: '2025-12-01',
          endDate: '2025-12-03',
          details: [],
          createdAt: '2025-01-27',
          updatedAt: '2025-01-27',
        },
      ],
    };

    const action = {
      type: deletePlan.fulfilled.type,
      payload: 1,
    };
    const state = plansReducer(stateWithPlan, action);

    expect(state.plans).toHaveLength(0);
  });

  it('should handle clearError', () => {
    const stateWithError: PlansState = {
      ...initialState,
      error: 'Some error',
    };

    const state = plansReducer(stateWithError, clearError());
    expect(state.error).toBeNull();
  });
});

