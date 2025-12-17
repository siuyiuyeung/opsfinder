<template>
  <v-container fluid>
    <!-- Mode Toggle -->
    <v-card class="mb-4">
      <v-tabs v-model="activeTab" bg-color="primary">
        <v-tab value="search">
          <v-icon start>mdi-magnify</v-icon>
          Quick Search
        </v-tab>
        <v-tab value="management" v-if="authStore.isAdmin">
          <v-icon start>mdi-cog</v-icon>
          Management
        </v-tab>
      </v-tabs>
    </v-card>

    <!-- Search Mode -->
    <v-card v-show="activeTab === 'search'">
      <v-card-title class="text-h5 pa-3 text-left">
        <v-icon class="mr-2" size="large">mdi-alert-circle-check</v-icon>
        Find Action for Your Issue
      </v-card-title>

      <v-card-text class="pa-3 text-left">
        <!-- Search Input -->
        <v-row class="mb-3">
          <v-col cols="12">
            <v-textarea
              v-model="searchText"
              label="Paste error message or enter keywords..."
              placeholder=""
              variant="outlined"
              rows="4"
              clearable
              autofocus
              :loading="searching"
              @input="onSearchInput"
              @keydown.enter.prevent="performSearch"
            >
              <template v-slot:prepend-inner>
                <v-icon size="large" color="primary">mdi-magnify</v-icon>
              </template>
            </v-textarea>

            <div class="d-flex align-center">
              <v-text-field
                v-model.number="occurrenceCount"
                label="How many times has this occurred? (optional)"
                type="number"
                min="1"
                variant="outlined"
                density="compact"
                style="max-width: 300px"
                class="mr-4"
                hide-details
              ></v-text-field>

              <v-btn
                color="primary"
                size="large"
                :loading="searching"
                :disabled="!searchText || searchText.trim().length < 3"
                @click="performSearch"
              >
                <v-icon start>mdi-magnify</v-icon>
                Search
              </v-btn>

              <v-spacer></v-spacer>

              <v-chip v-if="searchText && searchText.length >= 3" size="small" variant="outlined">
                {{ searchText.length }} characters
              </v-chip>
            </div>
          </v-col>
        </v-row>

        <!-- Loading State -->
        <div v-if="searching" class="py-8 text-left">
          <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
          <p class="text-h6 mt-4 text-left">Searching for matches...</p>
        </div>

        <!-- No Results -->
        <v-alert
          v-else-if="searchPerformed && searchResults.length === 0"
          type="warning"
          variant="tonal"
          prominent
          class="my-3 text-left"
        >
          <v-alert-title class="text-h6 text-left">No Matching Tech Messages Found</v-alert-title>
          <p class="mt-2 text-left">
            We couldn't find any tech messages matching your search.
          </p>
          <p class="mb-0 text-left">
            <strong>Suggestions:</strong>
          </p>
          <ul class="text-left">
            <li>Try using different keywords</li>
            <li>Use fewer keywords for broader results</li>
            <li>Contact your administrator to add a new pattern</li>
          </ul>
        </v-alert>

        <!-- Search Results -->
        <div v-else-if="searchResults.length > 0">
          <v-alert type="success" variant="tonal" class="mb-3 text-left">
            <v-alert-title class="text-h6 text-left">
              Found {{ searchResults.length }} Match{{ searchResults.length > 1 ? 'es' : '' }}
            </v-alert-title>
            <p class="mb-0 text-left">
              {{ occurrenceCount ? `Showing recommended actions for ${occurrenceCount} occurrence${occurrenceCount > 1 ? 's' : ''}` : 'Showing all action levels' }}
            </p>
          </v-alert>

          <!-- Match Cards -->
          <v-card
            v-for="(match, index) in searchResults"
            :key="index"
            class="mb-3"
            :color="getMatchCardColor(match)"
            variant="outlined"
            :border="getMatchBorderStyle(match)"
          >
            <v-card-title class="d-flex align-center pa-3 text-left">
              <v-chip :color="getSeverityColor(match.techMessage.severity)" class="mr-2">
                {{ match.techMessage.severity }}
              </v-chip>
              <span class="text-h6">{{ match.techMessage.category }}</span>
              <v-spacer></v-spacer>
              <v-chip size="small" :color="match.matchType === 'EXACT' ? 'success' : 'info'" variant="flat">
                {{ match.matchType === 'EXACT' ? '🎯 Exact Match' : '🔍 Fuzzy Match' }}
                <span class="ml-1">({{ Math.round(match.matchScore * 100) }}%)</span>
              </v-chip>
            </v-card-title>

            <v-card-text class="pa-3 text-left">
              <!-- Description -->
              <div v-if="match.techMessage.description" class="mb-3 text-body-1 text-left">
                <strong class="text-grey-darken-3">Description:</strong>
                <span class="text-grey-darken-2 ml-1">{{ match.techMessage.description }}</span>
              </div>

              <!-- Pattern (for exact matches) -->
              <div v-if="match.matchType === 'EXACT'" class="mb-3 text-body-1 text-left">
                <strong class="text-grey-darken-3">Matched Pattern:</strong>
                <code class="ml-1 pa-2 bg-grey-lighten-4 text-grey-darken-4 d-inline-block">{{ match.techMessage.pattern }}</code>
              </div>

              <!-- Extracted Variables -->
              <div v-if="match.extractedVariables && Object.keys(match.extractedVariables).length > 0" class="mb-3 text-left">
                <strong class="text-grey-darken-3">Extracted Information:</strong>
                <v-chip
                  v-for="(value, key) in match.extractedVariables"
                  :key="key"
                  size="small"
                  class="mr-2 mt-1"
                  variant="outlined"
                  color="primary"
                >
                  {{ key }}: {{ value }}
                </v-chip>
              </div>

              <v-divider class="my-3"></v-divider>

              <!-- Recommended Action -->
              <div v-if="match.recommendedAction" class="mb-3 text-left">
                <div class="d-flex align-center mb-2">
                  <v-icon color="warning" size="large" class="mr-2">mdi-star</v-icon>
                  <strong class="text-h6 text-grey-darken-3">RECOMMENDED ACTION</strong>
                  <v-chip size="small" class="ml-2" color="warning" variant="flat">
                    {{ match.recommendedAction.occurrenceMin }}{{ match.recommendedAction.occurrenceMax ? `-${match.recommendedAction.occurrenceMax}` : '+' }} occurrences
                  </v-chip>
                </div>

                <v-card color="warning-lighten-5" variant="flat" class="pa-3">
                  <div class="text-body-1 text-grey-darken-3 text-left" style="white-space: pre-line">{{ match.recommendedAction.actionText }}</div>
                  <v-btn
                    size="small"
                    variant="outlined"
                    color="warning-darken-2"
                    class="mt-2"
                    @click="copyToClipboard(match.recommendedAction.actionText)"
                  >
                    <v-icon start>mdi-content-copy</v-icon>
                    Copy Action
                  </v-btn>
                </v-card>
              </div>

              <!-- All Action Levels -->
              <div class="text-left">
                <strong class="text-subtitle-1 text-grey-darken-3">All Action Levels:</strong>
                <v-list density="compact" class="mt-2" bg-color="transparent">
                  <v-list-item
                    v-for="action in match.allActionLevels"
                    :key="action.id"
                    :class="{'bg-warning-lighten-4': match.recommendedAction && action.id === match.recommendedAction.id}"
                    class="text-left"
                  >
                    <template v-slot:prepend>
                      <v-chip size="x-small" class="mr-2" variant="flat" color="primary">
                        {{ action.occurrenceMin }}{{ action.occurrenceMax ? `-${action.occurrenceMax}` : '+' }} times
                      </v-chip>
                      <v-chip size="x-small" class="mr-2" variant="outlined" color="grey-darken-2">
                        Priority: {{ action.priority }}
                      </v-chip>
                    </template>
                    <v-list-item-title class="text-grey-darken-3 text-left">{{ action.actionText }}</v-list-item-title>
                  </v-list-item>
                </v-list>
              </div>
            </v-card-text>
          </v-card>
        </div>

        <!-- Search History -->
        <v-card v-if="searchHistory.length > 0 && !searchPerformed" variant="outlined" class="mt-6">
          <v-card-title class="text-subtitle-1 pa-3 text-left">
            <v-icon class="mr-2">mdi-history</v-icon>
            Recent Searches
          </v-card-title>
          <v-card-text class="pa-3 text-left">
            <v-chip
              v-for="(term, index) in searchHistory"
              :key="index"
              class="mr-2 mb-2"
              variant="outlined"
              closable
              @click="searchText = term; performSearch()"
              @click:close="removeFromHistory(term)"
            >
              {{ term }}
            </v-chip>
          </v-card-text>
        </v-card>
      </v-card-text>
    </v-card>

    <!-- Management Mode (Existing CRUD Interface) -->
    <div v-show="activeTab === 'management'">
      <TechMessageListView />
    </div>

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
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import TechMessageListView from './TechMessageListView.vue'

interface TechMessage {
  id: number
  category: string
  severity: string
  pattern: string
  description: string
  actionLevels: ActionLevel[]
}

interface ActionLevel {
  id: number
  occurrenceMin: number
  occurrenceMax: number | null
  actionText: string
  priority: number
}

interface SearchMatch {
  techMessage: TechMessage
  matchType: 'FUZZY' | 'EXACT'
  matchScore: number
  matchedText: string | null
  extractedVariables: Record<string, string> | null
  recommendedAction: ActionLevel | null
  allActionLevels: ActionLevel[]
}

const authStore = useAuthStore()

// Tab state
const activeTab = ref('search')

// Search state
const searchText = ref('')
const occurrenceCount = ref<number | null>(null)
const searching = ref(false)
const searchPerformed = ref(false)
const searchResults = ref<SearchMatch[]>([])
const searchHistory = ref<string[]>([])

// Debounce timer
let searchDebounceTimer: number | null = null

// Notifications
const showSuccess = ref(false)
const successMessage = ref('')
const showError = ref(false)
const errorMessage = ref('')

// Load search history from localStorage
onMounted(() => {
  const savedHistory = localStorage.getItem('techMessageSearchHistory')
  if (savedHistory) {
    try {
      searchHistory.value = JSON.parse(savedHistory)
    } catch (e) {
      console.error('Failed to parse search history', e)
    }
  }
})

// Handle search input with debouncing
function onSearchInput() {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }

  // Only auto-search if text is >= 3 characters
  if (searchText.value && searchText.value.trim().length >= 3) {
    searchDebounceTimer = window.setTimeout(() => {
      performSearch()
    }, 500) // 500ms debounce
  }
}

// Perform search
async function performSearch() {
  if (!searchText.value || searchText.value.trim().length < 3) {
    errorMessage.value = 'Please enter at least 3 characters to search'
    showError.value = true
    return
  }

  searching.value = true
  searchPerformed.value = false

  try {
    const response = await api.post('/tech-messages/search', {
      searchText: searchText.value.trim(),
      occurrenceCount: occurrenceCount.value || null,
      matchMode: 'BOTH' // Use hybrid search by default
    })

    searchResults.value = response.data.matches || []
    searchPerformed.value = true

    // Add to search history
    addToSearchHistory(searchText.value.trim())

  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Search failed. Please try again.'
    showError.value = true
    searchResults.value = []
    searchPerformed.value = true
  } finally {
    searching.value = false
  }
}

// Add search term to history
function addToSearchHistory(term: string) {
  if (!term || term.length < 3) return

  // Remove if already exists
  searchHistory.value = searchHistory.value.filter(t => t !== term)

  // Add to beginning
  searchHistory.value.unshift(term)

  // Keep only last 10 searches
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }

  // Save to localStorage
  localStorage.setItem('techMessageSearchHistory', JSON.stringify(searchHistory.value))
}

// Remove from search history
function removeFromHistory(term: string) {
  searchHistory.value = searchHistory.value.filter(t => t !== term)
  localStorage.setItem('techMessageSearchHistory', JSON.stringify(searchHistory.value))
}

// Copy text to clipboard
async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    successMessage.value = 'Action copied to clipboard!'
    showSuccess.value = true
  } catch (err) {
    errorMessage.value = 'Failed to copy to clipboard'
    showError.value = true
  }
}

// Get severity color
function getSeverityColor(severity: string): string {
  switch (severity) {
    case 'CRITICAL':
      return 'error'
    case 'HIGH':
      return 'orange'
    case 'MEDIUM':
      return 'warning'
    case 'LOW':
      return 'info'
    default:
      return 'grey'
  }
}

// Get match card color based on match type
function getMatchCardColor(match: SearchMatch): string {
  if (match.matchType === 'EXACT') {
    return 'success-lighten-5'
  }
  return 'grey-lighten-5'
}

// Get match border style
function getMatchBorderStyle(match: SearchMatch): string {
  if (match.matchType === 'EXACT') {
    return 'success md'
  }
  return 'primary sm'
}
</script>

<style scoped>
/* Ensure all text is left-aligned by default */
.v-card,
.v-card-title,
.v-card-text,
.v-list-item,
.v-alert {
  text-align: left !important;
}

code {
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
  border-radius: 4px;
}

.v-card {
  transition: transform 0.2s;
}

.v-card:hover {
  transform: translateY(-2px);
}

/* Large touch targets for mobile */
.v-btn {
  min-height: 44px;
}

/* Responsive font sizes */
@media (max-width: 600px) {
  .text-h5 {
    font-size: 1.25rem !important;
  }

  .text-h6 {
    font-size: 1.1rem !important;
  }
}
</style>
