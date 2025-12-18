import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

/**
 * Vuetify plugin configuration with Argon Design System theme.
 * Color palette adapted from Creative Tim's Argon Design System.
 */
export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#5e72e4',      // Argon primary (vibrant indigo-blue)
          secondary: '#f4f5f7',    // Argon secondary (light gray)
          accent: '#11cdef',       // Argon info/teal
          error: '#f5365c',        // Argon danger (red)
          info: '#11cdef',         // Argon info (cyan)
          success: '#2dce89',      // Argon success (bright green)
          warning: '#fb6340',      // Argon warning (orange)
          background: '#f6f9fc',   // Argon gray-100
          surface: '#ffffff',      // White
          'on-primary': '#ffffff',
          'on-secondary': '#525f7f',
          'on-background': '#525f7f',
          'on-surface': '#525f7f',
        },
      },
      dark: {
        colors: {
          primary: '#5e72e4',
          secondary: '#32325d',
          accent: '#11cdef',
          error: '#f5365c',
          info: '#11cdef',
          success: '#2dce89',
          warning: '#fb6340',
          background: '#172b4d',
          surface: '#1a1a1a',
          'on-primary': '#ffffff',
          'on-secondary': '#ffffff',
        },
      },
    },
  },
  defaults: {
    VBtn: {
      color: 'primary',
      variant: 'elevated',
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
    },
    VSelect: {
      variant: 'outlined',
      density: 'comfortable',
    },
    VCard: {
      elevation: 2,
    },
  },
  display: {
    mobileBreakpoint: 'sm',
    thresholds: {
      xs: 0,
      sm: 640,
      md: 768,
      lg: 1024,
      xl: 1280,
    },
  },
})
