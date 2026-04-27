<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-file-chart</v-icon>
            Generate WAR Report of Senior Design Team
          </v-card-title>
          <v-card-subtitle>
            Weekly Activity Report for team members
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Frontend Filters -->
            <v-row class="mb-4">
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedWeekId"
                  :items="weekOptions"
                  label="Active week"
                  prepend-inner-icon="mdi-calendar-week"
                  variant="outlined"
                  @update:model-value="onWeekChange"
                />
              </v-col>
              
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedTeamId"
                  :items="teamOptions"
                  label="Select Team"
                  prepend-inner-icon="mdi-account-group"
                  variant="outlined"
                  :disabled="!selectedWeekId"
                  @update:model-value="onTeamChange"
                />
              </v-col>
            </v-row>
            
            <!-- Generate Report Button -->
            <v-row class="mb-4">
              <v-col cols="12">
                <v-btn
                  color="primary"
                  size="large"
                  :disabled="!selectedWeekId || !selectedTeamId"
                  :loading="loading"
                  @click="loadTeamWARReport"
                >
                  <v-icon class="mr-2">mdi-file-chart</v-icon>
                  Generate Report
                </v-btn>
              </v-col>
            </v-row>
            
            <!-- Loading State -->
            <v-row v-if="loading" class="justify-center">
              <v-col cols="12" class="text-center">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  size="48"
                />
                <p class="mt-4 text-body-2">Generating team WAR report...</p>
              </v-col>
            </v-row>
            
            <!-- WAR Report Table -->
            <div v-else-if="warReport">
              <!-- Debug Display -->
              <v-alert type="info" variant="tonal" class="mb-4">
                <strong>Debug - Active Students:</strong> {{ warReport.activeStudents?.length || 0 }}
                <br>
                <strong>Debug - Missing Students:</strong> {{ warReport.missingStudents?.length || 0 }}
                <br>
                <strong>Debug - First Student:</strong> {{ warReport.activeStudents?.[0]?.name || 'None' }}
                <br>
                <strong>Debug - First Student Activities:</strong> {{ warReport.activeStudents?.[0]?.activities?.length || 0 }}
              </v-alert>
              
              <v-card class="mb-4">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-chart-box</v-icon>
                  {{ warReport.teamName }} - {{ getSelectedWeekDisplay() }}
                </v-card-title>
                
                <v-card-text>
                  <!-- Basic v-table with custom thead/tbody (no v-data-table) -->
                  <v-table class="war-table" density="compact">
                    <thead>
                      <tr>
                        <th class="text-left">Student</th>
                        <th class="text-left">Activity category</th>
                        <th class="text-left">Planned activity</th>
                        <th class="text-left">Description</th>
                        <th class="text-left">Planned hours</th>
                        <th class="text-left">Actual hours</th>
                        <th class="text-left">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      <template v-for="student in warReport.activeStudents" :key="student.name">
                        <tr v-for="(activity, index) in student.activities" :key="activity.description">
                          <td v-if="index === 0" :rowspan="student.activities.length" class="student-name-cell">
                            <div class="font-weight-medium">{{ student.name }}</div>
                          </td>
                          <td class="activity-cell">{{ activity.category }}</td>
                          <td class="activity-cell">{{ activity.plannedActivity }}</td>
                          <td class="description-cell">{{ activity.description }}</td>
                          <td class="hours-cell">{{ activity.plannedHours }}</td>
                          <td class="hours-cell">{{ activity.actualHours }}</td>
                          <td class="status-cell">
                            <v-chip
                              :color="getStatusColor(activity.status)"
                              variant="tonal"
                              size="small"
                            >
                              {{ activity.status }}
                            </v-chip>
                          </td>
                        </tr>
                      </template>
                    </tbody>
                  </v-table>
                </v-card-text>
              </v-card>
              
              <!-- Missing Students Section (CRITICAL) -->
              <v-card v-if="warReport.missingStudents && warReport.missingStudents.length > 0">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-account-alert</v-icon>
                  Missing Students
                </v-card-title>
                
                <v-card-text>
                  <!-- Exact text as specified -->
                  <p class="text-body-1 mb-3">
                    The report shall show who did not turn in the WAR for that week.
                  </p>
                  
                  <!-- Bulleted list of missing students -->
                  <ul>
                    <li v-for="missingStudent in warReport.missingStudents" :key="missingStudent">
                      {{ missingStudent }}
                    </li>
                  </ul>
                </v-card-text>
              </v-card>
            </div>
            
            <!-- No Report Generated Message -->
            <v-alert
              v-else-if="selectedTeamId && selectedWeekId"
              type="info"
              variant="tonal"
            >
              <v-alert-title>Generate Report</v-alert-title>
              <div>
                Click "Generate Report" to create the WAR report for this team.
              </div>
              <v-btn
                color="primary"
                class="mt-3"
                @click="loadTeamWARReport"
                :loading="loading"
              >
                <v-icon class="mr-2">mdi-file-chart</v-icon>
                Generate Report
              </v-btn>
            </v-alert>
            
            <!-- Initial State Message -->
            <v-alert
              v-else
              type="info"
              variant="tonal"
            >
              <v-alert-title>Select Parameters</v-alert-title>
              <div>
                Please select an active week and team to generate the WAR report.
              </div>
            </v-alert>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Success Message -->
    <v-snackbar
      v-model="showSuccessMessage"
      color="success"
      timeout="3000"
    >
      {{ successMessage }}
    </v-snackbar>
    
    <!-- Error Message -->
    <v-snackbar
      v-model="showErrorMessage"
      color="error"
      timeout="5000"
    >
      {{ errorMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

// Reactive data
const loading = ref(false)
const warReport = ref(null)

// Selection states
const selectedWeekId = ref('2024-week5') // Default to previous week
const selectedTeamId = ref(null)

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Mock data for dropdowns (replace with actual API calls)
const weekOptions = [
  { title: '01-15-2024 to 01-21-2024', value: '2024-week1' },
  { title: '01-22-2024 to 01-28-2024', value: '2024-week2' },
  { title: '01-29-2024 to 02-04-2024', value: '2024-week3' },
  { title: '02-05-2024 to 02-11-2024', value: '2024-week4' },
  { title: '02-12-2024 to 02-18-2024', value: '2024-week5' }
]

const teamOptions = ref([
  { title: 'Team Alpha - CS 201', value: 1 },
  { title: 'Team Beta - CS 201', value: 2 },
  { title: 'Team Gamma - CS 301', value: 3 }
])

// Step handlers
const onWeekChange = () => {
  selectedTeamId.value = null
  warReport.value = null
}

const onTeamChange = () => {
  warReport.value = null
}

// Load team WAR report
const loadTeamWARReport = async () => {
  if (!selectedTeamId.value || !selectedWeekId.value) return
  
  loading.value = true
  try {
    const response = await api.get(`/teams/${selectedTeamId.value}/war-report/${selectedWeekId.value}`)
    console.log('WAR Report Response:', response.data) // Debug logging
    warReport.value = response.data
    
    successMessage.value = 'Team WAR report generated successfully'
    showSuccessMessage.value = true
  } catch (error) {
    console.error('Error loading team WAR report:', error)
    const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to generate team WAR report'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Helper methods
const getSelectedWeekDisplay = () => {
  const week = weekOptions.find(w => w.value === selectedWeekId.value)
  return week ? week.title : 'Selected Week'
}

const getStatusColor = (status) => {
  switch (status?.toLowerCase()) {
    case 'completed':
      return 'green'
    case 'in progress':
      return 'yellow'
    case 'not started':
      return 'red'
    default:
      return 'grey'
  }
}

// Load initial data
onMounted(() => {
  // Initialize with default week
})
</script>

<style scoped>
.war-table {
  border-radius: 8px;
}

.war-table th {
  background-color: #f5f5f5;
  font-weight: 600;
  border-bottom: 2px solid #e0e0e0;
}

.war-table td {
  border-bottom: 1px solid #e0e0e0;
  vertical-align: top;
  padding: 12px 8px;
}

.student-name-cell {
  font-weight: 600;
  min-width: 120px;
  border-right: 2px solid #e0e0e0;
  vertical-align: middle !important;
}

.activity-cell {
  min-width: 100px;
}

.description-cell {
  max-width: 250px;
  word-wrap: break-word;
}

.hours-cell {
  min-width: 80px;
  text-align: center;
}

.status-cell {
  min-width: 100px;
  text-align: center;
}

.v-card {
  border-radius: 12px;
}

/* Ensure proper table borders for rowspan */
.war-table tr:hover {
  background-color: #f9f9f9;
}

/* Missing students list styling */
ul {
  margin-left: 20px;
}

ul li {
  margin-bottom: 4px;
}
</style>
