<template>
  <div>
    <h2 class="text-h5 mb-4">Students</h2>

    <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card class="mb-6">
      <v-card-title>Find Students</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-text-field v-model="filters.firstName" label="First Name" clearable />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="filters.lastName" label="Last Name" clearable />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="filters.email" label="Email" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="filters.sectionName" label="Section Name" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="filters.teamName" label="Team Name" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="filters.sectionId"
              :items="sections"
              item-title="name"
              item-value="id"
              label="Section ID"
              clearable
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="filters.teamId"
              :items="teams"
              item-title="name"
              item-value="id"
              label="Team ID"
              clearable
            />
          </v-col>
        </v-row>

        <div class="d-flex flex-wrap ga-3 mt-2">
          <v-btn color="primary" @click="searchStudents">Search</v-btn>
          <v-btn variant="text" @click="resetFilters">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-alert
      v-if="searched && !students.length && !error"
      type="info"
      class="mb-4"
      variant="tonal"
    >
      No matching students were found. You can invite students from the
      <router-link to="/sections">Sections</router-link>
      page or adjust your search criteria.
    </v-alert>

    <v-data-table
      :headers="headers"
      :items="students"
      item-value="resultKey"
      :items-per-page="10"
    >
      <template #item.sectionName="{ item }">
        {{ item.sectionName || 'Not assigned to a section' }}
      </template>
      <template #item.teamName="{ item }">
        {{ item.teamName || 'Not assigned to a team' }}
      </template>
      <template #item.actions="{ item }">
        <v-btn size="small" variant="text" color="primary" @click="viewStudent(item)">View</v-btn>
      </template>
    </v-data-table>

    <v-card v-if="selectedStudent" class="mt-6">
      <v-card-title>{{ selectedStudent.firstName }} {{ selectedStudent.lastName }}</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>First Name:</strong> {{ selectedStudent.firstName }}</div>
        <div class="mb-2"><strong>Last Name:</strong> {{ selectedStudent.lastName }}</div>
        <div class="mb-2"><strong>Section Name:</strong> {{ selectedStudent.sectionName }}</div>
        <div class="mb-4"><strong>Team Name:</strong> {{ selectedStudent.teamName }}</div>

        <div class="text-subtitle-1 mb-2">Peer Evaluations</div>
        <div class="mb-4">{{ selectedStudent.peerEvaluations.join(', ') }}</div>

        <div class="text-subtitle-1 mb-2">WARs</div>
        <div>{{ selectedStudent.wars.join(', ') }}</div>

        <div class="mt-4">
          <v-btn color="error" variant="tonal" @click="startDeleteSelectedStudent">Delete Student</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="deleteDialog" max-width="720">
      <v-card v-if="pendingDelete">
        <v-card-title>Confirm Student Deletion</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Delete <strong>{{ pendingDelete.firstName }} {{ pendingDelete.lastName }}</strong>?
          </p>

          <div class="mb-2"><strong>Section Name:</strong> {{ pendingDelete.sectionName }}</div>
          <div class="mb-2"><strong>Team Name:</strong> {{ pendingDelete.teamName }}</div>
          <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
            This permanently deletes the student record. Any WARs and peer evaluations tied to this student would also be deleted. You can cancel now to keep this student unchanged.
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelDelete">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Confirm Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'

const students = ref([])
const sections = ref([])
const teams = ref([])
const selectedStudent = ref(null)
const pendingDelete = ref(null)
const deleteDialog = ref(false)
const deleting = ref(false)
const searched = ref(false)
const error = ref('')
const success = ref('')

const headers = [
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Section', key: 'sectionName' },
  { title: 'Team', key: 'teamName' },
  { title: 'Actions', key: 'actions', sortable: false }
]

const filters = ref({
  firstName: '',
  lastName: '',
  email: '',
  sectionName: '',
  teamName: '',
  sectionId: null,
  teamId: null
})

const hasSearchCriteria = computed(() =>
  Object.values(filters.value).some((value) => value !== null && String(value).trim() !== '')
)

const loadMetadata = async () => {
  const [sectionRes, teamRes] = await Promise.all([
    api.get('/sections'),
    api.get('/teams')
  ])
  sections.value = sectionRes.data
  teams.value = teamRes.data
}

const resetFilters = () => {
  filters.value = {
    firstName: '',
    lastName: '',
    email: '',
    sectionName: '',
    teamName: '',
    sectionId: null,
    teamId: null
  }
  students.value = []
  selectedStudent.value = null
  pendingDelete.value = null
  deleteDialog.value = false
  searched.value = false
  error.value = ''
  success.value = ''
}

const searchStudents = async () => {
  error.value = ''
  success.value = ''
  selectedStudent.value = null

  if (!hasSearchCriteria.value) {
    error.value = 'Enter at least one search value before searching for students.'
    students.value = []
    searched.value = false
    return
  }

  const params = {}
  for (const [key, value] of Object.entries(filters.value)) {
    if (value !== null && String(value).trim() !== '') {
      params[key] = value
    }
  }

  try {
    const res = await api.get('/students', { params })
    // Ralph: Give each row a stable table key even when the same student appears in multiple section contexts.
    students.value = res.data.map((student) => ({
      ...student,
      resultKey: `${student.id}-${student.sectionId ?? 'none'}-${student.teamId ?? 'none'}`
    }))
    searched.value = true
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to search students.'
    students.value = []
    searched.value = false
  }
}

const viewStudent = async (student) => {
  error.value = ''
  success.value = ''

  const params = {}
  if (student.sectionId) {
    params.sectionId = student.sectionId
  }
  if (student.teamId) {
    params.teamId = student.teamId
  }

  try {
    // Ralph: Use the row's section/team context so the detail view matches the exact student result the user clicked.
    const res = await api.get(`/students/${student.id}`, { params })
    selectedStudent.value = res.data
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to load student details.'
    selectedStudent.value = null
  }
}

const startDeleteSelectedStudent = () => {
  if (!selectedStudent.value) {
    return
  }

  pendingDelete.value = { ...selectedStudent.value }
  deleteDialog.value = true
  error.value = ''
  success.value = ''
}

const cancelDelete = () => {
  deleteDialog.value = false
  pendingDelete.value = null
}

const confirmDelete = async () => {
  if (!pendingDelete.value) {
    return
  }

  deleting.value = true
  error.value = ''
  success.value = ''

  try {
    await api.delete(`/students/${pendingDelete.value.id}`)
    // Ralph: Remove every row for the deleted student because the same person can appear in multiple section contexts.
    students.value = students.value.filter((student) => student.id !== pendingDelete.value.id)
    selectedStudent.value = null
    success.value = `${pendingDelete.value.firstName} ${pendingDelete.value.lastName} was deleted.`
    cancelDelete()
    await loadMetadata()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to delete student.'
  } finally {
    deleting.value = false
  }
}

onMounted(async () => {
  await loadMetadata()
})
</script>
