<template>
  <div>
    <h2 class="text-h5 mb-4">Sections</h2>
    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3"><v-text-field v-model="form.name" label="Section Name" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model="form.semester" label="Semester" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.year" label="Year" type="number" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.activeWeekStart" label="Start Week" type="number" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.activeWeekEnd" label="End Week" type="number" /></v-col>
          <v-col cols="12" md="1" class="d-flex align-end"><v-btn color="primary" @click="createSection">Add</v-btn></v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="sections"></v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const sections = ref([])
const form = ref({ name: '', semester: 'Spring', year: new Date().getFullYear(), activeWeekStart: 1, activeWeekEnd: 15 })
const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Name', key: 'name' },
  { title: 'Semester', key: 'semester' },
  { title: 'Year', key: 'year' },
  { title: 'Active Start', key: 'activeWeekStart' },
  { title: 'Active End', key: 'activeWeekEnd' }
]

const load = async () => {
  const res = await api.get('/sections')
  sections.value = res.data
}

const createSection = async () => {
  await api.post('/sections', form.value)
  await load()
}

onMounted(load)
</script>
