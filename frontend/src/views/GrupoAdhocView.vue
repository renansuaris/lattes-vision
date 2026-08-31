<template>
  <main class="page-shell">
    <header class="dashboard-header">
      <div>
        <RouterLink class="back-link" to="/">Voltar para consulta</RouterLink>
        <p class="eyebrow">Grupo</p>
        <h1 class="dashboard-title">Análise de grupo</h1>
        <p class="lead">{{ totalIdsLabel }}</p>
      </div>

      <div class="dashboard-actions">
        <button class="button button-secondary" type="button" @click.prevent>
          Exportar dados
        </button>
      </div>
    </header>

    <section v-if="store.errorResumo" class="status-box error">
      {{ store.errorResumo }}
    </section>

    <section v-else-if="store.loadingResumo" class="status-box">
      Carregando análise de grupo...
    </section>

    <template v-else-if="store.resumoGrupo">
      <section class="layout-grid stats-grid">
        <article
          v-for="card in summaryCards"
          :key="card.label"
          class="stat-card"
        >
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span class="stat-meta">{{ card.meta }}</span>
        </article>
      </section>

      <BibliographicProductionPanel
        :total="store.resumoGrupo.totalProducaoBibliografica"
        :artigos="store.resumoGrupo.totalArtigos"
        :trabalhos-eventos="store.resumoGrupo.totalTrabalhosEventos"
        :livros="store.resumoGrupo.totalLivros"
        :capitulos="store.resumoGrupo.totalCapitulos"
      />

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Produção por ano</h2>
              <p class="section-subtitle">Produção acadêmica do grupo ao longo dos anos.</p>
            </div>
          </div>

          <section v-if="store.errorSerie && store.serieGrupo.length === 0" class="status-box error">
            {{ store.errorSerie }}
          </section>

          <section v-else-if="store.loadingSerie && store.serieGrupo.length === 0" class="status-box">
            Carregando séries do grupo...
          </section>

          <section v-else-if="store.serieGrupo.length === 0" class="status-box">
            Sem dados anuais para o grupo analisado.
          </section>

          <GrupoStackedLineChart
            v-else
            :data="store.serieGrupo"
          />
        </div>
      </section>

      <QualisSummaryPanel
        v-if="store.resumoQualisGrupo"
        :resumo="store.resumoQualisGrupo"
        contexto="grupo"
      />

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Ranking de pesquisadores</h2>
              <p class="section-subtitle">Comparação por indicador selecionado.</p>
            </div>

            <label class="ranking-select">
              <span class="field-label">Indicador</span>
              <select v-model="selectedRankingMetric" class="select-input">
                <option
                  v-for="option in rankingOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </label>
          </div>

          <section v-if="store.errorRanking" class="status-box error">
            {{ store.errorRanking }}
          </section>

          <section v-else-if="store.loadingRanking && store.rankingGrupo.length === 0" class="status-box">
            Carregando ranking do grupo...
          </section>

          <section v-else-if="store.rankingGrupo.length === 0" class="status-box">
            Sem dados para montar o ranking do grupo.
          </section>

          <GrupoRankingChart
            v-else
            :data="store.rankingGrupo"
            :metric-label="selectedRankingLabel"
          />
        </div>
      </section>

      <section class="panel chart-panel">
        <div class="panel-body">
          <div class="section-header">
            <div>
              <h2 class="section-title">Membros do grupo</h2>
              <p class="section-subtitle">Pesquisadores incluídos nesta análise.</p>
            </div>
          </div>

          <div class="researcher-list">
            <article
              v-for="pesquisador in store.resumoGrupo.pesquisadores"
              :key="pesquisador.idLattes"
              class="researcher-row"
            >
              <div>
                <strong>{{ pesquisador.nomeCompleto || 'Nome não informado' }}</strong>
                <span>ID Lattes {{ pesquisador.idLattes }}</span>
              </div>
              <RouterLink
                class="button button-secondary"
                :to="{ name: 'pesquisador', params: { id: pesquisador.idLattes } }"
              >
                Ver individual
              </RouterLink>
            </article>
          </div>
        </div>
      </section>
    </template>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import BibliographicProductionPanel from '../components/dashboard/BibliographicProductionPanel.vue'
import GrupoRankingChart from '../components/dashboard/GrupoRankingChart.vue'
import GrupoStackedLineChart from '../components/dashboard/GrupoStackedLineChart.vue'
import QualisSummaryPanel from '../components/dashboard/QualisSummaryPanel.vue'
import { useDashboardStore } from '../stores/dashboard'
import type { TipoRankingGrupo } from '../types/dashboard'

const route = useRoute()
const store = useDashboardStore()
const selectedRankingMetric = ref<TipoRankingGrupo>('total')

const rankingOptions: Array<{ label: string; value: TipoRankingGrupo }> = [
  { label: 'Total geral', value: 'total' },
  { label: 'Artigos', value: 'artigos' },
  { label: 'Trabalhos em eventos', value: 'trabalhos_eventos' },
  { label: 'Produções técnicas', value: 'producoes_tecnicas' },
  { label: 'Orientações', value: 'orientacoes' },
  { label: 'Projetos', value: 'projetos' },
  { label: 'Bancas', value: 'bancas' },
]

const routeIds = computed(() => {
  const ids = String(route.query.ids ?? '')
  return ids
    .split(',')
    .map((id) => id.trim())
    .filter((id) => /^\d{16}$/.test(id))
})

const totalIdsLabel = computed(() => {
  const total = store.resumoGrupo?.totalPesquisadores ?? routeIds.value.length
  return `${total} currículos analisados`
})

const summaryCards = computed(() => {
  const resumo = store.resumoGrupo

  if (!resumo) {
    return []
  }

  return [
    { label: 'Orientações', value: resumo.totalOrientacoes, meta: `${resumo.totalOrientacoesConcluidas} concluídas / ${resumo.totalOrientacoesEmAndamento} em andamento` },
    { label: 'Projetos', value: resumo.totalProjetos, meta: 'Pesquisa, extensão e desenvolvimento.' },
    { label: 'Bancas', value: resumo.totalBancas, meta: 'Participações registradas.' },
    { label: 'Prod. técnicas', value: resumo.totalProducoesTecnicas, meta: 'Softwares, produtos e outros.' },
  ]
})

const selectedRankingLabel = computed(() => {
  return rankingOptions.find((option) => option.value === selectedRankingMetric.value)?.label ?? 'Total geral'
})

watch(
  () => routeIds.value.join(','),
  async () => {
    if (routeIds.value.length < 2) {
      return
    }

    if (store.resumoGrupo && store.grupoIds.join(',') === routeIds.value.join(',')) {
      if (store.serieGrupo.length === 0 || !store.resumoQualisGrupo) {
        await store.loadSerieGrupoAdhoc(routeIds.value)
      }
      if (store.rankingGrupo.length === 0 || store.rankingMetrica !== selectedRankingMetric.value) {
        await store.loadRankingGrupoAdhoc(routeIds.value, selectedRankingMetric.value)
      }
      return
    }

    await store.loadGrupoAdhoc(routeIds.value)
    await store.loadSerieGrupoAdhoc(routeIds.value)
    await store.loadRankingGrupoAdhoc(routeIds.value, selectedRankingMetric.value)
  },
  { immediate: true },
)

watch(
  () => selectedRankingMetric.value,
  async () => {
    if (routeIds.value.length < 2 || !store.resumoGrupo) {
      return
    }

    await store.loadRankingGrupoAdhoc(routeIds.value, selectedRankingMetric.value)
  },
)
</script>
