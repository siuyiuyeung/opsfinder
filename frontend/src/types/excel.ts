/**
 * TypeScript types for Excel file management.
 */

export interface ExcelFile {
  id: number
  originalFilename: string
  fileSize: number
  uploadedBy: string
  uploadedAt: string
  sheetCount: number
  rowCount: number
  cellCount: number
  status: string
}

export interface SheetInfo {
  sheetId: number
  sheetName: string
  sheetIndex: number
  rowCount: number
  columnCount: number
  headers: string[]
}

export interface ExcelFileDetail extends ExcelFile {
  sheets: SheetInfo[]
}

export interface RowCellData {
  columnHeader: string
  columnIndex: number
  cellValue: string
  isMatchedCell: boolean
}

export interface ExcelSearchResult {
  cellId: number
  fileName: string
  sheetName: string
  columnHeader: string
  rowNumber: number
  columnIndex: number
  cellValue: string
  fileId: number
  sheetId: number
  rowData: RowCellData[]
}

export interface ExcelStats {
  totalFiles: number
  activeFiles: number
  totalSheets: number
  totalCells: number
  totalStorageBytes: number
}

export interface PageResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
    offset: number
    paged: boolean
    unpaged: boolean
  }
  totalPages: number
  totalElements: number
  last: boolean
  size: number
  number: number
  sort: {
    sorted: boolean
    unsorted: boolean
    empty: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}
