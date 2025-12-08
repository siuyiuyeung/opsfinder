import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/auth.service'
import type { LoginRequest, User, UserRole } from '@/types/auth'

/**
 * Authentication store using Pinia.
 */
export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)
  const userRole = computed(() => user.value?.role || null)
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  const isOperator = computed(() => userRole.value === 'OPERATOR' || isAdmin.value)

  // Actions
  /**
   * Initialize auth state from localStorage.
   */
  function initializeAuth() {
    const storedToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')

    if (storedToken && storedUser) {
      accessToken.value = storedToken
      refreshToken.value = storedRefreshToken
      user.value = JSON.parse(storedUser)
    }
  }

  /**
   * Login user with credentials.
   */
  async function login(credentials: LoginRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await authService.login(credentials)

      // Store tokens and user info
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      user.value = {
        id: response.userId,
        username: response.username,
        fullName: response.fullName,
        role: response.role as UserRole,
      }

      // Persist to localStorage
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      localStorage.setItem('user', JSON.stringify(user.value))

      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Logout user and clear all auth data.
   */
  async function logout() {
    try {
      await authService.logout()
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      // Clear state
      user.value = null
      accessToken.value = null
      refreshToken.value = null
      error.value = null

      // Clear localStorage
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }

  /**
   * Refresh user data from server.
   */
  async function refreshUser() {
    if (!isAuthenticated.value) return

    try {
      const userResponse = await authService.getCurrentUser()
      user.value = {
        id: userResponse.id,
        username: userResponse.username,
        fullName: userResponse.fullName,
        role: userResponse.role as UserRole,
      }
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch (err) {
      console.error('Failed to refresh user data:', err)
    }
  }

  /**
   * Clear error message.
   */
  function clearError() {
    error.value = null
  }

  // Initialize on store creation
  initializeAuth()

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loading,
    error,
    // Getters
    isAuthenticated,
    userRole,
    isAdmin,
    isOperator,
    // Actions
    login,
    logout,
    refreshUser,
    clearError,
  }
})
