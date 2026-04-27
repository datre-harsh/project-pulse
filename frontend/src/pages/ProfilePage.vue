<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold">
            <v-icon class="mr-2">mdi-account-circle</v-icon>
            Profile Settings
          </v-card-title>
          <v-card-subtitle>
            Manage your account information
          </v-card-subtitle>
          
          <v-card-text>
            <!-- Current Profile Information -->
            <v-row v-if="!isEditing">
              <v-col cols="12" md="6">
                <v-list>
                  <v-list-item>
                    <template v-slot:prepend>
                      <v-icon>mdi-account</v-icon>
                    </template>
                    <v-list-item-title>First Name</v-list-item-title>
                    <v-list-item-subtitle>{{ currentProfile.firstName }}</v-list-item-subtitle>
                  </v-list-item>
                  
                  <v-list-item>
                    <template v-slot:prepend>
                      <v-icon>mdi-account</v-icon>
                    </template>
                    <v-list-item-title>Last Name</v-list-item-title>
                    <v-list-item-subtitle>{{ currentProfile.lastName }}</v-list-item-subtitle>
                  </v-list-item>
                  
                  <v-list-item>
                    <template v-slot:prepend>
                      <v-icon>mdi-email</v-icon>
                    </template>
                    <v-list-item-title>Email</v-list-item-title>
                    <v-list-item-subtitle>{{ currentProfile.email }}</v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
            
            <!-- Edit Form -->
            <v-form v-if="isEditing" ref="profileForm" v-model="formValid">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="editedProfile.firstName"
                    label="First Name"
                    :rules="[v => !!v || 'First name is required', v => (v && v.length <= 50) || 'First name must be less than 50 characters']"
                    prepend-inner-icon="mdi-account"
                    variant="outlined"
                    class="mb-4"
                  />
                  
                  <v-text-field
                    v-model="editedProfile.lastName"
                    label="Last Name"
                    :rules="[v => !!v || 'Last name is required', v => (v && v.length <= 50) || 'Last name must be less than 50 characters']"
                    prepend-inner-icon="mdi-account"
                    variant="outlined"
                    class="mb-4"
                  />
                  
                  <v-text-field
                    v-model="editedProfile.email"
                    label="Email"
                    :rules="[v => !!v || 'Email is required', v => /.+@.+\..+/.test(v) || 'Email must be valid']"
                    prepend-inner-icon="mdi-email"
                    variant="outlined"
                    class="mb-4"
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>
          
          <v-card-actions>
            <v-spacer />
            <v-btn
              v-if="!isEditing"
              color="primary"
              prepend-icon="mdi-pencil"
              @click="startEditing"
            >
              Edit Profile
            </v-btn>
            
            <template v-if="isEditing">
              <v-btn
                color="grey"
                variant="text"
                @click="cancelEditing"
              >
                Cancel
              </v-btn>
              <v-btn
                color="primary"
                prepend-icon="mdi-content-save"
                :disabled="!formValid"
                @click="showConfirmationDialog"
              >
                Save Changes
              </v-btn>
            </template>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Confirmation Dialog -->
    <v-dialog v-model="confirmationDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h6">
          Confirm Profile Update
        </v-card-title>
        <v-card-text>
          Please review your changes before saving:
          
          <v-list class="mt-4">
            <v-list-item>
              <v-list-item-title>First Name:</v-list-item-title>
              <v-list-item-subtitle>
                <span class="text-red">{{ currentProfile.firstName }}</span>
                <v-icon class="mx-2">mdi-arrow-right</v-icon>
                <span class="text-green">{{ editedProfile.firstName }}</span>
              </v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
              <v-list-item-title>Last Name:</v-list-item-title>
              <v-list-item-subtitle>
                <span class="text-red">{{ currentProfile.lastName }}</span>
                <v-icon class="mx-2">mdi-arrow-right</v-icon>
                <span class="text-green">{{ editedProfile.lastName }}</span>
              </v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
              <v-list-item-title>Email:</v-list-item-title>
              <v-list-item-subtitle>
                <span class="text-red">{{ currentProfile.email }}</span>
                <v-icon class="mx-2">mdi-arrow-right</v-icon>
                <span class="text-green">{{ editedProfile.email }}</span>
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
          
          <v-alert
            type="info"
            variant="tonal"
            class="mt-4"
          >
            Are you sure you want to save these changes?
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
            :loading="isSaving"
            @click="saveProfile"
          >
            Confirm & Save
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
import axios from 'axios'

// Reactive data
const isEditing = ref(false)
const formValid = ref(false)
const isSaving = ref(false)
const confirmationDialog = ref(false)
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const profileForm = ref(null)

// Profile data
const currentProfile = ref({
  firstName: '',
  lastName: '',
  email: ''
})

const editedProfile = ref({
  firstName: '',
  lastName: '',
  email: ''
})

// Load current profile data
const loadProfile = async () => {
  try {
    // Use the new GET endpoint to load current student profile
    const response = await axios.get('/api/students/profile')
    const profileData = response.data
    
    currentProfile.value = {
      firstName: profileData.firstName,
      lastName: profileData.lastName,
      email: profileData.email
    }
    
    // Reset edited profile to current values
    editedProfile.value = { ...currentProfile.value }
  } catch (error) {
    console.error('Error loading profile:', error)
    errorMessage.value = 'Failed to load profile information'
    showErrorMessage.value = true
  }
}

// Start editing
const startEditing = () => {
  editedProfile.value = { ...currentProfile.value }
  isEditing.value = true
}

// Cancel editing
const cancelEditing = () => {
  editedProfile.value = { ...currentProfile.value }
  isEditing.value = false
}

// Show confirmation dialog
const showConfirmationDialog = () => {
  if (profileForm.value) {
    profileForm.value.validate()
  }
  if (formValid.value) {
    confirmationDialog.value = true
  }
}

// Save profile
const saveProfile = async () => {
  isSaving.value = true
  confirmationDialog.value = false
  
  try {
    // TODO: Get actual student ID from authentication
    const studentId = 1
    
    const response = await axios.put('/api/students/profile', {
      firstName: editedProfile.value.firstName,
      lastName: editedProfile.value.lastName,
      email: editedProfile.value.email
    })
    
    // Update current profile with saved data
    currentProfile.value = {
      firstName: response.data.firstName,
      lastName: response.data.lastName,
      email: response.data.email
    }
    
    // Exit editing mode
    isEditing.value = false
    
    // Show success message
    successMessage.value = response.data.message || 'Profile updated successfully'
    showSuccessMessage.value = true
    
  } catch (error) {
    console.error('Error saving profile:', error)
    const errorMsg = error.response?.data?.message || 'Failed to update profile'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
  } finally {
    isSaving.value = false
  }
}

// Load profile on component mount
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.text-red {
  color: #f44336;
  text-decoration: line-through;
}

.text-green {
  color: #4caf50;
  font-weight: 500;
}
</style>
