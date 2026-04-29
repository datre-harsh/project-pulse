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
              v-model="form.studentIds"
              :items="availableStudents"
              item-title="email"
              item-value="id"
              label="Students"
              multiple
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="form.instructorIds"
              :items="availableInstructors"
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
          <v-btn color="primary" :loading="savingTeam" :disabled="!canSaveTeam" @click="submitTeam">{{ form.id ? 'Save Team' : 'Create Team' }}</v-btn>
          <v-btn variant="text" @click="resetForm">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card class="mb-6">
      <v-card-title>Find Teams</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-select v-model="filters.sectionId" :items="sections" item-title="name" item-value="id" label="Filter by Section" clearable />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="filters.teamName" label="Filter by Team Name" clearable />
          </v-col>
          <v-col cols="12" md="4" class="d-flex align-center ga-3">
            <v-btn color="secondary" variant="tonal" @click="loadTeams">Apply Filters</v-btn>
            <v-btn variant="text" @click="resetFilters">Clear</v-btn>
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
        <v-btn size="small" variant="text" color="error" @click="startDelete(item)">Delete</v-btn>
      </template>
    </v-data-table>

    <v-card v-if="selectedTeam" class="mt-6">
      <v-card-title>{{ selectedTeam.name }}</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>Section:</strong> {{ selectedTeam.sectionName }}</div>
        <div class="mb-2"><strong>Description:</strong> {{ selectedTeam.description }}</div>
        <div class="mb-2"><strong>Website:</strong> {{ selectedTeam.websiteUrl || 'Not provided' }}</div>
        <div class="mb-2">
          <strong>Students:</strong>
          <div v-if="selectedTeam.students.length" class="mt-2">
            <div
              v-for="student in selectedTeam.students"
              :key="student.id"
              class="d-flex justify-space-between align-center py-2"
            >
              <span>{{ formatUser(student) }}</span>
              <v-btn size="small" color="error" variant="text" @click="startRemoval(student)">Remove</v-btn>
            </div>
          </div>
          <span v-else> None </span>
        </div>
        <div class="mb-2">
          <strong>Instructors:</strong>
          <div v-if="selectedTeam.instructors.length" class="mt-2">
            <div
              v-for="instructor in selectedTeam.instructors"
              :key="instructor.id"
              class="d-flex justify-space-between align-center py-2"
            >
              <span>{{ formatUser(instructor) }}</span>
              <v-btn size="small" color="error" variant="text" @click="startInstructorRemoval(instructor)">Remove</v-btn>
            </div>
          </div>
          <span v-else> None </span>
        </div>
        <div v-if="latestNotification" class="mt-4">
          <strong>Latest student notification:</strong> {{ latestNotification.message }}
        </div>
        <div v-if="latestInstructorNotification" class="mt-2">
          <strong>Latest instructor notification:</strong> {{ latestInstructorNotification.message }}
        </div>
        <div class="mt-4">
          <v-btn color="error" variant="tonal" @click="startDelete(selectedTeam)">Delete Team</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="confirmDialog" max-width="720">
      <v-card v-if="pendingRemoval">
        <v-card-title>Confirm Student Removal</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Remove <strong>{{ formatUser(pendingRemoval.student) }}</strong> from
            <strong>{{ pendingRemoval.team.name }}</strong>?
          </p>

          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">Current Team Assignment</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="student in pendingRemoval.currentStudents"
                  :key="student.id"
                  :title="formatUser(student)"
                  :subtitle="student.email"
                />
              </v-list>
            </v-col>

            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">Team After Removal</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="student in pendingRemoval.previewStudents"
                  :key="student.id"
                  :title="formatUser(student)"
                  :subtitle="student.email"
                />
                <v-list-item v-if="!pendingRemoval.previewStudents.length" title="No students remain on this team" />
              </v-list>
            </v-col>
          </v-row>

          <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
            You can cancel now to keep the current assignment unchanged.
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelRemoval">Cancel</v-btn>
          <v-btn color="error" :loading="removing" @click="confirmRemoval">Confirm Removal</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="680">
      <v-card v-if="pendingDelete">
        <v-card-title>Confirm Team Deletion</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Delete <strong>{{ pendingDelete.name }}</strong> from
            <strong>{{ pendingDelete.sectionName }}</strong>?
          </p>

          <div class="mb-2"><strong>Students:</strong> {{ deleteStudentNames }}</div>
          <div class="mb-2"><strong>Instructors:</strong> {{ deleteInstructorNames }}</div>
          <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
            Deleting this team will automatically remove its students and instructors from the team. You can cancel now to keep this team unchanged.
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelDelete">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Confirm Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="instructorRemovalDialog" max-width="720">
      <v-card v-if="pendingInstructorRemoval">
        <v-card-title>Confirm Instructor Removal</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Remove <strong>{{ formatUser(pendingInstructorRemoval.instructor) }}</strong> from
            <strong>{{ pendingInstructorRemoval.team.name }}</strong>?
          </p>

          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">Current Team Assignment</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="instructor in pendingInstructorRemoval.currentInstructors"
                  :key="instructor.id"
                  :title="formatUser(instructor)"
                  :subtitle="instructor.email"
                />
              </v-list>
            </v-col>

            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">Team After Removal</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="instructor in pendingInstructorRemoval.previewInstructors"
                  :key="instructor.id"
                  :title="formatUser(instructor)"
                  :subtitle="instructor.email"
                />
                <v-list-item v-if="!pendingInstructorRemoval.previewInstructors.length" title="No instructors remain on this team" />
              </v-list>
            </v-col>
          </v-row>

          <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
            You can cancel now to keep the current assignment unchanged.
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelInstructorRemoval">Cancel</v-btn>
          <v-btn color="error" :loading="removingInstructor" @click="confirmInstructorRemoval">Confirm Removal</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="assignmentDialog" max-width="720">
      <v-card v-if="pendingAssignment">
        <v-card-title>Confirm Instructor Assignment</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Confirm the instructor assignment for <strong>{{ pendingAssignment.teamName }}</strong>?
          </p>

          <div class="mb-4"><strong>Section:</strong> {{ pendingAssignment.sectionName }}</div>

          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">Current Instructors</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="instructor in pendingAssignment.currentInstructors"
                  :key="instructor.id"
                  :title="formatUser(instructor)"
                  :subtitle="instructor.email"
                />
                <v-list-item v-if="!pendingAssignment.currentInstructors.length" title="No instructors assigned yet" />
              </v-list>
            </v-col>

            <v-col cols="12" md="6">
              <div class="text-subtitle-2 mb-2">New Instructors</div>
              <v-list density="compact" border rounded="lg">
                <v-list-item
                  v-for="instructor in pendingAssignment.proposedInstructors"
                  :key="instructor.id"
                  :title="formatUser(instructor)"
                  :subtitle="instructor.email"
                />
              </v-list>
            </v-col>
          </v-row>

          <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
            You can cancel now to keep editing before the assignment is submitted and notifications are sent.
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelAssignment">Cancel</v-btn>
          <v-btn color="primary" :loading="assigningInstructors" @click="confirmAssignment">Confirm Assignment</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../api'

const teams = ref([])
const sections = ref([])
const students = ref([])
const instructors = ref([])
const availableStudents = ref([])
const availableInstructors = ref([])
const selectedTeam = ref(null)
const latestNotification = ref(null)
const latestInstructorNotification = ref(null)
const confirmDialog = ref(false)
const pendingRemoval = ref(null)
const removing = ref(false)
const deleteDialog = ref(false)
const pendingDelete = ref(null)
const deleting = ref(false)
const instructorRemovalDialog = ref(false)
const pendingInstructorRemoval = ref(null)
const removingInstructor = ref(false)
const assignmentDialog = ref(false)
const pendingAssignment = ref(null)
const assigningInstructors = ref(false)
const savingTeam = ref(false)
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
  studentIds: [],
  instructorIds: []
})

const formatUser = (user) => `${user.firstName} ${user.lastName}`
const deleteStudentNames = computed(() =>
  pendingDelete.value?.students?.map(formatUser).join(', ') || 'None'
)
const deleteInstructorNames = computed(() =>
  pendingDelete.value?.instructors?.map(formatUser).join(', ') || 'None'
)

const resetForm = () => {
  form.value = {
    id: null,
    sectionId: null,
    name: '',
    description: '',
    websiteUrl: '',
    studentIds: [],
    instructorIds: []
  }
  availableStudents.value = []
  availableInstructors.value = []
}

const resetFilters = async () => {
  filters.value = {
    sectionId: null,
    teamName: ''
  }
  await loadTeams()
}

const canSaveTeam = computed(() =>
  Boolean(form.value.sectionId && form.value.name.trim() && form.value.instructorIds.length)
)

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
  const [sectionRes, instructorRes, studentRes] = await Promise.all([
    api.get('/sections'),
    api.get('/options/instructors'),
    api.get('/options/students')
  ])
  sections.value = sectionRes.data
  instructors.value = instructorRes.data
  students.value = studentRes.data
}

const viewTeam = async (id) => {
  const res = await api.get(`/teams/${id}`)
  selectedTeam.value = res.data
}

const startRemoval = (student) => {
  if (!selectedTeam.value) {
    return
  }

  // Ralph: Build the post-removal roster locally first so the admin can confirm before submitting.
  const previewStudents = selectedTeam.value.students.filter((member) => member.id !== student.id)
  pendingRemoval.value = {
    team: selectedTeam.value,
    student,
    currentStudents: selectedTeam.value.students,
    previewStudents
  }
  confirmDialog.value = true
  error.value = ''
}

const cancelRemoval = () => {
  confirmDialog.value = false
  pendingRemoval.value = null
}

const confirmRemoval = async () => {
  if (!pendingRemoval.value) {
    return
  }

  removing.value = true
  error.value = ''
  success.value = ''

  try {
    await api.delete(`/teams/${pendingRemoval.value.team.id}/students/${pendingRemoval.value.student.id}`)
    const notificationRes = await api.get(`/users/${pendingRemoval.value.student.id}/notifications`)
    latestNotification.value = notificationRes.data[0] || null
    await viewTeam(pendingRemoval.value.team.id)
    await loadTeams()
    success.value = `${formatUser(pendingRemoval.value.student)} was removed from ${pendingRemoval.value.team.name}.`
    cancelRemoval()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to remove student.'
  } finally {
    removing.value = false
  }
}

const startDelete = async (team) => {
  error.value = ''
  success.value = ''

  if (team.students && team.instructors) {
    pendingDelete.value = team
  } else {
    const res = await api.get(`/teams/${team.id}`)
    pendingDelete.value = res.data
  }

  deleteDialog.value = true
}

const cancelDelete = () => {
  deleteDialog.value = false
  pendingDelete.value = null
}

const startInstructorRemoval = (instructor) => {
  if (!selectedTeam.value) {
    return
  }

  // Ralph: Build the post-removal instructor roster locally first so the admin can confirm before submitting.
  const previewInstructors = selectedTeam.value.instructors.filter((member) => member.id !== instructor.id)
  pendingInstructorRemoval.value = {
    team: selectedTeam.value,
    instructor,
    currentInstructors: selectedTeam.value.instructors,
    previewInstructors
  }
  instructorRemovalDialog.value = true
  error.value = ''
}

const cancelInstructorRemoval = () => {
  instructorRemovalDialog.value = false
  pendingInstructorRemoval.value = null
}

const confirmInstructorRemoval = async () => {
  if (!pendingInstructorRemoval.value) {
    return
  }

  removingInstructor.value = true
  error.value = ''
  success.value = ''

  try {
    await api.delete(`/teams/${pendingInstructorRemoval.value.team.id}/instructors/${pendingInstructorRemoval.value.instructor.id}`)
    const notificationRes = await api.get(`/users/${pendingInstructorRemoval.value.instructor.id}/notifications`)
    latestInstructorNotification.value = notificationRes.data[0] || null
    await viewTeam(pendingInstructorRemoval.value.team.id)
    await loadTeams()
    success.value = `${formatUser(pendingInstructorRemoval.value.instructor)} was removed from ${pendingInstructorRemoval.value.team.name}.`
    cancelInstructorRemoval()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to remove instructor.'
  } finally {
    removingInstructor.value = false
  }
}

const confirmDelete = async () => {
  if (!pendingDelete.value) {
    return
  }

  deleting.value = true
  error.value = ''
  success.value = ''

  try {
    await api.delete(`/teams/${pendingDelete.value.id}`)
    // Ralph: Clear stale detail/form state after deletion so the UI reflects that the team is gone.
    if (selectedTeam.value?.id === pendingDelete.value.id) {
      selectedTeam.value = null
    }
    if (form.value.id === pendingDelete.value.id) {
      resetForm()
    }
    success.value = `${pendingDelete.value.name} was deleted.`
    await loadTeams()
    cancelDelete()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to delete team.'
  } finally {
    deleting.value = false
  }
}

const loadTeamIntoForm = async (id) => {
  await viewTeam(id)
  form.value = {
    id: selectedTeam.value.id,
    sectionId: selectedTeam.value.sectionId,
    name: selectedTeam.value.name,
    description: selectedTeam.value.description,
    websiteUrl: selectedTeam.value.websiteUrl || '',
    studentIds: selectedTeam.value.students.map((student) => student.id),
    instructorIds: selectedTeam.value.instructors.map((instructor) => instructor.id)
  }
}

const arraysEqual = (left, right) => {
  const leftSorted = [...left].sort((a, b) => a - b)
  const rightSorted = [...right].sort((a, b) => a - b)
  return leftSorted.length === rightSorted.length && leftSorted.every((value, index) => value === rightSorted[index])
}

const performTeamSubmit = async () => {
  error.value = ''
  success.value = ''

  const payload = {
    sectionId: form.value.sectionId,
    name: form.value.name,
    description: form.value.description,
    websiteUrl: form.value.websiteUrl,
    studentIds: form.value.studentIds,
    instructorIds: form.value.instructorIds
  }

  try {
    savingTeam.value = true
    if (form.value.id) {
      await api.put(`/teams/${form.value.id}`, payload)
      success.value = pendingAssignment.value
        ? 'Instructor assignments updated and notifications prepared.'
        : 'Team updated.'
      await viewTeam(form.value.id)
    } else {
      await api.post('/teams', payload)
      success.value = pendingAssignment.value
        ? 'Team created and instructor assignments prepared.'
        : 'Team created.'
    }
    resetForm()
    await loadTeams()
    cancelAssignment()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to save team.'
  } finally {
    savingTeam.value = false
  }
}

const submitTeam = async () => {
  error.value = ''
  success.value = ''

  const currentInstructors = form.value.id && selectedTeam.value
    ? selectedTeam.value.instructors
    : []
  const currentInstructorIds = currentInstructors.map((instructor) => instructor.id)

  if (!arraysEqual(currentInstructorIds, form.value.instructorIds)) {
    const section = sections.value.find((entry) => entry.id === form.value.sectionId)
    // Ralph: Show the exact instructor assignment diff before saving so the admin can confirm or correct it.
    pendingAssignment.value = {
      teamName: form.value.name || selectedTeam.value?.name || 'this team',
      sectionName: section?.name || selectedTeam.value?.sectionName || 'Unknown section',
      currentInstructors,
      proposedInstructors: availableInstructors.value.filter((instructor) => form.value.instructorIds.includes(instructor.id))
    }
    assignmentDialog.value = true
    return
  }

  await performTeamSubmit()
}

const cancelAssignment = () => {
  assignmentDialog.value = false
  pendingAssignment.value = null
}

const confirmAssignment = async () => {
  assigningInstructors.value = true
  try {
    await performTeamSubmit()
  } finally {
    assigningInstructors.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadMetadata(), loadTeams()])
})

watch(
  () => form.value.sectionId,
  async (sectionId) => {
    if (!sectionId) {
      availableStudents.value = []
      availableInstructors.value = []
      form.value.studentIds = []
      form.value.instructorIds = []
      return
    }

    const res = await api.get(`/sections/${sectionId}`)
    const allowedStudentIds = new Set(res.data.students.map((student) => student.id))
    const allowedInstructorIds = new Set(res.data.instructors.map((instructor) => instructor.id))
    // Ralph: Keep the team selector limited to students already attached to the chosen section.
    availableStudents.value = students.value.filter((student) => allowedStudentIds.has(student.id))
    availableInstructors.value = instructors.value.filter((instructor) => allowedInstructorIds.has(instructor.id))
    form.value.studentIds = form.value.studentIds.filter((studentId) => allowedStudentIds.has(studentId))
    form.value.instructorIds = form.value.instructorIds.filter((instructorId) => allowedInstructorIds.has(instructorId))
  },
  { immediate: true }
)
</script>
