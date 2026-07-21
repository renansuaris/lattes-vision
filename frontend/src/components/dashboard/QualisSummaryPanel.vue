<template>
  <section class="panel chart-panel qualis-panel">
    <div class="panel-body">
      <div class="section-header qualis-header">
        <div>
          <p class="eyebrow">Indicador bibliográfico</p>
          <h2 class="section-title">Qualis dos periódicos</h2>
          <p class="section-subtitle">
            {{ contextLabel }} Classificação de referência CAPES {{ resumo.baseReferencia }},
            aplicada aos periódicos identificados por ISSN independentemente do ano do artigo.
          </p>
        </div>
        <span class="qualis-source">{{ resumo.fonte }}</span>
      </div>

      <div v-if="resumo.cobertura.totalArtigos === 0" class="status-box">
        Não há artigos em periódicos para classificar neste recorte.
      </div>

      <template v-else>
        <div class="qualis-metrics" aria-label="Cobertura da classificação Qualis">
          <article>
            <span>Classificados</span>
            <strong>{{ resumo.cobertura.classificados }}</strong>
            <small>de {{ resumo.cobertura.totalArtigos }} artigos</small>
          </article>
          <article>
            <span>Cobertura</span>
            <strong>{{ formatPercent(resumo.cobertura.percentualClassificados) }}</strong>
            <small>correspondência exata por ISSN</small>
          </article>
          <article>
            <span>Sem ISSN</span>
            <strong>{{ resumo.cobertura.semIssn }}</strong>
            <small>não foi possível consultar</small>
          </article>
          <article>
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
          >
            <strong>{{ item.estrato }}</strong>
            {{ item.quantidade }}
          </span>
        </div>

        <div class="qualis-chart-heading">
          <h3>Evolução anual por estrato</h3>
          <p>Clique nos estratos da legenda para comparar ou ocultar séries.</p>
        </div>

        <QualisStackedBarChart
          v-if="resumo.serieAnual.length"
          :data="resumo.serieAnual"
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
import type { ResumoQualis } from '../../types/dashboard'

const props = withDefaults(defineProps<{
  resumo: ResumoQualis
  contexto?: 'pesquisador' | 'grupo'
}>(), {
  contexto: 'pesquisador',
})

const contextLabel = computed(() => props.contexto === 'grupo'
  ? 'Consolidação dos artigos dos pesquisadores selecionados.'
  : '')

const distribution = computed(() => props.resumo.distribuicao.filter((item) => item.quantidade > 0))

function formatPercent(value: number) {
  return `${new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(value)}%`
}
</script>
