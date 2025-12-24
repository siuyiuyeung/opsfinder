<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { excelService } from '@/services/excel.service'
import { useAuthStore } from '@/stores/auth'
import type { ExcelFile, ExcelSearchResult, ExcelFileDetail } from '@/types/excel'

const authStore = useAuthStore()

// State
const loading = ref(false)
const searchLoading = ref(false)
const uploadLoading = ref(false)
const files = ref<ExcelFile[]>([])
const searchResults = ref<ExcelSearchResult[]>([])
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
const selectedFileFilter = ref<number | undefined>(undefined)
const selectedSheetFilter = ref<string | undefined>(undefined)
const showSearchResults = ref(false)
const expandedRows = ref<any[]>([])
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

const searchResultHeaders = [
  { title: 'File', key: 'fileName', sortable: false },
  { title: 'Sheet', key: 'sheetName', sortable: false },
  { title: 'Column', key: 'columnHeader', sortable: false },
  { title: 'Row', key: 'rowNumber', sortable: false },
  { title: 'Value', key: 'cellValue', sortable: false },
]

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
    const filters: { fileId?: number; sheetName?: string } = {}
    if (selectedFileFilter.value) {
      filters.fileId = selectedFileFilter.value
    }
    if (selectedSheetFilter.value) {
      filters.sheetName = selectedSheetFilter.value
    }
    const response = await excelService.searchExcelData(
      searchKeywords.value.trim(),
      filters,
      0,
      100
    )
    searchResults.value = response.content
    console.log('Search results:', response.content)
    console.log('First result rowData:', response.content[0]?.rowData)
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
  selectedFileFilter.value = undefined
  selectedSheetFilter.value = undefined
  showSearchResults.value = false
  searchResults.value = []
  expandedRows.value = []
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

const handlePageChange = (page: number) => {
  currentPage.value = page - 1
  loadFiles()
}

const loadSheetNames = async (fileId: number) => {
  try {
    const fileDetails = await excelService.getExcelFileById(fileId)
    availableSheets.value = fileDetails.sheets.map(sheet => sheet.sheetName)
  } catch (error) {
    console.error('Failed to load sheet names:', error)
    availableSheets.value = []
  }
}

// Watchers
watch(selectedFileFilter, (newFileId) => {
  selectedSheetFilter.value = undefined // Clear sheet filter when file changes
  if (newFileId) {
    loadSheetNames(newFileId)
  } else {
    availableSheets.value = []
  }
})

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
                  label="Search keywords (comma-separated for AND logic)"
                  prepend-inner-icon="mdi-magnify"
                  placeholder="e.g., apple,fruit,red"
                  clearable
                  @click:clear="clearSearch"
                  @keyup.enter="handleSearch"
                  hint="Enter multiple keywords separated by commas"
                  persistent-hint
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedFileFilter"
                  :items="files"
                  item-title="originalFilename"
                  item-value="id"
                  label="Filter by File (optional)"
                  clearable
                ></v-select>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedSheetFilter"
                  :items="availableSheets"
                  label="Filter by Sheet (optional)"
                  clearable
                  :disabled="!selectedFileFilter"
                  hint="Select a file first to filter by sheet"
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
              <v-card-title class="bg-primary text-white">
                Search Results ({{ searchResults.length }} found)
                <v-spacer></v-spacer>
                <v-btn icon size="small" @click="clearSearch">
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </v-card-title>
              <v-card-text>
                <v-data-table
                  :headers="searchResultHeaders"
                  :items="searchResults"
                  :items-per-page="10"
                  density="compact"
                  show-expand
                  v-model:expanded="expandedRows"
                  item-value="cellId"
                >
                  <template v-slot:item.cellValue="{ item }">
                    <span class="text-truncate" style="max-width: 300px; display: inline-block;">
                      {{ item.cellValue }}
                    </span>
                  </template>

                  <template v-slot:expanded-row="{ columns, item }">
                    <tr>
                      <td :colspan="columns.length" class="pa-4 bg-grey-lighten-5">
                        <v-card flat v-if="item.rowData && item.rowData.length > 0">
                          <v-card-title class="text-subtitle-1">
                            <v-icon class="mr-2" color="info">mdi-table-row</v-icon>
                            Full Row Data (Row {{ item.rowNumber }})
                          </v-card-title>
                          <v-card-text>
                            <v-table density="compact">
                              <thead>
                                <tr>
                                  <th class="text-left">Column</th>
                                  <th class="text-left">Header</th>
                                  <th class="text-left">Value</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr
                                  v-for="cell in item.rowData"
                                  :key="cell.columnIndex"
                                  :class="{ 'bg-yellow-lighten-4': cell.isMatchedCell }"
                                >
                                  <td class="font-weight-medium">{{ cell.columnIndex + 1 }}</td>
                                  <td class="text-primary">{{ cell.columnHeader || '(no header)' }}</td>
                                  <td>{{ cell.cellValue || '(empty)' }}</td>
                                </tr>
                              </tbody>
                            </v-table>
                            <v-alert
                              v-if="item.rowData.some(c => c.isMatchedCell)"
                              type="info"
                              variant="tonal"
                              density="compact"
                              class="mt-2"
                            >
                              <v-icon icon="mdi-information" size="small" class="mr-2"></v-icon>
                              Highlighted row indicates the matched cell
                            </v-alert>
                          </v-card-text>
                        </v-card>
                        <v-alert v-else type="warning" variant="tonal">
                          <div>Debug Info:</div>
                          <div>rowData exists: {{ !!item.rowData }}</div>
                          <div>rowData length: {{ item.rowData?.length || 0 }}</div>
                          <div>Full item: {{ JSON.stringify(item, null, 2) }}</div>
                        </v-alert>
                      </td>
                    </tr>
                  </template>
                </v-data-table>
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
</style>
