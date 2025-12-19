<template>
  <v-container fluid>
    <v-card class="argon-card">
      <v-card-title class="d-flex align-center flex-wrap pa-4">
        <v-icon color="primary" size="large" class="mr-2">mdi-alert-circle-outline</v-icon>
        <span style="word-break: break-word; flex: 1 1 auto; min-width: 0;">Tech Message Knowledge Base</span>
        <v-btn v-if="authStore.isAdmin" color="primary" prepend-icon="mdi-plus" @click="showCreateDialog = true" class="mt-2">
          Add Pattern
        </v-btn>
      </v-card-title>

      <v-card-text class="pa-4">
        <!-- Filter Section -->
        <v-row class="mb-4">
          <v-col cols="12" md="6">
            <v-select
              v-model="selectedCategory"
              :items="categories"
              label="Category"
              clearable
              @update:model-value="loadTechMessages"
            ></v-select>
          </v-col>
          <v-col cols="12" md="6">
            <v-select
              v-model="selectedSeverity"
              :items="severityOptions"
              label="Severity"
              clearable
              @update:model-value="loadTechMessages"
            ></v-select>
          </v-col>
        </v-row>

        <!-- Tech Message List - Grouped by Category with Card Layout -->
        <div v-for="category in sortedCategories" :key="category" class="mb-6">
          <!-- Category Header -->
          <div class="d-flex align-center mb-3 pa-3 bg-grey-lighten-4 rounded">
            <v-icon color="primary" size="large" class="mr-2">mdi-folder-outline</v-icon>
            <span class="font-weight-bold text-h6 mr-2" style="word-break: break-word; flex: 1 1 auto; min-width: 0;">{{ category }}</span>
            <v-chip size="small" variant="outlined" color="primary">{{ getCategoryMessageCount(category) }} messages</v-chip>
          </div>

          <!-- Messages in Category -->
          <div v-for="severity in getActiveSeverities(category)" :key="severity">
            <v-card
              v-for="techMessage in groupedMessages[category][severity]"
              :key="techMessage.id"
              class="mb-4 shadow"
              variant="outlined"
            >
              <v-card-title class="d-flex align-center flex-wrap pa-4 text-left">
                <v-chip :color="getSeverityColor(techMessage.severity)" class="mr-2 mb-2" variant="tonal">
                  {{ techMessage.severity }}
                </v-chip>
                <span class="text-h6 text-grey-darken-4 mr-2 mb-2" style="word-break: break-word; flex: 1 1 auto; min-width: 0;">{{ techMessage.category }}</span>
                <v-chip size="small" color="info" variant="outlined" class="mr-2 mb-2">
                  {{ techMessage.actionLevels.length }} action level{{ techMessage.actionLevels.length !== 1 ? 's' : '' }}
                </v-chip>
                <v-btn
                  @click="toggleMessageDetails(techMessage.id)"
                  :icon="isMessageExpanded(techMessage.id) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                  size="small"
                  variant="text"
                  class="mb-2"
                ></v-btn>
              </v-card-title>

              <v-card-text class="pa-4 text-left">
                <!-- Pattern (always visible) -->
                <div class="mb-3 text-body-1 text-left">
                  <strong class="text-grey-darken-3">Pattern:</strong>
                  <pre class="mt-2 pa-3 bg-grey-lighten-4 rounded" style="overflow-x: auto; word-wrap: break-word; white-space: pre-wrap; max-width: 100%;"><code class="text-grey-darken-4">{{ techMessage.pattern }}</code></pre>
                </div>

                <!-- Collapsible Details -->
                <v-expand-transition>
                  <div v-show="isMessageExpanded(techMessage.id)">
                    <!-- Description -->
                    <div v-if="techMessage.description" class="mb-3 text-left">
                      <strong class="text-grey-darken-3 d-block mb-2">Description:</strong>
                      <p class="mb-0 text-grey-darken-2" style="word-wrap: break-word; overflow-wrap: break-word;">{{ techMessage.description }}</p>
                    </div>

                    <!-- Action Levels -->
                    <div v-if="techMessage.actionLevels.length > 0" class="mb-3 text-left">
                      <strong class="text-grey-darken-3 d-block mb-2">Action Levels:</strong>
                      <v-card
                        v-for="action in techMessage.actionLevels"
                        :key="action.id"
                        class="mb-3 pa-3"
                        variant="tonal"
                        color="grey-lighten-4"
                      >
                        <div class="mb-2 d-flex flex-wrap">
                          <v-chip size="small" class="mr-2 mb-1" variant="tonal" color="primary">
                            {{ action.occurrenceMin }}{{ action.occurrenceMax ? `-${action.occurrenceMax}` : '+' }} occurrences
                          </v-chip>
                          <v-chip size="small" class="mb-1" variant="outlined" color="grey-darken-2">
                            Priority: {{ action.priority }}
                          </v-chip>
                        </div>
                        <div class="text-grey-darken-4" style="word-wrap: break-word; overflow-wrap: break-word; white-space: pre-wrap;">{{ action.actionText }}</div>
                      </v-card>
                    </div>

                    <!-- Admin Actions -->
                    <div v-if="authStore.isAdmin" class="mt-4 text-left">
                      <v-btn size="small" @click="editTechMessage(techMessage)" class="mr-2">
                        <v-icon start class="text-primary">mdi-pencil</v-icon>
                        Edit
                      </v-btn>
                      <v-btn size="small" color="error" @click="confirmDelete(techMessage)">
                        <v-icon start class="text-white">mdi-delete</v-icon>
                        Delete
                      </v-btn>
                    </div>
                  </div>
                </v-expand-transition>
              </v-card-text>
            </v-card>
          </div>
        </div>

        <!-- Pagination -->
        <div class="text-center mt-4" v-if="totalPages > 1">
          <v-pagination v-model="page" :length="totalPages" @update:model-value="loadTechMessages"></v-pagination>
        </div>
      </v-card-text>
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog v-model="showCreateDialog" max-width="800px" persistent>
      <v-card>
        <v-card-title>{{ editMode ? 'Edit' : 'Create' }} Tech Message</v-card-title>

        <v-card-text>
          <v-form ref="formRef" v-model="formValid">
            <v-text-field
              v-model="formData.category"
              label="Category"
              :rules="categoryRules"
              required
              variant="outlined"
              density="comfortable"
            ></v-text-field>

            <v-select
              v-model="formData.severity"
              :items="severityOptions"
              label="Severity"
              :rules="severityRules"
              required
              variant="outlined"
              density="comfortable"
            ></v-select>

            <v-textarea
              v-model="formData.pattern"
              label="Regex Pattern"
              :rules="patternRules"
              required
              variant="outlined"
              density="comfortable"
              rows="3"
              hint="Enter a valid regex pattern"
            ></v-textarea>

            <v-textarea
              v-model="formData.description"
              label="Description (optional)"
              :rules="descriptionRules"
              variant="outlined"
              density="comfortable"
              rows="3"
            ></v-textarea>

            <v-alert v-if="patternError" type="error" density="compact" class="mb-4">
              {{ patternError }}
            </v-alert>

            <v-btn
              @click="testPattern"
              :loading="testingPattern"
              variant="outlined"
              size="small"
              class="mb-4"
            >
              Test Pattern
            </v-btn>
          </v-form>

          <!-- Action Levels Section (only in edit mode) -->
          <div v-if="editMode && currentTechMessageId">
            <v-divider class="my-4"></v-divider>
            <div class="d-flex align-center mb-2">
              <h3>Action Levels</h3>
              <v-spacer></v-spacer>
              <v-btn
                @click="showActionLevelDialog = true"
                color="primary"
                size="small"
                prepend-icon="mdi-plus"
              >
                Add Action Level
              </v-btn>
            </div>

            <v-alert v-if="!actionLevels.length" type="info" density="compact" class="mb-2">
              No action levels defined yet. Add action levels to specify recommended actions based on occurrence frequency.
            </v-alert>

            <v-list v-else density="compact">
              <v-list-item v-for="action in actionLevels" :key="action.id" class="border mb-2">
                <template v-slot:prepend>
                  <v-chip size="small" class="mr-2">
                    {{ action.occurrenceMin }}{{ action.occurrenceMax ? `-${action.occurrenceMax}` : '+' }}
                  </v-chip>
                  <v-chip size="small" color="primary" class="mr-2">
                    Priority: {{ action.priority }}
                  </v-chip>
                </template>

                <v-list-item-title>{{ action.actionText }}</v-list-item-title>

                <template v-slot:append>
                  <v-btn
                    @click="editActionLevel(action)"
                    icon="mdi-pencil"
                    size="small"
                    variant="text"
                  ></v-btn>
                  <v-btn
                    @click="confirmDeleteActionLevel(action)"
                    icon="mdi-delete"
                    size="small"
                    variant="text"
                    color="error"
                  ></v-btn>
                </template>
              </v-list-item>
            </v-list>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-btn @click="closeDialog" :disabled="saving">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            @click="saveTechMessage"
            :disabled="!formValid"
            :loading="saving"
          >
            {{ editMode ? 'Update' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Action Level Dialog -->
    <v-dialog v-model="showActionLevelDialog" max-width="600px" persistent>
      <v-card>
        <v-card-title>{{ actionLevelEditMode ? 'Edit' : 'Add' }} Action Level</v-card-title>

        <v-card-text>
          <v-form ref="actionFormRef" v-model="actionFormValid">
            <v-text-field
              v-model.number="actionFormData.occurrenceMin"
              label="Minimum Occurrences"
              type="number"
              :rules="occurrenceMinRules"
              required
              variant="outlined"
              density="comfortable"
              hint="Minimum number of times the message must occur"
            ></v-text-field>

            <v-text-field
              v-model.number="actionFormData.occurrenceMax"
              label="Maximum Occurrences (optional)"
              type="number"
              :rules="occurrenceMaxRules"
              variant="outlined"
              density="comfortable"
              hint="Leave empty for no upper limit"
            ></v-text-field>

            <v-text-field
              v-model.number="actionFormData.priority"
              label="Priority"
              type="number"
              :rules="priorityRules"
              required
              variant="outlined"
              density="comfortable"
              hint="Higher number = higher priority when multiple levels match"
            ></v-text-field>

            <v-textarea
              v-model="actionFormData.actionText"
              label="Recommended Action"
              :rules="actionTextRules"
              required
              variant="outlined"
              density="comfortable"
              rows="4"
              hint="Describe the recommended action for this occurrence range"
            ></v-textarea>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-btn @click="closeActionLevelDialog" :disabled="savingActionLevel">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            @click="saveActionLevel"
            :disabled="!actionFormValid"
            :loading="savingActionLevel"
          >
            {{ actionLevelEditMode ? 'Update' : 'Add' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success Snackbar -->
    <v-snackbar v-model="showSuccess" color="success" timeout="3000">
      {{ successMessage }}
    </v-snackbar>

    <!-- Error Snackbar -->
    <v-snackbar v-model="showError" color="error" timeout="5000">
      {{ errorMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { TechMessage, TechMessageRequest, ActionLevel, ActionLevelRequest } from '@/types/tech'
import api from '@/services/api'

const authStore = useAuthStore()

const techMessages = ref<TechMessage[]>([])
const categories = ref<string[]>([])
const selectedCategory = ref<string | null>(null)
const selectedSeverity = ref<string | null>(null)
const page = ref(1)
const totalPages = ref(0)
const showCreateDialog = ref(false)
const editMode = ref(false)
const currentTechMessageId = ref<number | null>(null)

// Expansion state for message details
const expandedMessages = ref<Set<number>>(new Set())

// Severity order (highest to lowest priority)
const severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

// Group messages by category, then by severity
const groupedMessages = computed(() => {
  const grouped: Record<string, Record<string, TechMessage[]>> = {}

  techMessages.value.forEach(msg => {
    if (!grouped[msg.category]) {
      grouped[msg.category] = {}
    }
    if (!grouped[msg.category][msg.severity]) {
      grouped[msg.category][msg.severity] = []
    }
    grouped[msg.category][msg.severity].push(msg)
  })

  return grouped
})

// Get sorted categories (alphabetically)
const sortedCategories = computed(() => {
  return Object.keys(groupedMessages.value).sort()
})

// Get count of messages in a category
function getCategoryMessageCount(category: string): number {
  let count = 0
  Object.values(groupedMessages.value[category] || {}).forEach(messages => {
    count += messages.length
  })
  return count
}

// Get active severities for a category (sorted by severity order)
function getActiveSeverities(category: string): string[] {
  const severities = Object.keys(groupedMessages.value[category] || {})
  return severityOrder.filter(sev => severities.includes(sev))
}

// Form state
const formRef = ref()
const formValid = ref(false)
const formData = reactive<TechMessageRequest>({
  category: '',
  severity: 'MEDIUM',
  pattern: '',
  description: ''
})

// Action Levels state
const actionLevels = ref<ActionLevel[]>([])
const showActionLevelDialog = ref(false)
const actionLevelEditMode = ref(false)
const currentActionLevelId = ref<number | null>(null)
const actionFormRef = ref()
const actionFormValid = ref(false)
const actionFormData = reactive<ActionLevelRequest>({
  occurrenceMin: 1,
  occurrenceMax: undefined,
  actionText: '',
  priority: 1
})

// Loading states
const saving = ref(false)
const testingPattern = ref(false)
const savingActionLevel = ref(false)

// Notification states
const showSuccess = ref(false)
const showError = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const patternError = ref('')

const severityOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

// Validation rules
const categoryRules = [
  (v: string) => !!v || 'Category is required',
  (v: string) => (v && v.length <= 100) || 'Category must be less than 100 characters'
]

const severityRules = [
  (v: string) => !!v || 'Severity is required'
]

const patternRules = [
  (v: string) => !!v || 'Pattern is required',
  (v: string) => (v && v.length <= 1000) || 'Pattern must be less than 1000 characters'
]

const descriptionRules = [
  (v: string) => !v || v.length <= 500 || 'Description must be less than 500 characters'
]

// Action Level validation rules
const occurrenceMinRules = [
  (v: number) => !!v || 'Minimum occurrence is required',
  (v: number) => v >= 1 || 'Must be at least 1'
]

const occurrenceMaxRules = [
  (v: number) => !v || v >= actionFormData.occurrenceMin || 'Maximum must be >= Minimum'
]

const priorityRules = [
  (v: number) => !!v || 'Priority is required',
  (v: number) => v >= 1 || 'Priority must be at least 1'
]

const actionTextRules = [
  (v: string) => !!v || 'Action text is required',
  (v: string) => (v && v.length <= 500) || 'Action text must be less than 500 characters'
]

onMounted(async () => {
  await loadCategories()
  await loadTechMessages()
})

async function loadCategories() {
  try {
    const response = await api.get<string[]>('/tech-messages/filters/categories')
    categories.value = response.data
  } catch (err) {
    console.error('Failed to load categories:', err)
  }
}

async function loadTechMessages() {
  try {
    const params: any = { page: page.value - 1, size: 20 }
    if (selectedCategory.value) params.category = selectedCategory.value
    if (selectedSeverity.value) params.severity = selectedSeverity.value

    const response = await api.get('/tech-messages', { params })
    techMessages.value = response.data.content
    totalPages.value = response.data.totalPages
  } catch (err) {
    console.error('Failed to load tech messages:', err)
  }
}

function getSeverityColor(severity: string) {
  const colors: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'error',
    CRITICAL: 'error'
  }
  return colors[severity] || 'grey'
}

function toggleMessageDetails(messageId: number) {
  if (expandedMessages.value.has(messageId)) {
    expandedMessages.value.delete(messageId)
  } else {
    expandedMessages.value.add(messageId)
  }
}

function isMessageExpanded(messageId: number): boolean {
  return expandedMessages.value.has(messageId)
}

async function editTechMessage(techMessage: TechMessage) {
  try {
    // Fetch full tech message details with action levels
    const response = await api.get<TechMessage>(`/tech-messages/${techMessage.id}`)
    const fullData = response.data

    // Populate form
    formData.category = fullData.category
    formData.severity = fullData.severity
    formData.pattern = fullData.pattern
    formData.description = fullData.description || ''

    // Load action levels
    actionLevels.value = fullData.actionLevels || []

    currentTechMessageId.value = techMessage.id
    editMode.value = true
    showCreateDialog.value = true
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Failed to load tech message details'
    showError.value = true
  }
}

function confirmDelete(techMessage: TechMessage) {
  if (confirm(`Delete tech message pattern "${techMessage.category}"?`)) {
    deleteTechMessage(techMessage.id)
  }
}

async function deleteTechMessage(id: number) {
  try {
    await api.delete(`/tech-messages/${id}`)
    successMessage.value = 'Tech message deleted successfully'
    showSuccess.value = true
    await loadTechMessages()
    await loadCategories()
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Failed to delete tech message'
    showError.value = true
  }
}

async function saveTechMessage() {
  if (!formRef.value?.validate()) return

  saving.value = true
  patternError.value = ''

  try {
    if (editMode.value && currentTechMessageId.value) {
      // Update existing tech message
      await api.put(`/tech-messages/${currentTechMessageId.value}`, formData)
      successMessage.value = 'Tech message updated successfully'
    } else {
      // Create new tech message
      await api.post('/tech-messages', formData)
      successMessage.value = 'Tech message created successfully'
    }

    showSuccess.value = true
    closeDialog()
    await loadTechMessages()
    await loadCategories()
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Failed to save tech message'
    showError.value = true
  } finally {
    saving.value = false
  }
}

function testPattern() {
  patternError.value = ''
  testingPattern.value = true

  try {
    // Test if pattern is valid regex
    new RegExp(formData.pattern)
    successMessage.value = 'Pattern is valid!'
    showSuccess.value = true
  } catch (err: any) {
    patternError.value = `Invalid regex: ${err.message}`
  } finally {
    testingPattern.value = false
  }
}

function closeDialog() {
  showCreateDialog.value = false
  editMode.value = false
  currentTechMessageId.value = null
  patternError.value = ''
  actionLevels.value = []

  // Reset form
  formData.category = ''
  formData.severity = 'MEDIUM'
  formData.pattern = ''
  formData.description = ''

  formRef.value?.resetValidation()
}

// Action Level Management Functions
function editActionLevel(actionLevel: ActionLevel) {
  actionFormData.occurrenceMin = actionLevel.occurrenceMin
  actionFormData.occurrenceMax = actionLevel.occurrenceMax || undefined
  actionFormData.priority = actionLevel.priority
  actionFormData.actionText = actionLevel.actionText

  currentActionLevelId.value = actionLevel.id
  actionLevelEditMode.value = true
  showActionLevelDialog.value = true
}

function confirmDeleteActionLevel(actionLevel: ActionLevel) {
  if (confirm(`Delete action level for ${actionLevel.occurrenceMin}${actionLevel.occurrenceMax ? `-${actionLevel.occurrenceMax}` : '+'} occurrences?`)) {
    deleteActionLevel(actionLevel.id)
  }
}

async function deleteActionLevel(id: number) {
  try {
    await api.delete(`/tech-messages/actions/${id}`)
    successMessage.value = 'Action level deleted successfully'
    showSuccess.value = true

    // Reload action levels
    if (currentTechMessageId.value) {
      const response = await api.get<TechMessage>(`/tech-messages/${currentTechMessageId.value}`)
      actionLevels.value = response.data.actionLevels || []
    }

    await loadTechMessages()
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Failed to delete action level'
    showError.value = true
  }
}

async function saveActionLevel() {
  if (!actionFormRef.value?.validate()) return
  if (!currentTechMessageId.value) return

  savingActionLevel.value = true

  try {
    if (actionLevelEditMode.value && currentActionLevelId.value) {
      // Update existing action level
      await api.put(`/tech-messages/actions/${currentActionLevelId.value}`, actionFormData)
      successMessage.value = 'Action level updated successfully'
    } else {
      // Create new action level
      await api.post(`/tech-messages/${currentTechMessageId.value}/actions`, actionFormData)
      successMessage.value = 'Action level added successfully'
    }

    showSuccess.value = true
    closeActionLevelDialog()

    // Reload action levels
    const response = await api.get<TechMessage>(`/tech-messages/${currentTechMessageId.value}`)
    actionLevels.value = response.data.actionLevels || []

    await loadTechMessages()
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Failed to save action level'
    showError.value = true
  } finally {
    savingActionLevel.value = false
  }
}

function closeActionLevelDialog() {
  showActionLevelDialog.value = false
  actionLevelEditMode.value = false
  currentActionLevelId.value = null

  // Reset form
  actionFormData.occurrenceMin = 1
  actionFormData.occurrenceMax = undefined
  actionFormData.priority = 1
  actionFormData.actionText = ''

  actionFormRef.value?.resetValidation()
}
</script>
