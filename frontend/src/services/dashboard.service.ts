import { AxiosError } from 'axios'

import { api } from './api'
import type {
  ArticleFilters,
  ArticlePage,
  OrientationFilters,
  OrientationPage,
  ApiErrorResponse,
  CategoriaQuantidadeItem,
  RankingPesquisadorItem,
  ResumoGrupo,
  ResumoPesquisador,
  ResumoQualis,
  SerieGrupoItem,
  SerieOrientacaoStatusItem,
  SerieTemporalItem,
  TipoRankingGrupo,
  TipoSerie,
} from '../types/dashboard'

const LOAD_REQUEST_TIMEOUT_MS = 5 * 60 * 1000
const LOAD_TIMEOUT_MESSAGE = 'A importação está levando mais tempo que o normal. Aguarde alguns instantes e atualize a página.'

function extractApiError(error: unknown, timeoutMessage?: string): string {
  const axiosError = error as AxiosError<ApiErrorResponse>

  if (axiosError.code === 'ECONNABORTED' || axiosError.code === 'ETIMEDOUT') {
    return timeoutMessage ?? 'A consulta levou mais tempo que o esperado. Tente novamente.'
  }

  return axiosError.response?.data?.message ?? 'Não foi possível acessar o LattesVision. Tente novamente.'
}

export async function loadResumoPesquisador(idLattes: string): Promise<ResumoPesquisador> {
  try {
    const response = await api.post<ResumoPesquisador>(
      `/dashboard/pesquisadores/${idLattes}/carregar`,
      undefined,
      { timeout: LOAD_REQUEST_TIMEOUT_MS },
    )
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error, LOAD_TIMEOUT_MESSAGE))
  }
}

export async function loadResumoGrupoAdhoc(idsLattes: string[]): Promise<ResumoGrupo> {
  try {
    const response = await api.post<ResumoGrupo>('/dashboard/grupo-adhoc/carregar', {
      idsLattes,
    }, {
      timeout: LOAD_REQUEST_TIMEOUT_MS,
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error, LOAD_TIMEOUT_MESSAGE))
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

export async function fetchBancasPorTipo(idLattes: string): Promise<CategoriaQuantidadeItem[]> {
  try {
    const response = await api.get<CategoriaQuantidadeItem[]>(`/dashboard/pesquisadores/${idLattes}/bancas/tipos`)
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchOrientacoesPorStatusAno(idLattes: string): Promise<SerieOrientacaoStatusItem[]> {
  try {
    const response = await api.get<SerieOrientacaoStatusItem[]>(
      `/dashboard/pesquisadores/${idLattes}/orientacoes/status-series`,
    )
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchResumoQualis(idLattes: string): Promise<ResumoQualis> {
  try {
    const response = await api.get<ResumoQualis>(`/dashboard/pesquisadores/${idLattes}/qualis`)
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchResumoQualisGrupoAdhoc(idsLattes: string[]): Promise<ResumoQualis> {
  try {
    const response = await api.post<ResumoQualis>('/dashboard/grupo-adhoc/qualis', {
      idsLattes,
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchArticles(
  idLattes: string,
  filters: ArticleFilters,
  page = 0,
  size = 20,
): Promise<ArticlePage> {
  try {
    const response = await api.get<ArticlePage>(`/dashboard/pesquisadores/${idLattes}/artigos`, {
      params: {
        ...filters,
        page,
        size,
      },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchOrientations(
  idLattes: string,
  filters: OrientationFilters,
  page = 0,
  size = 20,
): Promise<OrientationPage> {
  try {
    const response = await api.get<OrientationPage>(`/dashboard/pesquisadores/${idLattes}/orientacoes`, {
      params: {
        ...filters,
        page,
        size,
      },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}
