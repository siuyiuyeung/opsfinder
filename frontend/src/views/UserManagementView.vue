<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-6">
          <v-icon color="primary" size="large" class="mr-2">mdi-account-group</v-icon>
          User Management
        </h1>
      </v-col>
    </v-row>

    <!-- Pending Approvals Alert -->
    <v-row v-if="pendingCount > 0">
      <v-col cols="12">
        <v-alert type="warning" variant="tonal" prominent>
          <v-row align="center">
            <v-col>
              <strong>{{ pendingCount }}</strong> user(s) pending approval
            </v-col>
            <v-col cols="auto">
              <v-btn color="warning" variant="elevated" @click="currentTab = 'pending'">
                View Pending Users
              </v-btn>
            </v-col>
          </v-row>
        </v-alert>
      </v-col>
    </v-row>

    <!-- Tabs -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-tabs v-model="currentTab" bg-color="primary" class="white-text-tabs">
            <v-tab value="all">
              <v-icon left class="mr-2 text-white">mdi-account-multiple</v-icon>
              <span class="text-white">All Users</span>
            </v-tab>
            <v-tab value="pending">
              <v-icon left class="mr-2 text-white">mdi-account-clock</v-icon>
              <span class="text-white">Pending Approval</span>
              <v-badge v-if="pendingCount > 0" :content="pendingCount" color="warning" inline class="ml-2" />
            </v-tab>
          </v-tabs>

          <v-card-text>
            <!-- All Users Tab -->
            <v-window v-model="currentTab">
              <v-window-item value="all">
                <v-row class="mb-4">
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="search"
                      prepend-inner-icon="mdi-magnify"
                      label="Search users..."
                      clearable
                      hide-details
                    />
                  </v-col>
                  <v-col cols="12" md="6" class="text-right">
                    <v-btn color="primary" @click="openCreateDialog">
                      <v-icon left class="mr-2">mdi-plus</v-icon>
                      Create User
                    </v-btn>
                  </v-col>
                </v-row>

                <v-data-table
                  :headers="headers"
                  :items="filteredUsers"
                  :loading="loading"
                  :search="search"
                  class="elevation-1"
                >
                  <template v-slot:item.active="{ item }">
                    <v-chip :color="item.active ? 'success' : 'error'" size="small">
                      {{ item.active ? 'Active' : 'Inactive' }}
                    </v-chip>
                  </template>

                  <template v-slot:item.role="{ item }">
                    <v-chip :color="getRoleColor(item.role)" size="small">
                      {{ item.role }}
                    </v-chip>
                  </template>

                  <template v-slot:item.createdAt="{ item }">
                    {{ formatDate(item.createdAt) }}
                  </template>

                  <template v-slot:item.actions="{ item }">
                    <v-btn
                      icon
                      size="small"
                      variant="text"
                      @click="openEditDialog(item)"
                      title="Edit user"
                    >
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                    <v-btn
                      icon
                      size="small"
                      variant="text"
                      color="error"
                      @click="confirmDelete(item)"
                      title="Delete user"
                      :disabled="item.id === authStore.user?.id"
                    >
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-data-table>
              </v-window-item>

              <!-- Pending Users Tab -->
              <v-window-item value="pending">
                <v-data-table
                  :headers="pendingHeaders"
                  :items="pendingUsers"
                  :loading="loading"
                  class="elevation-1"
                >
                  <template v-slot:item.createdAt="{ item }">
                    {{ formatDate(item.createdAt) }}
                  </template>

                  <template v-slot:item.actions="{ item }">
                    <v-btn
                      color="success"
                      size="small"
                      variant="elevated"
                      @click="approveUser(item)"
                      class="mr-2"
                    >
                      <v-icon left size="small">mdi-check</v-icon>
                      Approve
                    </v-btn>
                    <v-btn
                      color="error"
                      size="small"
                      variant="elevated"
                      @click="rejectUser(item)"
                    >
                      <v-icon left size="small">mdi-close</v-icon>
                      Reject
                    </v-btn>
                  </template>
                </v-data-table>
              </v-window-item>
            </v-window>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Create/Edit User Dialog -->
    <v-dialog v-model="showDialog" max-width="500">
      <v-card>
        <v-card-title>
          <v-icon left class="mr-2">{{ isEditing ? 'mdi-pencil' : 'mdi-plus' }}</v-icon>
          {{ isEditing ? 'Edit User' : 'Create User' }}
        </v-card-title>

        <v-card-text>
          <v-form ref="formRef">
            <v-text-field
              v-model="formData.username"
              label="Username"
              :rules="[rules.required]"
              :disabled="isEditing"
              class="mb-2"
            />

            <v-text-field
              v-if="!isEditing"
              v-model="formData.password"
              label="Password"
              type="password"
              :rules="[rules.required, rules.passwordLength]"
              class="mb-2"
            />

            <v-text-field
              v-model="formData.fullName"
              label="Full Name"
              class="mb-2"
            />

            <v-select
              v-model="formData.role"
              label="Role"
              :items="roles"
              :rules="[rules.required]"
              class="mb-2"
            />

            <v-switch
              v-model="formData.active"
              label="Active"
              color="primary"
            />
          </v-form>

          <v-alert v-if="dialogError" type="error" variant="tonal" class="mt-4">
            {{ dialogError }}
          </v-alert>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="closeDialog">Cancel</v-btn>
          <v-btn color="primary" @click="saveUser" :loading="saving">
            {{ isEditing ? 'Update' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="showDeleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">Confirm Delete</v-card-title>
        <v-card-text>
          Are you sure you want to delete user <strong>{{ userToDelete?.username }}</strong>?
          This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showDeleteDialog = false">Cancel</v-btn>
          <v-btn color="error" @click="deleteUser" :loading="deleting">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar for notifications -->
    <v-snackbar v-model="showSnackbar" :color="snackbarColor" :timeout="3000">
      {{ snackbarMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/user.service'
import type { UserListResponse } from '@/types/user'
import { format } from 'date-fns'

const authStore = useAuthStore()

// State
const currentTab = ref('all')
const users = ref<UserListResponse[]>([])
const pendingUsers = ref<UserListResponse[]>([])
const loading = ref(false)
const search = ref('')

// Dialog state
const showDialog = ref(false)
const isEditing = ref(false)
const formRef = ref()
const formData = ref({
  id: null as number | null,
  username: '',
  password: '',
  fullName: '',
  role: 'VIEWER',
  active: true,
})
const dialogError = ref('')
const saving = ref(false)

// Delete dialog
const showDeleteDialog = ref(false)
const userToDelete = ref<UserListResponse | null>(null)
const deleting = ref(false)

// Snackbar
const showSnackbar = ref(false)
const snackbarMessage = ref('')
const snackbarColor = ref('success')

// Table headers
const headers = [
  { title: 'Username', key: 'username', sortable: true },
  { title: 'Full Name', key: 'fullName', sortable: true },
  { title: 'Role', key: 'role', sortable: true },
  { title: 'Status', key: 'active', sortable: true },
  { title: 'Created', key: 'createdAt', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false },
]

const pendingHeaders = [
  { title: 'Username', key: 'username', sortable: true },
  { title: 'Full Name', key: 'fullName', sortable: true },
  { title: 'Registered', key: 'createdAt', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false },
]

const roles = ['ADMIN', 'OPERATOR', 'VIEWER']

const rules = {
  required: (value: string) => !!value || 'This field is required',
  passwordLength: (value: string) => value.length >= 6 || 'Password must be at least 6 characters',
}

// Computed
const filteredUsers = computed(() => users.value)
const pendingCount = computed(() => pendingUsers.value.length)

// Methods
function getRoleColor(role: string) {
  switch (role) {
    case 'ADMIN': return 'error'
    case 'OPERATOR': return 'warning'
    case 'VIEWER': return 'info'
    default: return 'default'
  }
}

function formatDate(dateString: string) {
  return format(new Date(dateString), 'MMM dd, yyyy HH:mm')
}

async function loadUsers() {
  loading.value = true
  try {
    users.value = await userService.getAllUsers()
  } catch (error: any) {
    showNotification('Failed to load users', 'error')
  } finally {
    loading.value = false
  }
}

async function loadPendingUsers() {
  loading.value = true
  try {
    pendingUsers.value = await userService.getPendingUsers()
  } catch (error: any) {
    showNotification('Failed to load pending users', 'error')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEditing.value = false
  formData.value = {
    id: null,
    username: '',
    password: '',
    fullName: '',
    role: 'VIEWER',
    active: true,
  }
  dialogError.value = ''
  showDialog.value = true
}

function openEditDialog(user: UserListResponse) {
  isEditing.value = true
  formData.value = {
    id: user.id,
    username: user.username,
    password: '',
    fullName: user.fullName || '',
    role: user.role,
    active: user.active,
  }
  dialogError.value = ''
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
  formData.value = {
    id: null,
    username: '',
    password: '',
    fullName: '',
    role: 'VIEWER',
    active: true,
  }
  dialogError.value = ''
}

async function saveUser() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  dialogError.value = ''

  try {
    if (isEditing.value && formData.value.id) {
      await userService.updateUser(formData.value.id, {
        fullName: formData.value.fullName || undefined,
        role: formData.value.role,
        active: formData.value.active,
      })
      showNotification('User updated successfully', 'success')
    } else {
      await userService.createUser({
        username: formData.value.username,
        password: formData.value.password,
        fullName: formData.value.fullName || undefined,
        role: formData.value.role,
        active: formData.value.active,
      })
      showNotification('User created successfully', 'success')
    }
    closeDialog()
    await loadUsers()
    await loadPendingUsers()
  } catch (error: any) {
    if (error.response?.status === 409) {
      dialogError.value = 'Username already exists'
    } else {
      dialogError.value = error.response?.data?.message || 'Failed to save user'
    }
  } finally {
    saving.value = false
  }
}

function confirmDelete(user: UserListResponse) {
  userToDelete.value = user
  showDeleteDialog.value = true
}

async function deleteUser() {
  if (!userToDelete.value) return

  deleting.value = true
  try {
    await userService.deleteUser(userToDelete.value.id)
    showNotification('User deleted successfully', 'success')
    showDeleteDialog.value = false
    await loadUsers()
    await loadPendingUsers()
  } catch (error: any) {
    showNotification(error.response?.data?.message || 'Failed to delete user', 'error')
  } finally {
    deleting.value = false
  }
}

async function approveUser(user: UserListResponse) {
  try {
    await userService.approveUser(user.id)
    showNotification(`User ${user.username} approved successfully`, 'success')
    await loadUsers()
    await loadPendingUsers()
  } catch (error: any) {
    showNotification(error.response?.data?.message || 'Failed to approve user', 'error')
  }
}

async function rejectUser(user: UserListResponse) {
  if (!confirm(`Are you sure you want to reject user ${user.username}? This will delete their account.`)) {
    return
  }

  try {
    await userService.rejectUser(user.id)
    showNotification(`User ${user.username} rejected and deleted`, 'success')
    await loadUsers()
    await loadPendingUsers()
  } catch (error: any) {
    showNotification(error.response?.data?.message || 'Failed to reject user', 'error')
  }
}

function showNotification(message: string, color: string) {
  snackbarMessage.value = message
  snackbarColor.value = color
  showSnackbar.value = true
}

// Lifecycle
onMounted(async () => {
  await loadUsers()
  await loadPendingUsers()
})
</script>

<style scoped>
.v-data-table {
  background-color: white;
}

.white-text-tabs :deep(.v-tab) {
  color: white !important;
}

.white-text-tabs :deep(.v-tab--selected) {
  color: white !important;
}
</style>
