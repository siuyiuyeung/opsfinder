<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex align-center">
        <v-icon class="mr-2">mdi-alert-circle</v-icon>
        <span>Tech Message Knowledge Base</span>
        <v-spacer></v-spacer>
        <v-btn v-if="authStore.isAdmin" color="primary" prepend-icon="mdi-plus" @click="showCreateDialog = true">
          Add Tech Message Pattern
        </v-btn>
      </v-card-title>

      <v-card-text>
        <!-- Filter Section -->
        <v-row class="mb-4">
          <v-col cols="12" md="6">
            <v-select
              v-model="selectedCategory"
              :items="categories"
              label="Filter by Category"
              clearable
              @update:model-value="loadTechMessages"
            ></v-select>
          </v-col>
          <v-col cols="12" md="6">
            <v-select
              v-model="selectedSeverity"
              :items="severityOptions"
              label="Filter by Severity"
              clearable
              @update:model-value="loadTechMessages"
            ></v-select>
          </v-col>
        </v-row>

        <!-- Tech Message List -->
        <v-expansion-panels>
          <v-expansion-panel v-for="techMessage in techMessages" :key="techMessage.id">
            <v-expansion-panel-title>
              <div class="d-flex align-center w-100">
                <v-chip :color="getSeverityColor(techMessage.severity)" size="small" class="mr-2">
                  {{ techMessage.severity }}
                </v-chip>
                <span class="font-weight-bold">{{ techMessage.category }}</span>
                <v-spacer></v-spacer>
                <span class="text-caption text-grey">{{ techMessage.actionLevels.length }} action levels</span>
              </div>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="mb-3">
                <strong>Pattern:</strong>
                <code class="ml-2">{{ techMessage.pattern }}</code>
              </div>
              <div v-if="techMessage.description" class="mb-3">
                <strong>Description:</strong> {{ techMessage.description }}
              </div>
              <div v-if="techMessage.actionLevels.length > 0">
                <strong>Action Levels:</strong>
                <v-list dense>
                  <v-list-item v-for="action in techMessage.actionLevels" :key="action.id">
                    <v-list-item-title>
                      <v-chip size="x-small" class="mr-2">
                        {{ action.occurrenceMin }}{{ action.occurrenceMax ? `-${action.occurrenceMax}` : '+' }} occurrences
                      </v-chip>
                      <v-chip size="x-small" color="primary" class="mr-2">Priority: {{ action.priority }}</v-chip>
                      {{ action.actionText }}
                    </v-list-item-title>
                  </v-list-item>
                </v-list>
              </div>
              <div class="mt-3" v-if="authStore.isAdmin">
                <v-btn size="small" @click="editTechMessage(techMessage)" class="mr-2">Edit</v-btn>
                <v-btn size="small" color="error" @click="confirmDelete(techMessage)">Delete</v-btn>
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>

        <!-- Pagination -->
        <div class="text-center mt-4" v-if="totalPages > 1">
          <v-pagination v-model="page" :length="totalPages" @update:model-value="loadTechMessages"></v-pagination>
        </div>
      </v-card-text>
    </v-card>

    <!-- Create/Edit Dialog (simplified) -->
    <v-dialog v-model="showCreateDialog" max-width="600px" persistent>
      <v-card>
        <v-card-title>{{ editMode ? 'Edit' : 'Add' }} Tech Message Pattern</v-card-title>
        <v-card-text>
          <p class="text-caption">Note: Full tech message pattern management UI coming soon. Use API for now.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="closeDialog">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { TechMessage } from '@/types/tech'
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

const severityOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

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

function editTechMessage(techMessage: TechMessage) {
  console.log('Edit tech message:', techMessage)
  editMode.value = true
  showCreateDialog.value = true
}

function confirmDelete(techMessage: TechMessage) {
  if (confirm(`Delete tech message pattern "${techMessage.category}"?`)) {
    deleteTechMessage(techMessage.id)
  }
}

async function deleteTechMessage(id: number) {
  try {
    await api.delete(`/tech-messages/${id}`)
    await loadTechMessages()
  } catch (err) {
    console.error('Failed to delete tech message:', err)
  }
}

function closeDialog() {
  showCreateDialog.value = false
  editMode.value = false
}
</script>
