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
import RegisterInstructor from './pages/RegisterInstructor.vue'
import EvaluateStudent from './pages/EvaluateStudent.vue'
import TeamWARReport from './pages/TeamWARReport.vue'
import StudentPeerEvalReport from './pages/StudentPeerEvalReport.vue'
import LoginPage from './pages/LoginPage.vue'
import { getCurrentUser } from './api'

const routes = [
  { path: '/login', component: LoginPage, meta: { public: true } },
  { path: '/', component: DashboardPage },
  { path: '/sections', component: SectionsPage, meta: { roles: ['ADMIN'] } },
  { path: '/teams', component: TeamsPage, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/students', component: StudentsPage, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/instructors', component: InstructorsPage, meta: { roles: ['ADMIN'] } },
  { path: '/rubric', component: RubricPage, meta: { roles: ['ADMIN'] } },
  { path: '/war', component: ManageWAR, meta: { roles: ['STUDENT'] } },
  { path: '/peer-evaluation', component: SubmitPeerEval, meta: { roles: ['STUDENT'] } },
  { path: '/peer-evaluation-report', component: ViewPeerEvalReport, meta: { roles: ['STUDENT'] } },
  { path: '/register-instructor/:token', component: RegisterInstructor, meta: { public: true } },
  { path: '/evaluate-student', component: EvaluateStudent, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/team-war-report', component: TeamWARReport, meta: { roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] } },
  { path: '/student-peer-eval-report', component: StudentPeerEvalReport, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/profile', component: ProfilePage, meta: { roles: ['STUDENT'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }

  const user = getCurrentUser()
  if (!user) {
    return '/login'
  }

  const allowedRoles = to.meta.roles
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return '/'
  }

  return true
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
