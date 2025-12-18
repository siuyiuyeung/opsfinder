<template>
  <v-app>
    <v-main class="bg-gradient-primary">
      <v-container fluid class="fill-height pa-0">
        <v-row no-gutters class="fill-height" align="center" justify="center">
          <v-col cols="12" sm="8" md="6" lg="4" xl="3">
            <v-card class="mx-4 shadow-lg">
              <v-card-title class="text-h4 text-center py-6 text-primary d-flex align-center justify-center flex-wrap">
                <v-icon size="large" class="mr-2" color="primary">mdi-shield-lock</v-icon>
                <span style="word-break: break-word;">OpsFinder</span>
              </v-card-title>

              <v-card-text class="pa-6">
                <v-form @submit.prevent="handleLogin" ref="formRef">
                  <v-text-field
                    v-model="username"
                    label="Username"
                    prepend-inner-icon="mdi-account"
                    :rules="[rules.required]"
                    :disabled="authStore.loading"
                    autofocus
                    class="mb-2"
                  />

                  <v-text-field
                    v-model="password"
                    label="Password"
                    prepend-inner-icon="mdi-lock"
                    :type="showPassword ? 'text' : 'password'"
                    :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    @click:append-inner="showPassword = !showPassword"
                    :rules="[rules.required]"
                    :disabled="authStore.loading"
                    @keyup.enter="handleLogin"
                  />

                  <v-alert
                    v-if="authStore.error"
                    type="error"
                    variant="tonal"
                    closable
                    @click:close="authStore.clearError()"
                    class="mb-4"
                  >
                    {{ authStore.error }}
                  </v-alert>

                  <v-btn
                    type="submit"
                    color="primary"
                    size="large"
                    block
                    :loading="authStore.loading"
                    :disabled="!username || !password"
                    class="mt-2"
                  >
                    <v-icon left class="mr-2">mdi-login</v-icon>
                    Login
                  </v-btn>
                </v-form>
              </v-card-text>

              <v-card-text class="text-center text-caption text-medium-emphasis pb-6" style="word-wrap: break-word;">
                OpsFinder - Production Operations Tracker
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref()
const username = ref('')
const password = ref('')
const showPassword = ref(false)

const rules = {
  required: (value: string) => !!value || 'This field is required',
}

async function handleLogin() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  const success = await authStore.login({
    username: username.value,
    password: password.value,
  })

  if (success) {
    const redirect = route.query.redirect as string || '/'
    router.push(redirect)
  }
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
}

.bg-primary {
  background: linear-gradient(135deg, #1976D2 0%, #1565C0 100%);
  color: white;
}
</style>
