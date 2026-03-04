import api from './api'
import type {
  ApiKeyCreateRequest,
  ApiKeyResponse,
  ApiKeyCreatedResponse,
  ApiKeyUsageLogResponse,
  ApiKeyStatsResponse,
  PageResponse,
} from '@/types/apikey'

/**
 * API Key management service (admin only).
 */
export const apiKeyService = {
  /**
   * Create a new API key. Returns plainTextKey exactly once.
   */
  async createApiKey(request: ApiKeyCreateRequest): Promise<ApiKeyCreatedResponse> {
    const response = await api.post<ApiKeyCreatedResponse>('/admin/api-keys', request)
    return response.data
  },

  /**
   * List all API keys (paginated).
   */
  async listApiKeys(page = 0, size = 20): Promise<PageResponse<ApiKeyResponse>> {
    const response = await api.get<PageResponse<ApiKeyResponse>>('/admin/api-keys', {
      params: { page, size, sort: 'createdAt,desc' },
    })
    return response.data
  },

  /**
   * Get a single API key by ID.
   */
  async getApiKey(id: number): Promise<ApiKeyResponse> {
    const response = await api.get<ApiKeyResponse>(`/admin/api-keys/${id}`)
    return response.data
  },

  /**
   * Revoke an API key (sets active=false).
   */
  async revokeApiKey(id: number): Promise<ApiKeyResponse> {
    const response = await api.patch<ApiKeyResponse>(`/admin/api-keys/${id}/revoke`)
    return response.data
  },

  /**
   * Delete an API key and all its usage logs.
   */
  async deleteApiKey(id: number): Promise<void> {
    await api.delete(`/admin/api-keys/${id}`)
  },

  /**
   * Get usage logs for an API key.
   */
  async getUsageLogs(
    id: number,
    page = 0,
    size = 50,
    from?: string,
    to?: string,
  ): Promise<PageResponse<ApiKeyUsageLogResponse>> {
    const response = await api.get<PageResponse<ApiKeyUsageLogResponse>>(
      `/admin/api-keys/${id}/usage`,
      { params: { page, size, ...(from && { from }), ...(to && { to }) } },
    )
    return response.data
  },

  /**
   * Get aggregate statistics.
   */
  async getStats(): Promise<ApiKeyStatsResponse> {
    const response = await api.get<ApiKeyStatsResponse>('/admin/api-keys/stats')
    return response.data
  },
}
