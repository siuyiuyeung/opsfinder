<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { excelService } from '@/services/excel.service'
import { useAuthStore } from '@/stores/auth'
import type { ExcelFile, ExcelRowSearchResult, ExcelFileDetail } from '@/types/excel'

interface GroupColumn {
  columnIndex: number
  columnHeader: string
}

interface ResultGroup {
  key: string
  fileName: string
  sheetName: string
  rows: ExcelRowSearchResult[]
  columns: GroupColumn[]
}

const authStore = useAuthStore()

// State
const loading = ref(false)
const searchLoading = ref(false)
const uploadLoading = ref(false)
const files = ref<ExcelFile[]>([])
const searchResults = ref<ExcelRowSearchResult[]>([])
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(20)

// File upload
const showUploadDialog = ref(false)
const selectedFile = ref<File | null>(null)
const uploadError = ref('')

// Search
const searchKeywords = ref('')
const selectedFileFilter = ref<number[]>([])
const selectedSheetFilter = ref<string[]>([])
const showSearchResults = ref(false)
const showAllColumns = ref(false)
const availableSheets = ref<string[]>([])

// File details
const showDetailsDialog = ref(false)
const selectedFileDetails = ref<ExcelFileDetail | null>(null)
const detailsLoading = ref(false)

// File input ref
const fileInput = ref<HTMLInputElement | null>(null)

// Computed
const canUpload = computed(() => authStore.isAdmin || authStore.isOperator)
const canDelete = computed(() => (file: ExcelFile) => {
  return authStore.isAdmin || (authStore.isOperator && file.uploadedBy === authStore.user?.username)
})

const headers = [
  { title: 'Filename', key: 'originalFilename', sortable: false },
  { title: 'Size', key: 'fileSize', sortable: false },
  { title: 'Sheets', key: 'sheetCount', sortable: false },
  { title: 'Rows', key: 'rowCount', sortable: false },
  { title: 'Cells', key: 'cellCount', sortable: false },
  { title: 'Uploaded By', key: 'uploadedBy', sortable: false },
  { title: 'Uploaded At', key: 'uploadedAt', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false },
]

/**
 * Group search results by file + sheet. Each group owns its own column set,
 * since different sheets have different headers.
 */
const groupedResults = computed<ResultGroup[]>(() => {
  const groups = new Map<string, ResultGroup>()

  for (const row of searchResults.value) {
    const key = `${row.fileId}_${row.sheetId}`
    let group = groups.get(key)
    if (!group) {
      group = {
        key,
        fileName: row.fileName,
        sheetName: row.sheetName,
        rows: [],
        columns: [],
      }
      groups.set(key, group)
    }
    group.rows.push(row)
  }

  // Build each group's column set from the union of its rows' cells
  for (const group of groups.values()) {
    const columns = new Map<number, string>()
    for (const row of group.rows) {
      for (const cell of row.rowData ?? []) {
        if (!columns.has(cell.columnIndex)) {
          columns.set(cell.columnIndex, cell.columnHeader)
        }
      }
    }
    group.columns = Array.from(columns.entries())
      .map(([columnIndex, columnHeader]) => ({ columnIndex, columnHeader }))
      .sort((a, b) => a.columnIndex - b.columnIndex)
  }

  return Array.from(groups.values())
})

// Methods
const loadFiles = async () => {
  loading.value = true
  try {
    const response = await excelService.getExcelFiles({}, currentPage.value, pageSize.value)
    files.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (error: any) {
    console.error('Failed to load Excel files:', error)
  } finally {
    loading.value = false
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0]
    uploadError.value = ''
  }
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    uploadError.value = 'Please select a file'
    return
  }

  if (!selectedFile.value.name.endsWith('.xlsx')) {
    uploadError.value = 'File must be an Excel file (.xlsx)'
    return
  }

  if (selectedFile.value.size > 10 * 1024 * 1024) {
    uploadError.value = 'File size exceeds 10MB limit'
    return
  }

  uploadLoading.value = true
  uploadError.value = ''

  try {
    await excelService.uploadExcelFile(selectedFile.value)
    showUploadDialog.value = false
    selectedFile.value = null
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    await loadFiles()
  } catch (error: any) {
    uploadError.value = error.response?.data?.message || 'Failed to upload file'
  } finally {
    uploadLoading.value = false
  }
}

const handleDelete = async (fileId: number) => {
  if (!confirm('Are you sure you want to delete this file?')) {
    return
  }

  try {
    await excelService.deleteExcelFile(fileId)
    await loadFiles()
  } catch (error: any) {
    console.error('Failed to delete file:', error)
    alert(error.response?.data?.message || 'Failed to delete file')
  }
}

const handleSearch = async () => {
  if (!searchKeywords.value.trim()) {
    showSearchResults.value = false
    return
  }

  searchLoading.value = true
  try {
    const filters: { fileIds?: number[]; sheetNames?: string[] } = {}
    if (selectedFileFilter.value.length > 0) {
      filters.fileIds = selectedFileFilter.value
    }
    if (selectedSheetFilter.value.length > 0) {
      filters.sheetNames = selectedSheetFilter.value
    }
    const response = await excelService.searchExcelData(
      searchKeywords.value.trim(),
      filters,
      0,
      100
    )
    searchResults.value = response.content
    showSearchResults.value = true
  } catch (error: any) {
    console.error('Failed to search:', error)
    alert(error.response?.data?.message || 'Failed to search')
  } finally {
    searchLoading.value = false
  }
}

const clearSearch = () => {
  searchKeywords.value = ''
  selectedFileFilter.value = []
  selectedSheetFilter.value = []
  showSearchResults.value = false
  searchResults.value = []
}

const viewFileDetails = async (fileId: number) => {
  detailsLoading.value = true
  showDetailsDialog.value = true
  selectedFileDetails.value = null

  try {
    selectedFileDetails.value = await excelService.getExcelFileById(fileId)
  } catch (error: any) {
    console.error('Failed to load file details:', error)
    alert(error.response?.data?.message || 'Failed to load file details')
    showDetailsDialog.value = false
  } finally {
    detailsLoading.value = false
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleString()
}

const truncate = (value: string, max = 50): string => {
  if (!value) return ''
  return value.length > max ? value.substring(0, max) + '...' : value
}

/**
 * Look up a row's cell by column index. Returns undefined when the row has no
 * cell at that position (sparse rows within a group).
 */
const cellAt = (row: ExcelRowSearchResult, columnIndex: number) => {
  return row.rowData?.find(cell => cell.columnIndex === columnIndex)
}

const handlePageChange = (page: number) => {
  currentPage.value = page - 1
  loadFiles()
}

const loadSheetNames = async (fileIds: number[]) => {
  if (fileIds.length === 0) {
    availableSheets.value = []
    return
  }

  try {
    // Load sheets from all selected files and combine them (deduplicated)
    const allSheets = new Set<string>()
    for (const fileId of fileIds) {
      const fileDetails = await excelService.getExcelFileById(fileId)
      fileDetails.sheets.forEach(sheet => allSheets.add(sheet.sheetName))
    }
    availableSheets.value = Array.from(allSheets).sort()
  } catch (error) {
    console.error('Failed to load sheet names:', error)
    availableSheets.value = []
  }
}

// Watchers
watch(selectedFileFilter, (newFileIds) => {
  // Keep only sheets that exist in the newly selected files
  // For simplicity, clear sheet filter when file selection changes
  selectedSheetFilter.value = []
  loadSheetNames(newFileIds)
}, { deep: true })

// Lifecycle
onMounted(() => {
  loadFiles()
})
</script>

<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card class="argon-card">
          <v-card-title class="d-flex align-center flex-wrap pa-4">
            <v-icon color="info" size="large" class="mr-2">mdi-file-excel</v-icon>
            <span style="word-break: break-word; flex: 1 1 auto; min-width: 0;">Excel Files</span>
            <div class="d-flex gap-2 flex-wrap mt-2">
              <v-btn
                v-if="canUpload"
                color="primary"
                prepend-icon="mdi-upload"
                @click="showUploadDialog = true"
              >
                Upload Excel
              </v-btn>
            </div>
          </v-card-title>

          <v-card-text class="pa-4">
            <!-- Search Section -->
            <v-row class="mb-4">
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="searchKeywords"
                  label="Search keywords (comma-separated for row-level AND)"
                  prepend-inner-icon="mdi-magnify"
                  placeholder="e.g., apple,fruit,red"
                  clearable
                  @click:clear="clearSearch"
                  @keyup.enter="handleSearch"
                  hint="All keywords must appear in the same row (across different cells)"
                  persistent-hint
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedFileFilter"
                  :items="files"
                  item-title="originalFilename"
                  item-value="id"
                  label="Filter by Files (optional)"
                  clearable
                  multiple
                  chips
                  closable-chips
                ></v-select>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedSheetFilter"
                  :items="availableSheets"
                  label="Filter by Sheets (optional)"
                  clearable
                  multiple
                  chips
                  closable-chips
                  :disabled="selectedFileFilter.length === 0"
                  hint="Select file(s) first to filter by sheet"
                  persistent-hint
                ></v-select>
              </v-col>
              <v-col cols="12" md="2" class="d-flex align-center">
                <v-btn color="primary" @click="handleSearch" :loading="searchLoading" block>
                  Search
                </v-btn>
              </v-col>
            </v-row>

            <!-- Search Results -->
            <v-card v-if="showSearchResults" class="mb-4" elevation="2">
              <v-card-title class="bg-primary text-white d-flex align-center">
                <span>Search Results ({{ searchResults.length }} found)</span>
                <v-spacer></v-spacer>
                <v-switch
                  v-model="showAllColumns"
                  label="Show all columns"
                  color="white"
                  density="compact"
                  hide-details
                  class="mr-4 flex-grow-0"
                ></v-switch>
                <v-btn icon size="small" @click="clearSearch">
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </v-card-title>
              <v-card-text>
                <div
                  v-for="group in groupedResults"
                  :key="group.key"
                  class="mb-6"
                >
                  <div class="d-flex align-center mb-2">
                    <v-icon color="info" size="small" class="mr-2">mdi-file-excel</v-icon>
                    <span class="font-weight-medium">{{ group.fileName }}</span>
                    <span class="mx-1 text-grey">/</span>
                    <span class="text-primary">{{ group.sheetName }}</span>
                    <span class="ml-2 text-caption text-grey">
                      ({{ group.rows.length }} {{ group.rows.length === 1 ? 'row' : 'rows' }})
                    </span>
                  </div>

                  <div class="result-table-wrapper">
                    <v-table density="compact">
                      <thead>
                        <tr>
                          <th class="text-left">Row</th>
                          <template v-if="showAllColumns">
                            <th
                              v-for="column in group.columns"
                              :key="column.columnIndex"
                              class="text-left"
                            >
                              {{ column.columnHeader || '(no header)' }}
                            </th>
                          </template>
                          <th v-else class="text-left">Matched Values</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="row in group.rows" :key="row.rowNumber">
                          <td class="font-weight-medium">{{ row.rowNumber }}</td>

                          <template v-if="showAllColumns">
                            <td
                              v-for="column in group.columns"
                              :key="column.columnIndex"
                              :class="{ 'bg-yellow-lighten-4': cellAt(row, column.columnIndex)?.isMatchedCell }"
                              :title="cellAt(row, column.columnIndex)?.cellValue || ''"
                            >
                              {{ truncate(cellAt(row, column.columnIndex)?.cellValue || '') || '—' }}
                            </td>
                          </template>

                          <td v-else>
                            <div class="d-flex flex-wrap gap-1">
                              <v-chip
                                v-for="(value, index) in row.matchedValues.slice(0, 3)"
                                :key="index"
                                size="small"
                                color="primary"
                                variant="tonal"
                              >
                                {{ truncate(value, 30) }}
                              </v-chip>
                              <v-chip
                                v-if="row.matchedValues.length > 3"
                                size="small"
                                color="grey"
                                variant="tonal"
                              >
                                +{{ row.matchedValues.length - 3 }} more
                              </v-chip>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </v-table>
                  </div>
                </div>

                <div v-if="groupedResults.length === 0" class="text-center pa-4 text-grey">
                  No matching rows found
                </div>

                <v-alert
                  v-else-if="showAllColumns"
                  type="info"
                  variant="tonal"
                  density="compact"
                >
                  <v-icon icon="mdi-information" size="small" class="mr-2"></v-icon>
                  Matched cells are highlighted in yellow
                </v-alert>
              </v-card-text>
            </v-card>

            <!-- Files List -->
            <v-data-table
              :headers="headers"
              :items="files"
              :loading="loading"
              :items-per-page="pageSize"
              hide-default-footer
              class="shadow rounded"
            >
              <template v-slot:item.originalFilename="{ item }">
                <span class="font-weight-medium">{{ item.originalFilename }}</span>
              </template>

              <template v-slot:item.fileSize="{ item }">
                {{ formatFileSize(item.fileSize) }}
              </template>

              <template v-slot:item.uploadedAt="{ item }">
                {{ formatDate(item.uploadedAt) }}
              </template>

              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon="mdi-eye"
                  size="small"
                  variant="text"
                  @click="viewFileDetails(item.id)"
                  title="View Details"
                ></v-btn>
                <v-btn
                  v-if="canDelete(item)"
                  icon="mdi-delete"
                  size="small"
                  variant="text"
                  color="error"
                  @click="handleDelete(item.id)"
                  title="Delete"
                ></v-btn>
              </template>

              <template v-slot:no-data>
                <div class="text-center pa-4">
                  <v-icon size="64" color="grey">mdi-file-excel-outline</v-icon>
                  <p class="text-h6 mt-2">No Excel files uploaded yet</p>
                  <p class="text-body-2 text-grey">Upload your first Excel file to get started</p>
                </div>
              </template>
            </v-data-table>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="text-center pt-4">
              <v-pagination
                v-model="currentPage"
                :length="totalPages"
                @update:model-value="handlePageChange"
              ></v-pagination>
            </div>

            <div class="text-caption text-grey mt-2">
              Total: {{ totalElements }} files
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Upload Dialog -->
    <v-dialog v-model="showUploadDialog" max-width="500px">
      <v-card>
        <v-card-title class="bg-primary text-white">
          Upload Excel File
        </v-card-title>
        <v-card-text class="pt-4">
          <v-file-input
            ref="fileInput"
            label="Select Excel file (.xlsx)"
            accept=".xlsx"
            prepend-icon="mdi-file-excel"
            @change="handleFileSelect"
            :error-messages="uploadError"
            hint="Max size: 10MB, Max cells: 100,000"
            persistent-hint
          ></v-file-input>

          <v-alert v-if="selectedFile" type="info" class="mt-4" density="compact">
            Selected: {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showUploadDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="handleUpload"
            :loading="uploadLoading"
            :disabled="!selectedFile"
          >
            Upload
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- File Details Dialog -->
    <v-dialog v-model="showDetailsDialog" max-width="800px">
      <v-card>
        <v-card-title class="bg-primary text-white">
          File Details
        </v-card-title>
        <v-card-text class="pt-4">
          <v-progress-linear v-if="detailsLoading" indeterminate></v-progress-linear>

          <div v-else-if="selectedFileDetails">
            <v-list density="compact">
              <v-list-item>
                <v-list-item-title>Filename</v-list-item-title>
                <v-list-item-subtitle>{{ selectedFileDetails.originalFilename }}</v-list-item-subtitle>
              </v-list-item>
              <v-list-item>
                <v-list-item-title>Size</v-list-item-title>
                <v-list-item-subtitle>{{ formatFileSize(selectedFileDetails.fileSize) }}</v-list-item-subtitle>
              </v-list-item>
              <v-list-item>
                <v-list-item-title>Uploaded By</v-list-item-title>
                <v-list-item-subtitle>{{ selectedFileDetails.uploadedBy }}</v-list-item-subtitle>
              </v-list-item>
              <v-list-item>
                <v-list-item-title>Uploaded At</v-list-item-title>
                <v-list-item-subtitle>{{ formatDate(selectedFileDetails.uploadedAt) }}</v-list-item-subtitle>
              </v-list-item>
              <v-list-item>
                <v-list-item-title>Statistics</v-list-item-title>
                <v-list-item-subtitle>
                  {{ selectedFileDetails.sheetCount }} sheets,
                  {{ selectedFileDetails.rowCount }} rows,
                  {{ selectedFileDetails.cellCount }} cells
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>

            <v-divider class="my-4"></v-divider>

            <h3 class="mb-2">Sheets</h3>
            <v-expansion-panels>
              <v-expansion-panel
                v-for="sheet in selectedFileDetails.sheets"
                :key="sheet.sheetId"
              >
                <v-expansion-panel-title>
                  {{ sheet.sheetName }} ({{ sheet.rowCount }} rows × {{ sheet.columnCount }} columns)
                </v-expansion-panel-title>
                <v-expansion-panel-text>
                  <h4 class="mb-2">Headers:</h4>
                  <v-chip
                    v-for="(header, index) in sheet.headers"
                    :key="index"
                    class="ma-1"
                    size="small"
                    color="primary"
                    variant="outlined"
                  >
                    {{ header }}
                  </v-chip>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showDetailsDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<style scoped>
.argon-card {
  box-shadow: 0 0 2rem 0 rgba(136, 152, 170, 0.15);
}

/* Wide sheets scroll horizontally instead of forcing the page to */
.result-table-wrapper {
  overflow-x: auto;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 4px;
}

.result-table-wrapper :deep(td),
.result-table-wrapper :deep(th) {
  white-space: nowrap;
}
</style>
