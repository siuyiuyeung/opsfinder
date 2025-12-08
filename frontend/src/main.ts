import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import pinia from './plugins/pinia'
import router from './router'

createApp(App)
  .use(vuetify)
  .use(pinia)
  .use(router)
  .mount('#app')
