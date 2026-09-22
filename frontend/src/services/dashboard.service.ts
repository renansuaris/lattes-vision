import { AxiosError } from 'axios'

import { api } from './api'
import type {
  ArticleFilters,
  ArticlePage,
  OrientationFilters,
  OrientationPage,
  PeriodFilter,
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

export async function loadResumoPesquisador(idLattes: string, period: PeriodFilter = {}): Promise<ResumoPesquisador> {
  try {
    const response = await api.post<ResumoPesquisador>(
      `/dashboard/pesquisadores/${idLattes}/carregar`,
      undefined,
      { timeout: LOAD_REQUEST_TIMEOUT_MS, params: period },
    )
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error, LOAD_TIMEOUT_MESSAGE))
  }
}

export async function loadResumoGrupoAdhoc(idsLattes: string[], period: PeriodFilter = {}): Promise<ResumoGrupo> {
  try {
    const response = await api.post<ResumoGrupo>('/dashboard/grupo-adhoc/carregar', {
      idsLattes,
    }, {
      timeout: LOAD_REQUEST_TIMEOUT_MS,
      params: period,
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error, LOAD_TIMEOUT_MESSAGE))
  }
}

export async function fetchSerieGrupoAdhoc(idsLattes: string[], period: PeriodFilter = {}): Promise<SerieGrupoItem[]> {
  try {
    const response = await api.post<SerieGrupoItem[]>('/dashboard/grupo-adhoc/series', {
      idsLattes,
    }, { params: period })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchRankingGrupoAdhoc(
  idsLattes: string[],
  metrica: TipoRankingGrupo,
  period: PeriodFilter = {},
): Promise<RankingPesquisadorItem[]> {
  try {
    const response = await api.post<RankingPesquisadorItem[]>('/dashboard/grupo-adhoc/ranking', {
      idsLattes,
    }, {
      params: { metrica, ...period },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchSerieTemporal(
  idLattes: string,
  tipo: TipoSerie,
  period: PeriodFilter = {},
): Promise<SerieTemporalItem[]> {
  try {
    const response = await api.get<SerieTemporalItem[]>(`/dashboard/pesquisadores/${idLattes}/series`, {
      params: { tipo, ...period },
    })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchBancasPorTipo(idLattes: string, period: PeriodFilter = {}): Promise<CategoriaQuantidadeItem[]> {
  try {
    const response = await api.get<CategoriaQuantidadeItem[]>(`/dashboard/pesquisadores/${idLattes}/bancas/tipos`, { params: period })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchOrientacoesPorStatusAno(idLattes: string, period: PeriodFilter = {}): Promise<SerieOrientacaoStatusItem[]> {
  try {
    const response = await api.get<SerieOrientacaoStatusItem[]>(
      `/dashboard/pesquisadores/${idLattes}/orientacoes/status-series`,
      { params: period },
    )
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchResumoQualis(idLattes: string, period: PeriodFilter = {}): Promise<ResumoQualis> {
  try {
    const response = await api.get<ResumoQualis>(`/dashboard/pesquisadores/${idLattes}/qualis`, { params: period })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export async function fetchResumoQualisGrupoAdhoc(idsLattes: string[], period: PeriodFilter = {}): Promise<ResumoQualis> {
  try {
    const response = await api.post<ResumoQualis>('/dashboard/grupo-adhoc/qualis', {
      idsLattes,
    }, { params: period })
    return response.data
  } catch (error) {
    throw new Error(extractApiError(error))
  }
}

export interface CsvDownload {
  blob: Blob
  fileName: string
}

export async function exportPesquisadorCsv(idLattes: string, period: PeriodFilter = {}): Promise<CsvDownload> {
  try {
    const response = await api.get<Blob>(`/dashboard/pesquisadores/${idLattes}/exportacao.csv`, {
      params: period,
      responseType: 'blob',
    })
    return { blob: response.data, fileName: extractFileName(response.headers['content-disposition']) }
  } catch (error) {
    throw new Error(await extractDownloadError(error))
  }
}

export async function exportGrupoAdhocCsv(idsLattes: string[], period: PeriodFilter = {}): Promise<CsvDownload> {
  try {
    const response = await api.post<Blob>('/dashboard/grupo-adhoc/exportacao.csv', { idsLattes }, {
      params: period,
      responseType: 'blob',
    })
    return { blob: response.data, fileName: extractFileName(response.headers['content-disposition']) }
  } catch (error) {
    throw new Error(await extractDownloadError(error))
  }
}

function extractFileName(contentDisposition: string | undefined): string {
  const encodedMatch = contentDisposition?.match(/filename\*=UTF-8''([^;]+)/i)
  if (encodedMatch?.[1]) {
    return decodeURIComponent(encodedMatch[1])
  }

  const plainMatch = contentDisposition?.match(/filename="?([^";]+)"?/i)
  return plainMatch?.[1] ?? 'lattesvision-exportacao.csv'
}

async function extractDownloadError(error: unknown): Promise<string> {
  const axiosError = error as AxiosError<Blob | ApiErrorResponse>
  const data = axiosError.response?.data
  if (data instanceof Blob) {
    try {
      const body = JSON.parse(await data.text()) as ApiErrorResponse
      return body.message ?? 'Não foi possível gerar a exportação.'
    } catch {
      return 'Não foi possível gerar a exportação.'
    }
  }
  return extractApiError(error)
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
