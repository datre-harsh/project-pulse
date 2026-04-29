<template>
  <div>
    <div class="d-flex flex-wrap align-center justify-space-between mb-5 ga-3">
      <div>
        <h1 class="text-h4">Home</h1>
        <div class="text-body-1 text-medium-emphasis mt-1">Set up the course first, then collect student work and review reports.</div>
      </div>
      <v-btn v-if="primaryAction" color="primary" :to="primaryAction.to">{{ primaryAction.title }}</v-btn>
    </div>

    <v-row class="mb-2">
      <v-col cols="12" md="4" v-for="card in summaryCards" :key="card.title">
        <v-card>
          <v-card-text>
            <div class="text-overline text-medium-emphasis">{{ card.title }}</div>
            <div class="text-h4 mt-1">{{ card.value }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" lg="4" v-for="group in groups" :key="group.title">
        <v-card class="h-100">
          <v-card-title>{{ group.title }}</v-card-title>
          <v-list lines="two">
            <v-list-item
              v-for="item in group.items"
              :key="item.title"
              :to="item.to"
              :title="item.title"
              :subtitle="item.subtitle"
              :prepend-icon="item.icon"
            />
          </v-list>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api, { getCurrentUser } from '../api'

const summaryCards = ref([
  { title: 'Rubrics', value: '0' },
  { title: 'Sections', value: '0' },
  { title: 'Teams', value: '0' }
])

const user = computed(() => getCurrentUser())

const allGroups = [
  {
    title: 'Setup',
    roles: ['ADMIN'],
    items: [
      { title: '1. Rubric', subtitle: 'Create evaluation criteria', to: '/rubric', icon: 'mdi-clipboard-text' },
      { title: '2. Sections', subtitle: 'Create course sections and active weeks', to: '/sections', icon: 'mdi-school' },
      { title: '3. Students', subtitle: 'Invite, find, view, and delete students', to: '/students', icon: 'mdi-account-search' },
      { title: '4. Instructors', subtitle: 'Invite and manage instructors', to: '/instructors', icon: 'mdi-account-tie' },
      { title: '5. Teams', subtitle: 'Create teams and assign people', to: '/teams', icon: 'mdi-account-multiple' }
    ]
  },
  {
    title: 'Student Work',
    roles: ['STUDENT'],
    items: [
      { title: 'Weekly Activities', subtitle: 'Add and update WAR activities', to: '/war', icon: 'mdi-chart-line' },
      { title: 'Peer Evaluation', subtitle: 'Submit teammate evaluations', to: '/peer-evaluation', icon: 'mdi-account-group' },
      { title: 'My Report', subtitle: 'View personal peer feedback', to: '/peer-evaluation-report', icon: 'mdi-chart-box' }
    ]
  },
  {
    title: 'Reports',
    roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'],
    items: [
      { title: 'Section Evaluations', subtitle: 'Review peer evaluations by section', to: '/evaluate-student', icon: 'mdi-account-check', roles: ['ADMIN', 'INSTRUCTOR'] },
      { title: 'Team WAR', subtitle: 'Review weekly activity reports by team', to: '/team-war-report', icon: 'mdi-file-chart', roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] },
      { title: 'Student History', subtitle: 'Review one student over time', to: '/student-peer-eval-report', icon: 'mdi-account-search', roles: ['ADMIN', 'INSTRUCTOR'] }
    ]
  }
]

const groups = computed(() => {
  const role = user.value?.role
  return allGroups
    .filter(group => group.roles.includes(role))
    .map(group => ({
      ...group,
      items: group.items.filter(item => !item.roles || item.roles.includes(role))
    }))
    .filter(group => group.items.length)
})

const primaryAction = computed(() => {
  if (user.value?.role === 'ADMIN') return { title: 'Start Setup', to: '/rubric' }
  if (user.value?.role === 'INSTRUCTOR') return { title: 'Open Reports', to: '/evaluate-student' }
  if (user.value?.role === 'STUDENT') return { title: 'Add Weekly Activities', to: '/war' }
  return null
})

onMounted(async () => {
  const [rubrics, sections, teams] = await Promise.all([
    api.get('/rubrics'),
    api.get('/sections'),
    api.get('/teams')
  ])

  summaryCards.value = [
    { title: 'Rubrics', value: rubrics.data.length },
    { title: 'Sections', value: sections.data.length },
    { title: 'Teams', value: teams.data.length }
  ]
})
</script>
