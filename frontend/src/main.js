import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import App from './App.vue'
import DashboardPage from './pages/DashboardPage.vue'
import SectionsPage from './pages/SectionsPage.vue'
import TeamsPage from './pages/TeamsPage.vue'
import RubricPage from './pages/RubricPage.vue'
import StudentsPage from './pages/StudentsPage.vue'
import InstructorsPage from './pages/InstructorsPage.vue'
import ProfilePage from './pages/ProfilePage.vue'
import ManageWAR from './pages/ManageWAR.vue'
import SubmitPeerEval from './pages/SubmitPeerEval.vue'
import ViewPeerEvalReport from './pages/ViewPeerEvalReport.vue'

const routes = [
  { path: '/', component: DashboardPage },
  { path: '/sections', component: SectionsPage },
  { path: '/teams', component: TeamsPage },
  { path: '/students', component: StudentsPage },
  { path: '/instructors', component: InstructorsPage },
  { path: '/rubric', component: RubricPage },
  { path: '/war', component: ManageWAR },
  { path: '/peer-evaluation', component: SubmitPeerEval },
  { path: '/peer-evaluation-report', component: ViewPeerEvalReport },
  { path: '/profile', component: ProfilePage }
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
