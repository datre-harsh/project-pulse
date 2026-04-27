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
              <!-- Average Scores Section -->
              <v-card class="mb-4">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-chart-line</v-icon>
                  Average Scores
                </v-card-title>
                <v-card-subtitle>
                  Aggregated scores from all peer evaluations (anonymous)
                </v-card-subtitle>
                
                <v-card-text>
                  <v-data-table
                    :headers="scoreHeaders"
                    :items="scoreData"
                    class="elevation-1"
                    hide-default-footer
                    density="compact"
                  >
                    <template v-slot:item.averageScore="{ item }">
                      <div class="d-flex align-center">
                        <v-progress-linear
                          :model-value="(item.averageScore / 5) * 100"
                          :color="getScoreColor(item.averageScore)"
                          height="8"
                          rounded
                          class="mr-3"
                          style="max-width: 100px;"
                        />
                        <span class="font-weight-medium">
                          {{ item.averageScore.toFixed(1) }}
                        </span>
                        <v-chip
                          :color="getScoreColor(item.averageScore)"
                          variant="tonal"
                          size="small"
                          class="ml-2"
                        >
                          {{ getScoreLabel(item.averageScore) }}
                        </v-chip>
                      </div>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-card>
              
              <!-- Public Comments Section -->
              <v-card v-if="publicComments.length > 0">
                <v-card-title class="text-h6">
                  <v-icon class="mr-2">mdi-comment-multiple</v-icon>
                  Public Comments
                </v-card-title>
                <v-card-subtitle>
                  Anonymous feedback from your team members
                </v-card-subtitle>
                
                <v-card-text>
                  <v-list>
                    <v-list-item
                      v-for="(comment, index) in publicComments"
                      :key="index"
                      class="mb-2"
                    >
                      <template v-slot:prepend>
                        <v-avatar color="grey-lighten-2" size="32">
                          <v-icon color="grey-darken-1">mdi-account-outline</v-icon>
                        </v-avatar>
                      </template>
                      
                      <v-list-item-title class="text-body-2">
                        Anonymous Team Member
                      </v-list-item-title>
                      
                      <v-list-item-subtitle class="text-body-1 mt-1">
                        {{ comment }}
                      </v-list-item-subtitle>
                      
                      <template v-slot:append>
                        <v-chip
                          color="grey"
                          variant="tonal"
                          size="small"
                        >
                          Comment #{{ index + 1 }}
                        </v-chip>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card-text>
              </v-card>
              
              <!-- No Comments Message -->
              <v-alert
                v-else
                type="info"
                variant="tonal"
              >
                <v-alert-title>No public comments</v-alert-title>
                <div>
                  Your team members didn't provide any public comments for this week.
                </div>
              </v-alert>
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
import axios from 'axios'

// Reactive data
const loading = ref(false)
const selectedWeekId = ref('2024-week1')
const evaluationReport = ref(null)

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Week options (mock data for demo)
const weekOptions = [
  { title: 'Week 1', value: '2024-week1' },
  { title: 'Week 2', value: '2024-week2' },
  { title: 'Week 3', value: '2024-week3' },
  { title: 'Week 4', value: '2024-week4' }
]

// Score labels for display
const scoreLabels = {
  1: 'Poor',
  2: 'Fair', 
  3: 'Good',
  4: 'Very Good',
  5: 'Excellent'
}

// Table headers for scores
const scoreHeaders = [
  { title: 'Criterion', key: 'criterion', sortable: false },
  { title: 'Average Score', key: 'averageScore', sortable: false }
]

// Computed properties
const hasEvaluations = computed(() => {
  return evaluationReport.value && 
         evaluationReport.value.averageScores && 
         Object.keys(evaluationReport.value.averageScores).length > 0
})

const scoreData = computed(() => {
  if (!hasEvaluations.value) return []
  
  return Object.entries(evaluationReport.value.averageScores).map(([criterion, score]) => ({
    criterion: formatCriterionName(criterion),
    averageScore: score
  }))
})

const publicComments = computed(() => {
  return evaluationReport.value?.publicComments || []
})

// Load evaluation report
const loadEvaluationReport = async () => {
  if (!selectedWeekId.value) return
  
  loading.value = true
  try {
    // TODO: Get actual student ID from authentication
    const studentId = 1
    const response = await axios.get(`/api/students/peer-evaluations/report/${selectedWeekId.value}`)
    evaluationReport.value = response.data
    
    if (response.data.message && response.data.message.includes('No evaluations available')) {
      // This is handled by the hasEvaluations computed property
    }
  } catch (error) {
    console.error('Error loading evaluation report:', error)
    const errorMsg = error.response?.data?.message || 'Failed to load evaluation report'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Format criterion name for display
const formatCriterionName = (criterion) => {
  return criterion
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, l => l.toUpperCase())
}

// Get score label
const getScoreLabel = (score) => {
  const roundedScore = Math.round(score)
  return scoreLabels[roundedScore] || 'Not Rated'
}

// Get score color
const getScoreColor = (score) => {
  if (score >= 4.5) return 'green'
  if (score >= 3.5) return 'light-green'
  if (score >= 2.5) return 'yellow'
  if (score >= 1.5) return 'orange'
  return 'red'
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
