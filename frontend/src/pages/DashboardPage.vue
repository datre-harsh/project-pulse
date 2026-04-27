<template>
  <div>
    <h1 class="text-h4 mb-4">Project Pulse Dashboard</h1>
    <v-row>
      <v-col cols="12" md="4" v-for="card in cards" :key="card.title">
        <v-card>
          <v-card-title>{{ card.title }}</v-card-title>
          <v-card-text>{{ card.value }}</v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const cards = ref([
  { title: 'Rubrics', value: '0' },
  { title: 'Sections', value: '0' },
  { title: 'Teams', value: '0' }
])

onMounted(async () => {
  const [rubrics, sections, teams] = await Promise.all([
    api.get('/rubrics'),
    api.get('/sections'),
    api.get('/teams')
  ])

  cards.value = [
    { title: 'Rubrics', value: rubrics.data.length },
    { title: 'Sections', value: sections.data.length },
    { title: 'Teams', value: teams.data.length }
  ]
})
</script>
