import { defineStore } from 'pinia'

import {
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
  serieTipo: TipoSerie
  loadingResumo: boolean
  loadingSerie: boolean
  loadingRanking: boolean
  errorResumo: string | null
  errorSerie: string | null
  errorRanking: string | null
}

export const useDashboardStore = defineStore('dashboard', {
  state: (): DashboardState => ({
    currentResumoId: null,
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
    serieTipo: 'artigos',
    loadingResumo: false,
    loadingSerie: false,
    loadingRanking: false,
    errorResumo: null,
    errorSerie: null,
    errorRanking: null,
  }),
  actions: {
    async loadResumo(idLattes: string) {
      this.loadingResumo = true
      this.errorResumo = null
      this.errorSerie = null
      this.currentResumoId = null
      this.resumo = null
      this.serie = []
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null

      try {
        this.resumo = await loadResumoPesquisador(idLattes)
        this.currentResumoId = idLattes
      } catch (error) {
        this.resumo = null
        this.currentResumoId = null
        this.errorResumo = error instanceof Error ? error.message : 'Falha ao carregar resumo.'
      } finally {
        this.loadingResumo = false
      }
    },
    async loadSerie(idLattes: string, tipo: TipoSerie) {
      this.loadingSerie = true
      this.errorSerie = null
      this.serieTipo = tipo

      try {
        this.serie = await fetchSerieTemporal(idLattes, tipo)
      } catch (error) {
        this.serie = []
        this.errorSerie = error instanceof Error ? error.message : 'Falha ao carregar serie.'
      } finally {
        this.loadingSerie = false
      }
    },
    async loadSeries(idLattes: string, tipos: TipoSerie[]) {
      this.loadingSerie = true
      this.errorSerie = null
      this.seriesByTipo = {}
      this.bancasPorTipo = []
      this.orientacoesPorStatusAno = []
      this.resumoQualis = null
      this.resumoQualisGrupo = null

      try {
        const temporalTipos = tipos.filter((tipo) => tipo !== 'bancas' && tipo !== 'orientacoes')
        const [results, bancasPorTipo, orientacoesPorStatusAno, resumoQualis] = await Promise.all([
          Promise.all(temporalTipos.map(async (tipo) => {
            const data = await fetchSerieTemporal(idLattes, tipo)
            return [tipo, data] as const
          })),
          fetchBancasPorTipo(idLattes),
          fetchOrientacoesPorStatusAno(idLattes),
          fetchResumoQualis(idLattes),
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
    async loadGrupoAdhoc(idsLattes: string[]) {
      this.loadingResumo = true
      this.errorResumo = null
      this.errorSerie = null
      this.errorRanking = null
      this.currentResumoId = null
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

      try {
        this.resumoGrupo = await loadResumoGrupoAdhoc(idsLattes)
        this.grupoIds = idsLattes
      } catch (error) {
        this.resumoGrupo = null
        this.grupoIds = []
        this.errorResumo = error instanceof Error ? error.message : 'Falha ao carregar grupo.'
      } finally {
        this.loadingResumo = false
      }
    },
    async loadSerieGrupoAdhoc(idsLattes: string[]) {
      this.loadingSerie = true
      this.errorSerie = null

      try {
        const [serieGrupo, resumoQualisGrupo] = await Promise.all([
          fetchSerieGrupoAdhoc(idsLattes),
          fetchResumoQualisGrupoAdhoc(idsLattes),
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
    async loadRankingGrupoAdhoc(idsLattes: string[], metrica: TipoRankingGrupo) {
      this.loadingRanking = true
      this.errorRanking = null
      this.rankingMetrica = metrica

      try {
        this.rankingGrupo = await fetchRankingGrupoAdhoc(idsLattes, metrica)
      } catch (error) {
        this.errorRanking = error instanceof Error ? error.message : 'Falha ao carregar ranking do grupo.'
      } finally {
        this.loadingRanking = false
      }
    },
    reset() {
      this.currentResumoId = null
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
      this.errorResumo = null
      this.errorSerie = null
      this.errorRanking = null
      this.loadingResumo = false
      this.loadingSerie = false
      this.loadingRanking = false
      this.serieTipo = 'artigos'
    },
  },
})
