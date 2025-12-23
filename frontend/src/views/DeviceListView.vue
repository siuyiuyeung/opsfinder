<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card class="argon-card">
          <v-card-title class="d-flex align-center flex-wrap pa-4">
            <v-icon color="info" size="large" class="mr-2">mdi-server</v-icon>
            <span style="word-break: break-word; flex: 1 1 auto; min-width: 0;">Devices</span>
            <div class="d-flex gap-2 flex-wrap mt-2">
              <v-btn
                color="success"
                prepend-icon="mdi-download"
                @click="handleExport"
                :loading="exportLoading"
              >
                Export CSV
              </v-btn>
              <v-btn
                v-if="authStore.isAdmin || authStore.isOperator"
                color="info"
                prepend-icon="mdi-upload"
                @click="showImportDialog = true"
              >
                Import CSV
              </v-btn>
              <v-btn
                v-if="authStore.isAdmin || authStore.isOperator"
                color="primary"
                prepend-icon="mdi-plus"
                @click="showCreateDialog = true"
              >
                Add Device
              </v-btn>
            </div>
          </v-card-title>

          <v-card-text class="pa-4">
            <!-- Search and Filters -->
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="searchTerm"
                  label="Search devices"
                  prepend-inner-icon="mdi-magnify"
                  clearable
                  @click:clear="handleClearSearch"
                  @keyup.enter="handleSearch"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedZone"
                  :items="deviceStore.zones"
                  label="Filter by Zone"
                  clearable
                  @update:model-value="handleFilterChange"
                ></v-select>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedType"
                  :items="deviceStore.types"
                  label="Filter by Type"
                  clearable
                  @update:model-value="handleFilterChange"
                ></v-select>
              </v-col>
              <v-col cols="12" md="2" class="d-flex align-center">
                <v-btn color="primary" @click="handleSearch" :loading="deviceStore.loading">
                  Search
                </v-btn>
              </v-col>
            </v-row>

            <!-- Data Table -->
            <v-data-table
              :items="deviceStore.devices"
              :loading="deviceStore.loading"
              :items-per-page="deviceStore.pageSize"
              :page="deviceStore.currentPage + 1"
              hide-default-footer
              class="shadow rounded"
            >
              <template v-slot:item="{ item }">
                <tr>
                  <td style="word-wrap: break-word; overflow-wrap: break-word; max-width: 200px;">{{ item.zone }}</td>
                  <td style="word-wrap: break-word; overflow-wrap: break-word; max-width: 150px;">{{ item.type }}</td>
                  <td style="word-wrap: break-word; overflow-wrap: break-word; max-width: 200px;">{{ item.hostname || '-' }}</td>
                  <td style="word-wrap: break-word; overflow-wrap: break-word; max-width: 150px;">{{ item.ip || '-' }}</td>
                  <td style="word-wrap: break-word; overflow-wrap: break-word; max-width: 200px;">{{ item.location || '-' }}</td>
                  <td style="white-space: nowrap;">
                    <v-btn
                      icon="mdi-eye"
                      size="small"
                      variant="text"
                      @click="viewDevice(item.id)"
                    ></v-btn>
                    <v-btn
                      v-if="authStore.isAdmin || authStore.isOperator"
                      icon="mdi-pencil"
                      size="small"
                      variant="text"
                      @click="editDevice(item)"
                    ></v-btn>
                    <v-btn
                      v-if="authStore.isAdmin"
                      icon="mdi-delete"
                      size="small"
                      variant="text"
                      color="error"
                      @click="confirmDelete(item)"
                    ></v-btn>
                  </td>
                </tr>
              </template>

              <template v-slot:headers>
                <tr>
                  <th>Zone</th>
                  <th>Type</th>
                  <th>Hostname</th>
                  <th>IP</th>
                  <th>Location</th>
                  <th>Actions</th>
                </tr>
              </template>

              <template v-slot:no-data>
                <div class="text-center pa-4">
                  <v-icon size="64" class="text-secondary">mdi-server-off</v-icon>
                  <p class="text-h6 mt-2">No devices found</p>
                </div>
              </template>
            </v-data-table>

            <!-- Pagination -->
            <div class="text-center mt-4">
              <v-pagination
                v-model="page"
                :length="deviceStore.totalPages"
                :total-visible="7"
                @update:model-value="handlePageChange"
              ></v-pagination>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Create/Edit Dialog -->
    <v-dialog v-model="showCreateDialog" max-width="800px" persistent>
      <v-card>
        <v-card-title>{{ editMode ? 'Edit Device' : 'Add Device' }}</v-card-title>
        <v-card-text>
          <v-form ref="formRef" @submit.prevent="handleSubmit">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.zone" label="Zone *" :rules="[rules.required]"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.type" label="Type *" :rules="[rules.required]"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.username" label="Username"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.hostname" label="Hostname"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.ip" label="IP Address"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.location" label="Location"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.datacenter" label="Datacenter"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.hardwareModel" label="Hardware Model"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.accountType" label="Account Type"></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="formData.passwordIndex" label="Password Index"></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-textarea v-model="formData.remark" label="Remark" rows="3"></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="closeDialog">Cancel</v-btn>
          <v-btn color="primary" @click="handleSubmit" :loading="deviceStore.loading">
            {{ editMode ? 'Update' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="showDeleteDialog" max-width="400px">
      <v-card>
        <v-card-title>Confirm Delete</v-card-title>
        <v-card-text>
          Are you sure you want to delete this device?
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="showDeleteDialog = false">Cancel</v-btn>
          <v-btn color="error" @click="handleDelete" :loading="deviceStore.loading">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Import CSV Dialog -->
    <v-dialog v-model="showImportDialog" max-width="600px" persistent>
      <v-card>
        <v-card-title>Import Devices from CSV</v-card-title>
        <v-card-text>
          <v-file-input
            v-model="importFile"
            label="Select CSV file"
            accept=".csv"
            prepend-icon="mdi-file-delimited"
            show-size
            :rules="[fileValidationRule]"
            @update:model-value="importError = ''"
          ></v-file-input>
          <v-alert v-if="importError" type="error" class="mt-2">
            {{ importError }}
          </v-alert>
          <v-alert type="info" class="mt-2">
            <div class="text-body-2">
              <strong>CSV Format:</strong> zone, username, type, remark, location, ip, hostname, hardwareModel, datacenter, accountType, passwordIndex
            </div>
            <div class="text-body-2 mt-1">
              <strong>Required fields:</strong> zone, type
            </div>
            <div class="text-body-2 mt-1">
              <strong>Max file size:</strong> 10MB
            </div>
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="closeImportDialog">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="handleImport"
            :loading="importLoading"
            :disabled="!importFile"
          >
            Upload
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Import Results Dialog -->
    <v-dialog v-model="showImportResultsDialog" max-width="800px" persistent>
      <v-card>
        <v-card-title>Import Results</v-card-title>
        <v-card-text>
          <v-row class="mb-4">
            <v-col cols="4">
              <v-card color="info" variant="tonal">
                <v-card-text class="text-center">
                  <div class="text-h4">{{ importResults?.totalRows || 0 }}</div>
                  <div class="text-body-2">Total Rows</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="4">
              <v-card color="success" variant="tonal">
                <v-card-text class="text-center">
                  <div class="text-h4">{{ importResults?.successCount || 0 }}</div>
                  <div class="text-body-2">Success</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="4">
              <v-card color="error" variant="tonal">
                <v-card-text class="text-center">
                  <div class="text-h4">{{ importResults?.failureCount || 0 }}</div>
                  <div class="text-body-2">Failed</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>

          <v-data-table
            :items="importResults?.results || []"
            :items-per-page="10"
            class="elevation-1"
          >
            <template v-slot:headers>
              <tr>
                <th>Row</th>
                <th>Status</th>
                <th>Message</th>
              </tr>
            </template>
            <template v-slot:item="{ item }">
              <tr>
                <td>{{ item.rowNumber }}</td>
                <td>
                  <v-chip
                    :color="item.success ? 'success' : 'error'"
                    size="small"
                  >
                    {{ item.success ? 'Success' : 'Failed' }}
                  </v-chip>
                </td>
                <td>{{ item.errorMessage || 'Imported successfully' }}</td>
              </tr>
            </template>
          </v-data-table>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="closeImportResultsDialog">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDeviceStore } from '@/stores/device'
import { useAuthStore } from '@/stores/auth'
import { deviceService } from '@/services/device.service'
import type { Device, DeviceRequest, DeviceImportResult } from '@/types/device'

const router = useRouter()
const deviceStore = useDeviceStore()
const authStore = useAuthStore()

const searchTerm = ref('')
const selectedZone = ref<string | null>(null)
const selectedType = ref<string | null>(null)
const page = ref(1)

const showCreateDialog = ref(false)
const showDeleteDialog = ref(false)
const showImportDialog = ref(false)
const showImportResultsDialog = ref(false)
const editMode = ref(false)
const formRef = ref()
const formData = ref<DeviceRequest>({
  zone: '',
  username: '',
  type: '',
  remark: '',
  location: '',
  ip: '',
  hostname: '',
  hardwareModel: '',
  datacenter: '',
  accountType: '',
  passwordIndex: '',
})
const deviceToDelete = ref<Device | null>(null)
const deviceToEdit = ref<Device | null>(null)

// CSV Import/Export
const importFile = ref<File[] | null>(null)
const importLoading = ref(false)
const exportLoading = ref(false)
const importError = ref('')
const importResults = ref<DeviceImportResult | null>(null)

const rules = {
  required: (value: string) => !!value || 'This field is required',
}

const fileValidationRule = (files: File[] | null) => {
  if (!files || files.length === 0) return true
  const file = files[0]
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    return 'File size must be less than 10MB'
  }
  if (!file.name.toLowerCase().endsWith('.csv')) {
    return 'File must be a CSV file'
  }
  return true
}

onMounted(async () => {
  await deviceStore.loadFilterOptions()
  await deviceStore.fetchDevices()
})

function handleSearch() {
  if (searchTerm.value) {
    deviceStore.searchDevices(searchTerm.value, 0, deviceStore.pageSize)
  } else {
    handleFilterChange()
  }
  page.value = 1
}

function handleClearSearch() {
  searchTerm.value = ''
  handleFilterChange()
}

function handleFilterChange() {
  const filters: { zone?: string; type?: string } = {}
  if (selectedZone.value) filters.zone = selectedZone.value
  if (selectedType.value) filters.type = selectedType.value
  deviceStore.fetchDevices(filters, 0, deviceStore.pageSize)
  page.value = 1
}

function handlePageChange(newPage: number) {
  const filters: { zone?: string; type?: string } = {}
  if (selectedZone.value) filters.zone = selectedZone.value
  if (selectedType.value) filters.type = selectedType.value

  if (searchTerm.value) {
    deviceStore.searchDevices(searchTerm.value, newPage - 1, deviceStore.pageSize)
  } else {
    deviceStore.fetchDevices(filters, newPage - 1, deviceStore.pageSize)
  }
}

function viewDevice(id: number) {
  router.push(`/devices/${id}`)
}

function editDevice(device: Device) {
  editMode.value = true
  deviceToEdit.value = device
  formData.value = {
    zone: device.zone,
    username: device.username,
    type: device.type,
    remark: device.remark,
    location: device.location,
    ip: device.ip,
    hostname: device.hostname,
    hardwareModel: device.hardwareModel,
    datacenter: device.datacenter,
    accountType: device.accountType,
    passwordIndex: device.passwordIndex,
  }
  showCreateDialog.value = true
}

function confirmDelete(device: Device) {
  deviceToDelete.value = device
  showDeleteDialog.value = true
}

async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  try {
    if (editMode.value && deviceToEdit.value) {
      await deviceStore.updateDevice(deviceToEdit.value.id, formData.value)
    } else {
      await deviceStore.createDevice(formData.value)
    }
    closeDialog()
    // Refresh list
    handleFilterChange()
  } catch (err) {
    console.error('Form submission error:', err)
  }
}

async function handleDelete() {
  if (deviceToDelete.value) {
    try {
      await deviceStore.deleteDevice(deviceToDelete.value.id)
      showDeleteDialog.value = false
      deviceToDelete.value = null
    } catch (err) {
      console.error('Delete error:', err)
    }
  }
}

function closeDialog() {
  showCreateDialog.value = false
  editMode.value = false
  deviceToEdit.value = null
  formData.value = {
    zone: '',
    username: '',
    type: '',
    remark: '',
    location: '',
    ip: '',
    hostname: '',
    hardwareModel: '',
    datacenter: '',
    accountType: '',
    passwordIndex: '',
  }
}

// CSV Import/Export handlers
async function handleImport() {
  if (!importFile.value || importFile.value.length === 0) {
    importError.value = 'Please select a file'
    return
  }

  const file = importFile.value[0]
  const validationResult = fileValidationRule(importFile.value)
  if (validationResult !== true) {
    importError.value = validationResult
    return
  }

  try {
    importLoading.value = true
    importError.value = ''
    const result = await deviceService.importDevices(file)
    importResults.value = result
    showImportDialog.value = false
    showImportResultsDialog.value = true
    // Refresh the device list
    await handleFilterChange()
  } catch (error: any) {
    console.error('Import error:', error)
    importError.value = error.response?.data?.message || 'Failed to import devices'
  } finally {
    importLoading.value = false
  }
}

async function handleExport() {
  try {
    exportLoading.value = true
    const blob = await deviceService.exportDevices(
      selectedZone.value || undefined,
      selectedType.value || undefined
    )

    // Create download link
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // Generate filename
    let filename = 'devices'
    if (selectedZone.value) filename += `_${selectedZone.value}`
    if (selectedType.value) filename += `_${selectedType.value}`
    filename += `_${new Date().toISOString().split('T')[0]}.csv`

    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Export error:', error)
    alert('Failed to export devices. Please try again.')
  } finally {
    exportLoading.value = false
  }
}

function closeImportDialog() {
  showImportDialog.value = false
  importFile.value = null
  importError.value = ''
}

function closeImportResultsDialog() {
  showImportResultsDialog.value = false
  importResults.value = null
}
</script>
