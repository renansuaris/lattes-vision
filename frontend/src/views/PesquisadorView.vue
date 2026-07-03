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

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Séries temporais</h2>
              <p class="section-subtitle">
                Dados agrupados por ano para {{ currentOptionLabel.toLowerCase() }}.
              </p>
            </div>

            <div class="segmented-control" aria-label="Tipo de serie temporal">
              <button
                v-for="option in seriesOptions"
                :key="option.value"
                class="segment-button"
                :class="{ 'is-active': option.value === selectedTipo }"
                @click="selectedTipo = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <section v-if="store.errorSerie && store.serie.length === 0" class="status-box error">
            {{ store.errorSerie }}
          </section>

          <section v-else-if="store.loadingSerie && store.serie.length === 0" class="status-box">
            Carregando serie temporal...
          </section>

          <section v-else-if="store.serie.length === 0" class="status-box">
            Sem dados para o tipo selecionado.
          </section>

          <SerieChart
            v-else
            :data="store.serie"
            :label="currentOptionLabel"
          />
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
import { RouterLink, useRoute, useRouter } from 'vue-router'

import SerieChart from '../components/dashboard/SerieChart.vue'
import { useDashboardStore } from '../stores/dashboard'
import type { TipoSerie } from '../types/dashboard'

const route = useRoute()
const router = useRouter()
const store = useDashboardStore()

const seriesOptions: Array<{ value: TipoSerie; label: string }> = [
  { value: 'artigos', label: 'Artigos' },
  { value: 'orientacoes', label: 'Orientações' },
  { value: 'projetos', label: 'Projetos' },
  { value: 'bancas', label: 'Bancas' },
  { value: 'producoes_tecnicas', label: 'Produções técnicas' },
  { value: 'trabalhos_eventos', label: 'Trabalhos em eventos' },
]

const currentId = computed(() => String(route.params.id ?? ''))

const currentResumo = computed(() => {
  if (store.currentResumoId !== currentId.value) {
    return null
  }

  return store.resumo
})

const selectedTipo = computed<TipoSerie>({
  get: () => {
    const tipo = route.query.tipo
    return seriesOptions.some((option) => option.value === tipo) ? (tipo as TipoSerie) : 'artigos'
  },
  set: (value) => {
    router.replace({
      query: {
        ...route.query,
        tipo: value,
      },
    })
  },
})

const currentOptionLabel = computed(() => {
  return seriesOptions.find((option) => option.value === selectedTipo.value)?.label ?? 'Artigos'
})

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
    await store.loadSerie(currentId.value, selectedTipo.value)
  }
}

watch(
  () => currentId.value,
  async () => {
    await loadPage()
  },
  { immediate: true },
)

watch(
  () => selectedTipo.value,
  async (tipo, previousTipo) => {
    if (tipo === previousTipo || !/^\d{16}$/.test(currentId.value) || !currentResumo.value) {
      return
    }

    await store.loadSerie(currentId.value, tipo)
  },
)
</script>
