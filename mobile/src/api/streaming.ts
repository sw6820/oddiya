/**
 * Streaming API for real-time travel plan generation
 * Supports Server-Sent Events (SSE) for ChatGPT-style progressive updates
 */

import { BASE_URL } from '@/constants/config';
import { CreatePlanRequest, TravelPlan } from '@/types';

export interface StreamEvent {
  type: 'status' | 'progress' | 'chunk' | 'complete' | 'error' | 'done';
  message?: string;
  progress?: number;
  step?: string;
  content?: string;
  plan?: TravelPlan;
  cached?: boolean;
  error?: string;
}

export interface StreamCallbacks {
  onStatus?: (message: string, progress: number) => void;
  onProgress?: (message: string, progress: number) => void;
  onChunk?: (content: string) => void;
  onComplete?: (plan: TravelPlan, cached: boolean) => void;
  onError?: (error: string) => void;
}

/**
 * Generate travel plan with real-time streaming updates
 *
 * @param request - Plan generation request
 * @param callbacks - Callbacks for streaming events
 * @returns Promise that resolves with the final plan
 */
export async function generatePlanStreaming(
  request: CreatePlanRequest,
  callbacks: StreamCallbacks
): Promise<TravelPlan> {
  // Use LLM Agent directly (port 8000, not API Gateway 8080)
  const llmAgentUrl = BASE_URL.replace('8080', '8000');
  const url = `${llmAgentUrl}/api/v1/plans/generate/stream`;

  console.log('[Streaming] Connecting to:', url);
  console.log('[Streaming] Request:', request);

  return new Promise(async (resolve, reject) => {
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify({
          location: request.location,
          startDate: request.startDate,
          endDate: request.endDate,
          budget: request.budget || 'medium',
        }),
      });

      console.log('[Streaming] Response status:', response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('[Streaming] Error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      // Check if response body is readable
      if (!response.body) {
        throw new Error('Response body is not readable - streaming may not be supported');
      }

      console.log('[Streaming] Stream opened, reading events...');

      // Read SSE stream
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let finalPlan: TravelPlan | null = null;

      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          break;
        }

        // Decode and append to buffer
        buffer += decoder.decode(value, { stream: true });

        // Process complete lines
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // Keep incomplete line in buffer

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const eventData = line.substring(6).trim();
            if (!eventData) continue;

            try {
              const event: StreamEvent = JSON.parse(eventData);

              // Handle different event types
              switch (event.type) {
                case 'status':
                  console.log('[Streaming] Status:', event.message, event.progress);
                  callbacks.onStatus?.(event.message || '', event.progress || 0);
                  break;

                case 'progress':
                  console.log('[Streaming] Progress:', event.message, event.progress);
                  callbacks.onProgress?.(event.message || '', event.progress || 0);
                  break;

                case 'chunk':
                  console.log('[Streaming] Chunk:', event.content?.substring(0, 50) + '...');
                  callbacks.onChunk?.(event.content || '');
                  break;

                case 'complete':
                  console.log('[Streaming] Complete! Plan:', event.plan?.title);
                  finalPlan = event.plan || null;
                  callbacks.onComplete?.(event.plan!, event.cached || false);
                  break;

                case 'error':
                  console.error('[Streaming] Error:', event.message);
                  callbacks.onError?.(event.message || 'Unknown error');
                  reject(new Error(event.message || 'Generation failed'));
                  return;

                case 'done':
                  console.log('[Streaming] Done signal received');
                  // Stream completed
                  if (finalPlan) {
                    resolve(finalPlan);
                  } else {
                    reject(new Error('Stream completed without final plan'));
                  }
                  return;
              }
            } catch (parseError) {
              console.error('Failed to parse SSE event:', eventData, parseError);
            }
          }
        }
      }

      // If loop exits without done event
      if (finalPlan) {
        resolve(finalPlan);
      } else {
        reject(new Error('Stream ended unexpectedly'));
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to generate plan';
      callbacks.onError?.(errorMessage);
      reject(error);
    }
  });
}

/**
 * Helper function to format Korean status messages
 */
export const StreamMessages = {
  GATHERING_WEATHER: '날씨 정보를 수집하고 있습니다...',
  WEATHER_COMPLETE: '날씨 정보 수집 완료',
  GENERATING_PLAN: 'AI가 여행 계획을 생성하고 있습니다...',
  DRAFT_COMPLETE: '일정 초안 생성 완료',
  VALIDATING: '계획을 검증하고 있습니다...',
  VALIDATION_COMPLETE: '검증 완료',
  REFINING: '계획을 개선하고 있습니다...',
  FINALIZING: '최종 계획을 완성하고 있습니다...',
  COMPLETE: '여행 계획 생성 완료!',
  CACHED: '저장된 계획을 불러오는 중...',
  CACHED_COMPLETE: '저장된 계획 로드 완료!',
};
