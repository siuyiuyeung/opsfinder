<template>
  <v-app>
    <v-main class="bg-gradient-primary">
      <v-container fluid class="fill-height pa-0">
        <v-row no-gutters class="fill-height" align="center" justify="center">
          <v-col cols="12" sm="8" md="6" lg="4" xl="3">
            <v-card class="mx-4 shadow-lg">
              <v-card-title class="text-h4 text-center py-6 text-primary d-flex align-center justify-center flex-wrap">
                <v-icon color="primary" size="x-large" class="mr-2">mdi-account-plus</v-icon>
                <span style="word-break: break-word;">Register</span>
              </v-card-title>

              <v-card-text class="pa-6">
                <v-form @submit.prevent="handleRegister" ref="formRef">
                  <v-text-field
                    v-model="username"
                    label="Username"
                    prepend-inner-icon="mdi-account"
                    :rules="[rules.required, rules.minLength]"
                    :disabled="authStore.loading || registrationSuccess"
                    autofocus
                    class="mb-2"
                  />

                  <v-text-field
                    v-model="fullName"
                    label="Full Name (Optional)"
                    prepend-inner-icon="mdi-account-circle"
                    :disabled="authStore.loading || registrationSuccess"
                    class="mb-2"
                  />

                  <v-text-field
                    v-model="password"
                    label="Password"
                    prepend-inner-icon="mdi-lock"
                    :type="showPassword ? 'text' : 'password'"
                    :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    @click:append-inner="showPassword = !showPassword"
                    :rules="[rules.required, rules.passwordLength]"
                    :disabled="authStore.loading || registrationSuccess"
                    class="mb-2"
                  />

                  <v-text-field
                    v-model="confirmPassword"
                    label="Confirm Password"
                    prepend-inner-icon="mdi-lock-check"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    :append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    @click:append-inner="showConfirmPassword = !showConfirmPassword"
                    :rules="[rules.required, rules.passwordMatch]"
                    :disabled="authStore.loading || registrationSuccess"
                    @keyup.enter="handleRegister"
                  />

                  <v-alert
                    v-if="registrationSuccess"
                    type="success"
                    variant="tonal"
                    class="mb-4"
                  >
                    Registration successful! Your account is pending approval.
                    Please wait for an administrator to activate your account.
                  </v-alert>

                  <v-alert
                    v-if="authStore.error && !registrationSuccess"
                    type="error"
                    variant="tonal"
                    closable
                    @click:close="authStore.clearError()"
                    class="mb-4"
                  >
                    {{ authStore.error }}
                  </v-alert>

                  <v-btn
                    v-if="!registrationSuccess"
                    type="submit"
                    color="primary"
                    size="large"
                    block
                    :loading="authStore.loading"
                    :disabled="!username || !password || !confirmPassword"
                    class="mt-2"
                  >
                    <v-icon left class="mr-2 text-white">mdi-account-plus</v-icon>
                    Register
                  </v-btn>

                  <v-btn
                    v-else
                    color="primary"
                    size="large"
                    block
                    @click="router.push({ name: 'Login' })"
                    class="mt-2"
                  >
                    <v-icon left class="mr-2 text-white">mdi-login</v-icon>
                    Go to Login
                  </v-btn>
                </v-form>

                <v-divider class="my-4" />

                <v-btn
                  variant="text"
                  color="primary"
                  block
                  @click="router.push({ name: 'Login' })"
                  :disabled="authStore.loading"
                >
                  Already have an account? Login
                </v-btn>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const username = ref('')
const fullName = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const registrationSuccess = ref(false)

const rules = {
  required: (value: string) => !!value || 'This field is required',
  minLength: (value: string) => value.length >= 3 || 'Username must be at least 3 characters',
  passwordLength: (value: string) => value.length >= 6 || 'Password must be at least 6 characters',
  passwordMatch: (value: string) => value === password.value || 'Passwords do not match',
}

async function handleRegister() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  const success = await authStore.register({
    username: username.value,
    password: password.value,
    fullName: fullName.value || undefined,
  })

  if (success) {
    registrationSuccess.value = true
  }
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
}

.bg-gradient-primary {
  background: linear-gradient(135deg, #1976D2 0%, #1565C0 100%);
  color: white;
}
</style>
