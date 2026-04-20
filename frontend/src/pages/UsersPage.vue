<template>
  <div>
    <h2 class="text-h5 mb-4">Users</h2>
    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3"><v-text-field v-model="form.email" label="Email" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model="form.firstName" label="First Name" /></v-col>
          <v-col cols="12" md="2"><v-text-field v-model="form.lastName" label="Last Name" /></v-col>
          <v-col cols="12" md="2">
            <v-select v-model="form.role" :items="roles" label="Role" />
          </v-col>
          <v-col cols="12" md="3" class="d-flex align-end">
            <v-btn color="primary" @click="createUser">Add User</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :items="users" :headers="headers"></v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const roles = ['ADMIN', 'INSTRUCTOR', 'STUDENT']
const users = ref([])
const form = ref({ email: '', firstName: '', lastName: '', role: 'STUDENT' })

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Email', key: 'email' },
  { title: 'Name', key: 'firstName' },
  { title: 'Role', key: 'role' },
  { title: 'Active', key: 'active' }
]

const load = async () => {
  const res = await api.get('/users')
  users.value = res.data
}

const createUser = async () => {
  await api.post('/users', { ...form.value, active: true })
  form.value = { email: '', firstName: '', lastName: '', role: 'STUDENT' }
  await load()
}

onMounted(load)
</script>
