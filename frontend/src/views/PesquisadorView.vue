<template>
  <main class="page-shell">
    <header class="dashboard-header">
      <div>
        <RouterLink class="back-link" to="/">Voltar para consulta</RouterLink>
        <p class="eyebrow">Pesquisador</p>
        <h1 class="dashboard-title">
          {{ currentResumo?.nomeCompleto ?? 'Dashboard do pesquisador' }}
        </h1>
        <p class="lead">ID Lattes {{ currentId }}</p>
      </div>

      <div class="dashboard-actions">
        <button class="button button-secondary" type="button" @click.prevent>
          Exportar Dados
        </button>
      </div>
    </header>

    <section v-if="store.errorResumo" class="status-box error">
      {{ store.errorResumo }}
    </section>

    <section v-else-if="store.loadingResumo" class="status-box">
      Consultando base local. Se o pesquisador não estiver cadastrado, a sincronização com o
      Lattes será iniciada automaticamente.
    </section>

    <template v-else-if="currentResumo">
      <section class="layout-grid stats-grid">
        <article
          v-for="card in summaryCards"
          :key="card.label"
          class="stat-card"
        >
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span class="stat-meta">{{ card.meta }}</span>
        </article>
      </section>

      <section class="filter-panel" aria-label="Filtros do dashboard">
        <div class="filter-panel__title">
          <h2>Filtros</h2>
          <span>Em preparação</span>
        </div>

        <div class="filter-fields">
          <label class="field-group">
            <span class="field-label">Período inicial</span>
            <input class="text-input" type="number" value="2000" disabled />
          </label>

          <label class="field-group">
            <span class="field-label">Período final</span>
            <input class="text-input" type="number" value="2026" disabled />
          </label>

          <button class="button button-secondary" type="button" disabled>
            Aplicar filtros
          </button>
        </div>
      </section>

      <QualisSummaryPanel
        v-if="store.resumoQualis"
        :resumo="store.resumoQualis"
      />

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Séries temporais</h2>
              <p class="section-subtitle">
                Indicadores anuais do currículo, exibidos por categoria de produção.
              </p>
            </div>
          </div>

          <section v-if="store.errorSerie && !hasChartData" class="status-box error">
            {{ store.errorSerie }}
          </section>

          <section v-else-if="store.loadingSerie && !hasChartData" class="status-box">
            Carregando series temporais...
          </section>

          <section v-else-if="!hasChartData" class="status-box">
            Sem dados para exibir.
          </section>

          <div v-else class="chart-stack">
            <article
              v-for="card in seriesCards"
              :key="card.value"
              class="chart-card"
            >
              <div class="chart-card__header">
                <div>
                  <h3>{{ card.label }}</h3>
                  <p>{{ card.description }}</p>
                </div>
              </div>

              <section v-if="isEmptyCard(card.value)" class="status-box">
                Sem dados para esta categoria.
              </section>

              <CategoryPieChart
                v-else-if="card.value === 'bancas'"
                :data="store.bancasPorTipo"
                :label="card.label"
              />

              <OrientacaoStatusChart
                v-else-if="card.value === 'orientacoes'"
                :data="store.orientacoesPorStatusAno"
              />

              <SerieChart
                v-else
                :data="getSerieData(card.value)"
                :label="card.label"
                :chart-type="card.value === 'artigos' ? 'line' : 'bar'"
              />
            </article>
          </div>
        </div>
      </section>

      <section class="layout-grid metadata-grid">
        <article class="metadata-item">
          <span class="metadata-label">Última atualização no Lattes</span>
          <div class="metadata-value">{{ formatDate(currentResumo.dataUltimaAtualizacao) }}</div>
        </article>

        <article class="metadata-item">
          <span class="metadata-label">Última sincronização local</span>
          <div class="metadata-value">{{ formatDateTime(currentResumo.dataSincronizacao) }}</div>
        </article>
      </section>
    </template>
  </main>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import CategoryPieChart from '../components/dashboard/CategoryPieChart.vue'
import OrientacaoStatusChart from '../components/dashboard/OrientacaoStatusChart.vue'
import QualisSummaryPanel from '../components/dashboard/QualisSummaryPanel.vue'
import SerieChart from '../components/dashboard/SerieChart.vue'
import { useDashboardStore } from '../stores/dashboard'
import type { TipoSerie } from '../types/dashboard'

const route = useRoute()
const store = useDashboardStore()

const seriesOptions: Array<{
  value: TipoSerie
  label: string
  description: string
}> = [
  {
    value: 'artigos',
    label: 'Artigos publicados em periódicos',
    description: 'Produção bibliográfica em periódicos agrupada por ano.',
  },
  {
    value: 'trabalhos_eventos',
    label: 'Trabalhos em eventos',
    description: 'Publicações em anais de eventos agrupadas por ano.',
  },
  {
    value: 'orientacoes',
    label: 'Orientações',
    description: 'Orientações concluídas e em andamento identificadas no currículo.',
  },
  {
    value: 'projetos',
    label: 'Projetos',
    description: 'Projetos vinculados ao pesquisador ao longo do tempo.',
  },
  {
    value: 'bancas',
    label: 'Participações em bancas',
    description: 'Distribuição das participações em bancas acadêmicas por tipo.',
  },
  {
    value: 'producoes_tecnicas',
    label: 'Produções técnicas',
    description: 'Produções técnicas consolidadas pelo parser atual.',
  },
]

const currentId = computed(() => String(route.params.id ?? ''))

const currentResumo = computed(() => {
  if (store.currentResumoId !== currentId.value) {
    return null
  }

  return store.resumo
})

const seriesCards = computed(() => {
  return seriesOptions.map((option) => ({
    value: option.value,
    label: option.label,
    description: option.description,
  }))
})

const hasChartData = computed(() => {
  return seriesOptions.some((option) => !isEmptyCard(option.value))
})

function getSerieData(tipo: TipoSerie) {
  return store.seriesByTipo[tipo] ?? []
}

function isEmptyCard(tipo: TipoSerie) {
  if (tipo === 'bancas') {
    return store.bancasPorTipo.length === 0
  }

  if (tipo === 'orientacoes') {
    return store.orientacoesPorStatusAno.length === 0
  }

  return getSerieData(tipo).length === 0
}

const summaryCards = computed(() => {
  const resumo = currentResumo.value

  if (!resumo) {
    return []
  }

  return [
    { label: 'Artigos', value: resumo.totalArtigos, meta: 'Produção bibliográfica indexada no parser atual.' },
    { label: 'Orientações', value: resumo.totalOrientacoes, meta: `${resumo.totalOrientacoesConcluidas} concluídas / ${resumo.totalOrientacoesEmAndamento} em andamento` },
    { label: 'Projetos', value: resumo.totalProjetos, meta: 'Projetos vinculados ao currículo sincronizado.' },
    { label: 'Bancas', value: resumo.totalBancas, meta: 'Participações em bancas extraídas.' },
    { label: 'Prod. técnicas', value: resumo.totalProducoesTecnicas, meta: 'Categorias técnicas consolidadas.' },
    { label: 'Trab. eventos', value: resumo.totalTrabalhosEventos, meta: 'Trabalhos completos em anais.' },
  ]
})

function formatDate(value: string | null) {
  if (!value) {
    return 'Não informado'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
  }).format(new Date(`${value}T00:00:00`))
}

function formatDateTime(value: string | null) {
  if (!value) {
    return 'Não informado'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

async function loadPage() {
  if (!/^\d{16}$/.test(currentId.value)) {
    store.reset()
    return
  }

  if (!currentResumo.value) {
    await store.loadResumo(currentId.value)
  }

  if (!store.errorResumo) {
    await store.loadSeries(currentId.value, seriesOptions.map((option) => option.value))
  }
}

watch(
  () => currentId.value,
  async () => {
    await loadPage()
  },
  { immediate: true },
)
</script>
