<template>
  <div ref="chartRoot" class="chart-shell chart-shell-qualis" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { EstratoQualis, SerieQualisAnoItem } from '../../types/dashboard'

const props = defineProps<{
  data: SerieQualisAnoItem[]
  interactive?: boolean
}>()

const emit = defineEmits<{
  selectQualis: [payload: { ano: number; estrato: EstratoQualis }]
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = 'Poppins, "Segoe UI", Arial, sans-serif'
const estratos: EstratoQualis[] = ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4', 'C']
const colors: Record<EstratoQualis, string> = {
  A1: '#213c78',
  A2: '#29488d',
  A3: '#6b82b0',
  A4: '#9aa9c8',
  B1: '#4f772d',
  B2: '#719b45',
  B3: '#98b969',
  B4: '#bfd39a',
  C: '#9ca3af',
}

function renderChart() {
  if (!chartRoot.value) {
    return
  }

  if (!chart) {
    chart = echarts.init(chartRoot.value)
  }

  chart.setOption({
    animationDuration: 500,
    color: estratos.map((estrato) => colors[estrato]),
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#343a40',
    },
    legend: {
      type: 'scroll',
      top: 4,
      data: estratos,
      textStyle: {
        color: '#495057',
        fontFamily: chartFontFamily,
      },
    },
    grid: {
      top: 58,
      left: 42,
      right: 18,
      bottom: 42,
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#ffffff',
      borderColor: '#dee2e6',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
    },
    xAxis: {
      type: 'category',
      data: props.data.map((item) => item.ano),
      axisLine: { lineStyle: { color: '#dee2e6' } },
      axisLabel: { color: '#6c757d', fontFamily: chartFontFamily },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: 'Artigos',
      nameTextStyle: { color: '#6c757d', fontFamily: chartFontFamily },
      splitLine: { lineStyle: { color: '#e9ecef' } },
      axisLabel: { color: '#6c757d', fontFamily: chartFontFamily },
    },
    series: estratos.map((estrato) => ({
      name: estrato,
      type: 'bar',
      stack: 'qualis',
      emphasis: { focus: 'series' },
      data: props.data.map((item) => item.estratos[estrato] ?? 0),
      cursor: props.interactive ? 'pointer' : 'default',
    })),
  }, true)
}

function resizeChart() {
  chart?.resize()
}

function handleChartClick(event: { dataIndex?: number; seriesName?: string }) {
  if (!props.interactive || event.dataIndex == null || !event.seriesName) {
    return
  }

  const ano = props.data[event.dataIndex]?.ano
  const estrato = event.seriesName as EstratoQualis
  if (ano != null && estratos.includes(estrato)) {
    emit('selectQualis', { ano, estrato })
  }
}

onMounted(() => {
  renderChart()
  chart?.on('click', handleChartClick)
  window.addEventListener('resize', resizeChart)
})

watch(
  () => props.data,
  () => renderChart(),
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>
