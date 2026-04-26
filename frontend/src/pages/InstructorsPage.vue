<template>
  <div>
    <h2 class="text-h5 mb-4">Instructors</h2>

    <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card class="mb-6">
      <v-card-title>Find Instructors</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-text-field v-model="searchForm.firstName" label="First Name" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="searchForm.lastName" label="Last Name" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="searchForm.teamName" label="Team Name" clearable />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="searchForm.status"
              :items="statusOptions"
              item-title="title"
              item-value="value"
              label="Status"
              clearable
            />
          </v-col>
        </v-row>

        <div class="d-flex flex-wrap ga-3 mt-2">
          <v-btn color="primary" @click="searchInstructors">Search</v-btn>
          <v-btn variant="text" @click="clearSearch">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card class="mb-6">
      <v-card-title>Matching Instructors</v-card-title>
      <v-card-text v-if="searchPerformed && !searchResults.length" class="text-body-1">
        No matching instructors were found. You can use the invitation form below to invite instructors to register.
      </v-card-text>
      <v-data-table
        v-else
        :headers="searchHeaders"
        :items="searchResults"
        item-value="resultKey"
      >
        <template #item.actions="{ item }">
          <v-btn size="small" variant="text" color="primary" @click="viewInstructor(item)">View</v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-card v-if="selectedInstructor" class="mb-6">
      <v-card-title>{{ selectedInstructor.firstName }} {{ selectedInstructor.lastName }}</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>First Name:</strong> {{ selectedInstructor.firstName }}</div>
        <div class="mb-2"><strong>Last Name:</strong> {{ selectedInstructor.lastName }}</div>
        <div class="mb-4"><strong>Status:</strong> {{ selectedInstructor.status }}</div>

        <div class="text-subtitle-1 mb-2">Supervised Teams</div>
        <div v-if="selectedInstructor.supervisedTeams.length">
          <div
            v-for="section in selectedInstructor.supervisedTeams"
            :key="section.sectionId"
            class="mb-3"
          >
            <div><strong>{{ section.sectionName }}</strong></div>
            <div>{{ section.teamNames.join(', ') }}</div>
          </div>
        </div>
        <div v-else>
          This instructor is not supervising any teams.
        </div>

        <div v-if="selectedInstructor.status === 'Active'" class="mt-4">
          <v-btn color="warning" variant="tonal" @click="startDeactivateInstructor">Deactivate Instructor</v-btn>
        </div>
        <div v-if="selectedInstructor.status === 'Deactivated'" class="mt-4">
          <v-btn color="success" variant="tonal" @click="startReactivateInstructor">Reactivate Instructor</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="deactivateDialog" max-width="720">
      <v-card v-if="selectedInstructor">
        <v-card-title>Confirm Instructor Deactivation</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Deactivate <strong>{{ selectedInstructor.firstName }} {{ selectedInstructor.lastName }}</strong>?
          </p>

          <v-textarea
            v-model="deactivationReason"
            label="Reason"
            rows="3"
            class="mb-4"
          />

          <p class="text-body-2 mb-2">
            If you continue, this instructor will no longer have access to the system.
          </p>
          <p class="text-body-2 mb-0">
            The instructor record will stay in the system and can be recovered later. You can cancel now to keep this instructor active.
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelDeactivateInstructor">Cancel</v-btn>
          <v-btn color="warning" :loading="deactivating" @click="confirmDeactivateInstructor">Confirm Deactivation</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="reactivateDialog" max-width="540">
      <v-card v-if="selectedInstructor">
        <v-card-title>Confirm Instructor Reactivation</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Reactivate <strong>{{ selectedInstructor.firstName }} {{ selectedInstructor.lastName }}</strong>?
          </p>

          <p class="text-body-2 mb-0">
            If you continue, this instructor will regain access to the system and be notified of their reactivation.
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelReactivateInstructor">Cancel</v-btn>
          <v-btn color="success" :loading="reactivating" @click="confirmReactivateInstructor">Confirm Reactivation</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-card class="mb-6">
      <v-card-title>Invite Instructors To Register</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="5">
            <v-textarea
              v-model="form.emails"
              label="Instructor Emails"
              rows="4"
              hint="Separate emails with semicolons. Spaces between emails are ignored."
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="form.subject" label="Subject" />
          </v-col>
          <v-col cols="12" md="4">
            <v-textarea
              v-model="form.message"
              label="Email Message"
              rows="4"
              hint="Optional. Use [Registration link] if you want to place the invite URL manually."
              persistent-hint
            />
          </v-col>
        </v-row>

        <div class="d-flex flex-wrap ga-3 mt-2">
          <v-btn color="primary" @click="previewInvitations">Preview Invitation</v-btn>
          <v-btn variant="text" @click="resetForm">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card v-if="preview" class="mb-6">
      <v-card-title>Invitation Preview</v-card-title>
      <v-card-text>
        <div class="mb-2"><strong>Number of Emails:</strong> {{ preview.emailCount }}</div>
        <div class="mb-2"><strong>Recipients:</strong> {{ preview.emails.join('; ') }}</div>
        <div class="mb-2"><strong>Subject:</strong> {{ preview.subject }}</div>
        <div class="text-subtitle-1 mt-4 mb-2">Email Message</div>
        <pre class="preview-message">{{ preview.message }}</pre>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="modifyPreview">Modify Details</v-btn>
        <v-btn color="primary" :loading="sending" @click="sendInvitations">Send Invitations</v-btn>
      </v-card-actions>
    </v-card>

    <v-card>
      <v-card-title>Pending Instructor Invitations</v-card-title>
      <v-data-table :headers="invitationHeaders" :items="pendingInvitations" item-value="id">
        <template #item.sentAt="{ item }">
          {{ formatDate(item.sentAt) }}
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const defaultSubject = 'Welcome to The Peer Evaluation Tool - Complete Your Registration'
const defaultMessage = `Hello,

[Name of the Admin] has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below:

[Registration link]

If you have any questions or need assistance, feel free to contact [Admin's email] or our team directly.

Please note: This email is not monitored, so do not reply directly to this message.

Best regards,
Peer Evaluation Tool Team`

const statusOptions = [
  { title: 'Active', value: 'ACTIVE' },
  { title: 'Deactivated', value: 'DEACTIVATED' }
]

const error = ref('')
const success = ref('')
const sending = ref(false)
const preview = ref(null)
const pendingInvitations = ref([])
const searchResults = ref([])
const searchPerformed = ref(false)
const selectedInstructor = ref(null)
const deactivateDialog = ref(false)
const deactivating = ref(false)
const deactivationReason = ref('')
const reactivateDialog = ref(false)
const reactivating = ref(false)

const searchForm = ref({
  firstName: '',
  lastName: '',
  teamName: '',
  status: null
})

const form = ref({
  emails: '',
  subject: defaultSubject,
  message: defaultMessage
})

const searchHeaders = [
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Team Name', key: 'displayTeamName' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions', sortable: false }
]

const invitationHeaders = [
  { title: 'Email', key: 'email' },
  { title: 'Subject', key: 'subject' },
  { title: 'Sent At', key: 'sentAt' }
]

const parseEmails = (value) =>
  value
    .split(';')
    .map((email) => email.trim())
    .filter(Boolean)

const resetForm = () => {
  form.value = {
    emails: '',
    subject: defaultSubject,
    message: defaultMessage
  }
  preview.value = null
  error.value = ''
  success.value = ''
}

const clearSearch = () => {
  searchForm.value = {
    firstName: '',
    lastName: '',
    teamName: '',
    status: null
  }
  searchResults.value = []
  searchPerformed.value = false
  selectedInstructor.value = null
  deactivateDialog.value = false
  deactivationReason.value = ''
  reactivateDialog.value = false
  error.value = ''
}

const loadInvitations = async () => {
  const res = await api.get('/instructor-invitations')
  pendingInvitations.value = res.data
}

const searchInstructors = async () => {
  error.value = ''
  success.value = ''
  selectedInstructor.value = null

  if (!searchForm.value.firstName && !searchForm.value.lastName && !searchForm.value.teamName && !searchForm.value.status) {
    error.value = 'Enter at least one search value.'
    searchResults.value = []
    searchPerformed.value = false
    return
  }

  try {
    const res = await api.get('/instructors', {
      params: {
        firstName: searchForm.value.firstName || undefined,
        lastName: searchForm.value.lastName || undefined,
        teamName: searchForm.value.teamName || undefined,
        status: searchForm.value.status || undefined
      }
    })

    // Ralph: Keep a stable row key even when the same instructor appears more than once across different team contexts.
    searchResults.value = res.data.map((entry, index) => ({
      ...entry,
      resultKey: `${entry.id}-${entry.teamId ?? 'none'}-${entry.sectionId ?? 'none'}-${index}`,
      displayTeamName: entry.teamName || 'Not assigned to a team'
    }))
    searchPerformed.value = true
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to search instructors.'
  }
}

const viewInstructor = async (instructor) => {
  error.value = ''
  success.value = ''
  deactivateDialog.value = false
  deactivationReason.value = ''
  reactivateDialog.value = false

  try {
    // Ralph: Load one instructor-level detail record so every matching search row lands on the same canonical detail view.
    const res = await api.get(`/instructors/${instructor.id}`)
    selectedInstructor.value = res.data
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to load instructor details.'
    selectedInstructor.value = null
  }
}

const startDeactivateInstructor = () => {
  if (!selectedInstructor.value || selectedInstructor.value.status !== 'Active') {
    return
  }

  deactivationReason.value = ''
  deactivateDialog.value = true
  error.value = ''
  success.value = ''
}

const cancelDeactivateInstructor = () => {
  deactivateDialog.value = false
  deactivationReason.value = ''
}

const confirmDeactivateInstructor = async () => {
  if (!selectedInstructor.value) {
    return
  }
  if (!deactivationReason.value.trim()) {
    error.value = 'Enter a reason before deactivating this instructor.'
    return
  }

  deactivating.value = true
  error.value = ''
  success.value = ''

  try {
    const res = await api.put(`/instructors/${selectedInstructor.value.id}/deactivate`, {
      reason: deactivationReason.value
    })
    selectedInstructor.value = res.data
    // Ralph: Update every visible row for this instructor because the same person can appear once per supervised team.
    searchResults.value = searchResults.value
      .map((entry) => entry.id === selectedInstructor.value.id ? { ...entry, status: 'Deactivated' } : entry)
      .filter((entry) => searchForm.value.status !== 'ACTIVE' || entry.id !== selectedInstructor.value.id)
    success.value = `${selectedInstructor.value.firstName} ${selectedInstructor.value.lastName} was deactivated.`
    deactivateDialog.value = false
    deactivationReason.value = ''
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to deactivate instructor.'
  } finally {
    deactivating.value = false
  }
}

const startReactivateInstructor = () => {
  if (!selectedInstructor.value || selectedInstructor.value.status !== 'Deactivated') {
    return
  }

  reactivateDialog.value = true
  error.value = ''
  success.value = ''
}

const cancelReactivateInstructor = () => {
  reactivateDialog.value = false
}

const confirmReactivateInstructor = async () => {
  if (!selectedInstructor.value) {
    return
  }

  reactivating.value = true
  error.value = ''
  success.value = ''

  try {
    const res = await api.put(`/instructors/${selectedInstructor.value.id}/reactivate`)
    selectedInstructor.value = res.data
    // Ralph: Update every visible row for this instructor because the same person can appear once per supervised team.
    searchResults.value = searchResults.value
      .map((entry) => entry.id === selectedInstructor.value.id ? { ...entry, status: 'Active' } : entry)
      .filter((entry) => searchForm.value.status !== 'DEACTIVATED' || entry.id !== selectedInstructor.value.id)
    success.value = `${selectedInstructor.value.firstName} ${selectedInstructor.value.lastName} was reactivated and notified.`
    reactivateDialog.value = false
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to reactivate instructor.'
  } finally {
    reactivating.value = false
  }
}

const previewInvitations = () => {
  error.value = ''
  success.value = ''

  const emails = parseEmails(form.value.emails)
  const normalized = form.value.emails.trim()
  if (!emails.length) {
    error.value = 'Enter at least one instructor email.'
    preview.value = null
    return
  }
  if (normalized.endsWith(';')) {
    error.value = 'Instructor emails cannot end with a semicolon.'
    preview.value = null
    return
  }
  if (!normalized.includes(';') && normalized.includes(' ')) {
    error.value = 'Instructor emails must be separated by semicolons.'
    preview.value = null
    return
  }

  // Ralph: Preview the exact message and recipient count before anything is submitted so the admin can still edit safely.
  preview.value = {
    emails,
    emailCount: emails.length,
    subject: form.value.subject?.trim() || defaultSubject,
    message: form.value.message?.trim() || defaultMessage
  }
}

const modifyPreview = () => {
  preview.value = null
}

const sendInvitations = async () => {
  if (!preview.value) {
    return
  }

  sending.value = true
  error.value = ''
  success.value = ''

  try {
    await api.post('/instructor-invitations', {
      emails: form.value.emails,
      subject: preview.value.subject,
      message: preview.value.message
    })
    const emailCount = preview.value.emailCount
    resetForm()
    success.value = `Prepared ${emailCount} instructor invitation(s).`
    await loadInvitations()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to send instructor invitations.'
  } finally {
    sending.value = false
  }
}

const formatDate = (value) => new Date(value).toLocaleString()

onMounted(async () => {
  await loadInvitations()
})
</script>

<style scoped>
.preview-message {
  white-space: pre-wrap;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  font-family: inherit;
}
</style>
