<template>
  <div>
    <h2 class="text-h5 mb-4">Peer Evaluation Submission</h2>

    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="2"><v-select v-model="form.sectionId" :items="sections" item-title="name" item-value="id" label="Section" /></v-col>
          <v-col cols="12" md="2"><v-select v-model="form.teamId" :items="teams" item-title="name" item-value="id" label="Team" /></v-col>
          <v-col cols="12" md="3"><v-select v-model="form.evaluatorStudentId" :items="students" item-title="email" item-value="id" label="Evaluator" /></v-col>
          <v-col cols="12" md="3"><v-select v-model="form.evaluateeStudentId" :items="students" item-title="email" item-value="id" label="Evaluatee" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model.number="form.targetWeekNumber" type="number" label="Target Week" /></v-col>

          <v-col cols="12" md="8"><v-text-field v-model="form.publicComment" label="Public Comment" /></v-col>
        </v-row>

        <v-divider class="my-3" />

        <h3 class="text-subtitle-1 mb-2">Criterion Scores</h3>
        <v-row v-for="criterion in criteria" :key="criterion.id">
          <v-col cols="12" md="8">{{ criterion.name }} (max {{ criterion.maxScore }})</v-col>
          <v-col cols="12" md="4">
            <v-text-field
              v-model.number="scoreMap[criterion.id]"
              type="number"
              :min="0"
              :max="criterion.maxScore"
              label="Score"
            />
          </v-col>
        </v-row>

        <v-btn color="primary" class="mt-4" @click="submit">Submit Evaluation</v-btn>
      </v-card-text>
    </v-card>

    <v-alert v-if="message" type="info">{{ message }}</v-alert>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'

const sections = ref([])
const teams = ref([])
const students = ref([])
const criteria = ref([])
const message = ref('')

const form = reactive({
  sectionId: null,
  teamId: null,
  evaluatorStudentId: null,
  evaluateeStudentId: null,
  targetWeekNumber: 1,
  publicComment: ''
})

const scoreMap = reactive({})

const load = async () => {
  const [sectionRes, teamRes, studentRes, rubricRes] = await Promise.all([
    api.get('/sections'),
    api.get('/teams'),
    api.get('/users?role=STUDENT'),
    api.get('/rubric')
  ])

  sections.value = sectionRes.data
  teams.value = teamRes.data
  students.value = studentRes.data
  criteria.value = rubricRes.data.filter((r) => r.active)

  criteria.value.forEach((c) => {
    if (scoreMap[c.id] === undefined) {
      scoreMap[c.id] = 0
    }
  })
}

const submit = async () => {
  const payload = {
    ...form,
    scores: criteria.value.map((c) => ({ criterionId: c.id, score: Number(scoreMap[c.id] || 0) }))
  }

  await api.post('/peer-evaluations', payload)
  message.value = 'Peer evaluation submitted successfully.'
}

onMounted(load)
</script>
