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
