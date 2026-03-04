<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-6">
          <v-icon color="primary" size="large" class="mr-2">mdi-key-variant</v-icon>
          API Key Management
        </h1>
      </v-col>
    </v-row>

    <!-- Stats cards -->
    <v-row class="mb-4">
      <v-col cols="6" md="3">
        <v-card color="primary" variant="tonal">
          <v-card-text class="text-center">
            <div class="text-h4 font-weight-bold">{{ stats.totalKeys }}</div>
            <div class="text-caption">Total Keys</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="6" md="3">
        <v-card color="success" variant="tonal">
          <v-card-text class="text-center">
            <div class="text-h4 font-weight-bold">{{ stats.activeKeys }}</div>
            <div class="text-caption">Active Keys</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="6" md="3">
        <v-card color="info" variant="tonal">
          <v-card-text class="text-center">
            <div class="text-h4 font-weight-bold">{{ stats.totalRequests }}</div>
            <div class="text-caption">Total Requests</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="6" md="3">
        <v-card color="warning" variant="tonal">
          <v-card-text class="text-center">
            <div class="text-h4 font-weight-bold">{{ stats.requestsLast24h }}</div>
            <div class="text-caption">Requests (24h)</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- API Keys table -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center pa-4">
            <span>API Keys</span>
            <v-spacer />
            <v-btn color="primary" @click="openCreateDialog">
              <v-icon left class="mr-2">mdi-plus</v-icon>
              Create API Key
            </v-btn>
          </v-card-title>

          <v-data-table
            :headers="headers"
            :items="apiKeys"
            :loading="loading"
            :items-per-page="pageSize"
            class="elevation-1"
          >
            <template v-slot:item.active="{ item }">
              <v-chip :color="item.active ? 'success' : 'error'" size="small">
                {{ item.active ? 'Active' : 'Revoked' }}
              </v-chip>
            </template>

            <template v-slot:item.keyPrefix="{ item }">
              <code class="text-caption">{{ item.keyPrefix }}…</code>
            </template>

            <template v-slot:item.expiresAt="{ item }">
              <span v-if="item.expiresAt" :class="isExpired(item.expiresAt) ? 'text-error' : ''">
                {{ formatDate(item.expiresAt) }}
              </span>
              <span v-else class="text-medium-emphasis">Never</span>
            </template>

            <template v-slot:item.lastUsedAt="{ item }">
              <span v-if="item.lastUsedAt">{{ formatDate(item.lastUsedAt) }}</span>
              <span v-else class="text-medium-emphasis">—</span>
            </template>

            <template v-slot:item.usageCount="{ item }">
              {{ item.usageCount.toLocaleString() }}
            </template>

            <template v-slot:item.createdAt="{ item }">
              {{ formatDate(item.createdAt) }}
            </template>

            <template v-slot:item.actions="{ item }">
              <v-btn
                icon
                size="small"
                variant="text"
                title="View usage logs"
                @click="openUsageDialog(item)"
              >
                <v-icon>mdi-chart-bar</v-icon>
              </v-btn>
              <v-btn
                v-if="item.active"
                icon
                size="small"
                variant="text"
                color="warning"
                title="Revoke key"
                @click="confirmRevoke(item)"
              >
                <v-icon>mdi-cancel</v-icon>
              </v-btn>
              <v-btn
                icon
                size="small"
                variant="text"
                color="error"
                title="Delete key"
                @click="confirmDelete(item)"
              >
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <!-- ── Create API Key Dialog ── -->
    <v-dialog v-model="showCreateDialog" max-width="520">
      <v-card>
        <v-card-title>
          <v-icon left class="mr-2">mdi-plus</v-icon>
          Create API Key
        </v-card-title>

        <v-card-text>
          <v-form ref="createFormRef">
            <v-text-field
              v-model="createForm.name"
              label="Name"
              :rules="[rules.required]"
              class="mb-2"
              hint="Human-readable label (e.g. CI Pipeline Key)"
              persistent-hint
            />
            <v-textarea
              v-model="createForm.description"
              label="Description (optional)"
              rows="2"
              class="mb-2"
            />
            <v-text-field
              v-model="createForm.rateLimitPerHour"
              label="Rate Limit (requests/hour)"
              type="number"
              :rules="[rules.positiveInt]"
              class="mb-2"
              hint="Leave blank for default (1000)"
              persistent-hint
            />
            <v-text-field
              v-model="createForm.expiresAt"
              label="Expires At (optional)"
              type="datetime-local"
              class="mb-2"
              hint="Leave blank for never"
              persistent-hint
            />
          </v-form>

          <v-alert v-if="createError" type="error" variant="tonal" class="mt-4">
            {{ createError }}
          </v-alert>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showCreateDialog = false">Cancel</v-btn>
          <v-btn color="primary" @click="createApiKey" :loading="creating">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── New Key Display Dialog (one-time plaintext) ── -->
    <v-dialog v-model="showNewKeyDialog" max-width="560" persistent>
      <v-card>
        <v-card-title class="d-flex align-center">
          <v-icon color="success" class="mr-2">mdi-check-circle</v-icon>
          API Key Created
        </v-card-title>

        <v-card-text>
          <v-alert type="warning" variant="tonal" class="mb-4">
            <strong>Copy this key now.</strong> It will never be shown again.
          </v-alert>

          <v-text-field
            :model-value="newPlainTextKey"
            label="Your new API key"
            readonly
            variant="outlined"
            :append-inner-icon="keyCopied ? 'mdi-check' : 'mdi-content-copy'"
            @click:append-inner="copyKey"
          />
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn color="primary" @click="showNewKeyDialog = false">I've copied my key</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── Revoke Confirmation Dialog ── -->
    <v-dialog v-model="showRevokeDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">Revoke API Key</v-card-title>
        <v-card-text>
          Are you sure you want to revoke <strong>{{ keyToAction?.name }}</strong>?
          Any clients using this key will immediately lose access.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showRevokeDialog = false">Cancel</v-btn>
          <v-btn color="warning" @click="revokeApiKey" :loading="actioning">Revoke</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── Delete Confirmation Dialog ── -->
    <v-dialog v-model="showDeleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">Delete API Key</v-card-title>
        <v-card-text>
          Are you sure you want to permanently delete <strong>{{ keyToAction?.name }}</strong>?
          All usage logs will also be deleted. This cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showDeleteDialog = false">Cancel</v-btn>
          <v-btn color="error" @click="deleteApiKey" :loading="actioning">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── Usage Logs Dialog ── -->
    <v-dialog v-model="showUsageDialog" max-width="900">
      <v-card>
        <v-card-title>
          <v-icon class="mr-2">mdi-chart-bar</v-icon>
          Usage Logs — {{ selectedKey?.name }}
        </v-card-title>

        <v-card-text>
          <v-data-table
            :headers="usageHeaders"
            :items="usageLogs"
            :loading="usageLoading"
            :items-per-page="usagePageSize"
            class="elevation-1"
            density="compact"
          >
            <template v-slot:item.responseStatus="{ item }">
              <v-chip
                :color="statusColor(item.responseStatus)"
                size="x-small"
              >
                {{ item.responseStatus ?? '—' }}
              </v-chip>
            </template>

            <template v-slot:item.responseTimeMs="{ item }">
              {{ item.responseTimeMs != null ? item.responseTimeMs + ' ms' : '—' }}
            </template>

            <template v-slot:item.requestedAt="{ item }">
              {{ formatDate(item.requestedAt) }}
            </template>
          </v-data-table>

          <!-- Pagination -->
          <div class="d-flex justify-center mt-3" v-if="usageTotalPages > 1">
            <v-pagination
              v-model="usagePage"
              :length="usageTotalPages"
              :total-visible="7"
              @update:model-value="loadUsageLogs"
            />
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showUsageDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="showSnackbar" :color="snackbarColor" :timeout="3000">
      {{ snackbarMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { format } from 'date-fns'
import { apiKeyService } from '@/services/apikey.service'
import type { ApiKeyResponse, ApiKeyUsageLogResponse } from '@/types/apikey'

// ── State ──────────────────────────────────────────────────────────────────

const apiKeys = ref<ApiKeyResponse[]>([])
const loading = ref(false)
const pageSize = ref(20)

const stats = ref({ totalKeys: 0, activeKeys: 0, totalRequests: 0, requestsLast24h: 0 })

// Create dialog
const showCreateDialog = ref(false)
const createFormRef = ref()
const creating = ref(false)
const createError = ref('')
const createForm = ref({
  name: '',
  description: '',
  rateLimitPerHour: null as number | null,
  expiresAt: '',
})

// New key display dialog
const showNewKeyDialog = ref(false)
const newPlainTextKey = ref('')
const keyCopied = ref(false)

// Revoke / delete
const showRevokeDialog = ref(false)
const showDeleteDialog = ref(false)
const keyToAction = ref<ApiKeyResponse | null>(null)
const actioning = ref(false)

// Usage logs
const showUsageDialog = ref(false)
const selectedKey = ref<ApiKeyResponse | null>(null)
const usageLogs = ref<ApiKeyUsageLogResponse[]>([])
const usageLoading = ref(false)
const usagePage = ref(1)
const usageTotalPages = ref(1)
const usagePageSize = 50

// Snackbar
const showSnackbar = ref(false)
const snackbarMessage = ref('')
const snackbarColor = ref('success')

// ── Table headers ──────────────────────────────────────────────────────────

const headers = [
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Prefix', key: 'keyPrefix', sortable: false },
  { title: 'Owner', key: 'username', sortable: true },
  { title: 'Status', key: 'active', sortable: true },
  { title: 'Expires', key: 'expiresAt', sortable: true },
  { title: 'Rate Limit/hr', key: 'rateLimitPerHour', sortable: true },
  { title: 'Last Used', key: 'lastUsedAt', sortable: true },
  { title: 'Requests', key: 'usageCount', sortable: true },
  { title: 'Created', key: 'createdAt', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false },
]

const usageHeaders = [
  { title: 'Time', key: 'requestedAt', sortable: false },
  { title: 'Method', key: 'httpMethod', sortable: false },
  { title: 'Endpoint', key: 'endpoint', sortable: false },
  { title: 'IP', key: 'clientIp', sortable: false },
  { title: 'Status', key: 'responseStatus', sortable: false },
  { title: 'Duration', key: 'responseTimeMs', sortable: false },
]

// ── Validation rules ───────────────────────────────────────────────────────

const rules = {
  required: (v: string) => !!v || 'This field is required',
  positiveInt: (v: string | null) =>
    !v || (Number(v) > 0 && Number.isInteger(Number(v))) || 'Must be a positive integer',
}

// ── Helpers ────────────────────────────────────────────────────────────────

function formatDate(dateString: string) {
  return format(new Date(dateString), 'MMM dd, yyyy HH:mm')
}

function isExpired(expiresAt: string) {
  return new Date(expiresAt) < new Date()
}

function statusColor(status: number | null) {
  if (status == null) return 'default'
  if (status < 300) return 'success'
  if (status < 400) return 'info'
  if (status < 500) return 'warning'
  return 'error'
}

function showNotification(message: string, color = 'success') {
  snackbarMessage.value = message
  snackbarColor.value = color
  showSnackbar.value = true
}

// ── Data loading ───────────────────────────────────────────────────────────

async function loadApiKeys() {
  loading.value = true
  try {
    const page = await apiKeyService.listApiKeys(0, pageSize.value)
    apiKeys.value = page.content
  } catch {
    showNotification('Failed to load API keys', 'error')
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    stats.value = await apiKeyService.getStats()
  } catch {
    // non-critical — silently ignore
  }
}

// ── Create ─────────────────────────────────────────────────────────────────

function openCreateDialog() {
  createForm.value = { name: '', description: '', rateLimitPerHour: null, expiresAt: '' }
  createError.value = ''
  showCreateDialog.value = true
}

async function createApiKey() {
  const { valid } = await createFormRef.value.validate()
  if (!valid) return

  creating.value = true
  createError.value = ''
  try {
    const created = await apiKeyService.createApiKey({
      name: createForm.value.name,
      description: createForm.value.description || undefined,
      rateLimitPerHour: createForm.value.rateLimitPerHour ?? undefined,
      expiresAt: createForm.value.expiresAt
        ? new Date(createForm.value.expiresAt).toISOString()
        : null,
    })
    showCreateDialog.value = false
    newPlainTextKey.value = created.plainTextKey
    keyCopied.value = false
    showNewKeyDialog.value = true
    await loadApiKeys()
    await loadStats()
  } catch (err: any) {
    createError.value = err.response?.data?.message || 'Failed to create API key'
  } finally {
    creating.value = false
  }
}

async function copyKey() {
  await navigator.clipboard.writeText(newPlainTextKey.value)
  keyCopied.value = true
  showNotification('API key copied to clipboard')
}

// ── Revoke ─────────────────────────────────────────────────────────────────

function confirmRevoke(key: ApiKeyResponse) {
  keyToAction.value = key
  showRevokeDialog.value = true
}

async function revokeApiKey() {
  if (!keyToAction.value) return
  actioning.value = true
  try {
    await apiKeyService.revokeApiKey(keyToAction.value.id)
    showNotification(`Key "${keyToAction.value.name}" revoked`)
    showRevokeDialog.value = false
    await loadApiKeys()
    await loadStats()
  } catch (err: any) {
    showNotification(err.response?.data?.message || 'Failed to revoke key', 'error')
  } finally {
    actioning.value = false
  }
}

// ── Delete ─────────────────────────────────────────────────────────────────

function confirmDelete(key: ApiKeyResponse) {
  keyToAction.value = key
  showDeleteDialog.value = true
}

async function deleteApiKey() {
  if (!keyToAction.value) return
  actioning.value = true
  try {
    await apiKeyService.deleteApiKey(keyToAction.value.id)
    showNotification(`Key "${keyToAction.value.name}" deleted`)
    showDeleteDialog.value = false
    await loadApiKeys()
    await loadStats()
  } catch (err: any) {
    showNotification(err.response?.data?.message || 'Failed to delete key', 'error')
  } finally {
    actioning.value = false
  }
}

// ── Usage logs ─────────────────────────────────────────────────────────────

async function openUsageDialog(key: ApiKeyResponse) {
  selectedKey.value = key
  usagePage.value = 1
  showUsageDialog.value = true
  await loadUsageLogs()
}

async function loadUsageLogs() {
  if (!selectedKey.value) return
  usageLoading.value = true
  try {
    const result = await apiKeyService.getUsageLogs(
      selectedKey.value.id,
      usagePage.value - 1,
      usagePageSize,
    )
    usageLogs.value = result.content
    usageTotalPages.value = result.totalPages
  } catch {
    showNotification('Failed to load usage logs', 'error')
  } finally {
    usageLoading.value = false
  }
}

// ── Lifecycle ──────────────────────────────────────────────────────────────

onMounted(async () => {
  await Promise.all([loadApiKeys(), loadStats()])
})
</script>

<style scoped>
.v-data-table {
  background-color: white;
}
</style>
