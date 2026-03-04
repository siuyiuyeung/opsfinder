/**
 * TypeScript interfaces for API Key management.
 */

export interface ApiKeyCreateRequest {
  name: string
  description?: string
  expiresAt?: string | null
  rateLimitPerHour?: number | null
}

export interface ApiKeyResponse {
  id: number
  name: string
  description: string | null
  keyPrefix: string
  userId: number
  username: string
  active: boolean
  expiresAt: string | null
  rateLimitPerHour: number
  lastUsedAt: string | null
  usageCount: number
  createdAt: string
  updatedAt: string
  createdBy: string
}

/** Returned once on creation — includes plainTextKey. */
export interface ApiKeyCreatedResponse extends ApiKeyResponse {
  plainTextKey: string
}

export interface ApiKeyUsageLogResponse {
  id: number
  apiKeyId: number
  endpoint: string
  httpMethod: string
  clientIp: string
  responseStatus: number | null
  responseTimeMs: number | null
  requestedAt: string
}

export interface ApiKeyStatsResponse {
  totalKeys: number
  activeKeys: number
  totalRequests: number
  requestsLast24h: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
