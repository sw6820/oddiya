import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { videoService } from '@/api/services';
import { VideosState, CreateVideoRequest } from '@/types';
import { v4 as uuidv4 } from 'uuid';

const initialState: VideosState = {
  videos: [],
  isLoading: false,
  error: null,
};

// Async Thunks
export const fetchVideos = createAsyncThunk('videos/fetchVideos', async () => {
  return await videoService.getVideos();
});

export const createVideo = createAsyncThunk(
  'videos/createVideo',
  async (videoData: CreateVideoRequest) => {
    const idempotencyKey = uuidv4();
    return await videoService.createVideo(videoData, idempotencyKey);
  },
);

export const fetchVideoById = createAsyncThunk('videos/fetchById', async (id: number) => {
  return await videoService.getVideoById(id);
});

// Slice
const videosSlice = createSlice({
  name: 'videos',
  initialState,
  reducers: {
    clearError: state => {
      state.error = null;
    },
    updateVideoStatus: (state, action) => {
      const { id, status, videoUrl } = action.payload;
      const video = state.videos.find(v => v.id === id);
      if (video) {
        video.status = status;
        if (videoUrl) {
          video.videoUrl = videoUrl;
        }
      }
    },
  },
  extraReducers: builder => {
    // Fetch videos
    builder
      .addCase(fetchVideos.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchVideos.fulfilled, (state, action) => {
        state.isLoading = false;
        state.videos = action.payload;
      })
      .addCase(fetchVideos.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch videos';
      });

    // Create video
    builder
      .addCase(createVideo.pending, state => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createVideo.fulfilled, (state, action) => {
        state.isLoading = false;
        state.videos.unshift(action.payload);
      })
      .addCase(createVideo.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to create video';
      });

    // Fetch video by ID
    builder
      .addCase(fetchVideoById.pending, state => {
        state.isLoading = true;
      })
      .addCase(fetchVideoById.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.videos.findIndex(v => v.id === action.payload.id);
        if (index !== -1) {
          state.videos[index] = action.payload;
        } else {
          state.videos.push(action.payload);
        }
      })
      .addCase(fetchVideoById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch video';
      });
  },
});

export const { clearError, updateVideoStatus } = videosSlice.actions;
export default videosSlice.reducer;

