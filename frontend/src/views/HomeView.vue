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
            <div class="field-label-row">
              <label class="field-label" for="idLattes">ID(s) Lattes</label>
              <div ref="idsHelp" class="ids-help">
                <button
                  class="ids-help__trigger"
                  type="button"
                  aria-label="Como adicionar IDs Lattes"
                  aria-controls="idsHelpPopover"
                  :aria-expanded="isIdsHelpOpen"
                  @click="toggleIdsHelp"
                >
                  ?
                </button>
                <section
                  v-if="isIdsHelpOpen"
                  id="idsHelpPopover"
                  class="ids-help__popover"
                  aria-label="Como adicionar IDs Lattes"
                >
                  <div class="ids-help__header">
                    <strong>Como adicionar IDs</strong>
                    <button type="button" aria-label="Fechar ajuda" @click="isIdsHelpOpen = false">×</button>
                  </div>
                  <ul>
                    <li>Digite os 16 números de um ID ou cole uma lista.</li>
                    <li>Para inserir vários, use vírgula, espaço ou quebra de linha.</li>
                    <li>Use o × ao lado de uma tag para remover um ID.</li>
                    <li>IDs que não tenham 16 dígitos são sinalizados.</li>
                  </ul>
                </section>
              </div>
            </div>
            <input
              id="idLattes"
              v-model="idsInput"
              class="text-input ids-input"
              inputmode="numeric"
              maxlength="16"
              placeholder="Cole ou digite um ID Lattes"
              @blur="commitTypedIds"
              @input="handleTypedId"
              @keydown.enter.prevent="commitTypedIds"
              @paste="handlePaste"
            />

            <div v-if="selectedIds.length" class="ids-summary" aria-live="polite">
              {{ selectedIds.length === 1 ? '1 currículo adicionado' : `${selectedIds.length} currículos adicionados` }}
            </div>

            <ul v-if="selectedIds.length || invalidIds.length" class="id-tags" aria-label="IDs informados">
              <li v-for="id in selectedIds" :key="id" class="id-tag">
                <span>{{ id }}</span>
                <button type="button" :aria-label="`Remover ID ${id}`" @click="removeSelectedId(id)">×</button>
              </li>
              <li v-for="id in invalidIds" :key="`invalid-${id}`" class="id-tag id-tag--invalid">
                <span>{{ id }}</span>
                <button type="button" :aria-label="`Remover ID inválido ${id}`" @click="removeInvalidId(id)">×</button>
              </li>
            </ul>

            <p v-if="invalidIds.length" class="ids-validation" role="alert">
              {{ invalidIds.length === 1 ? 'Há 1 ID inválido.' : `Há ${invalidIds.length} IDs inválidos.` }} Cada ID deve conter exatamente 16 dígitos numéricos.
            </p>
          </div>

          <div class="action-row">
            <button class="button button-primary" :disabled="!isValidId || isAnalyzing" type="submit">
              Analisar
            </button>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useDashboardStore } from '../stores/dashboard'

const router = useRouter()
const store = useDashboardStore()
const idsInput = ref('')
const idsHelp = ref<HTMLElement | null>(null)
const selectedIds = ref<string[]>([])
const invalidIds = ref<string[]>([])
const isIdsHelpOpen = ref(false)
const isAnalyzing = ref(false)
const analysisError = ref<string | null>(null)

const isValidInput = computed(() => selectedIds.value.length > 0 && invalidIds.value.length === 0)
const isValidId = isValidInput
const modalTitle = computed(() => selectedIds.value.length > 1 ? 'Consultando currículos' : 'Consultando currículo')

function parseIds(value: string) {
  return value
    .split(/[\s,;]+/)
    .map((id) => id.trim())
    .filter(Boolean)
}

function addIds(value: string) {
  for (const id of parseIds(value)) {
    const target = /^\d{16}$/.test(id) ? selectedIds : invalidIds

    if (!target.value.includes(id)) {
      target.value.push(id)
    }
  }
}

function commitTypedIds() {
  if (!idsInput.value.trim()) {
    return
  }

  addIds(idsInput.value)
  idsInput.value = ''
}

function handleTypedId(event: Event) {
  const input = event.target as HTMLInputElement
  const numericValue = input.value.replace(/\D/g, '').slice(0, 16)

  input.value = numericValue
  idsInput.value = numericValue

  if (numericValue.length === 16) {
    addIds(numericValue)
    idsInput.value = ''
  }
}

function handlePaste(event: ClipboardEvent) {
  event.preventDefault()
  addIds(`${idsInput.value} ${event.clipboardData?.getData('text') ?? ''}`)
  idsInput.value = ''
}

function removeSelectedId(id: string) {
  selectedIds.value = selectedIds.value.filter((selectedId) => selectedId !== id)
}

function removeInvalidId(id: string) {
  invalidIds.value = invalidIds.value.filter((invalidId) => invalidId !== id)
}

function toggleIdsHelp() {
  isIdsHelpOpen.value = !isIdsHelpOpen.value
}

function closeIdsHelpWhenClickingOutside(event: MouseEvent) {
  if (isIdsHelpOpen.value && !idsHelp.value?.contains(event.target as Node)) {
    isIdsHelpOpen.value = false
  }
}

function closeIdsHelpOnEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    isIdsHelpOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeIdsHelpWhenClickingOutside)
  document.addEventListener('keydown', closeIdsHelpOnEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeIdsHelpWhenClickingOutside)
  document.removeEventListener('keydown', closeIdsHelpOnEscape)
})

async function analyzeCurriculum() {
  commitTypedIds()

  if (!isValidId.value || isAnalyzing.value) {
    return
  }

  analysisError.value = null
  isAnalyzing.value = true

  try {
    if (selectedIds.value.length === 1) {
      await store.loadResumo(selectedIds.value[0])

      if (store.errorResumo) {
        analysisError.value = store.errorResumo
        return
      }

      await router.push({
        name: 'pesquisador',
        params: { id: selectedIds.value[0] },
      })
      return
    }

    await store.loadGrupoAdhoc(selectedIds.value)

    if (store.errorResumo) {
      analysisError.value = store.errorResumo
      return
    }

    await router.push({
      name: 'grupo-adhoc',
      query: { ids: selectedIds.value.join(',') },
    })
  } finally {
    isAnalyzing.value = false
  }
}
</script>
