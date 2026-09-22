<template>
  <main class="page-shell">
    <header class="dashboard-header">
      <div>
        <RouterLink class="back-link" :to="backDestination">{{ backLabel }}</RouterLink>
        <p class="eyebrow">Pesquisador</p>
        <h1 class="dashboard-title">
          {{ currentResumo?.nomeCompleto ?? 'Dashboard do pesquisador' }}
        </h1>
        <p class="lead">ID Lattes {{ currentId }}</p>
      </div>

      <div class="dashboard-actions">
        <button class="button button-secondary" type="button" :disabled="exporting" @click="exportData">
          {{ exporting ? 'Preparando exportação...' : 'Exportar dados' }}
        </button>
      </div>
    </header>

    <PeriodFilterPanel :period="activePeriod" @apply="applyPeriod" />

    <section v-if="exportError" class="status-box error">{{ exportError }}</section>

    <section v-if="store.errorResumo" class="status-box error">
      {{ store.errorResumo }}
    </section>

    <section v-else-if="store.loadingResumo" class="status-box">
      Buscando dados do currículo. No primeiro acesso, isso pode levar alguns segundos.
    </section>

    <template v-else-if="currentResumo">
      <section class="layout-grid stats-grid">
        <article
          v-for="card in summaryCards"
          :key="card.label"
          class="stat-card"
          :class="{ 'stat-card--interactive': isDrilldownCard(card.key) }"
          :role="isDrilldownCard(card.key) ? 'button' : undefined"
          :tabindex="isDrilldownCard(card.key) ? 0 : undefined"
          @click="openSummaryCard(card.key)"
          @keydown.enter="openSummaryCard(card.key)"
        >
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span class="stat-meta">{{ card.meta }}</span>
        </article>
      </section>

      <BibliographicProductionPanel
        :total="currentResumo.totalProducaoBibliografica"
        :artigos="currentResumo.totalArtigos"
        :trabalhos-eventos="currentResumo.totalTrabalhosEventos"
        :livros="currentResumo.totalLivros"
        :capitulos="currentResumo.totalCapitulos"
      />

      <QualisSummaryPanel
        v-if="store.resumoQualis"
        :resumo="store.resumoQualis"
        interactive
        @open-articles="openArticleDetails"
      />

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Séries temporais</h2>
              <p class="section-subtitle">Produção acadêmica ao longo dos anos.</p>
            </div>
          </div>

          <section v-if="store.errorSerie && !hasChartData" class="status-box error">
            {{ store.errorSerie }}
          </section>

          <section v-else-if="store.loadingSerie && !hasChartData" class="status-box">
            Carregando séries temporais...
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
                interactive
                @select-orientation="openOrientationDetails({ ano: $event.ano, status: $event.status })"
              />

              <SerieChart
                v-else
                :data="getSerieData(card.value)"
                :label="card.label"
                :chart-type="card.value === 'artigos' ? 'line' : 'bar'"
                :interactive="card.value === 'artigos'"
                @select-year="openArticleDetails({ ano: $event })"
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
          <span class="metadata-label">Dados consultados em</span>
          <div class="metadata-value">{{ formatDateTime(currentResumo.dataSincronizacao) }}</div>
        </article>
      </section>
    </template>

    <DashboardDrilldownModal
      :open="activeDrilldown !== null"
      :title="drilldownTitle"
      :columns="drilldownColumns"
      :rows="drilldownRows"
      :page="drilldownPage"
      :loading="drilldownLoading"
      :error="drilldownError"
      :loading-message="drilldownLoadingMessage"
      :empty-message="drilldownEmptyMessage"
      :total-label="drilldownTotalLabel"
      :total-label-plural="drilldownTotalLabelPlural"
      @close="closeDrilldown"
      @change-page="changeDrilldownPage"
    />
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import BibliographicProductionPanel from '../components/dashboard/BibliographicProductionPanel.vue'
import CategoryPieChart from '../components/dashboard/CategoryPieChart.vue'
import DashboardDrilldownModal, {
  type DrilldownColumn,
  type DrilldownRow,
} from '../components/dashboard/DashboardDrilldownModal.vue'
import OrientacaoStatusChart from '../components/dashboard/OrientacaoStatusChart.vue'
import PeriodFilterPanel from '../components/dashboard/PeriodFilterPanel.vue'
import QualisSummaryPanel from '../components/dashboard/QualisSummaryPanel.vue'
import SerieChart from '../components/dashboard/SerieChart.vue'
import { exportPesquisadorCsv } from '../services/dashboard.service'
import { useDashboardStore } from '../stores/dashboard'
import type {
  ArticleFilters,
  OrientationFilters,
  OrientationStatus,
  PeriodFilter,
  StatusQualis,
  TipoSerie,
} from '../types/dashboard'

const route = useRoute()
const router = useRouter()
const store = useDashboardStore()
const activeDrilldown = ref<'articles' | 'orientations' | null>(null)
const exporting = ref(false)
const exportError = ref<string | null>(null)

const seriesOptions: Array<{
  value: TipoSerie
  label: string
  description: string
}> = [
  {
    value: 'artigos',
    label: 'Artigos publicados em periódicos',
    description: 'Clique em um ano para auditar as publicações em periódicos.',
  },
  {
    value: 'trabalhos_eventos',
    label: 'Trabalhos em eventos',
    description: 'Trabalhos completos publicados em anais.',
  },
  {
    value: 'orientacoes',
    label: 'Orientações',
    description: 'Orientações concluídas e em andamento por ano.',
  },
  {
    value: 'projetos',
    label: 'Projetos',
    description: 'Projetos desenvolvidos ao longo dos anos.',
  },
  {
    value: 'bancas',
    label: 'Participações em bancas',
    description: 'Participações distribuídas por tipo de banca.',
  },
  {
    value: 'producoes_tecnicas',
    label: 'Produções técnicas',
    description: 'Softwares, produtos e outras produções técnicas.',
  },
]

const currentId = computed(() => String(route.params.id ?? ''))

const activePeriod = computed<PeriodFilter>(() => ({
  anoInicial: parseRouteYear(route.query.anoInicial),
  anoFinal: parseRouteYear(route.query.anoFinal),
}))

const periodSignature = computed(() => `${activePeriod.value.anoInicial ?? ''}-${activePeriod.value.anoFinal ?? ''}`)

const groupReturnIds = computed(() => String(route.query.grupoIds ?? '')
  .split(',')
  .map((id) => id.trim())
  .filter((id) => /^\d{16}$/.test(id)))

const cameFromGroup = computed(() => route.query.origem === 'grupo' && groupReturnIds.value.length >= 2)

const backDestination = computed(() => {
  if (!cameFromGroup.value) {
    return '/'
  }

  return {
    name: 'grupo-adhoc',
    query: {
      ids: groupReturnIds.value.join(','),
      anoInicial: activePeriod.value.anoInicial?.toString(),
      anoFinal: activePeriod.value.anoFinal?.toString(),
    },
  }
})

const backLabel = computed(() => cameFromGroup.value ? 'Voltar para análise de grupo' : 'Voltar para consulta')

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
    { key: 'orientacoes', label: 'Orientações', value: resumo.totalOrientacoes, meta: `${resumo.totalOrientacoesConcluidas} concluídas / ${resumo.totalOrientacoesEmAndamento} em andamento` },
    { key: 'projetos', label: 'Projetos', value: resumo.totalProjetos, meta: 'Pesquisa, extensão e desenvolvimento.' },
    { key: 'bancas', label: 'Bancas', value: resumo.totalBancas, meta: 'Participações registradas.' },
    { key: 'producoes_tecnicas', label: 'Prod. técnicas', value: resumo.totalProducoesTecnicas, meta: 'Softwares, produtos e outros.' },
  ]
})

const drilldownTitle = computed(() => activeDrilldown.value === 'orientations'
  ? orientationDrilldownTitle()
  : articleDrilldownTitle())

const drilldownColumns = computed<DrilldownColumn[]>(() => activeDrilldown.value === 'orientations'
  ? [
      { key: 'year', label: 'Ano' },
      { key: 'orientedName', label: 'Orientado(a)' },
      { key: 'workTitle', label: 'Título' },
      { key: 'type', label: 'Tipo' },
      { key: 'status', label: 'Status' },
    ]
  : [
      { key: 'year', label: 'Ano' },
      { key: 'title', label: 'Título' },
      { key: 'journal', label: 'Periódico' },
      { key: 'issn', label: 'ISSN' },
      { key: 'qualisStratum', label: 'Qualis' },
      { key: 'qualisStatus', label: 'Cobertura' },
    ])

const drilldownRows = computed<DrilldownRow[]>(() => {
  if (activeDrilldown.value === 'orientations') {
    return (store.orientationPage?.items ?? []).map((orientation, index) => ({
      id: orientation.id ?? `orientation-${orientation.workTitle}-${index}`,
      cells: {
        year: String(orientation.year ?? '—'),
        orientedName: orientation.orientedName ?? 'Não identificado',
        workTitle: orientation.workTitle ?? 'Título não identificado',
        type: orientation.type ?? '—',
        status: orientationStatusLabel(orientation.status),
      },
    }))
  }

  return (store.articlePage?.items ?? []).map((article, index) => ({
    id: article.id ?? `article-${article.title}-${index}`,
    cells: {
      year: String(article.year ?? '—'),
      title: article.title ?? 'Título não identificado',
      journal: article.journal ?? 'Periódico não identificado',
      issn: article.issn ?? '—',
      qualisStratum: article.qualisStratum ?? '—',
      qualisStatus: qualisStatusLabel(article.qualisStatus),
    },
  }))
})

const drilldownPage = computed(() => activeDrilldown.value === 'orientations'
  ? store.orientationPage
  : store.articlePage)

const drilldownLoading = computed(() => activeDrilldown.value === 'orientations'
  ? store.loadingOrientations
  : store.loadingArticles)

const drilldownError = computed(() => activeDrilldown.value === 'orientations'
  ? store.errorOrientations
  : store.errorArticles)

const drilldownLoadingMessage = computed(() => activeDrilldown.value === 'orientations'
  ? 'Carregando orientações...'
  : 'Carregando artigos...')

const drilldownEmptyMessage = computed(() => activeDrilldown.value === 'orientations'
  ? 'Nenhuma orientação foi encontrada para este recorte.'
  : 'Nenhum artigo foi encontrado para este recorte.')

const drilldownTotalLabel = computed(() => activeDrilldown.value === 'orientations' ? 'orientação' : 'artigo')

const drilldownTotalLabelPlural = computed(() => activeDrilldown.value === 'orientations' ? 'orientações' : 'artigos')

function openSummaryCard(key: string) {
  if (key === 'artigos') {
    openArticleDetails()
  }
  if (key === 'orientacoes') {
    openOrientationDetails()
  }
}

function isDrilldownCard(key: string) {
  return key === 'artigos' || key === 'orientacoes'
}

async function openArticleDetails(filters: ArticleFilters = {}) {
  if (!currentResumo.value) {
    return
  }

  activeDrilldown.value = 'articles'
  await store.loadArticleDetails(currentId.value, filters)
}

async function openOrientationDetails(filters: OrientationFilters = {}) {
  if (!currentResumo.value) {
    return
  }

  activeDrilldown.value = 'orientations'
  await store.loadOrientationDetails(currentId.value, filters)
}

function closeDrilldown() {
  activeDrilldown.value = null
  store.clearArticleDetails()
  store.clearOrientationDetails()
}

async function changeDrilldownPage(page: number) {
  if (activeDrilldown.value === 'orientations') {
    await store.loadOrientationDetails(currentId.value, store.orientationFilters, page)
    return
  }

  if (activeDrilldown.value === 'articles') {
    await store.loadArticleDetails(currentId.value, store.articleFilters, page)
  }
}

function articleDrilldownTitle() {
  const filters = store.articleFilters
  const parts = ['Artigos em periódicos']

  if (filters.ano != null) {
    parts.push(String(filters.ano))
  }
  if (filters.estrato) {
    parts.push(`Qualis ${filters.estrato}`)
  }
  if (filters.statusQualis) {
    parts.push(qualisStatusLabel(filters.statusQualis))
  }

  return parts.join(' · ')
}

function orientationDrilldownTitle() {
  const filters = store.orientationFilters
  const parts = ['Orientações']

  if (filters.ano != null) {
    parts.push(String(filters.ano))
  }
  if (filters.status) {
    parts.push(orientationStatusLabel(filters.status))
  }
  if (filters.tipo) {
    parts.push(filters.tipo)
  }

  return parts.join(' · ')
}

function qualisStatusLabel(status: StatusQualis | null) {
  const labels: Record<StatusQualis, string> = {
    CLASSIFICADO: 'Classificados',
    SEM_ISSN: 'Sem ISSN',
    NAO_CLASSIFICADO: 'Não localizados',
  }
  return status ? labels[status] : 'Não informado'
}

function orientationStatusLabel(status: OrientationStatus | null) {
  if (status === 'CONCLUIDA') {
    return 'Concluídas'
  }
  if (status === 'EM_ANDAMENTO') {
    return 'Em andamento'
  }
  return 'Não informado'
}

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

  await store.loadResumo(currentId.value, activePeriod.value)

  if (!store.errorResumo) {
    await store.loadSeries(currentId.value, seriesOptions.map((option) => option.value), activePeriod.value)
  }
}

function parseRouteYear(value: unknown): number | undefined {
  const normalized = String(value ?? '')
  return /^\d{4}$/.test(normalized) ? Number(normalized) : undefined
}

async function applyPeriod(period: PeriodFilter) {
  await router.replace({
    query: {
      ...route.query,
      anoInicial: period.anoInicial?.toString(),
      anoFinal: period.anoFinal?.toString(),
    },
  })
}

async function exportData() {
  if (!currentResumo.value) {
    return
  }

  exporting.value = true
  exportError.value = null
  try {
    const download = await exportPesquisadorCsv(currentId.value, activePeriod.value)
    triggerDownload(download.blob, download.fileName)
  } catch (error) {
    exportError.value = error instanceof Error ? error.message : 'Não foi possível gerar a exportação.'
  } finally {
    exporting.value = false
  }
}

function triggerDownload(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

watch(
  () => `${currentId.value}:${periodSignature.value}`,
  async () => {
    activeDrilldown.value = null
    await loadPage()
  },
  { immediate: true },
)
</script>
