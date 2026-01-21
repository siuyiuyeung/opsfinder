import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import router from '@/router'

/**
 * Axios instance with JWT token interceptors.
 */
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
})

// Token refresh state to prevent race conditions
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

/**
 * Process queued requests after token refresh completes.
 */
function processQueue(error: unknown, token: string | null = null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token!)
    }
  })
  failedQueue = []
}

/**
 * Handle session expiration - clear tokens and redirect to login.
 */
async function handleSessionExpired() {
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()

  // Set session expired flag and clear session
  authStore.setSessionExpired(true)
  authStore.clearSession()

  // Redirect to login
  router.push({ name: 'Login' })
}

/**
 * Request interceptor to add JWT token to headers.
 */
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * Check if error is an authentication/session error.
 * 401: Token invalid/expired
 * 403: No token or session issue (when no auth header sent)
 */
function isAuthError(error: any): boolean {
  const status = error.response?.status
  if (status === 401) return true

  // 403 with no token in localStorage indicates session issue
  if (status === 403 && !localStorage.getItem('accessToken')) {
    return true
  }

  return false
}

/**
 * Response interceptor to handle authentication errors and token refresh.
 */
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // Handle authentication errors (401 or 403 with no token)
    if (isAuthError(error) && !originalRequest._retry) {
      // If already refreshing, queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => {
            return Promise.reject(err)
          })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          throw new Error('No refresh token available')
        }

        // Try to refresh the token
        const response = await axios.post(
          `${api.defaults.baseURL}/auth/refresh`,
          { refreshToken }
        )

        const { accessToken } = response.data
        localStorage.setItem('accessToken', accessToken)

        // Process queued requests with new token
        processQueue(null, accessToken)

        // Retry the original request with new token
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Process queued requests with error
        processQueue(refreshError, null)

        // Handle session expiration
        await handleSessionExpired()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
