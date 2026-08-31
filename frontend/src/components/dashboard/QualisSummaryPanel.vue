<template>
  <section class="panel chart-panel qualis-panel">
    <div class="panel-body">
      <div class="section-header qualis-header">
        <div>
          <p class="eyebrow">Indicador bibliográfico</p>
          <h2 class="section-title">Qualis dos periódicos</h2>
          <p class="section-subtitle">{{ description }}</p>
        </div>
      </div>

      <div v-if="resumo.cobertura.totalArtigos === 0" class="status-box">
        Não há artigos em periódicos para classificar neste recorte.
      </div>

      <template v-else>
        <div class="qualis-metrics" aria-label="Cobertura da classificação Qualis">
          <article
            :class="{ 'qualis-item--interactive': canOpenArticles({ statusQualis: 'CLASSIFICADO' }) }"
            :role="canOpenArticles({ statusQualis: 'CLASSIFICADO' }) ? 'button' : undefined"
            :tabindex="canOpenArticles({ statusQualis: 'CLASSIFICADO' }) ? 0 : undefined"
            @click="openArticles({ statusQualis: 'CLASSIFICADO' })"
            @keydown.enter="openArticles({ statusQualis: 'CLASSIFICADO' })"
          >
            <span>Classificados</span>
            <strong>{{ resumo.cobertura.classificados }}</strong>
            <small>de {{ resumo.cobertura.totalArtigos }} artigos</small>
          </article>
          <article
            :class="{ 'qualis-item--interactive': canOpenArticles({ statusQualis: 'CLASSIFICADO' }) }"
            :role="canOpenArticles({ statusQualis: 'CLASSIFICADO' }) ? 'button' : undefined"
            :tabindex="canOpenArticles({ statusQualis: 'CLASSIFICADO' }) ? 0 : undefined"
            @click="openArticles({ statusQualis: 'CLASSIFICADO' })"
            @keydown.enter="openArticles({ statusQualis: 'CLASSIFICADO' })"
          >
            <span>Cobertura</span>
            <strong>{{ formatPercent(resumo.cobertura.percentualClassificados) }}</strong>
            <small>correspondência exata por ISSN</small>
          </article>
          <article
            :class="{ 'qualis-item--interactive': canOpenArticles({ statusQualis: 'SEM_ISSN' }) }"
            :role="canOpenArticles({ statusQualis: 'SEM_ISSN' }) ? 'button' : undefined"
            :tabindex="canOpenArticles({ statusQualis: 'SEM_ISSN' }) ? 0 : undefined"
            @click="openArticles({ statusQualis: 'SEM_ISSN' })"
            @keydown.enter="openArticles({ statusQualis: 'SEM_ISSN' })"
          >
            <span>Sem ISSN</span>
            <strong>{{ resumo.cobertura.semIssn }}</strong>
            <small>não foi possível consultar</small>
          </article>
          <article
            :class="{ 'qualis-item--interactive': canOpenArticles({ statusQualis: 'NAO_CLASSIFICADO' }) }"
            :role="canOpenArticles({ statusQualis: 'NAO_CLASSIFICADO' }) ? 'button' : undefined"
            :tabindex="canOpenArticles({ statusQualis: 'NAO_CLASSIFICADO' }) ? 0 : undefined"
            @click="openArticles({ statusQualis: 'NAO_CLASSIFICADO' })"
            @keydown.enter="openArticles({ statusQualis: 'NAO_CLASSIFICADO' })"
          >
            <span>Não localizados</span>
            <strong>{{ resumo.cobertura.naoClassificados }}</strong>
            <small>ISSN ausente na base</small>
          </article>
        </div>

        <div class="coverage-track" aria-hidden="true">
          <span :style="{ width: `${resumo.cobertura.percentualClassificados}%` }" />
        </div>

        <div v-if="distribution.length" class="qualis-distribution" aria-label="Distribuição por estrato">
          <span
            v-for="item in distribution"
            :key="item.estrato"
            class="qualis-chip"
            :class="{ 'qualis-item--interactive': canOpenArticles({ estrato: item.estrato }) }"
            :role="canOpenArticles({ estrato: item.estrato }) ? 'button' : undefined"
            :tabindex="canOpenArticles({ estrato: item.estrato }) ? 0 : undefined"
            @click="openArticles({ estrato: item.estrato })"
            @keydown.enter="openArticles({ estrato: item.estrato })"
          >
            <strong>{{ item.estrato }}</strong>
            {{ item.quantidade }}
          </span>
        </div>

        <div class="qualis-chart-heading">
          <h3>Evolução anual por estrato</h3>
          <p>{{ chartDescription }}</p>
        </div>

        <QualisStackedBarChart
          v-if="resumo.serieAnual.length"
          :data="resumo.serieAnual"
          :interactive="interactive"
          @select-qualis="openArticles({ ano: $event.ano, estrato: $event.estrato })"
        />
        <div v-else class="status-box">
          Há artigos classificados, mas nenhum deles possui ano de publicação identificado.
        </div>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import QualisStackedBarChart from './QualisStackedBarChart.vue'
import type { ArticleFilters, ResumoQualis } from '../../types/dashboard'

const props = withDefaults(defineProps<{
  resumo: ResumoQualis
  contexto?: 'pesquisador' | 'grupo'
  interactive?: boolean
}>(), {
  contexto: 'pesquisador',
  interactive: false,
})

const emit = defineEmits<{
  openArticles: [filters: ArticleFilters]
}>()

const description = computed(() => {
  const reference = `Qualis CAPES ${props.resumo.baseReferencia}`
  return props.contexto === 'grupo'
    ? `${reference} para os artigos do grupo, com identificação por ISSN.`
    : `Baseado no ${reference}, com identificação por ISSN.`
})

const distribution = computed(() => props.resumo.distribuicao.filter((item) => item.quantidade > 0))

const chartDescription = computed(() => props.interactive
  ? 'Clique em uma barra para auditar os artigos daquele ano e estrato.'
  : 'Clique nos estratos da legenda para comparar ou ocultar séries.')

function formatPercent(value: number) {
  return `${new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(value)}%`
}

function openArticles(filters: ArticleFilters) {
  if (canOpenArticles(filters)) {
    emit('openArticles', filters)
  }
}

function canOpenArticles(filters: ArticleFilters) {
  if (!props.interactive) {
    return false
  }

  if (filters.statusQualis === 'CLASSIFICADO') {
    return props.resumo.cobertura.classificados > 0
  }
  if (filters.statusQualis === 'SEM_ISSN') {
    return props.resumo.cobertura.semIssn > 0
  }
  if (filters.statusQualis === 'NAO_CLASSIFICADO') {
    return props.resumo.cobertura.naoClassificados > 0
  }
  if (filters.estrato) {
    return props.resumo.distribuicao.some((item) => item.estrato === filters.estrato && item.quantidade > 0)
  }

  return true
}
</script>
