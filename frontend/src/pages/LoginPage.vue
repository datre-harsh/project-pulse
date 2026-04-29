<template>
  <div class="login-shell">
    <v-card class="login-card">
      <v-card-title>Sign in</v-card-title>
      <v-card-subtitle>Use your Project Pulse account.</v-card-subtitle>

      <v-card-text>
        <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>

        <v-form @submit.prevent="submit">
          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            autocomplete="email"
            class="mb-2"
          />
          <v-text-field
            v-model="password"
            label="Password"
            autocomplete="current-password"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
            @click:append-inner="showPassword = !showPassword"
          />

          <v-btn type="submit" color="primary" block :loading="loading" class="mt-4">Sign in</v-btn>
        </v-form>

        <div class="text-body-2 text-medium-emphasis mt-5">
          Seeded accounts use password <strong>password</strong>.
        </div>
        <div class="text-body-2 text-medium-emphasis mt-2">
          Try admin@projectpulse.local, instructor@projectpulse.local, or student1@projectpulse.local.
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api, { setCurrentUser } from '../api'

const router = useRouter()
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const submit = async () => {
  error.value = ''
  loading.value = true

  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      password: password.value
    })
    setCurrentUser(response.data)
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to sign in.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  min-height: calc(100vh - 96px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: min(100%, 440px);
}
</style>
