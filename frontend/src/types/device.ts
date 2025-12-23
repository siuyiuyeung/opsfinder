/**
 * Device interface matching backend DeviceResponse.
 */
export interface Device {
  id: number
  zone: string
  username?: string
  type: string
  remark?: string
  location?: string
  ip?: string
  hostname?: string
  hardwareModel?: string
  datacenter?: string
  accountType?: string
  passwordIndex?: string
  createdAt: string
  updatedAt: string
}

/**
 * Device request for create/update operations.
 */
export interface DeviceRequest {
  zone: string
  username?: string
  type: string
  remark?: string
  location?: string
  ip?: string
  hostname?: string
  hardwareModel?: string
  datacenter?: string
  accountType?: string
  passwordIndex?: string
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

/**
 * Row result from CSV import operation.
 */
export interface RowResult {
  rowNumber: number
  success: boolean
  errorMessage?: string
  deviceData?: any
}

/**
 * Result from CSV import operation.
 */
export interface DeviceImportResult {
  totalRows: number
  successCount: number
  failureCount: number
  results: RowResult[]
}
