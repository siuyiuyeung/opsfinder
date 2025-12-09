<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-btn prepend-icon="mdi-arrow-left" @click="goBack" class="mb-4">
          Back to Devices
        </v-btn>

        <v-card v-if="!deviceStore.loading && deviceStore.currentDevice">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-server</v-icon>
            <span>Device Details</span>
            <v-spacer></v-spacer>
            <v-btn
              v-if="authStore.isAdmin || authStore.isOperator"
              prepend-icon="mdi-pencil"
              @click="editDevice"
            >
              Edit
            </v-btn>
          </v-card-title>

          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-list>
                  <v-list-item>
                    <v-list-item-title class="text-grey">Zone</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.zone }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title class="text-grey">Type</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.type }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.username">
                    <v-list-item-title class="text-grey">Username</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.username }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.hostname">
                    <v-list-item-title class="text-grey">Hostname</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.hostname }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.ip">
                    <v-list-item-title class="text-grey">IP Address</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.ip }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.location">
                    <v-list-item-title class="text-grey">Location</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.location }}
                    </v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-col>

              <v-col cols="12" md="6">
                <v-list>
                  <v-list-item v-if="deviceStore.currentDevice.datacenter">
                    <v-list-item-title class="text-grey">Datacenter</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.datacenter }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.hardwareModel">
                    <v-list-item-title class="text-grey">Hardware Model</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.hardwareModel }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.accountType">
                    <v-list-item-title class="text-grey">Account Type</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.accountType }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item v-if="deviceStore.currentDevice.passwordIndex">
                    <v-list-item-title class="text-grey">Password Index</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ deviceStore.currentDevice.passwordIndex }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title class="text-grey">Created At</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ formatDate(deviceStore.currentDevice.createdAt) }}
                    </v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title class="text-grey">Updated At</v-list-item-title>
                    <v-list-item-subtitle class="text-h6">
                      {{ formatDate(deviceStore.currentDevice.updatedAt) }}
                    </v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-col>

              <v-col cols="12" v-if="deviceStore.currentDevice.remark">
                <v-divider class="my-4"></v-divider>
                <h3 class="text-grey mb-2">Remark</h3>
                <p>{{ deviceStore.currentDevice.remark }}</p>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-card v-else-if="deviceStore.loading" class="text-center pa-8">
          <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
          <p class="mt-4">Loading device details...</p>
        </v-card>

        <v-card v-else class="text-center pa-8">
          <v-icon size="64" color="error">mdi-alert-circle</v-icon>
          <p class="text-h6 mt-2">Device not found</p>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDeviceStore } from '@/stores/device'
import { useAuthStore } from '@/stores/auth'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()
const deviceStore = useDeviceStore()
const authStore = useAuthStore()

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    try {
      await deviceStore.fetchDeviceById(id)
    } catch (err) {
      console.error('Failed to load device:', err)
    }
  }
})

function goBack() {
  router.push('/devices')
}

function editDevice() {
  // Navigate back to list with edit mode
  router.push('/devices')
}

function formatDate(dateString: string): string {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
  } catch (err) {
    return dateString
  }
}
</script>
