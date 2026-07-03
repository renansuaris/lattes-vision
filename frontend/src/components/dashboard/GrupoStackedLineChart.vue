<template>
  <div ref="chartRoot" class="chart-shell chart-shell-tall" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { SerieGrupoItem } from '../../types/dashboard'

const props = defineProps<{
  data: SerieGrupoItem[]
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = '"Segoe UI", SegoeUI, "Helvetica Neue", Helvetica, Arial, sans-serif'

const seriesConfig = [
  { key: 'artigos', name: 'Artigos em periódicos', color: '#1976c9' },
  { key: 'trabalhosEventos', name: 'Trabalhos em eventos', color: '#0b3f75' },
  { key: 'producoesTecnicas', name: 'Produções técnicas', color: '#1e8e3e' },
  { key: 'orientacoes', name: 'Orientações', color: '#8a5cf6' },
  { key: 'projetos', name: 'Projetos', color: '#d97706' },
  { key: 'bancas', name: 'Bancas', color: '#6b7280' },
] as const

function renderChart() {
  if (!chartRoot.value) {
    return
  }

  if (!chart) {
    chart = echarts.init(chartRoot.value)
  }

  chart.setOption({
    animationDuration: 500,
    color: seriesConfig.map((item) => item.color),
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#1f2937',
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#d8e3f0',
      borderWidth: 1,
      textStyle: {
        color: '#1f2937',
        fontFamily: chartFontFamily,
      },
    },
    legend: {
      top: 0,
      type: 'scroll',
      textStyle: {
        color: '#1f2937',
        fontFamily: chartFontFamily,
      },
    },
    grid: {
      top: 58,
      left: 42,
      right: 18,
      bottom: 36,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.map((item) => item.ano),
      axisLine: {
        lineStyle: {
          color: '#d8e3f0',
        },
      },
      axisLabel: {
        color: '#6b7280',
        fontFamily: chartFontFamily,
      },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: {
        lineStyle: {
          color: '#e6eef7',
        },
      },
      axisLabel: {
        color: '#6b7280',
        fontFamily: chartFontFamily,
      },
    },
    series: seriesConfig.map((serie) => ({
      name: serie.name,
      type: 'line',
      stack: 'total',
      smooth: false,
      symbolSize: 5,
      emphasis: {
        focus: 'series',
      },
      lineStyle: {
        width: 2,
      },
      data: props.data.map((item) => item[serie.key]),
    })),
  })
}

function resizeChart() {
  chart?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', resizeChart)
})

watch(
  () => props.data,
  () => {
    renderChart()
  },
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>
