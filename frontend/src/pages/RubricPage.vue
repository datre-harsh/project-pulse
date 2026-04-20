<template>
  <div>
    <h2 class="text-h5 mb-4">Rubric</h2>
    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="5"><v-text-field v-model="form.name" label="Criterion" /></v-col>
          <v-col cols="12" md="3"><v-text-field v-model.number="form.maxScore" type="number" label="Max Score" /></v-col>
          <v-col cols="12" md="2"><v-switch v-model="form.active" label="Active" /></v-col>
          <v-col cols="12" md="2" class="d-flex align-end"><v-btn color="primary" @click="createCriterion">Add</v-btn></v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="criteria"></v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const criteria = ref([])
const form = ref({ name: '', maxScore: 5, active: true })

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Name', key: 'name' },
  { title: 'Max Score', key: 'maxScore' },
  { title: 'Active', key: 'active' }
]

const load = async () => {
  const res = await api.get('/rubric')
  criteria.value = res.data
}

const createCriterion = async () => {
  await api.post('/rubric', form.value)
  await load()
}

onMounted(load)
</script>
