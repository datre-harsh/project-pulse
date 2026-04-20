<template>
  <div>
    <h2 class="text-h5 mb-4">Reports</h2>

    <v-row>
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>WAR Report</v-card-title>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6"><v-select v-model="warReq.byTeam" :items="[true, false]" label="By Team" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="warReq.targetId" label="Target ID" type="number" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="warReq.startWeek" label="Start Week" type="number" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="warReq.endWeek" label="End Week" type="number" /></v-col>
            </v-row>
            <v-btn color="primary" @click="generateWarReport">Generate WAR Report</v-btn>
            <v-divider class="my-3" />
            <v-data-table :items="warRows" :headers="warHeaders" density="compact" />
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Peer Evaluation Report</v-card-title>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6"><v-select v-model="peerReq.bySection" :items="[true, false]" label="By Section" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="peerReq.targetId" label="Target ID" type="number" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="peerReq.startWeek" label="Start Week" type="number" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model.number="peerReq.endWeek" label="End Week" type="number" /></v-col>
            </v-row>
            <v-btn color="secondary" @click="generatePeerReport">Generate Peer Report</v-btn>
            <v-divider class="my-3" />
            <v-data-table :items="peerRows" :headers="peerHeaders" density="compact" />
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import api from '../api'

const warReq = reactive({ byTeam: true, targetId: null, startWeek: 1, endWeek: 15 })
const peerReq = reactive({ bySection: true, targetId: null, startWeek: 1, endWeek: 15 })

const warRows = ref([])
const peerRows = ref([])

const warHeaders = [
  { title: 'Week', key: 'weekNumber' },
  { title: 'Student', key: 'studentId' },
  { title: 'Activity', key: 'plannedActivity' },
  { title: 'Status', key: 'status' }
]

const peerHeaders = [
  { title: 'Week', key: 'targetWeekNumber' },
  { title: 'Evaluator', key: 'evaluatorStudentId' },
  { title: 'Evaluatee', key: 'evaluateeStudentId' },
  { title: 'Total Score', key: 'totalScore' }
]

const generateWarReport = async () => {
  const res = await api.post('/reports/war', warReq)
  warRows.value = res.data
}

const generatePeerReport = async () => {
  const res = await api.post('/reports/peer-evaluation', peerReq)
  peerRows.value = res.data
}
</script>
