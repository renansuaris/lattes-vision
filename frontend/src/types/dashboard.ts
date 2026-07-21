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
