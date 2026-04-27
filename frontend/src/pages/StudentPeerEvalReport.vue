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
                John Doe - Peer evaluations with instructor visibility
              </v-card-subtitle>
              
              <v-card-text>
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
                John Doe - Weekly Activity Report
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

// Initialize reportData as empty arrays to prevent crashes
const isReady = ref(false)
const activeTab = ref('peer-eval')
const peerEvalData = ref([])
const warData = ref([])

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

// Hardcode mock data in onMounted for immediate display
onMounted(() => {
  try {
    console.log('=== Student Reports component mounting ===')
    
    // UC-33 Mock Data for John Doe (Long IDs)
    peerEvalData.value = [
      {
        weekRange: '02-12-2024 - 02-18-2024',
        overallGrade: '54/60',
        evaluations: [
          {
            evaluatorName: 'Tim Smith',
            publicComments: 'Good work on the project implementation.',
            privateComments: 'John is doing well but needs to improve documentation.'
          },
          {
            evaluatorName: 'Lily Fisher',
            publicComments: 'Need to work harder on testing.',
            privateComments: 'Dr. Wei, I need to talk more about John\'s code quality.'
          }
        ]
      },
      {
        weekRange: '02-19-2024 - 02-25-2024',
        overallGrade: '55/60',
        evaluations: [
          {
            evaluatorName: 'Bob Johnson',
            publicComments: 'Excellent progress this week.',
            privateComments: 'John has shown significant improvement in collaboration.'
          }
        ]
      }
    ]
    
    // UC-34 Mock Data for John Doe (Long IDs)
    warData.value = [
      {
        weekRange: '02-12-2024 - 02-18-2024',
        activities: [
          {
            category: 'Development',
            plannedActivity: 'Feature Implementation',
            description: 'Implemented user authentication module',
            plannedHours: '8',
            actualHours: '10',
            status: 'Completed'
          },
          {
            category: 'Documentation',
            plannedActivity: 'API Documentation',
            description: 'Documented REST API endpoints',
            plannedHours: '3',
            actualHours: '2',
            status: 'Completed'
          },
          {
            category: 'Testing',
            plannedActivity: 'Unit Testing',
            description: 'Wrote unit tests for service layer',
            plannedHours: '4',
            actualHours: '4',
            status: 'Completed'
          }
        ]
      },
      {
        weekRange: '02-19-2024 - 02-25-2024',
        activities: [
          {
            category: 'Development',
            plannedActivity: 'Bug Fixes',
            description: 'Fixed critical bugs in payment module',
            plannedHours: '6',
            actualHours: '8',
            status: 'Completed'
          },
          {
            category: 'Meeting',
            plannedActivity: 'Sprint Planning',
            description: 'Attended sprint planning meeting',
            plannedHours: '2',
            actualHours: '2',
            status: 'Completed'
          },
          {
            category: 'Code Review',
            plannedActivity: 'Peer Review',
            description: 'Reviewed team member pull requests',
            plannedHours: '3',
            actualHours: '3',
            status: 'In Progress'
          }
        ]
      }
    ]
    
    // Mark component as ready
    isReady.value = true
    console.log('=== Student Reports component ready with mock data ===')
    
  } catch (error) {
    console.error('Error mounting component:', error)
    // Still mark as ready to prevent infinite loading
    isReady.value = true
  }
})
</script>

<style scoped>
.v-table td {
  vertical-align: middle !important;
}
</style>
