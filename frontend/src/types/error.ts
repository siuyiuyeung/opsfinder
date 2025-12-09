/**
 * Error message severity levels.
 */
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

/**
 * Action level interface.
 */
export interface ActionLevel {
  id: number
  occurrenceMin: number
  occurrenceMax?: number
  actionText: string
  priority: number
  createdAt: string
}

/**
 * Error message interface.
 */
export interface ErrorMessage {
  id: number
  category: string
  severity: Severity
  pattern: string
  description?: string
  createdAt: string
  updatedAt: string
  actionLevels: ActionLevel[]
}

/**
 * Error message request for create/update.
 */
export interface ErrorMessageRequest {
  category: string
  severity: Severity
  pattern: string
  description?: string
}

/**
 * Action level request for create/update.
 */
export interface ActionLevelRequest {
  occurrenceMin: number
  occurrenceMax?: number
  actionText: string
  priority: number
}

/**
 * Pattern match response.
 */
export interface PatternMatchResponse {
  matched: boolean
  errorMessage?: ErrorMessage
  matchedText?: string
  variables?: Record<string, string>
  recommendedAction?: string
  occurrenceCount?: number
}

/**
 * Pagination response wrapper.
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}
