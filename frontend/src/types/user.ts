/**
 * User management type definitions.
 */

export interface RegisterRequest {
  username: string
  password: string
  fullName?: string
}

export interface UserCreateRequest {
  username: string
  password: string
  fullName?: string
  role: string
  active?: boolean
}

export interface UserUpdateRequest {
  fullName?: string
  role?: string
  active?: boolean
}

export interface UserListResponse {
  id: number
  username: string
  fullName: string | null
  role: string
  active: boolean
  createdAt: string
  updatedAt: string
}
