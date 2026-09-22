<template>
  <form class="filter-panel" @submit.prevent="apply">
    <div class="filter-panel__title">
      <div>
        <h2>Período de análise</h2>
        <span>{{ activePeriodLabel }}</span>
      </div>
      <button v-if="hasActivePeriod" class="button button-secondary" type="button" @click="clear">
        Limpar
      </button>
    </div>

    <div class="filter-fields">
      <label class="field-group">
        <span class="field-label">Ano inicial</span>
        <input v-model="startYear" class="text-input" inputmode="numeric" maxlength="4" placeholder="Ex.: 2022">
      </label>
      <label class="field-group">
        <span class="field-label">Ano final</span>
        <input v-model="endYear" class="text-input" inputmode="numeric" maxlength="4" placeholder="Ex.: 2025">
      </label>
      <button class="button button-primary" type="submit">Aplicar filtros</button>
    </div>

    <p v-if="error" class="filter-panel__error" role="alert">{{ error }}</p>
    <p class="filter-panel__note">
      O período altera apenas a análise e a exportação; a sincronização preserva todo o histórico do currículo.
    </p>
  </form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import type { PeriodFilter } from '../../types/dashboard'

const props = defineProps<{
  period: PeriodFilter
}>()

const emit = defineEmits<{
  apply: [period: PeriodFilter]
}>()

const startYear = ref('')
const endYear = ref('')
const error = ref('')

const hasActivePeriod = computed(() => props.period.anoInicial != null || props.period.anoFinal != null)

const activePeriodLabel = computed(() => {
  if (props.period.anoInicial != null && props.period.anoFinal != null) {
    return `Exibindo dados de ${props.period.anoInicial} a ${props.period.anoFinal}.`
  }
  if (props.period.anoInicial != null) {
    return `Exibindo dados a partir de ${props.period.anoInicial}.`
  }
  if (props.period.anoFinal != null) {
    return `Exibindo dados até ${props.period.anoFinal}.`
  }
  return 'Exibindo todo o histórico disponível.'
})

watch(
  () => props.period,
  (period) => {
    startYear.value = period.anoInicial?.toString() ?? ''
    endYear.value = period.anoFinal?.toString() ?? ''
    error.value = ''
  },
  { immediate: true, deep: true },
)

function parseYear(value: string, fieldName: string): number | undefined {
  const normalized = value.trim()
  if (!normalized) {
    return undefined
  }
  if (!/^\d{4}$/.test(normalized)) {
    throw new Error(`${fieldName} deve ter quatro dígitos.`)
  }
  return Number(normalized)
}

function apply() {
  try {
    const anoInicial = parseYear(startYear.value, 'Ano inicial')
    const anoFinal = parseYear(endYear.value, 'Ano final')
    if (anoInicial != null && anoFinal != null && anoInicial > anoFinal) {
      throw new Error('O ano inicial não pode ser maior que o ano final.')
    }
    error.value = ''
    emit('apply', { anoInicial, anoFinal })
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : 'Período inválido.'
  }
}

function clear() {
  startYear.value = ''
  endYear.value = ''
  error.value = ''
  emit('apply', {})
}
</script>
