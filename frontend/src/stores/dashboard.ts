import { defineStore } from 'pinia'

import {
  fetchRankingGrupoAdhoc,
  fetchSerieGrupoAdhoc,
  fetchSerieTemporal,
  loadResumoGrupoAdhoc,
  loadResumoPesquisador,
} from '../services/dashboard.service'
import type {
  RankingPesquisadorItem,
  ResumoGrupo,
  ResumoPesquisador,
  SerieGrupoItem,
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
        this.serieGrupo = await fetchSerieGrupoAdhoc(idsLattes)
      } catch (error) {
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
