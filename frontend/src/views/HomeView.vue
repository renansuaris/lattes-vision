<template>
  <main class="home-page">
    <section class="home-form-shell">
      <div class="system-heading">
        <h1>Análise de Currículos Lattes</h1>
        <p>Informe o identificador do pesquisador para acessar os dados cadastrados.</p>
      </div>

      <div class="simple-panel">
        <form class="simple-form" @submit.prevent="analyzeCurriculum">
          <div class="field-group">
            <label class="field-label" for="idLattes">ID(s) Lattes</label>
            <textarea
              id="idLattes"
              v-model="idsInput"
              class="text-input ids-input"
              inputmode="numeric"
              placeholder="7364915463901013, 6255476538629622"
            ></textarea>
            <span class="field-help">Digite um ou mais IDs de 16 números, separados por vírgula, espaço ou quebra de linha.</span>
          </div>

          <div class="action-row">
            <button class="button button-primary" :disabled="!isValidId || isAnalyzing" type="submit">
              Analisar
            </button>
          </div>

          <div v-if="idsInput && !isValidInput" class="status-box error">
            {{ validationMessage }}
          </div>

          <div v-if="analysisError" class="status-box error">
            {{ analysisError }}
          </div>
        </form>
      </div>
    </section>

    <div v-if="isAnalyzing" class="modal-backdrop" role="dialog" aria-modal="true">
      <section class="loading-modal">
        <div class="loading-spinner" aria-hidden="true"></div>
        <h2>{{ modalTitle }}</h2>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useDashboardStore } from '../stores/dashboard'

const router = useRouter()
const store = useDashboardStore()
const idsInput = ref('')
const isAnalyzing = ref(false)
const analysisError = ref<string | null>(null)

const parsedIds = computed(() => parseIds(idsInput.value))
const invalidIds = computed(() => parsedIds.value.filter((id) => !/^\d{16}$/.test(id)))
const isValidInput = computed(() => parsedIds.value.length > 0 && invalidIds.value.length === 0)
const isValidId = isValidInput
const modalTitle = computed(() => parsedIds.value.length > 1 ? 'Consultando currículos' : 'Consultando currículo')
const validationMessage = computed(() => {
  if (parsedIds.value.length === 0) {
    return 'Informe ao menos um ID Lattes.'
  }

  return 'Todos os IDs Lattes devem conter exatamente 16 dígitos.'
})

function parseIds(value: string) {
  return value
    .split(/[\s,;]+/)
    .map((id) => id.trim())
    .filter(Boolean)
}

async function analyzeCurriculum() {
  if (!isValidId.value || isAnalyzing.value) {
    return
  }

  analysisError.value = null
  isAnalyzing.value = true

  try {
    if (parsedIds.value.length === 1) {
      await store.loadResumo(parsedIds.value[0])

      if (store.errorResumo) {
        analysisError.value = store.errorResumo
        return
      }

      await router.push({
        name: 'pesquisador',
        params: { id: parsedIds.value[0] },
      })
      return
    }

    await store.loadGrupoAdhoc(parsedIds.value)

    if (store.errorResumo) {
      analysisError.value = store.errorResumo
      return
    }

    await router.push({
      name: 'grupo-adhoc',
      query: { ids: parsedIds.value.join(',') },
    })
  } finally {
    isAnalyzing.value = false
  }
}
</script>
