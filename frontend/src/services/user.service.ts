import api from './api'
import type { UserListResponse, UserCreateRequest, UserUpdateRequest } from '@/types/user'

/**
 * User management service for API calls (admin only).
 */
export const userService = {
  /**
   * Get all users.
   */
  async getAllUsers(): Promise<UserListResponse[]> {
    const response = await api.get<UserListResponse[]>('/users')
    return response.data
  },

  /**
   * Get pending approval users (active=false).
   */
  async getPendingUsers(): Promise<UserListResponse[]> {
    const response = await api.get<UserListResponse[]>('/users/pending')
    return response.data
  },

  /**
   * Get user by ID.
   */
  async getUserById(id: number): Promise<UserListResponse> {
    const response = await api.get<UserListResponse>(`/users/${id}`)
    return response.data
  },

  /**
   * Create a new user (admin operation).
   */
  async createUser(request: UserCreateRequest): Promise<UserListResponse> {
    const response = await api.post<UserListResponse>('/users', request)
    return response.data
  },

  /**
   * Update an existing user.
   */
  async updateUser(id: number, request: UserUpdateRequest): Promise<UserListResponse> {
    const response = await api.put<UserListResponse>(`/users/${id}`, request)
    return response.data
  },

  /**
   * Delete a user.
   */
  async deleteUser(id: number): Promise<void> {
    await api.delete(`/users/${id}`)
  },

  /**
   * Approve a pending user.
   */
  async approveUser(id: number): Promise<UserListResponse> {
    const response = await api.post<UserListResponse>(`/users/${id}/approve`)
    return response.data
  },

  /**
   * Reject a pending user.
   */
  async rejectUser(id: number): Promise<void> {
    await api.post(`/users/${id}/reject`)
  },
}
