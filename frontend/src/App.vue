<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const drawer = ref(false)
const authStore = useAuthStore()
const router = useRouter()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}

const baseNavigationItems = [
  { title: 'Dashboard', icon: 'mdi-view-dashboard', to: '/' },
  { title: 'Devices', icon: 'mdi-server', to: '/devices' },
  { title: 'Tech Messages', icon: 'mdi-alert-circle', to: '/tech-messages' },
  { title: 'Incidents', icon: 'mdi-file-document-alert', to: '/incidents' },
]

const adminNavigationItems = [
  { title: 'User Management', icon: 'mdi-account-group', to: '/users', adminOnly: true },
]

const navigationItems = computed(() => {
  const items = [...baseNavigationItems]
  if (authStore.isAdmin) {
    items.push(...adminNavigationItems)
  }
  return items
})
</script>

<template>
  <v-app>
    <v-app-bar v-if="authStore.isAuthenticated" color="primary" prominent>
      <v-app-bar-nav-icon color="white" variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>OpsFinder</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="handleLogout">
        <v-icon class="text-white">mdi-logout</v-icon>
      </v-btn>
    </v-app-bar>

    <v-navigation-drawer v-if="authStore.isAuthenticated" v-model="drawer" temporary>
      <v-list density="compact" nav>
        <v-list-subheader>NAVIGATION</v-list-subheader>
        <v-list-item
          v-for="item in navigationItems"
          :key="item.title"
          :to="item.to"
          link
        >
          <template v-slot:prepend>
            <v-icon class="text-primary">{{ item.icon }}</v-icon>
          </template>
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>

        <v-divider class="my-2" />

        <v-list-item>
          <template v-slot:prepend>
            <v-icon class="text-primary">mdi-account</v-icon>
          </template>
          <v-list-item-title>{{ authStore.user?.username }}</v-list-item-title>
          <v-list-item-subtitle>{{ authStore.user?.role }}</v-list-item-subtitle>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>
