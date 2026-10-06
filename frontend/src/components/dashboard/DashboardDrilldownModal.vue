<template>
  <Teleport to="body">
    <div v-if="open" class="modal-backdrop article-modal-backdrop" role="presentation" @click.self="emit('close')">
      <section
        class="article-drilldown-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="dashboard-drilldown-title"
      >
        <header class="article-drilldown-modal__header">
          <div>
            <p class="eyebrow">Auditoria de dados</p>
            <h2 id="dashboard-drilldown-title">{{ title }}</h2>
          </div>
          <button class="button button-secondary" type="button" @click="emit('close')">
            Fechar
          </button>
        </header>

        <div v-if="loading" class="status-box">{{ loadingMessage }}</div>
        <div v-else-if="error" class="status-box error">{{ error }}</div>
        <div v-else-if="!page || rows.length === 0" class="status-box">
          {{ emptyMessage }}
        </div>

        <template v-else>
          <div class="article-table-wrap">
            <table class="article-table">
              <thead>
                <tr>
                  <th v-for="column in columns" :key="column.key" scope="col">{{ column.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rows" :key="row.id">
                  <td v-for="column in columns" :key="column.key" :class="column.cellClass">{{ row.cells[column.key] ?? '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="article-pagination">
            <span>{{ page.totalElements }} {{ page.totalElements === 1 ? totalLabel : totalLabelPlural }}</span>
            <div>
              <button
                class="button button-secondary"
                type="button"
                :disabled="page.page === 0"
                @click="emit('changePage', page.page - 1)"
              >
                Anterior
              </button>
              <span>Página {{ page.page + 1 }} de {{ page.totalPages }}</span>
              <button
                class="button button-secondary"
                type="button"
                :disabled="page.page + 1 >= page.totalPages"
                @click="emit('changePage', page.page + 1)"
              >
                Próxima
              </button>
            </div>
          </footer>
        </template>
      </section>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
export interface DrilldownColumn {
  key: string
  label: string
  cellClass?: string
}

export interface DrilldownRow {
  id: string | number
  cells: Record<string, string>
}

interface DrilldownPage {
  page: number
  totalElements: number
  totalPages: number
}

defineProps<{
  open: boolean
  title: string
  columns: DrilldownColumn[]
  rows: DrilldownRow[]
  page: DrilldownPage | null
  loading: boolean
  error: string | null
  loadingMessage: string
  emptyMessage: string
  totalLabel: string
  totalLabelPlural: string
}>()

const emit = defineEmits<{
  close: []
  changePage: [page: number]
}>()
</script>
