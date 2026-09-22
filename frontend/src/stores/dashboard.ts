import { defineStore } from 'pinia'

import {
  fetchArticles,
  fetchBancas,
  fetchEventWorks,
  fetchOrientations,
  fetchBancasPorTipo,
  fetchOrientacoesPorStatusAno,
  fetchResumoQualis,
  fetchResumoQualisGrupoAdhoc,
  fetchRankingGrupoAdhoc,
  fetchSerieGrupoAdhoc,
  fetchSerieTemporal,
  loadResumoGrupoAdhoc,
  loadResumoPesquisador,
} from '../services/dashboard.service'
import type {
  ArticleFilters,
  ArticlePage,
  BancaFilters,
  BancaPage,
  EventWorkFilters,
  EventWorkPage,
  OrientationFilters,
  OrientationPage,
  PeriodFilter,
  RankingPesquisadorItem,
  CategoriaQuantidadeItem,
  ResumoGrupo,
  ResumoPesquisador,
  ResumoQualis,
  SerieGrupoItem,
  SerieOrientacaoStatusItem,
  SerieTemporalItem,
  TipoRankingGrupo,
  TipoSerie,
} from '../types/dashboard'

interface DashboardState {
  currentResumoId: string | null
  periodFilter: PeriodFilter
  grupoIds: string[]
  resumoGrupo: ResumoGrupo | null
  serieGrupo: SerieGrupoItem[]
  rankingGrupo: RankingPesquisadorItem[]
  rankingMetrica: TipoRankingGrupo
  resumo: ResumoPesquisador | null
  serie: SerieTemporalItem[]
  seriesByTipo: Partial<Record<TipoSerie, SerieTemporalItem[]>>
  bancasPorTipo: CategoriaQuantidadeItem[]
  orientacoesPorStatusAno: SerieOrientacaoStatusItem[]
  resumoQualis: ResumoQualis | null
  resumoQualisGrupo: ResumoQualis | null
  articlePage: ArticlePage | null
  articleFilters: ArticleFilters
  orientationPage: OrientationPage | null
  orientationFilters: OrientationFilters
  bancaPage: BancaPage | null
  bancaFilters: BancaFilters
  eventWorkPage: EventWorkPage | null
  eventWorkFilters: EventWorkFilters
  serieTipo: TipoSerie
  loadingResumo: boolean
  loadingSerie: boolean
  loadingRanking: boolean
  loadingArticles: boolean
  loadingOrientations: boolean
  loadingBancas: boolean
  loadingEventWorks: boolean
  errorResumo: string | null
  errorSerie: string | null
  errorRanking: string | null
  errorArticles: string | null
  errorOrientations: string | null
  errorBancas: string | null
  errorEventWorks: string | null
}

export const useDashboardStore = defineStore('dashboard', {
  state: (): DashboardState => ({
    currentResumoId: null,
    periodFilter: {},
    grupoIds: [],
    resumoGrupo: null,
    serieGrupo: [],
    rankingGrupo: [],
    rankingMetrica: 'total',
    resumo: null,
    serie: [],
    seriesByTipo: {},
    bancasPorTipo: [],
    orientacoesPorStatusAno: [],
    resumoQualis: null,
    resumoQualisGrupo: null,
    articlePage: null,
    articleFilters: {},
    orientationPage: null,
    orientationFilters: {},
    bancaPage: null,
    bancaFilters: {},
    eventWorkPage: null,
    eventWorkFilters: {},
    serieTipo: 'artigos',
    loadingResumo: false,
    loadingSerie: false,
    loadingRanking: false,
    loadingArticles: false,
    loadingOrientations: false,
    loadingBancas: false,
    loadingEventWorks: false,
    errorResumo: null,
    errorSerie: null,
    errorRanking: null,
    errorArticles: null,
    errorOrientations: null,
    errorBancas: null,
    errorEventWorks: null,
  }),
  actions: {
    async loadResumo(idLattes: string, period: PeriodFilter = {}) {
      this.loadingResumo = true
      this.errorResumo = null
      this.errorSerie = null
      this.currentResumoId = null
      this.periodFilter = { ...period }
      this.resumo = null
      this.serie = []
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null
      this.clearArticleDetails()
      this.clearOrientationDetails()
      this.clearBancaDetails()
      this.clearEventWorkDetails()

      try {
        this.resumo = await loadResumoPesquisador(idLattes, period)
        this.currentResumoId = idLattes
      } catch (error) {
        this.resumo = null
        this.currentResumoId = null
        this.errorResumo = error instanceof Error ? error.message : 'Falha ao carregar resumo.'
      } finally {
        this.loadingResumo = false
      }
    },
    async loadSerie(idLattes: string, tipo: TipoSerie, period?: PeriodFilter) {
      this.loadingSerie = true
      this.errorSerie = null
      this.serieTipo = tipo
      const appliedPeriod = period ?? this.periodFilter

      try {
        this.serie = await fetchSerieTemporal(idLattes, tipo, appliedPeriod)
      } catch (error) {
        this.serie = []
        this.errorSerie = error instanceof Error ? error.message : 'Falha ao carregar serie.'
      } finally {
        this.loadingSerie = false
      }
    },
    async loadSeries(idLattes: string, tipos: TipoSerie[], period?: PeriodFilter) {
      this.loadingSerie = true
      this.errorSerie = null
      const appliedPeriod = period ?? this.periodFilter
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null
      this.clearArticleDetails()
      this.clearOrientationDetails()
      this.clearBancaDetails()
      this.clearEventWorkDetails()

      try {
        const temporalTipos = tipos.filter((tipo) => tipo !== 'bancas' && tipo !== 'orientacoes')
        const [results, bancasPorTipo, orientacoesPorStatusAno, resumoQualis] = await Promise.all([
          Promise.all(temporalTipos.map(async (tipo) => {
            const data = await fetchSerieTemporal(idLattes, tipo, appliedPeriod)
            return [tipo, data] as const
          })),
          fetchBancasPorTipo(idLattes, appliedPeriod),
          fetchOrientacoesPorStatusAno(idLattes, appliedPeriod),
          fetchResumoQualis(idLattes, appliedPeriod),
        ])

        this.seriesByTipo = Object.fromEntries(results) as Partial<Record<TipoSerie, SerieTemporalItem[]>>
        this.bancasPorTipo = bancasPorTipo
        this.orientacoesPorStatusAno = orientacoesPorStatusAno
        this.resumoQualis = resumoQualis
        this.serie = this.seriesByTipo.artigos ?? []
        this.serieTipo = 'artigos'
      } catch (error) {
        this.seriesByTipo = {}
        this.bancasPorTipo = []
        this.orientacoesPorStatusAno = []
        this.resumoQualis = null
        this.serie = []
        this.errorSerie = error instanceof Error ? error.message : 'Falha ao carregar series.'
      } finally {
        this.loadingSerie = false
      }
    },
    async loadGrupoAdhoc(idsLattes: string[], period: PeriodFilter = {}) {
      this.loadingResumo = true
      this.errorResumo = null
      this.errorSerie = null
      this.errorRanking = null
      this.currentResumoId = null
      this.periodFilter = { ...period }
      this.grupoIds = []
      this.resumo = null
      this.resumoGrupo = null
      this.serieGrupo = []
      this.rankingGrupo = []
      this.serie = []
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null
      this.clearArticleDetails()
      this.clearOrientationDetails()
      this.clearBancaDetails()
      this.clearEventWorkDetails()

      try {
        this.resumoGrupo = await loadResumoGrupoAdhoc(idsLattes, period)
        this.grupoIds = idsLattes
      } catch (error) {
        this.resumoGrupo = null
        this.grupoIds = []
        this.errorResumo = error instanceof Error ? error.message : 'Falha ao carregar grupo.'
      } finally {
        this.loadingResumo = false
      }
    },
    async loadSerieGrupoAdhoc(idsLattes: string[], period?: PeriodFilter) {
      this.loadingSerie = true
      this.errorSerie = null
      const appliedPeriod = period ?? this.periodFilter

      try {
        const [serieGrupo, resumoQualisGrupo] = await Promise.all([
          fetchSerieGrupoAdhoc(idsLattes, appliedPeriod),
          fetchResumoQualisGrupoAdhoc(idsLattes, appliedPeriod),
        ])
        this.serieGrupo = serieGrupo
        this.resumoQualisGrupo = resumoQualisGrupo
      } catch (error) {
        this.serieGrupo = []
        this.resumoQualisGrupo = null
        this.errorSerie = error instanceof Error ? error.message : 'Falha ao carregar serie do grupo.'
      } finally {
        this.loadingSerie = false
      }
    },
    async loadRankingGrupoAdhoc(idsLattes: string[], metrica: TipoRankingGrupo, period?: PeriodFilter) {
      this.loadingRanking = true
      this.errorRanking = null
      this.rankingMetrica = metrica
      const appliedPeriod = period ?? this.periodFilter

      try {
        this.rankingGrupo = await fetchRankingGrupoAdhoc(idsLattes, metrica, appliedPeriod)
      } catch (error) {
        this.errorRanking = error instanceof Error ? error.message : 'Falha ao carregar ranking do grupo.'
      } finally {
        this.loadingRanking = false
      }
    },
    async loadArticleDetails(idLattes: string, filters: ArticleFilters = {}, page = 0) {
      this.loadingArticles = true
      this.errorArticles = null
      this.articleFilters = { ...this.periodFilter, ...filters }

      try {
        this.articlePage = await fetchArticles(idLattes, this.articleFilters, page)
      } catch (error) {
        this.articlePage = null
        this.errorArticles = error instanceof Error ? error.message : 'Falha ao carregar artigos.'
      } finally {
        this.loadingArticles = false
      }
    },
    clearArticleDetails() {
      this.articlePage = null
      this.articleFilters = {}
      this.loadingArticles = false
      this.errorArticles = null
    },
    async loadOrientationDetails(idLattes: string, filters: OrientationFilters = {}, page = 0) {
      this.loadingOrientations = true
      this.errorOrientations = null
      this.orientationFilters = { ...this.periodFilter, ...filters }

      try {
        this.orientationPage = await fetchOrientations(idLattes, this.orientationFilters, page)
      } catch (error) {
        this.orientationPage = null
        this.errorOrientations = error instanceof Error ? error.message : 'Falha ao carregar orientações.'
      } finally {
        this.loadingOrientations = false
      }
    },
    clearOrientationDetails() {
      this.orientationPage = null
      this.orientationFilters = {}
      this.loadingOrientations = false
      this.errorOrientations = null
    },
    async loadBancaDetails(idLattes: string, filters: BancaFilters = {}, page = 0) {
      this.loadingBancas = true
      this.errorBancas = null
      this.bancaFilters = { ...this.periodFilter, ...filters }

      try {
        this.bancaPage = await fetchBancas(idLattes, this.bancaFilters, page)
      } catch (error) {
        this.bancaPage = null
        this.errorBancas = error instanceof Error ? error.message : 'Falha ao carregar participações em bancas.'
      } finally {
        this.loadingBancas = false
      }
    },
    clearBancaDetails() {
      this.bancaPage = null
      this.bancaFilters = {}
      this.loadingBancas = false
      this.errorBancas = null
    },
    async loadEventWorkDetails(idLattes: string, filters: EventWorkFilters = {}, page = 0) {
      this.loadingEventWorks = true
      this.errorEventWorks = null
      this.eventWorkFilters = { ...this.periodFilter, ...filters }

      try {
        this.eventWorkPage = await fetchEventWorks(idLattes, this.eventWorkFilters, page)
      } catch (error) {
        this.eventWorkPage = null
        this.errorEventWorks = error instanceof Error ? error.message : 'Falha ao carregar trabalhos em eventos.'
      } finally {
        this.loadingEventWorks = false
      }
    },
    clearEventWorkDetails() {
      this.eventWorkPage = null
      this.eventWorkFilters = {}
      this.loadingEventWorks = false
      this.errorEventWorks = null
    },
    reset() {
      this.currentResumoId = null
      this.periodFilter = {}
      this.grupoIds = []
      this.resumoGrupo = null
      this.serieGrupo = []
      this.rankingGrupo = []
      this.rankingMetrica = 'total'
      this.resumo = null
      this.serie = []
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null
      this.clearArticleDetails()
      this.clearOrientationDetails()
      this.clearBancaDetails()
      this.clearEventWorkDetails()
      this.errorResumo = null
      this.errorSerie = null
      this.errorRanking = null
      this.errorArticles = null
      this.errorOrientations = null
      this.errorBancas = null
      this.errorEventWorks = null
      this.loadingResumo = false
      this.loadingSerie = false
      this.loadingRanking = false
      this.loadingArticles = false
      this.loadingOrientations = false
      this.loadingBancas = false
      this.loadingEventWorks = false
      this.serieTipo = 'artigos'
    },
  },
})
