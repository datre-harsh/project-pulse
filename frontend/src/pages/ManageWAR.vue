<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-chart-line</v-icon>
            Weekly Activity Report (WAR)
          </v-card-title>
          <v-card-subtitle>
            Manage your weekly activities and track progress
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Week Selector -->
            <v-row class="mb-4">
              <v-col cols="12" md="4">
                <v-select
                  v-model="selectedWeekId"
                  :items="weekOptions"
                  label="Select Week"
                  prepend-inner-icon="mdi-calendar-week"
                  variant="outlined"
                  @update:model-value="loadActivities"
                />
              </v-col>
              <v-col cols="12" md="8" class="d-flex align-center justify-end">
                <v-btn
                  color="primary"
                  prepend-icon="mdi-plus"
                  @click="openAddDialog"
                >
                  Add Activity
                </v-btn>
              </v-col>
            </v-row>
            
            <!-- Activities Table -->
            <v-data-table
              :headers="headers"
              :items="activities"
              :loading="loading"
              class="elevation-1"
            >
              <template v-slot:item.category="{ item }">
                <v-chip
                  :color="getCategoryColor(item.category)"
                  variant="tonal"
                  size="small"
                >
                  {{ formatCategory(item.category) }}
                </v-chip>
              </template>
              
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  variant="tonal"
                  size="small"
                >
                  {{ formatStatus(item.status) }}
                </v-chip>
              </template>
              
              <template v-slot:item.plannedHours="{ item }">
                <span class="font-weight-medium">{{ item.plannedHours }}h</span>
              </template>
              
              <template v-slot:item.actualHours="{ item }">
                <span class="font-weight-medium">{{ item.actualHours }}h</span>
              </template>
              
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon="mdi-pencil"
                  size="small"
                  variant="text"
                  color="primary"
                  @click="openEditDialog(item)"
                />
                <v-btn
                  icon="mdi-delete"
                  size="small"
                  variant="text"
                  color="error"
                  @click="openDeleteDialog(item)"
                />
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Add/Edit Activity Dialog -->
    <v-dialog v-model="activityDialog" max-width="600">
      <v-card>
        <v-card-title class="text-h6">
          {{ isEditing ? 'Edit Activity' : 'Add Activity' }}
        </v-card-title>
        
        <v-card-text>
          <v-form ref="activityFormRef" v-model="formValid">
            <v-row>
              <v-col cols="12" md="6">
                <v-select
                  v-model="activityFormData.category"
                  :items="categoryOptions"
                  label="Category"
                  :rules="[v => !!v || 'Category is required']"
                  variant="outlined"
                  prepend-inner-icon="mdi-tag"
                />
              </v-col>
              
              <v-col cols="12" md="6">
                <v-select
                  v-model="activityFormData.status"
                  :items="statusOptions"
                  label="Status"
                  :rules="[v => !!v || 'Status is required']"
                  variant="outlined"
                  prepend-inner-icon="mdi-flag"
                />
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="activityFormData.description"
                  label="Description"
                  :rules="[v => !!v || 'Description is required', v => (v && v.trim().length > 0) || 'Description cannot be empty']"
                  variant="outlined"
                  prepend-inner-icon="mdi-text"
                  rows="3"
                  auto-grow
                />
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="activityFormData.plannedHours"
                  label="Planned Hours"
                  type="number"
                  step="0.5"
                  min="0.1"
                  :rules="[v => !!v || 'Planned hours is required', v => v > 0 || 'Planned hours must be positive']"
                  variant="outlined"
                  prepend-inner-icon="mdi-clock-outline"
                />
              </v-col>
              
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="activityFormData.actualHours"
                  label="Actual Hours"
                  type="number"
                  step="0.5"
                  min="0"
                  :rules="[v => v >= 0 || 'Actual hours cannot be negative']"
                  variant="outlined"
                  prepend-inner-icon="mdi-clock-check-outline"
                />
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey"
            variant="text"
            @click="activityDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            :disabled="!formValid"
            :loading="saving"
            @click="saveActivity"
          >
            {{ isEditing ? 'Update' : 'Add' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6">
          Delete Activity
        </v-card-title>
        
        <v-card-text>
          Are you sure you want to delete this activity?
          <div class="mt-2">
            <strong>{{ selectedActivity?.description }}</strong>
          </div>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey"
            variant="text"
            @click="deleteDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="error"
            :loading="deleting"
            @click="deleteActivity"
          >
            Delete
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
import { ref, onMounted } from 'vue'
import api from '../api'

// Reactive data
const loading = ref(false)
const activities = ref([])
const selectedWeekId = ref('2024-week1') // Default to current week
const activityDialog = ref(false)
const deleteDialog = ref(false)
const isEditing = ref(false)
const formValid = ref(false)
const saving = ref(false)
const deleting = ref(false)
const selectedActivity = ref(null)
const activityFormRef = ref(null)

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Form data
const activityFormData = ref({
  category: '',
  description: '',
  plannedHours: null,
  actualHours: null,
  status: '',
  weekId: ''
})

// Table headers
const headers = [
  { title: 'Category', key: 'category', sortable: true },
  { title: 'Description', key: 'description', sortable: true },
  { title: 'Planned Hours', key: 'plannedHours', sortable: true },
  { title: 'Actual Hours', key: 'actualHours', sortable: true },
  { title: 'Status', key: 'status', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, width: 100 }
]

// Week options (mock data for demo)
const weekOptions = [
  { title: 'Week 1', value: '2024-week1' },
  { title: 'Week 2', value: '2024-week2' },
  { title: 'Week 3', value: '2024-week3' },
  { title: 'Week 4', value: '2024-week4' }
]

// Category options
const categoryOptions = [
  { title: 'Development', value: 'DEVELOPMENT' },
  { title: 'Testing', value: 'TESTING' },
  { title: 'Bug Fix', value: 'BUGFIX' },
  { title: 'Communication', value: 'COMMUNICATION' },
  { title: 'Documentation', value: 'DOCUMENTATION' },
  { title: 'Design', value: 'DESIGN' },
  { title: 'Planning', value: 'PLANNING' },
  { title: 'Learning', value: 'LEARNING' },
  { title: 'Deployment', value: 'DEPLOYMENT' },
  { title: 'Support', value: 'SUPPORT' },
  { title: 'Miscellaneous', value: 'MISCELLANEOUS' }
]

// Status options
const statusOptions = [
  { title: 'In Progress', value: 'IN_PROGRESS' },
  { title: 'Under Testing', value: 'UNDER_TESTING' },
  { title: 'Done', value: 'DONE' }
]

// Load activities
const loadActivities = async () => {
  loading.value = true
  try {
    const response = await api.get('/students/war', {
      params: { weekId: selectedWeekId.value }
    })
    activities.value = response.data
  } catch (error) {
    console.error('Error loading activities:', error)
    errorMessage.value = 'Failed to load activities'
    showErrorMessage.value = true
  } finally {
    loading.value = false
  }
}

// Open add dialog
const openAddDialog = () => {
  isEditing.value = false
  activityFormData.value = {
    category: '',
    description: '',
    plannedHours: null,
    actualHours: null,
    status: '',
    weekId: selectedWeekId.value
  }
  activityDialog.value = true
}

// Open edit dialog
const openEditDialog = (activity) => {
  isEditing.value = true
  selectedActivity.value = activity
  activityFormData.value = {
    category: activity.category,
    description: activity.description,
    plannedHours: activity.plannedHours,
    actualHours: activity.actualHours,
    status: activity.status,
    weekId: activity.weekId
  }
  activityDialog.value = true
}

// Open delete dialog
const openDeleteDialog = (activity) => {
  selectedActivity.value = activity
  deleteDialog.value = true
}

// Save activity
const saveActivity = async () => {
  if (activityFormRef.value) {
    const result = await activityFormRef.value.validate()
    if (!result.valid) {
      return
    }
  }

  if (!formValid.value) return
  
  saving.value = true
  
  try {
    if (isEditing.value) {
      await api.put(`/students/war/${selectedActivity.value.id}`, activityFormData.value)
      successMessage.value = 'Activity updated successfully'
    } else {
      await api.post('/students/war', activityFormData.value)
      successMessage.value = 'Activity added successfully'
    }
    
    activityDialog.value = false
    showSuccessMessage.value = true
    loadActivities()
  } catch (error) {
    console.error('Error saving activity:', error)
    const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to save activity'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    saving.value = false
  }
}

// Delete activity
const deleteActivity = async () => {
  deleting.value = true
  
  try {
    await api.delete(`/students/war/${selectedActivity.value.id}`)
    deleteDialog.value = false
    successMessage.value = 'Activity deleted successfully'
    showSuccessMessage.value = true
    loadActivities()
  } catch (error) {
    console.error('Error deleting activity:', error)
    const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to delete activity'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    deleting.value = false
  }
}

// Format category for display
const formatCategory = (category) => {
  return category.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}

// Format status for display
const formatStatus = (status) => {
  return status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}

// Get category color
const getCategoryColor = (category) => {
  const colors = {
    DEVELOPMENT: 'blue',
    TESTING: 'green',
    BUGFIX: 'red',
    COMMUNICATION: 'purple',
    DOCUMENTATION: 'orange',
    DESIGN: 'pink',
    PLANNING: 'indigo',
    LEARNING: 'teal',
    DEPLOYMENT: 'cyan',
    SUPPORT: 'amber',
    MISCELLANEOUS: 'grey'
  }
  return colors[category] || 'grey'
}

// Get status color
const getStatusColor = (status) => {
  const colors = {
    IN_PROGRESS: 'blue',
    UNDER_TESTING: 'orange',
    DONE: 'green'
  }
  return colors[status] || 'grey'
}

// Load activities on component mount
onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.v-data-table {
  border-radius: 8px;
}
</style>
