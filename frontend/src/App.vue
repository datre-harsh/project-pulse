<template>
  <v-app>
    <v-app-bar color="primary" density="comfortable">
      <v-app-bar-title>Project Pulse</v-app-bar-title>
      <template v-if="user">
        <div class="text-body-2 mr-3">{{ user.firstName }} {{ user.lastName }} · {{ user.role }}</div>
        <v-btn variant="text" color="white" @click="logout">Sign out</v-btn>
      </template>
    </v-app-bar>

    <v-navigation-drawer v-if="user" permanent width="260">
      <v-list nav density="comfortable">
        <v-list-item to="/" title="Home" prepend-icon="mdi-view-dashboard"></v-list-item>

        <template v-if="user.role === 'ADMIN'">
          <v-list-subheader>Setup</v-list-subheader>
          <v-list-item to="/rubric" title="Rubric" prepend-icon="mdi-clipboard-text"></v-list-item>
          <v-list-item to="/sections" title="Sections" prepend-icon="mdi-school"></v-list-item>
          <v-list-item to="/students" title="Students" prepend-icon="mdi-account-search"></v-list-item>
          <v-list-item to="/instructors" title="Instructors" prepend-icon="mdi-account-tie"></v-list-item>
          <v-list-item to="/teams" title="Teams" prepend-icon="mdi-account-multiple"></v-list-item>
        </template>

        <template v-if="user.role === 'INSTRUCTOR'">
          <v-list-subheader>Course</v-list-subheader>
          <v-list-item to="/students" title="Students" prepend-icon="mdi-account-search"></v-list-item>
          <v-list-item to="/teams" title="Teams" prepend-icon="mdi-account-multiple"></v-list-item>
        </template>

        <template v-if="user.role === 'STUDENT'">
          <v-list-subheader>Student Work</v-list-subheader>
          <v-list-item to="/war" title="Weekly Activities" prepend-icon="mdi-chart-line"></v-list-item>
          <v-list-item to="/peer-evaluation" title="Peer Evaluation" prepend-icon="mdi-account-group"></v-list-item>
          <v-list-item to="/peer-evaluation-report" title="My Report" prepend-icon="mdi-chart-box"></v-list-item>
        </template>

        <template v-if="user.role === 'ADMIN' || user.role === 'INSTRUCTOR' || user.role === 'STUDENT'">
          <v-list-subheader>{{ user.role === 'STUDENT' ? 'Team Reports' : 'Reports' }}</v-list-subheader>
          <v-list-item v-if="user.role === 'ADMIN' || user.role === 'INSTRUCTOR'" to="/evaluate-student" title="Section Evaluations" prepend-icon="mdi-account-check"></v-list-item>
          <v-list-item to="/team-war-report" title="Team WAR" prepend-icon="mdi-file-chart"></v-list-item>
          <v-list-item v-if="user.role === 'ADMIN' || user.role === 'INSTRUCTOR'" to="/student-peer-eval-report" title="Student History" prepend-icon="mdi-account-search"></v-list-item>
        </template>

        <v-divider class="my-2"></v-divider>
        <v-list-item v-if="user.role === 'STUDENT'" to="/profile" title="Profile" prepend-icon="mdi-account-circle"></v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="py-6">
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearCurrentUser, getCurrentUser } from './api'

const route = useRoute()
const router = useRouter()
const user = computed(() => {
  route.fullPath
  return getCurrentUser()
})

const logout = () => {
  clearCurrentUser()
  router.push('/login')
}
</script>
