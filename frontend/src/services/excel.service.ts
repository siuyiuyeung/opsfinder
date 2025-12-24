import api from './api'
import type { ExcelFile, ExcelFileDetail, ExcelSearchResult, ExcelStats, PageResponse } from '@/types/excel'

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
   * Search Excel data with multi-keyword AND logic.
   */
  async searchExcelData(
    keywords: string,
    filters?: { fileId?: number; sheetName?: string },
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<ExcelSearchResult>> {
    const response = await api.get<PageResponse<ExcelSearchResult>>('/excel-files/search', {
      params: { keywords, ...filters, page, size }
    })
    console.log('API Response:', response.data)
    console.log('First result from API:', response.data.content[0])
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
