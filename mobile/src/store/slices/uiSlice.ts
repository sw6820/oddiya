import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { UIState } from '@/types';

const initialState: UIState = {
  isOffline: false,
  toast: null,
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    setOffline: (state, action: PayloadAction<boolean>) => {
      state.isOffline = action.payload;
    },
    showToast: (
      state,
      action: PayloadAction<{ message: string; type: 'success' | 'error' | 'info' }>,
    ) => {
      state.toast = {
        visible: true,
        message: action.payload.message,
        type: action.payload.type,
      };
    },
    hideToast: state => {
      state.toast = null;
    },
  },
});

export const { setOffline, showToast, hideToast } = uiSlice.actions;
export default uiSlice.reducer;

