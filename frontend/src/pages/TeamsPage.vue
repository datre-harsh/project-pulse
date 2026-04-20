<template>
  <div>
    <h2 class="text-h5 mb-4">Teams</h2>
    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3"><v-select v-model="form.sectionId" :items="sections" item-title="name" item-value="id" label="Section" /></v-col>
          <v-col cols="12" md="3"><v-text-field v-model="form.name" label="Team Name" /></v-col>
          <v-col cols="12" md="3"><v-select v-model="form.instructorIds" :items="instructors" item-title="email" item-value="id" label="Instructors" multiple /></v-col>
          <v-col cols="12" md="3"><v-select v-model="form.studentIds" :items="students" item-title="email" item-value="id" label="Students" multiple /></v-col>
          <v-col cols="12" class="d-flex justify-end"><v-btn color="primary" @click="createTeam">Create Team</v-btn></v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="teams"></v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const teams = ref([])
const sections = ref([])
const instructors = ref([])
const students = ref([])

const form = ref({ sectionId: null, name: '', instructorIds: [], studentIds: [] })

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Section', key: 'sectionId' },
  { title: 'Name', key: 'name' },
  { title: 'Students', key: 'studentIds' },
  { title: 'Instructors', key: 'instructorIds' }
]

const load = async () => {
  const [teamRes, sectionRes, instructorRes, studentRes] = await Promise.all([
    api.get('/teams'),
    api.get('/sections'),
    api.get('/users?role=INSTRUCTOR'),
    api.get('/users?role=STUDENT')
  ])

  teams.value = teamRes.data
  sections.value = sectionRes.data
  instructors.value = instructorRes.data
  students.value = studentRes.data
}

const createTeam = async () => {
  await api.post('/teams', form.value)
  await load()
}

onMounted(load)
</script>
