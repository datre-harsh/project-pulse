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
import api, { getCurrentUser } from '../api'

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
const currentMembership = ref(null)

const defaultRubricCriteria = () => ([
  { id: 'quality', name: 'Quality of work', description: 'How do you rate the quality of this teammate\'s work?' },
  { id: 'participation', name: 'Participation', description: 'How actively did your teammate participate in team activities?' },
  { id: 'communication', name: 'Communication', description: 'How effective was your teammate in communication?' }
])

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
  if (!selectedEvaluatee.value) return false
  if (!selectedWeekId.value) return false
  if (rubricCriteria.value.length === 0) return false
  
  // Check if all criteria have scores
  return rubricCriteria.value.every(criterion => {
    const score = evaluationScores.value[criterion.id]
    return score && score >= 1 && score <= 5
  })
})

// Load team members
const loadTeamMembers = async () => {
  loading.value = true
  try {
    const currentUser = getCurrentUser()
    if (!currentUser?.id) {
      throw new Error('Please sign in before submitting a peer evaluation')
    }
    if (currentUser.role !== 'STUDENT') {
      throw new Error('Only student accounts can submit peer evaluations')
    }

    const response = await api.get('/students')
    const studentMemberships = response.data.filter(student => student.id === currentUser.id)
    const membership = studentMemberships.find(student => student.teamId) || studentMemberships[0] || null

    if (!membership?.teamId || !membership?.sectionId) {
      currentMembership.value = null
      teamMembers.value = []
      errorMessage.value = 'You must be assigned to a team before you can submit peer evaluations'
      showErrorMessage.value = true
      return
    }

    currentMembership.value = membership
    teamMembers.value = response.data
      .filter(student => student.teamId === membership.teamId && student.id !== currentUser.id)
      .map(student => ({
        id: student.id,
        name: `${student.firstName} ${student.lastName}`
      }))
      .sort((left, right) => left.name.localeCompare(right.name))

    if (teamMembers.value.length === 0) {
      errorMessage.value = 'No teammates are currently assigned to your team'
      showErrorMessage.value = true
    }
  } catch (error) {
    console.error('Error loading team members:', error)
    errorMessage.value = error.response?.data?.error || error.message || 'Failed to load team members'
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
    if (!currentMembership.value?.sectionId) {
      throw new Error('Unable to determine your section for rubric loading')
    }

    const response = await api.get('/rubric', {
      params: { sectionId: currentMembership.value.sectionId }
    })
    rubricCriteria.value = response.data.filter(criterion => criterion.active)
    if (!rubricCriteria.value.length) {
      // Ralph: Keep the student demo unblocked when a section has not been linked to a rubric yet.
      rubricCriteria.value = defaultRubricCriteria()
    }
    
    // Initialize scores for new criteria
    rubricCriteria.value.forEach(criterion => {
      if (!evaluationScores.value[criterion.id]) {
        evaluationScores.value[criterion.id] = 3 // Default to middle score
      }
    })
  } catch (error) {
    console.error('Error loading rubric criteria:', error)
    rubricCriteria.value = defaultRubricCriteria()
    errorMessage.value = 'Using default evaluation criteria because the section rubric could not be loaded'
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
    
    console.log('Submitting evaluation data:', evaluationData)
    
    const response = await api.post('/students/peer-evaluation', evaluationData)
    
    console.log('Submission successful:', response.data)
    
    confirmationDialog.value = false
    successMessage.value = 'Peer evaluation submitted successfully'
    showSuccessMessage.value = true
    
    // Reset form after successful submission
    resetForm()
  } catch (error) {
    console.error('Error submitting evaluation:', error)
    console.error('Error response:', error.response)
    console.error('Error status:', error.response?.status)
    console.error('Error data:', error.response?.data)
    
    const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message || 'Failed to submit evaluation'
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
