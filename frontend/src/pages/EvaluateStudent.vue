<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-account-check</v-icon>
            Generate Peer Evaluation Report
          </v-card-title>
          <v-card-subtitle>
            Generate a peer evaluation report of the entire senior design section
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Report Generating Parameters -->
            <v-row class="mb-4">
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedWeekId"
                  :items="weekOptions"
                  label="Active Week"
                  prepend-inner-icon="mdi-calendar-week"
                  variant="outlined"
                  @update:model-value="onWeekChange"
                />
              </v-col>
              
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedSectionId"
                  :items="sectionOptions"
                  label="Select Section"
                  prepend-inner-icon="mdi-school"
                  variant="outlined"
                  :disabled="!selectedWeekId"
                  @update:model-value="onSectionChange"
                />
              </v-col>
            </v-row>
            
            <!-- Generate Report Button -->
            <v-row class="mb-4">
              <v-col cols="12">
                <v-btn
                  color="primary"
                  size="large"
                  :disabled="!selectedWeekId || !selectedSectionId"
                  :loading="loading"
                  @click="loadSectionReport"
                >
                  <v-icon class="mr-2">mdi-chart-box</v-icon>
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
                <p class="mt-4 text-body-2">Generating section evaluation report...</p>
              </v-col>
            </v-row>
            
            <!-- Section Evaluation Report Table -->
            <div v-else-if="sectionReport">
              <v-card class="mb-4">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-chart-box</v-icon>
                  {{ sectionReport.sectionName }} - {{ getSelectedWeekDisplay() }}
                </v-card-title>
                
                <v-card-text>
                  <!-- Custom Table with Rowspan for Student/Grade Grouping -->
                  <v-table class="evaluation-table" density="compact">
                    <thead>
                      <tr>
                        <th class="text-left">Student</th>
                        <th class="text-left">Grade</th>
                        <th class="text-left">Commented by</th>
                        <th class="text-left">Public comments</th>
                        <th class="text-left">Private comments</th>
                      </tr>
                    </thead>
                    <tbody>
                      <template v-for="(student, studentIndex) in sectionReport.studentReports" :key="student.studentId">
                        <tr v-for="(evaluator, evaluatorIndex) in student.evaluators" :key="`${student.studentId}-${evaluatorIndex}`">
                          <!-- Student Name (only show on first evaluator row) -->
                          <td v-if="evaluatorIndex === 0" :rowspan="student.evaluators.length || 1" class="student-name-cell">
                            <div class="font-weight-medium">{{ student.studentName }}</div>
                          </td>
                          
                          <!-- Grade (only show on first evaluator row) -->
                          <td v-if="evaluatorIndex === 0" :rowspan="student.evaluators.length || 1" class="grade-cell">
                            <v-chip
                              :color="getGradeColor(student.grade)"
                              variant="tonal"
                              size="small"
                            >
                              {{ student.grade }}
                            </v-chip>
                          </td>
                          
                          <!-- Evaluator Name -->
                          <td class="evaluator-cell">
                            <div class="font-weight-medium">{{ evaluator.evaluatorName }}</div>
                          </td>
                          
                          <!-- Public Comment -->
                          <td class="comment-cell">
                            <div class="text-body-2">
                              <v-icon size="small" class="mr-1" color="blue">mdi-comment-text</v-icon>
                              {{ evaluator.publicComment || 'No public comment' }}
                            </div>
                          </td>
                          
                          <!-- Private Comment -->
                          <td class="comment-cell">
                            <div class="text-body-2">
                              <v-icon size="small" class="mr-1" color="red">mdi-comment-lock</v-icon>
                              {{ evaluator.privateComment || 'No private comment' }}
                            </div>
                          </td>
                        </tr>
                        
                        <!-- Show "No Evaluations" row if student has no evaluators -->
                        <tr v-if="student.evaluators.length === 0">
                          <td class="student-name-cell">
                            <div class="font-weight-medium">{{ student.studentName }}</div>
                          </td>
                          <td class="grade-cell">
                            <v-chip
                              color="grey"
                              variant="tonal"
                              size="small"
                            >
                              {{ student.grade }}
                            </v-chip>
                          </td>
                          <td colspan="3" class="text-center text-grey">
                            No peer evaluations received
                          </td>
                        </tr>
                      </template>
                    </tbody>
                  </v-table>
                </v-card-text>
              </v-card>
            </div>
            
            <!-- No Report Generated Message -->
            <v-alert
              v-else-if="selectedSectionId && selectedWeekId"
              type="info"
              variant="tonal"
            >
              <v-alert-title>Generate Report</v-alert-title>
              <div>
                Click "Generate Report" to create the peer evaluation report for this section.
              </div>
              <v-btn
                color="primary"
                class="mt-3"
                @click="loadSectionReport"
                :loading="loading"
              >
                <v-icon class="mr-2">mdi-chart-box</v-icon>
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
                Please select an active week and section to generate the peer evaluation report.
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
import axios from 'axios'

// Reactive data
const loading = ref(false)
const sectionReport = ref(null)

// Selection states
const selectedWeekId = ref('2024-week5') // Default to previous week
const selectedSectionId = ref(null)

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

const sectionOptions = ref([
  { title: 'CS 201 Section A', value: 1 },
  { title: 'CS 201 Section B', value: 2 },
  { title: 'CS 301 Section A', value: 3 }
])

// Step handlers
const onWeekChange = () => {
  selectedSectionId.value = null
  sectionReport.value = null
}

const onSectionChange = () => {
  sectionReport.value = null
}

// Load section evaluation report
const loadSectionReport = async () => {
  if (!selectedSectionId.value || !selectedWeekId.value) return
  
  loading.value = true
  try {
    const response = await axios.get(`/api/instructors/sections/${selectedSectionId.value}/evaluations/${selectedWeekId.value}`)
    sectionReport.value = response.data
    
    successMessage.value = 'Section evaluation report generated successfully'
    showSuccessMessage.value = true
  } catch (error) {
    console.error('Error loading section report:', error)
    const errorMsg = error.response?.data?.message || 'Failed to generate section evaluation report'
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

const getGradeColor = (grade) => {
  if (!grade) return 'grey'
  
  // Extract score from grade format (e.g., "54/60" -> 54)
  const score = parseInt(grade.split('/')[0])
  const total = parseInt(grade.split('/')[1])
  const percentage = (score / total) * 100
  
  if (percentage >= 90) return 'green'
  if (percentage >= 80) return 'light-green'
  if (percentage >= 70) return 'yellow'
  if (percentage >= 60) return 'orange'
  return 'red'
}

// Load initial data
onMounted(() => {
  // Initialize with default week
})
</script>

<style scoped>
.evaluation-table {
  border-radius: 8px;
}

.evaluation-table th {
  background-color: #f5f5f5;
  font-weight: 600;
  border-bottom: 2px solid #e0e0e0;
}

.evaluation-table td {
  border-bottom: 1px solid #e0e0e0;
  vertical-align: top;
  padding: 12px 8px;
}

.student-name-cell {
  font-weight: 600;
  min-width: 150px;
  border-right: 2px solid #e0e0e0;
}

.grade-cell {
  min-width: 80px;
  text-align: center;
  border-right: 2px solid #e0e0e0;
}

.evaluator-cell {
  font-weight: 500;
  min-width: 120px;
}

.comment-cell {
  max-width: 200px;
  word-wrap: break-word;
}

.v-card {
  border-radius: 12px;
}

/* Ensure proper table borders for rowspan */
.evaluation-table tr:hover {
  background-color: #f9f9f9;
}
</style>
