<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="elevation-8">
          <v-card-title class="text-center pa-6">
            <h2 class="text-h4 font-weight-bold text-primary">Student Registration</h2>
          </v-card-title>

          <v-card-text class="pa-6">
            <!-- Registration Form -->
            <v-form v-if="!showConfirmation" ref="registrationForm" v-model="formValid" @submit.prevent>
              <v-text-field
                v-model="form.firstName"
                label="First Name"
                :rules="firstNameRules"
                variant="outlined"
                prepend-inner-icon="mdi-account"
                class="mb-4"
                required
              />

              <v-text-field
                v-model="form.lastName"
                label="Last Name"
                :rules="lastNameRules"
                variant="outlined"
                prepend-inner-icon="mdi-account"
                class="mb-4"
                required
              />

              <v-text-field
                v-model="form.email"
                label="Email"
                :rules="emailRules"
                variant="outlined"
                prepend-inner-icon="mdi-email"
                type="email"
                class="mb-4"
                required
              />

              <v-text-field
                v-model="form.password"
                label="Password"
                :rules="passwordRules"
                variant="outlined"
                prepend-inner-icon="mdi-lock"
                :type="showPassword ? 'text' : 'password'"
                :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click:append-inner="showPassword = !showPassword"
                class="mb-4"
                required
              />

              <v-text-field
                v-model="form.invitationToken"
                label="Invitation Token"
                :rules="tokenRules"
                variant="outlined"
                prepend-inner-icon="mdi-ticket"
                class="mb-6"
                required
              />

              <v-btn
                color="primary"
                size="large"
                block
                :disabled="!formValid || loading"
                :loading="loading"
                @click="startRegistration"
              >
                Register Account
              </v-btn>
            </v-form>

            <!-- Confirmation Dialog -->
            <div v-else>
              <v-alert type="info" variant="tonal" class="mb-4">
                Please review your information before submitting:
              </v-alert>

              <v-list density="compact" class="mb-6">
                <v-list-item>
                  <v-list-item-title class="font-weight-bold">Name:</v-list-item-title>
                  <v-list-item-subtitle>{{ form.firstName }} {{ form.lastName }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title class="font-weight-bold">Email:</v-list-item-title>
                  <v-list-item-subtitle>{{ form.email }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title class="font-weight-bold">Role:</v-list-item-title>
                  <v-list-item-subtitle>Student</v-list-item-subtitle>
                </v-list-item>
              </v-list>

              <v-btn
                color="success"
                size="large"
                block
                :loading="loading"
                @click="confirmRegistration"
                class="mb-3"
              >
                Confirm Registration
              </v-btn>

              <v-btn
                variant="outlined"
                size="large"
                block
                :disabled="loading"
                @click="cancelConfirmation"
              >
                Back to Edit
              </v-btn>
            </div>

            <!-- Error and Success Messages -->
            <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              class="mt-4"
              closable
              @input="error = ''"
            >
              {{ error }}
            </v-alert>

            <v-alert
              v-if="success"
              type="success"
              variant="tonal"
              class="mt-4"
              closable
              @input="success = ''"
            >
              {{ success }}
            </v-alert>
          </v-card-text>

          <v-card-actions class="pa-6 pt-0">
            <v-btn
              variant="text"
              block
              @click="$router.push('/login')"
            >
              Already have an account? Login
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'

const router = useRouter()

// Form state
const registrationForm = ref(null)
const formValid = ref(false)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmation = ref(false)
const error = ref('')
const success = ref('')

// Form data
const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  invitationToken: ''
})

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
  v => /.+@.+\..+/.test(v) || 'Email must be valid'
]

const passwordRules = [
  v => !!v || 'Password is required',
  v => (v && v.length >= 6) || 'Password must be at least 6 characters long'
]

const tokenRules = [
  v => !!v || 'Invitation token is required'
]

// Methods
const startRegistration = async () => {
  if (!registrationForm.value.validate()) {
    return
  }
  showConfirmation.value = true
  error.value = ''
}

const cancelConfirmation = () => {
  showConfirmation.value = false
}

const confirmRegistration = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const response = await api.post('/students/register', form)
    success.value = response.data.message || 'Registration successful! Redirecting to login...'
    
    // Redirect to login page after successful registration
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err) {
    error.value = err.response?.data?.error || 'Registration failed. Please try again.'
    showConfirmation.value = false
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
