<template>
  <div>
    <h2 class="text-h5 mb-4">Rubrics</h2>

    <v-alert v-if="error" type="error" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card class="mb-6">
      <v-card-title>{{ form.id ? 'Edit Rubric' : 'Create Rubric' }}</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field v-model="form.name" label="Rubric Name" />
          </v-col>
        </v-row>

        <div class="text-subtitle-1 mb-3">Criteria</div>
        <v-card
          v-for="(criterion, index) in form.criteria"
          :key="criterion.localId"
          variant="outlined"
          class="mb-3"
        >
          <v-card-text>
            <v-row>
              <v-col cols="12" md="3">
                <v-text-field v-model="criterion.name" :label="`Criterion ${index + 1} Name`" />
              </v-col>
              <v-col cols="12" md="5">
                <v-text-field v-model="criterion.description" label="Description" />
              </v-col>
              <v-col cols="12" md="2">
                <v-text-field v-model.number="criterion.maxScore" type="number" min="0.1" step="0.1" label="Max Score" />
              </v-col>
              <v-col cols="12" md="1">
                <v-switch v-model="criterion.active" label="Active" />
              </v-col>
              <v-col cols="12" md="1" class="d-flex align-center justify-end">
                <v-btn icon="mdi-delete" variant="text" color="error" @click="removeCriterion(index)" />
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <div class="d-flex flex-wrap ga-3">
          <v-btn color="secondary" variant="tonal" @click="addCriterion">Add Criterion</v-btn>
          <v-btn color="primary" @click="submitRubric">{{ form.id ? 'Save Rubric' : 'Create Rubric' }}</v-btn>
          <v-btn variant="text" @click="resetForm">Clear</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-data-table :headers="headers" :items="rubrics" item-value="id">
      <template #item.criteria="{ item }">
        {{ item.criteria.length }}
      </template>
      <template #item.actions="{ item }">
        <v-btn size="small" variant="text" color="primary" @click="editRubric(item)">Edit</v-btn>
      </template>
    </v-data-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'

const rubrics = ref([])
const error = ref('')
const success = ref('')

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Name', key: 'name' },
  { title: 'Criteria', key: 'criteria' },
  { title: 'Actions', key: 'actions', sortable: false }
]

const emptyCriterion = () => ({
  localId: crypto.randomUUID(),
  name: '',
  description: '',
  maxScore: 10,
  active: true
})

const form = ref({
  id: null,
  name: '',
  criteria: [emptyCriterion()]
})

const load = async () => {
  const res = await api.get('/rubrics')
  rubrics.value = res.data
}

const addCriterion = () => {
  form.value.criteria.push(emptyCriterion())
}

const removeCriterion = (index) => {
  if (form.value.criteria.length === 1) {
    return
  }
  form.value.criteria.splice(index, 1)
}

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    criteria: [emptyCriterion()]
  }
}

const editRubric = (rubric) => {
  form.value = {
    id: rubric.id,
    name: rubric.name,
    criteria: rubric.criteria.map((criterion) => ({
      localId: crypto.randomUUID(),
      name: criterion.name,
      description: criterion.description,
      maxScore: Number(criterion.maxScore),
      active: criterion.active
    }))
  }
}

const submitRubric = async () => {
  error.value = ''
  success.value = ''

  const payload = {
    name: form.value.name,
    criteria: form.value.criteria.map(({ name, description, maxScore, active }) => ({
      name,
      description,
      maxScore,
      active
    }))
  }

  try {
    if (form.value.id) {
      await api.put(`/rubrics/${form.value.id}`, payload)
      success.value = 'Rubric updated.'
    } else {
      await api.post('/rubrics', payload)
      success.value = 'Rubric created.'
    }
    resetForm()
    await load()
  } catch (err) {
    error.value = err.response?.data?.error || 'Unable to save rubric.'
  }
}

onMounted(load)
</script>
