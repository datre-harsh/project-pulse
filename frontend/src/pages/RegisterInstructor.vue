<template>
  <v-container fluid class="pa-4">
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <v-card>
          <v-card-title class="text-h5 font-weight-bold text-center">
            <v-icon class="mr-2">mdi-account-plus</v-icon>
            Instructor Registration
          </v-card-title>
          <v-card-subtitle class="text-center">
            Create your instructor account
          </v-card-subtitle>
          
          <v-card-text>
            <v-form ref="registrationForm" v-model="formValid">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="firstName"
                    label="First Name"
                    prepend-inner-icon="mdi-account"
                    variant="outlined"
                    :rules="firstNameRules"
                    required
                  />
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="lastName"
                    label="Last Name"
                    prepend-inner-icon="mdi-account"
                    variant="outlined"
                    :rules="lastNameRules"
                    required
                  />
                </v-col>
              </v-row>
              
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="email"
                    label="Email"
                    prepend-inner-icon="mdi-email"
                    variant="outlined"
                    :rules="emailRules"
                    required
                  />
                </v-col>
              </v-row>
              
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="password"
                    label="Password"
                    prepend-inner-icon="mdi-lock"
                    variant="outlined"
                    :type="showPassword ? 'text' : 'password'"
                    :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    @click:append-inner="showPassword = !showPassword"
                    :rules="passwordRules"
                    required
                  />
                </v-col>
              </v-row>
              
              <v-row>
                <v-col cols="12">
                  <v-btn
                    color="primary"
                    size="large"
                    block
                    :disabled="!formValid || submitting"
                    :loading="submitting"
                    @click="showConfirmationDialog"
                  >
                    Create Account
                  </v-btn>
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Confirmation Dialog -->
    <v-dialog v-model="confirmationDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h6">
          Confirm Registration
        </v-card-title>
        
        <v-card-text>
          <p class="mb-4">
            Please review your account details before confirming:
          </p>
          
          <v-table density="compact">
            <tbody>
              <tr>
                <td><strong>Name:</strong></td>
                <td>{{ firstName }} {{ lastName }}</td>
              </tr>
              <tr>
                <td><strong>Email:</strong></td>
                <td>{{ email }}</td>
              </tr>
            </tbody>
          </v-table>
          
          <v-alert type="info" variant="tonal" class="mt-4">
            <v-alert-title>Important</v-alert-title>
            <div>
              Once you confirm, your instructor account will be created and you will be redirected to the login page.
            </div>
          </v-alert>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="grey"
            variant="text"
            @click="confirmationDialog = false"
          >
            Modify Details
          </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            :loading="submitting"
            @click="submitRegistration"
          >
            Confirm Registration
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
import { useRouter, useRoute } from 'vue-router'
import api from '../api'

const router = useRouter()
const route = useRoute()

// Reactive data
const formValid = ref(false)
const submitting = ref(false)
const showPassword = ref(false)
const confirmationDialog = ref(false)

// Form fields
const firstName = ref('')
const lastName = ref('')
const email = ref('')
const password = ref('')

// Messages
const showSuccessMessage = ref(false)
const showErrorMessage = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Invitation token from URL
const invitationToken = ref('')

// Validation rules
const firstNameRules = [
  v => !!v || 'First name is required',
  v => (v && v.length <= 50) || 'First name must be less than 50 characters'
]

const lastNameRules = [
  v => !!v || 'Last name is required',
  v => (v && v.length <= 50) || 'Last name must be less than 50 characters'
]

const emailRules = [
  v => !!v || 'Email is required',
  v => /.+@.+\..+/.test(v) || 'Email must be valid',
  v => (v && v.length <= 100) || 'Email must be less than 100 characters'
]

const passwordRules = [
  v => !!v || 'Password is required',
  v => (v && v.length >= 6) || 'Password must be at least 6 characters',
  v => (v && v.length <= 100) || 'Password must be less than 100 characters'
]

// Show confirmation dialog
const showConfirmationDialog = () => {
  if (registrationForm.value) {
    registrationForm.value.validate()
  }
  confirmationDialog.value = true
}

// Submit registration
const submitRegistration = async () => {
  submitting.value = true
  
  try {
    const registrationData = {
      firstName: firstName.value.trim(),
      lastName: lastName.value.trim(),
      email: email.value.trim(),
      password: password.value,
      invitationToken: invitationToken.value
    }
    
    const response = await api.post('/instructors/register', registrationData)
    
    confirmationDialog.value = false
    successMessage.value = 'Instructor account created successfully! Redirecting to login...'
    showSuccessMessage.value = true
    
    // Auto-redirect to login page after 2 seconds
    setTimeout(() => {
      router.push('/login')
    }, 2000)
    
  } catch (error) {
    console.error('Error registering instructor:', error)
    const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to register instructor'
    errorMessage.value = errorMsg
    showErrorMessage.value = true
    confirmationDialog.value = false
  } finally {
    submitting.value = false
  }
}

// Get invitation token from URL on component mount
onMounted(() => {
  invitationToken.value = route.params.token || 'mock-token-for-testing'
})
</script>

<style scoped>
.v-card {
  border-radius: 12px;
}

.v-data-table {
  border-radius: 8px;
}
</style>
