<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">Dashboard</h1>
        <v-card>
          <v-card-text>
            <p>Welcome to OpsFinder, {{ authStore.user?.fullName || authStore.user?.username }}!</p>
            <p>Role: {{ authStore.userRole }}</p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>
