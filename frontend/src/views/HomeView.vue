<template>
  <main class="home-page">
    <section class="home-form-shell">
      <div class="system-heading">
        <h1>Análise de Currículos Lattes</h1>
        <p>Insira um ou mais IDs Lattes para visualizar os indicadores.</p>
      </div>

      <div class="simple-panel">
        <form class="simple-form" @submit.prevent="analyzeCurriculum">
          <div class="field-group">
            <label class="field-label" for="idLattes">ID(s) Lattes</label>
            <textarea
              id="idLattes"
              ref="idsTextarea"
              v-model="idsInput"
              class="text-input ids-input"
              inputmode="numeric"
              placeholder="7364915463901013, 6255476538629622"
              @input="resizeIdsTextarea"
            ></textarea>
            <span class="field-help">Cole os IDs Lattes aqui, separados por vírgula, espaço ou quebra de linha.</span>
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

    <div class="home-illustrations" aria-hidden="true">
      <img
        class="home-illustration home-illustration--growth"
        :src="growthChartUrl"
        alt=""
      />
      <img
        class="home-illustration home-illustration--pie"
        :src="pieChartUrl"
        alt=""
      />
    </div>

    <div v-if="isAnalyzing" class="modal-backdrop" role="dialog" aria-modal="true">
      <section class="loading-modal">
        <div class="loading-spinner" aria-hidden="true"></div>
        <h2>{{ modalTitle }}</h2>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import growthChartUrl from '../assets/grafico-crescimento.png'
import pieChartUrl from '../assets/grafico-pizza.png'
import { useDashboardStore } from '../stores/dashboard'

const router = useRouter()
const store = useDashboardStore()
const idsInput = ref('')
const idsTextarea = ref<HTMLTextAreaElement | null>(null)
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

function resizeIdsTextarea() {
  const textarea = idsTextarea.value

  if (!textarea) {
    return
  }

  textarea.style.height = 'auto'
  textarea.style.height = `${Math.max(textarea.scrollHeight, 58)}px`
}

watch(idsInput, async () => {
  await nextTick()
  resizeIdsTextarea()
})

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
