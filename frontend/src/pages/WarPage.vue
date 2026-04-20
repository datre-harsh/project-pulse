<template>
  <div>
    <h2 class="text-h5 mb-4">Weekly Activity Reports</h2>
    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="2"><v-select v-model="form.sectionId" :items="sections" item-title="name" item-value="id" label="Section" /></v-col>
          <v-col cols="12" md="2"><v-select v-model="form.teamId" :items="teams" item-title="name" item-value="id" label="Team" /></v-col>
          <v-col cols="12" md="2"><v-select v-model="form.studentId" :items="students" item-title="email" item-value="id" label="Student" /></v-col>
          <v-col cols="12" md="1"><v-text-field v-model.number="form.weekNumber" type="number" label="Week" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model="form.category" label="Category" /></v-col>
          <v-col cols="12" md="3"><v-text-field v-model="form.plannedActivity" label="Planned Activity" /></v-col>
          <v-col cols="12" md="4"><v-text-field v-model="form.description" label="Description" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.plannedHours" type="number" label="Planned Hrs" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.actualHours" type="number" label="Actual Hrs" /></v-col>
          <v-col cols="12" md="2"><v-select v-model="form.status" :items="statuses" label="Status" /></v-col>
          <v-col cols="12" md="2" class="d-flex align-end"><v-btn color="primary" @click="createWar">Add</v-btn></v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="rows"></v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const rows = ref([])
const sections = ref([])
const teams = ref([])
const students = ref([])
const statuses = ['NOT_STARTED', 'IN_PROGRESS', 'DONE']

const form = ref({
  sectionId: null,
  teamId: null,
  studentId: null,
  weekNumber: 1,
  category: '',
  plannedActivity: '',
  description: '',
  plannedHours: 0,
  actualHours: 0,
  status: 'IN_PROGRESS'
})

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Week', key: 'weekNumber' },
  { title: 'Student', key: 'studentId' },
  { title: 'Category', key: 'category' },
  { title: 'Planned', key: 'plannedHours' },
  { title: 'Actual', key: 'actualHours' },
  { title: 'Status', key: 'status' }
]

const load = async () => {
  const [warRes, sectionRes, teamRes, studentRes] = await Promise.all([
    api.get('/war'),
    api.get('/sections'),
    api.get('/teams'),
    api.get('/users?role=STUDENT')
  ])
  rows.value = warRes.data
  sections.value = sectionRes.data
  teams.value = teamRes.data
  students.value = studentRes.data
}

const createWar = async () => {
  await api.post('/war', form.value)
  await load()
}

onMounted(load)
</script>
