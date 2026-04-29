<template>
  <v-container v-if="isReady" fluid>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-6">Student Reports</h1>
        
        <!-- Navigation Tabs -->
        <v-tabs v-model="activeTab" color="primary" class="mb-6">
          <v-tab value="peer-eval">Peer Evaluation Report (UC-33)</v-tab>
          <v-tab value="war">Student WAR Report (UC-34)</v-tab>
        </v-tabs>
        
        <!-- Tab Content -->
        <v-window v-model="activeTab">
          <!-- UC-33: Peer Evaluation Report -->
          <v-window-item value="peer-eval">
            <v-card class="elevation-2">
              <v-card-title class="text-h5">
                <v-icon class="mr-2">mdi-account-search</v-icon>
                Peer Evaluation Report
              </v-card-title>
              <v-card-subtitle>
                Peer evaluations with instructor visibility
              </v-card-subtitle>
              
              <v-card-text>
                <v-row class="mb-4">
                  <v-col cols="12" md="4">
                    <v-select
                      v-model="selectedStudentId"
                      :items="students"
                      item-title="name"
                      item-value="id"
                      label="Student"
                      variant="outlined"
                      @update:model-value="loadReports"
                    />
                  </v-col>
                  <v-col cols="12" md="4">
                    <v-text-field
                      v-model="startWeekId"
                      label="Start Week ID"
                      variant="outlined"
                      @change="loadReports"
                    />
                  </v-col>
                  <v-col cols="12" md="4">
                    <v-text-field
                      v-model="endWeekId"
                      label="End Week ID"
                      variant="outlined"
                      @change="loadReports"
                    />
                  </v-col>
                </v-row>

                <v-table v-if="peerEvalData && peerEvalData.length > 0" class="elevation-1">
                  <thead>
                    <tr>
                      <th class="text-left">Week</th>
                      <th class="text-left">Grade</th>
                      <th class="text-left">Commented by</th>
                      <th class="text-left">Public comments</th>
                      <th class="text-left">Private comments</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="weekData in peerEvalData" :key="weekData.weekRange">
                      <tr v-for="(evaluation, index) in (weekData.evaluations || [])" :key="`${weekData.weekRange}-${index}`">
                        <!-- Week cell with rowspan -->
                        <td v-if="index === 0" :rowspan="weekData.evaluations.length" class="font-weight-bold">
                          {{ weekData.weekRange }}
                        </td>
                        <!-- Grade cell with rowspan -->
                        <td v-if="index === 0" :rowspan="weekData.evaluations.length" class="text-center">
                          <v-chip color="success" variant="tonal" size="small">
                            {{ weekData.overallGrade }}
                          </v-chip>
                        </td>
                        <!-- Normal cells for each evaluator -->
                        <td>{{ evaluation.evaluatorName }}</td>
                        <td>{{ evaluation.publicComments }}</td>
                        <td>{{ evaluation.privateComments }}</td>
                      </tr>
                    </template>
                  </tbody>
                </v-table>
                
                <v-alert v-else type="info" variant="tonal">
                  No peer evaluation data available.
                </v-alert>
              </v-card-text>
            </v-card>
          </v-window-item>
          
          <!-- UC-34: Student WAR Report -->
          <v-window-item value="war">
            <v-card class="elevation-2">
              <v-card-title class="text-h5">
                <v-icon class="mr-2">mdi-chart-box</v-icon>
                Student WAR Report
              </v-card-title>
              <v-card-subtitle>
                Weekly Activity Report
              </v-card-subtitle>
              
              <v-card-text>
                <v-table v-if="warData && warData.length > 0" class="elevation-1">
                  <thead>
                    <tr>
                      <th class="text-left">Category</th>
                      <th class="text-left">Planned</th>
                      <th class="text-left">Description</th>
                      <th class="text-left">Planned Hrs</th>
                      <th class="text-left">Actual Hrs</th>
                      <th class="text-left">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="weekData in warData" :key="weekData.weekRange">
                      <!-- Grouped Header Row -->
                      <tr class="bg-grey-lighten-2">
                        <td colspan="6" class="text-center font-weight-bold">
                          Active week: {{ weekData.weekRange }}
                        </td>
                      </tr>
                      <!-- Activity Rows for this week -->
                      <tr v-if="weekData.activities && weekData.activities.length > 0" 
                          v-for="(activity, index) in weekData.activities" 
                          :key="`${weekData.weekRange}-activity-${index}`">
                        <td>{{ activity.category }}</td>
                        <td>{{ activity.plannedActivity }}</td>
                        <td>{{ activity.description }}</td>
                        <td>{{ activity.plannedHours }}</td>
                        <td>{{ activity.actualHours }}</td>
                        <td>
                          <v-chip :color="getStatusColor(activity.status)" variant="tonal" size="small">
                            {{ activity.status }}
                          </v-chip>
                        </td>
                      </tr>
                      <!-- No Activities Row -->
                      <tr v-else>
                        <td colspan="6" class="text-center text-grey">
                          No activities recorded for this week
                        </td>
                      </tr>
                    </template>
                  </tbody>
                </v-table>
                
                <v-alert v-else type="info" variant="tonal">
                  No WAR data available.
                </v-alert>
              </v-card-text>
            </v-card>
          </v-window-item>
        </v-window>
      </v-col>
    </v-row>
  </v-container>
  
  <!-- Loading State -->
  <v-container v-else fluid>
    <v-row justify="center">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
        <p class="mt-4">Loading student reports...</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api, { getCurrentUser } from '../api'

const isReady = ref(false)
const activeTab = ref('peer-eval')
const peerEvalData = ref([])
const warData = ref([])
const students = ref([])
const selectedStudentId = ref(null)
const startWeekId = ref('2024-week1')
const endWeekId = ref('2024-week5')

// Status color helper
const getStatusColor = (status) => {
  switch (status?.toLowerCase()) {
    case 'completed':
      return 'success'
    case 'in progress':
      return 'warning'
    default:
      return 'grey'
  }
}

const deduplicateStudents = (studentRows) => {
  const uniqueStudents = new Map()

  studentRows.forEach((student) => {
    if (!uniqueStudents.has(student.id)) {
      uniqueStudents.set(student.id, {
        id: student.id,
        name: `${student.firstName} ${student.lastName}`
      })
    }
  })

  return Array.from(uniqueStudents.values())
}

const loadStudents = async () => {
  const currentUser = getCurrentUser()
  const response = await api.get('/students')
  let availableStudents = response.data

  if (currentUser?.role === 'INSTRUCTOR') {
    // Ralph: Instructors should only see students from the teams they supervise.
    const teamResponse = await api.get('/teams', {
      params: { instructorId: currentUser.id }
    })
    const supervisedTeamIds = new Set(teamResponse.data.map(team => team.id))

    availableStudents = availableStudents.filter(student =>
      student.teamId && supervisedTeamIds.has(student.teamId)
    )
  }

  students.value = deduplicateStudents(availableStudents)

  if (!selectedStudentId.value && students.value.length > 0) {
    selectedStudentId.value = students.value[0].id
  }

  if (students.value.length === 0) {
    selectedStudentId.value = null
  }
}

const loadReports = async () => {
  if (!selectedStudentId.value || !startWeekId.value || !endWeekId.value) return
  const params = { startWeekId: startWeekId.value, endWeekId: endWeekId.value }
  const [peerResponse, warResponse] = await Promise.all([
    api.get(`/students/${selectedStudentId.value}/peer-evaluation-report`, { params }),
    api.get(`/students/${selectedStudentId.value}/war-report`, { params })
  ])
  peerEvalData.value = peerResponse.data
  warData.value = warResponse.data
}

onMounted(async () => {
  try {
    await loadStudents()
    await loadReports()
    isReady.value = true
  } catch (error) {
    console.error('Error mounting component:', error)
    isReady.value = true
  }
})
</script>

<style scoped>
.v-table td {
  vertical-align: middle !important;
}
</style>
