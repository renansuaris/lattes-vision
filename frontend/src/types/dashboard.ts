export interface ResumoPesquisador {
  idLattes: string
  nomeCompleto: string
  dataUltimaAtualizacao: string | null
  dataSincronizacao: string | null
  totalArtigos: number
  totalOrientacoes: number
  totalOrientacoesConcluidas: number
  totalOrientacoesEmAndamento: number
  totalProjetos: number
  totalBancas: number
  totalProducoesTecnicas: number
  totalTrabalhosEventos: number
  totalLivros: number
  totalCapitulos: number
  totalProducaoBibliografica: number
}

export interface PeriodFilter {
  anoInicial?: number
  anoFinal?: number
}

export interface ResumoGrupo {
  totalPesquisadores: number
  pesquisadores: ResumoPesquisador[]
  totalArtigos: number
  totalOrientacoes: number
  totalOrientacoesConcluidas: number
  totalOrientacoesEmAndamento: number
  totalProjetos: number
  totalBancas: number
  totalProducoesTecnicas: number
  totalTrabalhosEventos: number
  totalLivros: number
  totalCapitulos: number
  totalProducaoBibliografica: number
}

export interface SerieGrupoItem {
  ano: number
  artigos: number
  trabalhosEventos: number
  orientacoes: number
  projetos: number
  bancas: number
  producoesTecnicas: number
}

export interface SerieTemporalItem {
  ano: number
  quantidade: number
}

export interface CategoriaQuantidadeItem {
  categoria: string
  quantidade: number
}

export interface SerieOrientacaoStatusItem {
  ano: number
  concluidas: number
  emAndamento: number
}

export type EstratoQualis = 'A1' | 'A2' | 'A3' | 'A4' | 'B1' | 'B2' | 'B3' | 'B4' | 'C'

export type StatusQualis = 'CLASSIFICADO' | 'SEM_ISSN' | 'NAO_CLASSIFICADO'

export interface ArticleFilters extends PeriodFilter {
  ano?: number
  estrato?: EstratoQualis
  statusQualis?: StatusQualis
}

export interface ArticleDetail {
  id: number | null
  year: number | null
  title: string | null
  journal: string | null
  issn: string | null
  qualisStratum: EstratoQualis | null
  qualisStatus: StatusQualis | null
}

export interface ArticlePage {
  items: ArticleDetail[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type OrientationStatus = 'CONCLUIDA' | 'EM_ANDAMENTO'

export interface OrientationFilters extends PeriodFilter {
  ano?: number
  status?: OrientationStatus
  tipo?: string
}

export interface OrientationDetail {
  id: number | null
  year: number | null
  orientedName: string | null
  workTitle: string | null
  type: string | null
  status: OrientationStatus | null
}

export interface OrientationPage {
  items: OrientationDetail[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface BancaFilters extends PeriodFilter {
  tipo?: string
}

export interface BancaDetail {
  id: number | null
  year: number | null
  type: string | null
  candidateName: string | null
  workTitle: string | null
}

export interface BancaPage {
  items: BancaDetail[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface EventWorkFilters extends PeriodFilter {
  ano?: number
}

export interface EventWorkDetail {
  id: number | null
  year: number | null
  title: string | null
  eventName: string | null
  authors: string | null
  type: string | null
}

export interface EventWorkPage {
  items: EventWorkDetail[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface CoberturaQualis {
  totalArtigos: number
  classificados: number
  semIssn: number
  naoClassificados: number
  percentualClassificados: number
}

export interface EstratoQuantidadeItem {
  estrato: EstratoQualis
  quantidade: number
}

export interface SerieQualisAnoItem {
  ano: number
  estratos: Record<EstratoQualis, number>
}

export interface ResumoQualis {
  baseReferencia: string
  fonte: string
  cobertura: CoberturaQualis
  distribuicao: EstratoQuantidadeItem[]
  serieAnual: SerieQualisAnoItem[]
}

export interface RankingPesquisadorItem {
  idLattes: string
  nomeCompleto: string
  metrica: TipoRankingGrupo
  quantidade: number
}

export type TipoSerie =
  | 'artigos'
  | 'orientacoes'
  | 'projetos'
  | 'bancas'
  | 'producoes_tecnicas'
  | 'trabalhos_eventos'

export type TipoRankingGrupo =
  | 'total'
  | 'artigos'
  | 'orientacoes'
  | 'projetos'
  | 'bancas'
  | 'producoes_tecnicas'
  | 'trabalhos_eventos'

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  code: string
  message: string
  path: string
}
