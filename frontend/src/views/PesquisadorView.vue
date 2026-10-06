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
        interactive-event-works
        @open-event-works="openEventWorkDetails"
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
                interactive
                @select-category="openBancaDetails({ tipo: $event })"
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
                :interactive="card.value === 'artigos' || card.value === 'trabalhos_eventos' || card.value === 'projetos' || card.value === 'producoes_tecnicas'"
                @select-year="openTemporalDetails(card.value, $event)"
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
  BancaFilters,
  EventWorkFilters,
  ProjectFilters,
  TechnicalProductionCategory,
  TechnicalProductionFilters,
  OrientationFilters,
  OrientationStatus,
  PeriodFilter,
  StatusQualis,
  TipoSerie,
} from '../types/dashboard'

const route = useRoute()
const router = useRouter()
const store = useDashboardStore()
const activeDrilldown = ref<'articles' | 'orientations' | 'bancas' | 'eventWorks' | 'projects' | 'technicalProductions' | null>(null)
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
    description: 'Clique em um ano para auditar os trabalhos completos publicados em anais.',
  },
  {
    value: 'orientacoes',
    label: 'Orientações',
    description: 'Orientações concluídas e em andamento por ano.',
  },
  {
    value: 'projetos',
    label: 'Projetos',
    description: 'Clique em um ano para auditar os projetos iniciados naquele ano.',
  },
  {
    value: 'bancas',
    label: 'Participações em bancas',
    description: 'Clique em uma fatia para auditar as participações por tipo.',
  },
  {
    value: 'producoes_tecnicas',
    label: 'Produções técnicas',
    description: 'Clique em um ano para auditar os softwares, produtos e outras produções técnicas.',
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

const drilldownTitle = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') {
    const year = store.technicalProductionFilters.ano
    return year != null ? `Produções técnicas · ${year}` : 'Produções técnicas'
  }
  if (activeDrilldown.value === 'projects') {
    const year = store.projectFilters.ano
    return year != null ? `Projetos · Iniciados em ${year}` : 'Projetos'
  }
  if (activeDrilldown.value === 'bancas') {
    return bancaDrilldownTitle()
  }
  if (activeDrilldown.value === 'eventWorks') {
    return eventWorkDrilldownTitle()
  }
  if (activeDrilldown.value === 'orientations') {
    return orientationDrilldownTitle()
  }
  return articleDrilldownTitle()
})

const drilldownColumns = computed<DrilldownColumn[]>(() => {
  if (activeDrilldown.value === 'technicalProductions') {
    return [
      { key: 'year', label: 'Ano' },
      { key: 'category', label: 'Categoria', cellClass: 'technical-category-cell' },
      { key: 'title', label: 'Título', cellClass: 'technical-text-cell' },
      { key: 'authors', label: 'Autores', cellClass: 'technical-text-cell' },
      { key: 'nature', label: 'Natureza', cellClass: 'technical-text-cell' },
    ]
  }
  if (activeDrilldown.value === 'projects') {
    return [
      { key: 'year', label: 'Ano de início' },
      { key: 'type', label: 'Tipo', cellClass: 'project-type-cell' },
      { key: 'title', label: 'Título / descrição', cellClass: 'project-description-cell' },
    ]
  }
  if (activeDrilldown.value === 'bancas') {
    return [
      { key: 'year', label: 'Ano' },
      { key: 'type', label: 'Tipo' },
      { key: 'candidateName', label: 'Candidato(a)' },
      { key: 'workTitle', label: 'Título / descrição' },
    ]
  }
  if (activeDrilldown.value === 'eventWorks') {
    return [
      { key: 'year', label: 'Ano' },
      { key: 'title', label: 'Título' },
      { key: 'eventName', label: 'Evento' },
      { key: 'authors', label: 'Autores' },
      { key: 'type', label: 'Tipo' },
    ]
  }
  if (activeDrilldown.value === 'orientations') {
    return [
      { key: 'year', label: 'Ano' },
      { key: 'orientedName', label: 'Orientado(a)' },
      { key: 'workTitle', label: 'Título' },
      { key: 'type', label: 'Tipo' },
      { key: 'status', label: 'Status' },
    ]
  }
  return [
      { key: 'year', label: 'Ano' },
      { key: 'title', label: 'Título' },
      { key: 'journal', label: 'Periódico' },
      { key: 'issn', label: 'ISSN' },
      { key: 'qualisStratum', label: 'Qualis' },
      { key: 'qualisStatus', label: 'Cobertura' },
  ]
})

const drilldownRows = computed<DrilldownRow[]>(() => {
  if (activeDrilldown.value === 'technicalProductions') {
    return (store.technicalProductionPage?.items ?? []).map((production, index) => ({
      id: production.id ?? `technical-production-${index}`,
      cells: {
        year: String(production.year ?? '—'),
        category: technicalProductionCategoryLabel(production.category),
        title: production.title?.trim() || production.rawDescription?.trim() || '—',
        authors: production.authors?.trim() || '—',
        nature: production.nature?.trim() || '—',
      },
    }))
  }
  if (activeDrilldown.value === 'projects') {
    return (store.projectPage?.items ?? []).map((project, index) => ({
      id: project.id ?? `project-${index}`,
      cells: {
        year: String(project.year ?? '—'),
        type: project.type === 'Extensao' ? 'Extensão' : project.type || '—',
        title: project.title || 'Título não identificado',
      },
    }))
  }
  if (activeDrilldown.value === 'bancas') {
    return (store.bancaPage?.items ?? []).map((banca, index) => ({
      id: banca.id ?? `banca-${banca.workTitle}-${index}`,
      cells: {
        year: String(banca.year ?? '—'),
        type: banca.type ?? 'Outra',
        candidateName: banca.candidateName ?? 'Não identificado',
        workTitle: banca.workTitle ?? 'Título não identificado',
      },
    }))
  }

  if (activeDrilldown.value === 'eventWorks') {
    return (store.eventWorkPage?.items ?? []).map((work, index) => ({
      id: work.id ?? `event-work-${work.title}-${index}`,
      cells: {
        year: String(work.year ?? '—'),
        title: work.title ?? 'Título não identificado',
        eventName: work.eventName ?? 'Evento não identificado',
        authors: work.authors ?? 'Autores não identificados',
        type: work.type ?? '—',
      },
    }))
  }

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

const drilldownPage = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') return store.technicalProductionPage
  if (activeDrilldown.value === 'projects') return store.projectPage
  if (activeDrilldown.value === 'bancas') {
    return store.bancaPage
  }
  if (activeDrilldown.value === 'eventWorks') {
    return store.eventWorkPage
  }
  return activeDrilldown.value === 'orientations' ? store.orientationPage : store.articlePage
})

const drilldownLoading = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') return store.loadingTechnicalProductions
  if (activeDrilldown.value === 'projects') return store.loadingProjects
  if (activeDrilldown.value === 'bancas') {
    return store.loadingBancas
  }
  if (activeDrilldown.value === 'eventWorks') {
    return store.loadingEventWorks
  }
  return activeDrilldown.value === 'orientations' ? store.loadingOrientations : store.loadingArticles
})

const drilldownError = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') return store.errorTechnicalProductions
  if (activeDrilldown.value === 'projects') return store.errorProjects
  if (activeDrilldown.value === 'bancas') {
    return store.errorBancas
  }
  if (activeDrilldown.value === 'eventWorks') {
    return store.errorEventWorks
  }
  return activeDrilldown.value === 'orientations' ? store.errorOrientations : store.errorArticles
})

const drilldownLoadingMessage = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') return 'Carregando produções técnicas...'
  if (activeDrilldown.value === 'projects') return 'Carregando projetos...'
  if (activeDrilldown.value === 'bancas') {
    return 'Carregando participações em bancas...'
  }
  if (activeDrilldown.value === 'eventWorks') {
    return 'Carregando trabalhos em eventos...'
  }
  return activeDrilldown.value === 'orientations' ? 'Carregando orientações...' : 'Carregando artigos...'
})

const drilldownEmptyMessage = computed(() => {
  if (activeDrilldown.value === 'technicalProductions') return 'Nenhuma produção técnica foi encontrada para este recorte.'
  if (activeDrilldown.value === 'projects') return 'Nenhum projeto foi encontrado para este recorte.'
  if (activeDrilldown.value === 'bancas') {
    return 'Nenhuma participação em banca foi encontrada para este recorte.'
  }
  if (activeDrilldown.value === 'eventWorks') {
    return 'Nenhum trabalho em evento foi encontrado para este recorte.'
  }
  return activeDrilldown.value === 'orientations'
    ? 'Nenhuma orientação foi encontrada para este recorte.'
    : 'Nenhum artigo foi encontrado para este recorte.'
})

const drilldownTotalLabel = computed(() => activeDrilldown.value === 'bancas'
  ? 'participação em banca'
  : activeDrilldown.value === 'technicalProductions' ? 'produção técnica'
  : activeDrilldown.value === 'projects' ? 'projeto'
  : activeDrilldown.value === 'eventWorks' ? 'trabalho em evento'
  : activeDrilldown.value === 'orientations' ? 'orientação' : 'artigo')

const drilldownTotalLabelPlural = computed(() => activeDrilldown.value === 'bancas'
  ? 'participações em bancas'
  : activeDrilldown.value === 'technicalProductions' ? 'produções técnicas'
  : activeDrilldown.value === 'projects' ? 'projetos'
  : activeDrilldown.value === 'eventWorks' ? 'trabalhos em eventos'
  : activeDrilldown.value === 'orientations' ? 'orientações' : 'artigos')

function openSummaryCard(key: string) {
  if (key === 'producoes_tecnicas') {
    openTechnicalProductionDetails()
  }
  if (key === 'projetos') {
    openProjectDetails()
  }
  if (key === 'artigos') {
    openArticleDetails()
  }
  if (key === 'orientacoes') {
    openOrientationDetails()
  }
  if (key === 'bancas') {
    openBancaDetails()
  }
}

function isDrilldownCard(key: string) {
  return key === 'artigos' || key === 'orientacoes' || key === 'bancas' || key === 'projetos' || key === 'producoes_tecnicas'
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

async function openBancaDetails(filters: BancaFilters = {}) {
  if (!currentResumo.value) {
    return
  }

  activeDrilldown.value = 'bancas'
  await store.loadBancaDetails(currentId.value, filters)
}

async function openEventWorkDetails(filters: EventWorkFilters = {}) {
  if (!currentResumo.value) {
    return
  }

  activeDrilldown.value = 'eventWorks'
  await store.loadEventWorkDetails(currentId.value, filters)
}

async function openProjectDetails(filters: ProjectFilters = {}) {
  if (!currentResumo.value) return

  activeDrilldown.value = 'projects'
  await store.loadProjectDetails(currentId.value, filters)
}

async function openTechnicalProductionDetails(filters: TechnicalProductionFilters = {}) {
  if (!currentResumo.value) return

  activeDrilldown.value = 'technicalProductions'
  await store.loadTechnicalProductionDetails(currentId.value, filters)
}

function openTemporalDetails(tipo: TipoSerie, ano: number) {
  if (tipo === 'producoes_tecnicas') {
    openTechnicalProductionDetails({ ano })
    return
  }
  if (tipo === 'projetos') {
    openProjectDetails({ ano })
    return
  }
  if (tipo === 'trabalhos_eventos') {
    openEventWorkDetails({ ano })
    return
  }
  if (tipo === 'artigos') {
    openArticleDetails({ ano })
  }
}

function closeDrilldown() {
  activeDrilldown.value = null
  store.clearArticleDetails()
  store.clearOrientationDetails()
  store.clearBancaDetails()
  store.clearEventWorkDetails()
  store.clearProjectDetails()
  store.clearTechnicalProductionDetails()
}

async function changeDrilldownPage(page: number) {
  if (activeDrilldown.value === 'technicalProductions') {
    await store.loadTechnicalProductionDetails(currentId.value, store.technicalProductionFilters, page)
    return
  }
  if (activeDrilldown.value === 'projects') {
    await store.loadProjectDetails(currentId.value, store.projectFilters, page)
    return
  }
  if (activeDrilldown.value === 'bancas') {
    await store.loadBancaDetails(currentId.value, store.bancaFilters, page)
    return
  }

  if (activeDrilldown.value === 'eventWorks') {
    await store.loadEventWorkDetails(currentId.value, store.eventWorkFilters, page)
    return
  }

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

function bancaDrilldownTitle() {
  const filters = store.bancaFilters
  const parts = ['Participações em bancas']

  if (filters.tipo) {
    parts.push(filters.tipo === 'Graduacao' ? 'Graduação' : filters.tipo)
  }

  return parts.join(' · ')
}

function eventWorkDrilldownTitle() {
  const filters = store.eventWorkFilters
  const parts = ['Trabalhos completos em eventos']

  if (filters.ano != null) {
    parts.push(String(filters.ano))
  }

  return parts.join(' · ')
}

function technicalProductionCategoryLabel(category: TechnicalProductionCategory | null) {
  const labels: Record<TechnicalProductionCategory, string> = {
    SOFTWARE_COM_REGISTRO: 'Software com registro',
    SOFTWARE_SEM_REGISTRO: 'Software sem registro',
    PRODUTO_TECNOLOGICO: 'Produto tecnológico',
    PROCESSO_OU_TECNICA: 'Processo ou técnica',
    TRABALHO_TECNICO: 'Trabalho técnico',
    OUTRA: 'Outra produção técnica',
  }
  return category ? labels[category] ?? '—' : '—'
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
