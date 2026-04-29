<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-chart-box</v-icon>
            My Peer Evaluation Report
          </v-card-title>
          <v-card-subtitle>
            View your anonymous peer evaluation results
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Week Selection -->
            <v-row class="mb-4">
              <v-col cols="12" md="4">
                <v-select
                  v-model="selectedWeekId"
                  :items="weekOptions"
                  label="Select Week"
                  prepend-inner-icon="mdi-calendar-week"
                  variant="outlined"
                  @update:model-value="loadEvaluationReport"
                />
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
                <p class="mt-4 text-body-2">Loading evaluation report...</p>
              </v-col>
            </v-row>
            
            <!-- No Evaluations Message -->
            <v-alert
              v-else-if="!hasEvaluations"
              type="info"
              variant="tonal"
              class="mb-4"
            >
              <v-alert-title>No evaluations available for this week</v-alert-title>
              <div>
                Your team members haven't submitted peer evaluations for this week yet. 
                Check back later or contact your team members to remind them to submit their evaluations.
              </div>
            </v-alert>
            
            <!-- Evaluation Report Content -->
            <div v-else>
              <!-- Peer Evaluation Report Table -->
              <v-card class="mb-4">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-chart-box</v-icon>
                  Peer Evaluation Report
                </v-card-title>
                <v-card-subtitle>
                  {{ getSelectedWeekDisplay() }} - Anonymous aggregated scores
                </v-card-subtitle>
                
                <v-card-text>
                  <v-data-table
                    :headers="tableHeaders"
                    :items="tableData"
                    class="elevation-1"
                    hide-default-footer
                    density="compact"
                  >
                    <template v-slot:item.student="{ item }">
                      <div class="font-weight-medium">
                        {{ item.student }}
                      </div>
                    </template>
                    
                    <!-- Dynamic rubric score columns -->
                    <template v-for="criterion in rubricCriteria" :key="criterion.id" v-slot:[`item.${criterion.id}`]="{ item }">
                      <div class="text-center">
                        <span class="font-weight-medium">
                          {{ item[criterion.id]?.toFixed(1) || 'N/A' }}
                        </span>
                      </div>
                    </template>
                    
                    <template v-slot:item.publicComments="{ item }">
                      <div class="text-body-2" style="max-width: 200px;">
                        <div v-for="(comment, index) in item.publicComments" :key="index" class="mb-1">
                          <v-icon size="small" class="mr-1" color="grey">mdi-comment</v-icon>
                          {{ comment }}
                        </div>
                      </div>
                    </template>
                    
                    <template v-slot:item.grade="{ item }">
                      <div class="text-center">
                        <v-chip
                          :color="getGradeColor(item.grade)"
                          variant="tonal"
                          size="small"
                        >
                          {{ item.grade }}
                        </v-chip>
                      </div>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-card>
            </div>
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
import { ref, computed, onMounted } from 'vue'
import api from '../api'

// Reactive data
const loading = ref(false)
const selectedWeekId = ref('2024-week5') // Previous week as default
const evaluationReport = ref(null)

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Week options with exact date ranges (MM-DD-YYYY to MM-DD-YYYY)
const weekOptions = [
  { title: '01-15-2024 to 01-21-2024', value: '2024-week1' },
  { title: '01-22-2024 to 01-28-2024', value: '2024-week2' },
  { title: '01-29-2024 to 02-04-2024', value: '2024-week3' },
  { title: '02-05-2024 to 02-11-2024', value: '2024-week4' },
  { title: '02-12-2024 to 02-18-2024', value: '2024-week5' }
]

const rubricCriteria = computed(() => Object.keys(evaluationReport.value?.averageScores || {}).map(key => ({
  id: key,
  name: key,
  description: ''
})))

// Computed properties
const hasEvaluations = computed(() => {
  return Object.keys(evaluationReport.value?.averageScores || {}).length > 0
})

// Dynamic table headers based on rubric criteria
const tableHeaders = computed(() => {
  const headers = [
    { title: 'Student', key: 'student', sortable: false, width: '120px' }
  ]
  
  // Add dynamic rubric columns with name and description
  rubricCriteria.value.forEach(criterion => {
    headers.push({
      title: `${criterion.name}\n${criterion.description}`,
      key: criterion.id,
      sortable: false,
      width: '200px'
    })
  })
  
  // Add Public Comments and Grade columns
  headers.push(
    { title: 'Public Comments', key: 'publicComments', sortable: false, width: '250px' },
    { title: 'Grade', key: 'grade', sortable: false, width: '100px' }
  )
  
  return headers
})

const tableData = computed(() => {
  if (!hasEvaluations.value) return []
  
  const scores = evaluationReport.value.averageScores || {}
  const row = {
    student: 'Me',
    publicComments: evaluationReport.value.publicComments || [],
    grade: calculateGrade(scores)
  }
  Object.entries(scores).forEach(([key, value]) => {
    row[key] = value
  })
  return [row]
})

// Get selected week display text
const getSelectedWeekDisplay = () => {
  const week = weekOptions.find(w => w.value === selectedWeekId.value)
  return week ? week.title : 'Selected Week'
}

// Get grade color based on score
const getGradeColor = (grade) => {
  if (!grade) return 'grey'
  const score = parseInt(grade.split('/')[0])
  const total = parseInt(grade.split('/')[1])
  const percentage = (score / total) * 100
  
  if (percentage >= 90) return 'green'
  if (percentage >= 80) return 'light-green'
  if (percentage >= 70) return 'yellow'
  if (percentage >= 60) return 'orange'
  return 'red'
}

const calculateGrade = (scores) => {
  const values = Object.values(scores || {})
  if (!values.length) return '0/0'
  const earned = values.reduce((sum, value) => sum + Number(value || 0), 0)
  return `${earned.toFixed(1)}/${values.length * 10}`
}

const loadEvaluationReport = async () => {
  if (!selectedWeekId.value) return
  
  loading.value = true
  try {
    const response = await api.get(`/students/peer-evaluations/report/${selectedWeekId.value}`)
    evaluationReport.value = response.data
    
  } catch (error) {
    console.error('Error loading evaluation report:', error)
    const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to load evaluation report'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Load data on component mount
onMounted(() => {
  loadEvaluationReport()
})
</script>

<style scoped>
.v-data-table {
  border-radius: 8px;
}

.v-list-item {
  border-radius: 8px;
  margin-bottom: 4px;
}

.v-progress-linear {
  min-width: 80px;
}
</style>
