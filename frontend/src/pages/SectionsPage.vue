<template>
  <div>
    <h2 class="text-h5 mb-4">Sections</h2>

    <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card class="mb-6">
      <v-card-title>{{ form.id ? 'Edit Section' : 'Create Section' }}</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.name" label="Section Name" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="form.startDate" type="date" label="Start Date" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="form.endDate" type="date" label="End Date" />
          </v-col>
          <v-col cols="12" md="2">
            <v-select v-model="form.rubricId" :items="rubrics" item-title="name" item-value="id" label="Rubric" />
          </v-col>
          <v-col cols="12" md="6">
            <v-select
              v-model="form.studentIds"
              :items="students"
              item-title="email"
              item-value="id"
              label="Students In Section"
              multiple
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-select
              v-model="form.instructorIds"
              :items="instructors"
              item-title="email"
              item-value="id"
              label="Instructors In Section"
              multiple
            />
          </v-col>
          <v-col cols="12">
            <v-text-field
              v-model="form.inactiveWeeksText"
              label="Inactive ISO Week Numbers"
              hint="Example: 48, 49, 1"
              persistent-hint
            />
          </v-col>
        </v-row>

        <div class="d-flex flex-wrap ga-3 mt-2">
          <v-btn color="primary" :loading="savingSection" :disabled="!canSaveSection" @click="submitSection">{{ form.id ? 'Save Section' : 'Create Section' }}</v-btn>
          <v-btn variant="text" @click="resetForm">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card class="mb-6">
      <v-card-title>Find Sections</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field v-model="search" label="Search Sections by Name" clearable @update:model-value="loadSections" />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="sections" item-value="id">
      <template #item.teams="{ item }">
        {{ item.teamNames.join(', ') || 'None yet' }}
      </template>
      <template #item.activeWeeks="{ item }">
        {{ item.activeWeekNumbers.join(', ') }}
      </template>
      <template #item.actions="{ item }">
        <v-btn size="small" variant="text" color="primary" @click="viewSection(item.id)">View</v-btn>
        <v-btn size="small" variant="text" color="secondary" @click="loadSectionIntoForm(item.id)">Edit</v-btn>
      </template>
    </v-data-table>

    <v-card v-if="selectedSection" class="mt-6">
      <v-card-title>{{ selectedSection.name }}</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>Dates:</strong> {{ selectedSection.startDate }} to {{ selectedSection.endDate }}</div>
        <div class="mb-2"><strong>Rubric:</strong> {{ selectedSection.rubric.name }}</div>
        <div class="mb-2"><strong>Active Weeks:</strong> {{ selectedSection.activeWeekNumbers.join(', ') || 'None' }}</div>
        <div class="mb-4"><strong>Inactive Weeks:</strong> {{ selectedSection.inactiveWeekNumbers.join(', ') || 'None' }}</div>

        <div class="text-subtitle-1 mb-2">Pending Student Invitations</div>
        <div class="mb-4">{{ selectedSection.pendingInvitations.map((invite) => invite.email).join(', ') || 'No pending invitations' }}</div>

        <div class="text-subtitle-1 mb-2">Unassigned Students</div>
        <div class="mb-4">{{ selectedSection.unassignedStudents.map(formatUser).join(', ') || 'None' }}</div>

        <div class="text-subtitle-1 mb-2">Unassigned Instructors</div>
        <div class="mb-4">{{ selectedSection.unassignedInstructors.map(formatUser).join(', ') || 'None' }}</div>

        <div class="text-subtitle-1 mb-2">Invite Students</div>
        <v-row>
          <v-col cols="12" md="5">
            <v-textarea
              v-model="invitation.emails"
              label="Emails"
              rows="3"
              hint="Separate emails with semicolons"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="invitation.subject" label="Subject" />
          </v-col>
          <v-col cols="12" md="4">
            <v-textarea
              v-model="invitation.message"
              label="Custom Message"
              rows="3"
              hint="Optional. Use [Registration link] if you want to place the invite URL manually."
              persistent-hint
            />
          </v-col>
        </v-row>
        <v-btn color="primary" :loading="sendingInvites" :disabled="!canSendInvites" @click="sendInvites">Send Invitations</v-btn>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'

const sections = ref([])
const rubrics = ref([])
const students = ref([])
const instructors = ref([])
const selectedSection = ref(null)
const search = ref('')
const error = ref('')
const success = ref('')
const savingSection = ref(false)
const sendingInvites = ref(false)

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Name', key: 'name' },
  { title: 'Dates', key: 'startDate' },
  { title: 'Rubric', key: 'rubricName' },
  { title: 'Teams', key: 'teams', sortable: false },
  { title: 'Active Weeks', key: 'activeWeeks', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false }
]

const form = ref({
  id: null,
  name: '',
  startDate: '',
  endDate: '',
  rubricId: null,
  studentIds: [],
  instructorIds: [],
  inactiveWeeksText: ''
})

const invitation = ref({
  emails: '',
  subject: '',
  message: ''
})

const canSaveSection = computed(() =>
  Boolean(form.value.name.trim() && form.value.startDate && form.value.endDate && form.value.rubricId)
)

const canSendInvites = computed(() =>
  Boolean(selectedSection.value && invitation.value.emails.trim())
)

const parseWeeks = (value) =>
  value
    .split(',')
    .map((part) => Number(part.trim()))
    .filter((week) => !Number.isNaN(week))

const formatUser = (user) => `${user.firstName} ${user.lastName}`

const loadRubrics = async () => {
  const res = await api.get('/rubrics')
  rubrics.value = res.data
}

const loadUserOptions = async () => {
  const [studentRes, instructorRes] = await Promise.all([
    api.get('/options/students'),
    api.get('/options/instructors')
  ])
  students.value = studentRes.data
  instructors.value = instructorRes.data
}

const loadSections = async () => {
  const params = {}
  if (search.value) {
    params.name = search.value
  }
  const res = await api.get('/sections', { params })
  sections.value = res.data
}

const viewSection = async (id) => {
  const res = await api.get(`/sections/${id}`)
  selectedSection.value = res.data
}

const loadSectionIntoForm = async (id) => {
  await viewSection(id)
  form.value = {
    id: selectedSection.value.id,
    name: selectedSection.value.name,
    startDate: selectedSection.value.startDate,
    endDate: selectedSection.value.endDate,
    rubricId: selectedSection.value.rubric.id,
    studentIds: selectedSection.value.students.map((student) => student.id),
    instructorIds: selectedSection.value.instructors.map((instructor) => instructor.id),
    inactiveWeeksText: selectedSection.value.inactiveWeekNumbers.join(', ')
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    startDate: '',
    endDate: '',
    rubricId: null,
    studentIds: [],
    instructorIds: [],
    inactiveWeeksText: ''
  }
}

const submitSection = async () => {
  error.value = ''
  success.value = ''

  const payload = {
    name: form.value.name,
    startDate: form.value.startDate,
    endDate: form.value.endDate,
    rubricId: form.value.rubricId,
    studentIds: form.value.studentIds,
    instructorIds: form.value.instructorIds,
    inactiveWeekNumbers: parseWeeks(form.value.inactiveWeeksText)
  }

  try {
    savingSection.value = true
    if (form.value.id) {
      await api.put(`/sections/${form.value.id}`, payload)
      success.value = 'Section updated.'
      await viewSection(form.value.id)
    } else {
      await api.post('/sections', payload)
      success.value = 'Section created.'
    }
    resetForm()
    await loadSections()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to save section.'
  } finally {
    savingSection.value = false
  }
}

const sendInvites = async () => {
  if (!selectedSection.value) {
    return
  }

  error.value = ''
  success.value = ''

  try {
    sendingInvites.value = true
    await api.post(`/sections/${selectedSection.value.id}/student-invitations`, {
      sectionId: selectedSection.value.id,
      emails: invitation.value.emails,
      subject: invitation.value.subject,
      message: invitation.value.message
    })
    success.value = 'Student invitations prepared.'
    invitation.value = { emails: '', subject: '', message: '' }
    await viewSection(selectedSection.value.id)
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to send invitations.'
  } finally {
    sendingInvites.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadRubrics(), loadSections(), loadUserOptions()])
})
</script>
