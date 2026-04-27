<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-account-group</v-icon>
            Submit Peer Evaluation
          </v-card-title>
          <v-card-subtitle>
            Evaluate your team members' performance
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Team Member Selection -->
            <v-row class="mb-4">
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedEvaluatee"
                  :items="teamMembers"
                  label="Select Team Member to Evaluate"
                  prepend-inner-icon="mdi-account"
                  variant="outlined"
                  item-title="name"
                  item-value="id"
                  @update:model-value="loadRubricCriteria"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedWeekId"
                  :items="weekOptions"
                  label="Select Week"
                  prepend-inner-icon="mdi-calendar-week"
                  variant="outlined"
                />
              </v-col>
            </v-row>
            
            <!-- Evaluation Form -->
            <v-form ref="evaluationForm" v-model="formValid">
              <v-card v-if="selectedEvaluatee && rubricCriteria.length > 0" class="mb-4">
                <v-card-title class="text-h6">
                  Evaluation Criteria
                </v-card-title>
                <v-card-text>
                  <v-row v-for="criterion in rubricCriteria" :key="criterion.id" class="mb-4">
                    <v-col cols="12">
                      <v-label class="text-subtitle-1 font-weight-medium">
                        {{ criterion.name }}
                      </v-label>
                      <p class="text-caption text-grey-600 mb-2">
                        {{ criterion.description }}
                      </p>
                      
                      <!-- Slider for scoring (1-5) -->
                      <v-slider
                        v-model="evaluationScores[criterion.id]"
                        :min="1"
                        :max="5"
                        :step="1"
                        :ticks="scoreLabels"
                        :tick-size="4"
                        thumb-label="always"
                        color="primary"
                        track-color="grey-lighten-2"
                        class="mt-4"
                      >
                        <template v-slot:thumb-label="{ modelValue }">
                          {{ getScoreLabel(modelValue) }}
                        </template>
                      </v-slider>
                      
                      <!-- Alternative: Radio buttons for scoring -->
                      <v-radio-group
                        v-model="evaluationScores[criterion.id]"
                        row
                        class="mt-2"
                        density="compact"
                      >
                        <v-radio
                          v-for="score in [1, 2, 3, 4, 5]"
                          :key="score"
                          :label="getScoreLabel(score)"
                          :value="score"
                          color="primary"
                        />
                      </v-radio-group>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
              
              <!-- Comments Section -->
              <v-card v-if="selectedEvaluatee" class="mb-4">
                <v-card-title class="text-h6">
                  Comments
                </v-card-title>
                <v-card-text>
                  <v-row>
                    <v-col cols="12">
                      <v-textarea
                        v-model="publicComment"
                        label="Public Comment"
                        placeholder="Provide feedback that will be visible to the evaluatee"
                        variant="outlined"
                        rows="3"
                        auto-grow
                        counter="1000"
                        :rules="[v => (v || '').length <= 1000 || 'Public comment must be less than 1000 characters']"
                      />
                    </v-col>
                  </v-row>
                  
                  <v-row>
                    <v-col cols="12">
                      <v-textarea
                        v-model="privateComment"
                        label="Private Comment"
                        placeholder="Provide confidential feedback for instructors only"
                        variant="outlined"
                        rows="3"
                        auto-grow
                        counter="1000"
                        :rules="[v => (v || '').length <= 1000 || 'Private comment must be less than 1000 characters']"
                      />
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
            </v-form>
            
            <!-- Action Buttons -->
            <v-row class="mt-4">
              <v-col cols="12" class="d-flex justify-end">
                <v-btn
                  color="grey"
                  variant="text"
                  @click="resetForm"
                  class="mr-2"
                >
                  Reset
                </v-btn>
                <v-btn
                  color="primary"
                  :disabled="!canSubmit"
                  :loading="submitting"
                  @click="showConfirmationDialog"
                >
                  Submit Evaluation
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Confirmation Dialog -->
    <v-dialog v-model="confirmationDialog" max-width="600">
      <v-card>
        <v-card-title class="text-h6">
          Confirm Peer Evaluation
        </v-card-title>
        
        <v-card-text>
          <p class="mb-4">
            You are about to submit the following evaluation for <strong>{{ getEvaluateeName() }}</strong>:
          </p>
          
          <v-table density="compact">
            <thead>
              <tr>
                <th>Criterion</th>
                <th class="text-center">Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="criterion in rubricCriteria" :key="criterion.id">
                <td>{{ criterion.name }}</td>
                <td class="text-center">
                  <v-chip
                    :color="getScoreColor(evaluationScores[criterion.id])"
                    variant="tonal"
                    size="small"
                  >
                    {{ getScoreLabel(evaluationScores[criterion.id]) }}
                  </v-chip>
                </td>
              </tr>
            </tbody>
          </v-table>
          
          <div v-if="publicComment || privateComment" class="mt-4">
            <v-expansion-panels>
              <v-expansion-panel v-if="publicComment">
                <v-expansion-panel-title>Public Comment</v-expansion-panel-title>
                <v-expansion-panel-text>
                  {{ publicComment }}
                </v-expansion-panel-text>
              </v-expansion-panel>
              
              <v-expansion-panel v-if="privateComment">
                <v-expansion-panel-title>Private Comment</v-expansion-panel-title>
                <v-expansion-panel-text>
                  {{ privateComment }}
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </div>
          
          <v-alert type="warning" class="mt-4">
            <strong>Important:</strong> Once submitted, evaluations cannot be edited or deleted.
          </v-alert>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey"
            variant="text"
            @click="confirmationDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            :loading="submitting"
            @click="submitEvaluation"
          >
            Confirm Submit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
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
const teamMembers = ref([])
const rubricCriteria = ref([])
const selectedEvaluatee = ref(null)
const selectedWeekId = ref('2024-week1')
const evaluationScores = ref({})
const publicComment = ref('')
const privateComment = ref('')
const evaluationForm = ref(null)
const formValid = ref(true)
const submitting = ref(false)
const confirmationDialog = ref(false)

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Score labels for slider
const scoreLabels = {
  1: 'Poor',
  2: 'Fair', 
  3: 'Good',
  4: 'Very Good',
  5: 'Excellent'
}

// Week options (mock data for demo)
const weekOptions = [
  { title: 'Week 1', value: '2024-week1' },
  { title: 'Week 2', value: '2024-week2' },
  { title: 'Week 3', value: '2024-week3' },
  { title: 'Week 4', value: '2024-week4' }
]

// Computed property for submit button validation
const canSubmit = computed(() => {
  console.log('canSubmit check:', {
    selectedEvaluatee: selectedEvaluatee.value,
    selectedWeekId: selectedWeekId.value,
    rubricCriteriaLength: rubricCriteria.value.length,
    evaluationScores: evaluationScores.value
  })
  
  if (!selectedEvaluatee.value) {
    console.log('Submit disabled: No evaluatee selected')
    return false
  }
  if (!selectedWeekId.value) {
    console.log('Submit disabled: No week selected')
    return false
  }
  if (rubricCriteria.value.length === 0) {
    console.log('Submit disabled: No rubric criteria')
    return false
  }
  
  // Check if all criteria have scores
  const allScoresValid = rubricCriteria.value.every(criterion => {
    const score = evaluationScores.value[criterion.id]
    const isValid = score && score >= 1 && score <= 5
    console.log(`Criterion ${criterion.id}: score=${score}, valid=${isValid}`)
    return isValid
  })
  
  console.log('All scores valid:', allScoresValid)
  return allScoresValid
})

// Load team members
const loadTeamMembers = async () => {
  loading.value = true
  try {
    // TODO: Get actual student ID from authentication
    const studentId = 1
    const response = await axios.get(`/api/students/${studentId}`)
    const studentData = response.data
    
    // Mock team members for demo (replace with actual team data)
    teamMembers.value = [
      { id: 2, name: 'Alice Johnson' },
      { id: 3, name: 'Bob Smith' },
      { id: 4, name: 'Carol Davis' }
    ].filter(member => member.id !== studentId)
  } catch (error) {
    console.error('Error loading team members:', error)
    errorMessage.value = 'Failed to load team members'
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Load rubric criteria
const loadRubricCriteria = async () => {
  if (!selectedEvaluatee.value) return
  
  loading.value = true
  try {
    // TODO: Get actual rubric ID from team/section
    const rubricId = 1
    
    // Try to load from API, fall back to mock data
    try {
      const response = await axios.get(`/api/rubrics/${rubricId}`)
      const rubricData = response.data
      rubricCriteria.value = rubricData.criteria || []
    } catch (apiError) {
      console.log('API not available, using mock rubric data')
      // Mock rubric criteria for testing
      rubricCriteria.value = [
        { id: 'participation', name: 'Participation', description: 'Active participation in team discussions and activities' },
        { id: 'quality', name: 'Work Quality', description: 'Quality and completeness of assigned tasks' },
        { id: 'communication', name: 'Communication', description: 'Clear and effective communication with team members' },
        { id: 'timeliness', name: 'Timeliness', description: 'Meeting deadlines and timely completion of work' },
        { id: 'collaboration', name: 'Collaboration', description: 'Ability to work effectively with team members' }
      ]
    }
    
    // Initialize scores for new criteria
    console.log('Initializing scores for criteria:', rubricCriteria.value)
    rubricCriteria.value.forEach(criterion => {
      if (!evaluationScores.value[criterion.id]) {
        evaluationScores.value[criterion.id] = 3 // Default to middle score
        console.log(`Initialized score for ${criterion.id}: 3`)
      } else {
        console.log(`Score already exists for ${criterion.id}: ${evaluationScores.value[criterion.id]}`)
      }
    })
    console.log('Final evaluationScores:', evaluationScores.value)
  } catch (error) {
    console.error('Error loading rubric criteria:', error)
    errorMessage.value = 'Failed to load evaluation criteria'
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Get score label
const getScoreLabel = (score) => {
  return scoreLabels[score] || 'Not Rated'
}

// Get score color
const getScoreColor = (score) => {
  const colors = {
    1: 'red',
    2: 'orange',
    3: 'yellow',
    4: 'light-green',
    5: 'green'
  }
  return colors[score] || 'grey'
}

// Get evaluatee name
const getEvaluateeName = () => {
  const member = teamMembers.value.find(m => m.id === selectedEvaluatee.value)
  return member ? member.name : 'Unknown'
}

// Show confirmation dialog
const showConfirmationDialog = () => {
  if (evaluationForm.value) {
    evaluationForm.value.validate()
  }
  confirmationDialog.value = true
}

// Submit evaluation
const submitEvaluation = async () => {
  submitting.value = true
  
  try {
    const evaluationData = {
      evaluateeId: selectedEvaluatee.value,
      weekId: selectedWeekId.value,
      scores: evaluationScores.value,
      publicComment: publicComment.value.trim() || null,
      privateComment: privateComment.value.trim() || null
    }
    
    await axios.post('/api/students/peer-evaluation', evaluationData)
    
    confirmationDialog.value = false
    successMessage.value = 'Peer evaluation submitted successfully'
    showSuccessMessage.value = true
    
    // Reset form after successful submission
    resetForm()
  } catch (error) {
    console.error('Error submitting evaluation:', error)
    const errorMsg = error.response?.data?.message || 'Failed to submit evaluation'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    submitting.value = false
  }
}

// Reset form
const resetForm = () => {
  selectedEvaluatee.value = null
  evaluationScores.value = {}
  publicComment.value = ''
  privateComment.value = ''
  rubricCriteria.value = []
  confirmationDialog.value = false
}

// Load data on component mount
onMounted(() => {
  loadTeamMembers()
})
</script>

<style scoped>
.v-slider {
  margin-top: 8px;
}

.v-radio-group {
  margin-top: 8px;
}
</style>
