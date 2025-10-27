import MockAdapter from 'axios-mock-adapter';
import axios from 'axios';
import { apiClient } from '@/api/client';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
  setItem: jest.fn(),
  multiGet: jest.fn(),
  multiSet: jest.fn(),
  multiRemove: jest.fn(),
}));

describe('API Client', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(axios);
    jest.clearAllMocks();
  });

  afterEach(() => {
    mock.restore();
  });

  it('adds auth token to requests', async () => {
    (AsyncStorage.getItem as jest.Mock).mockImplementation((key: string) => {
      if (key === 'accessToken') return Promise.resolve('test-token');
      if (key === 'userId') return Promise.resolve('1');
      return Promise.resolve(null);
    });

    mock.onGet('/test').reply(200, { data: 'test' });

    await apiClient.get('/test');

    expect(mock.history.get[0].headers?.Authorization).toBe('Bearer test-token');
    expect(mock.history.get[0].headers?.['X-User-Id']).toBe('1');
  });

  it('handles successful requests', async () => {
    mock.onGet('/api/users/me').reply(200, {
      id: 1,
      email: 'test@example.com',
      name: 'Test User',
    });

    const result = await apiClient.get('/api/users/me');

    expect(result).toEqual({
      id: 1,
      email: 'test@example.com',
      name: 'Test User',
    });
  });

  it('handles 404 errors', async () => {
    mock.onGet('/api/plans/999').reply(404, {
      message: 'Plan not found',
    });

    await expect(apiClient.get('/api/plans/999')).rejects.toThrow();
  });

  it('handles network errors', async () => {
    mock.onGet('/api/plans').networkError();

    await expect(apiClient.get('/api/plans')).rejects.toThrow('Network error');
  });

  it('formats errors correctly', async () => {
    mock.onGet('/api/test').reply(500, {
      message: 'Internal server error',
    });

    await expect(apiClient.get('/api/test')).rejects.toThrow('Internal server error');
  });
});

