import { AxiosError } from 'axios'

import { api } from './api'
import type {
  ApiErrorResponse,
  RankingPesquisadorItem,
  ResumoGrupo,
  ResumoPesquisador,
  SerieGrupoItem,
  SerieTemporalItem,
  TipoRankingGrupo,
  TipoSerie,
} from '../types/dashboard'

function extractApiError(error: unknown): string {
  const axiosError = error as AxiosError<ApiErrorResponse>
  return axiosError.response?.data?.message ?? 'Falha ao comunicar com o backend.'
}

export async function loadResumoPesquisador(idLattes: string): Promise<ResumoPesquisador> {
  try {
    const response = await api.post<ResumoPesquisador>(`/dashboard/pesquisadores/${idLattes}/carregar`)
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function loadResumoGrupoAdhoc(idsLattes: string[]): Promise<ResumoGrupo> {
  try {
    const response = await api.post<ResumoGrupo>('/dashboard/grupo-adhoc/carregar', {
      idsLattes,
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchSerieGrupoAdhoc(idsLattes: string[]): Promise<SerieGrupoItem[]> {
  try {
    const response = await api.post<SerieGrupoItem[]>('/dashboard/grupo-adhoc/series', {
      idsLattes,
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchRankingGrupoAdhoc(
  idsLattes: string[],
  metrica: TipoRankingGrupo,
): Promise<RankingPesquisadorItem[]> {
  try {
    const response = await api.post<RankingPesquisadorItem[]>('/dashboard/grupo-adhoc/ranking', {
      idsLattes,
    }, {
      params: { metrica },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchSerieTemporal(
  idLattes: string,
  tipo: TipoSerie,
): Promise<SerieTemporalItem[]> {
  try {
    const response = await api.get<SerieTemporalItem[]>(`/dashboard/pesquisadores/${idLattes}/series`, {
      params: { tipo },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}
