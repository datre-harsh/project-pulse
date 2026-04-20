import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import App from './App.vue'
import DashboardPage from './pages/DashboardPage.vue'
import UsersPage from './pages/UsersPage.vue'
import SectionsPage from './pages/SectionsPage.vue'
import TeamsPage from './pages/TeamsPage.vue'
import RubricPage from './pages/RubricPage.vue'
import WarPage from './pages/WarPage.vue'
import PeerEvaluationPage from './pages/PeerEvaluationPage.vue'
import ReportsPage from './pages/ReportsPage.vue'

const routes = [
  { path: '/', component: DashboardPage },
  { path: '/users', component: UsersPage },
  { path: '/sections', component: SectionsPage },
  { path: '/teams', component: TeamsPage },
  { path: '/rubric', component: RubricPage },
  { path: '/war', component: WarPage },
  { path: '/peer-evaluations', component: PeerEvaluationPage },
  { path: '/reports', component: ReportsPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const vuetify = createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#134074',
          secondary: '#0B6E4F',
          accent: '#F3A712',
          background: '#F8FAFC'
        }
      }
    }
  }
})

createApp(App).use(router).use(vuetify).mount('#app')
