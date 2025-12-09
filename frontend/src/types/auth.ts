/**
 * Authentication type definitions.
 */

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  username: string
  fullName: string | null
  role: string
}

export interface UserResponse {
  id: number
  username: string
  fullName: string | null
  role: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface User {
  id: number
  username: string
  fullName: string | null
  role: string
}

export const UserRole = {
  ADMIN: 'ADMIN',
  OPERATOR: 'OPERATOR',
  VIEWER: 'VIEWER',
} as const

export type UserRole = typeof UserRole[keyof typeof UserRole]
