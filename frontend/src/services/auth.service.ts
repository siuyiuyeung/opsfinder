import api from './api'
import type { LoginRequest, AuthResponse, UserResponse } from '@/types/auth'

/**
 * Authentication service for API calls.
 */
export const authService = {
  /**
   * Login user with username and password.
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials)
    return response.data
  },

  /**
   * Refresh access token using refresh token.
   */
  async refreshToken(refreshToken: string): Promise<{ accessToken: string; tokenType: string }> {
    const response = await api.post('/auth/refresh', { refreshToken })
    return response.data
  },

  /**
   * Get current authenticated user information.
   */
  async getCurrentUser(): Promise<UserResponse> {
    const response = await api.get<UserResponse>('/auth/me')
    return response.data
  },

  /**
   * Logout user (client-side token removal).
   */
  async logout(): Promise<void> {
    await api.post('/auth/logout')
  },
}
