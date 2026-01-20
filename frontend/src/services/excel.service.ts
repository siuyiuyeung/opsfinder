import api from './api'
import type { ExcelFile, ExcelFileDetail, ExcelRowSearchResult, ExcelStats, PageResponse } from '@/types/excel'

/**
 * Excel file service for API calls.
 */
export const excelService = {
  /**
   * Upload an Excel file.
   */
  async uploadExcelFile(file: File): Promise<ExcelFile> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await api.post<ExcelFile>('/excel-files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Get all Excel files with optional filters.
   */
  async getExcelFiles(
    filters?: { uploadedBy?: string },
    page: number = 0,
    size: number = 20,
    sort: string = 'uploadedAt,desc'
  ): Promise<PageResponse<ExcelFile>> {
    const response = await api.get<PageResponse<ExcelFile>>('/excel-files', {
      params: { ...filters, page, size, sort }
    })
    return response.data
  },

  /**
   * Get Excel file by ID with full details (sheets and headers).
   */
  async getExcelFileById(id: number): Promise<ExcelFileDetail> {
    const response = await api.get<ExcelFileDetail>(`/excel-files/${id}`)
    return response.data
  },

  /**
   * Delete an Excel file.
   */
  async deleteExcelFile(id: number): Promise<void> {
    await api.delete(`/excel-files/${id}`)
  },

  /**
   * Search Excel data with multi-keyword AND logic (row-grouped results).
   * Supports filtering by multiple file IDs and/or sheet names.
   */
  async searchExcelData(
    keywords: string,
    filters?: { fileIds?: number[]; sheetNames?: string[] },
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<ExcelRowSearchResult>> {
    // Build params object, converting arrays to comma-separated strings for query params
    const params: Record<string, any> = { keywords, page, size }
    if (filters?.fileIds && filters.fileIds.length > 0) {
      params.fileIds = filters.fileIds.join(',')
    }
    if (filters?.sheetNames && filters.sheetNames.length > 0) {
      params.sheetNames = filters.sheetNames.join(',')
    }
    const response = await api.get<PageResponse<ExcelRowSearchResult>>('/excel-files/search', {
      params
    })
    return response.data
  },

  /**
   * Get Excel file statistics.
   */
  async getStatistics(): Promise<ExcelStats> {
    const response = await api.get<ExcelStats>('/excel-files/stats')
    return response.data
  },
}
