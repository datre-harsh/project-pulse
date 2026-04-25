<template>
  <div>
    <h2 class="text-h5 mb-4">Teams</h2>

    <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card class="mb-6">
      <v-card-title>{{ form.id ? 'Edit Team' : 'Create Team' }}</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-select v-model="form.sectionId" :items="sections" item-title="name" item-value="id" label="Section" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="form.name" label="Team Name" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="form.websiteUrl" label="Team Website URL" />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="form.instructorIds"
              :items="instructors"
              item-title="email"
              item-value="id"
              label="Instructors"
              multiple
            />
          </v-col>
          <v-col cols="12">
            <v-textarea v-model="form.description" label="Team Description" rows="3" />
          </v-col>
        </v-row>

        <div class="d-flex flex-wrap ga-3 mt-2">
          <v-btn color="primary" @click="submitTeam">{{ form.id ? 'Save Team' : 'Create Team' }}</v-btn>
          <v-btn variant="text" @click="resetForm">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card class="mb-6">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-select v-model="filters.sectionId" :items="sections" item-title="name" item-value="id" label="Filter by Section" clearable />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="filters.teamName" label="Filter by Team Name" clearable />
          </v-col>
          <v-col cols="12" md="4" class="d-flex align-center">
            <v-btn color="secondary" variant="tonal" @click="loadTeams">Apply Filters</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="teams" item-value="id">
      <template #item.students="{ item }">
        {{ item.studentNames.join(', ') || 'None' }}
      </template>
      <template #item.instructors="{ item }">
        {{ item.instructorNames.join(', ') || 'None' }}
      </template>
      <template #item.actions="{ item }">
        <v-btn size="small" variant="text" color="primary" @click="viewTeam(item.id)">View</v-btn>
        <v-btn size="small" variant="text" color="secondary" @click="loadTeamIntoForm(item.id)">Edit</v-btn>
      </template>
    </v-data-table>

    <v-card v-if="selectedTeam" class="mt-6">
      <v-card-title>{{ selectedTeam.name }}</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>Section:</strong> {{ selectedTeam.sectionName }}</div>
        <div class="mb-2"><strong>Description:</strong> {{ selectedTeam.description }}</div>
        <div class="mb-2"><strong>Website:</strong> {{ selectedTeam.websiteUrl || 'Not provided' }}</div>
        <div class="mb-2"><strong>Students:</strong> {{ selectedTeam.students.map(formatUser).join(', ') || 'None' }}</div>
        <div><strong>Instructors:</strong> {{ selectedTeam.instructors.map(formatUser).join(', ') || 'None' }}</div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const teams = ref([])
const sections = ref([])
const instructors = ref([])
const selectedTeam = ref(null)
const error = ref('')
const success = ref('')

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Section', key: 'sectionName' },
  { title: 'Name', key: 'name' },
  { title: 'Description', key: 'description' },
  { title: 'Students', key: 'students', sortable: false },
  { title: 'Instructors', key: 'instructors', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false }
]

const filters = ref({
  sectionId: null,
  teamName: ''
})

const form = ref({
  id: null,
  sectionId: null,
  name: '',
  description: '',
  websiteUrl: '',
  instructorIds: []
})

const formatUser = (user) => `${user.firstName} ${user.lastName}`

const resetForm = () => {
  form.value = {
    id: null,
    sectionId: null,
    name: '',
    description: '',
    websiteUrl: '',
    instructorIds: []
  }
}

const loadTeams = async () => {
  const params = {}
  if (filters.value.sectionId) {
    params.sectionId = filters.value.sectionId
  }
  if (filters.value.teamName) {
    params.teamName = filters.value.teamName
  }
  const res = await api.get('/teams', { params })
  teams.value = res.data
}

const loadMetadata = async () => {
  const [sectionRes, instructorRes] = await Promise.all([
    api.get('/sections'),
    api.get('/options/instructors')
  ])
  sections.value = sectionRes.data
  instructors.value = instructorRes.data
}

const viewTeam = async (id) => {
  const res = await api.get(`/teams/${id}`)
  selectedTeam.value = res.data
}

const loadTeamIntoForm = async (id) => {
  await viewTeam(id)
  form.value = {
    id: selectedTeam.value.id,
    sectionId: selectedTeam.value.sectionId,
    name: selectedTeam.value.name,
    description: selectedTeam.value.description,
    websiteUrl: selectedTeam.value.websiteUrl || '',
    instructorIds: selectedTeam.value.instructors.map((instructor) => instructor.id)
  }
}

const submitTeam = async () => {
  error.value = ''
  success.value = ''

  const payload = {
    sectionId: form.value.sectionId,
    name: form.value.name,
    description: form.value.description,
    websiteUrl: form.value.websiteUrl,
    instructorIds: form.value.instructorIds
  }

  try {
    if (form.value.id) {
      await api.put(`/teams/${form.value.id}`, payload)
      success.value = 'Team updated.'
      await viewTeam(form.value.id)
    } else {
      await api.post('/teams', payload)
      success.value = 'Team created.'
    }
    resetForm()
    await loadTeams()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to save team.'
  }
}

onMounted(async () => {
  await Promise.all([loadMetadata(), loadTeams()])
})
</script>
