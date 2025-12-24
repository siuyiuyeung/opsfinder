import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/devices',
    name: 'Devices',
    component: () => import('@/views/DeviceListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/devices/:id',
    name: 'DeviceDetail',
    component: () => import('@/views/DeviceDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/tech-messages',
    name: 'TechMessages',
    component: () => import('@/views/TechMessageSearchView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/excel-files',
    name: 'ExcelFiles',
    component: () => import('@/views/ExcelFileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/incidents',
    name: 'Incidents',
    component: () => import('@/views/IncidentListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/incidents/create',
    name: 'IncidentCreate',
    component: () => import('@/views/IncidentCreateView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('@/views/UserManagementView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * Navigation guard to check authentication and authorization.
 */
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false
  const requiresAdmin = to.meta.requiresAdmin === true

  if (requiresAuth && !authStore.isAuthenticated) {
    // Redirect to login if not authenticated
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
    // Redirect to dashboard if already authenticated
    next({ name: 'Dashboard' })
  } else if (requiresAdmin && !authStore.isAdmin) {
    // Redirect to dashboard if not admin
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
