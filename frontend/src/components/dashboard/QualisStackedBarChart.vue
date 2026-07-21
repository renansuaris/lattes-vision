<template>
  <div ref="chartRoot" class="chart-shell chart-shell-qualis" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { EstratoQualis, SerieQualisAnoItem } from '../../types/dashboard'

const props = defineProps<{
  data: SerieQualisAnoItem[]
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = 'Inter, "Segoe UI", SegoeUI, "Helvetica Neue", Helvetica, Arial, sans-serif'
const estratos: EstratoQualis[] = ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4', 'C']
const colors: Record<EstratoQualis, string> = {
  A1: '#0b4f6c',
  A2: '#1976c9',
  A3: '#4f9bd3',
  A4: '#8dc2df',
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
      color: '#1f2937',
    },
    legend: {
      type: 'scroll',
      top: 4,
      data: estratos,
      textStyle: {
        color: '#4b5563',
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
      borderColor: '#d8e3f0',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#1f2937',
        fontFamily: chartFontFamily,
      },
    },
    xAxis: {
      type: 'category',
      data: props.data.map((item) => item.ano),
      axisLine: { lineStyle: { color: '#d8e3f0' } },
      axisLabel: { color: '#6b7280', fontFamily: chartFontFamily },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: 'Artigos',
      nameTextStyle: { color: '#6b7280', fontFamily: chartFontFamily },
      splitLine: { lineStyle: { color: '#e6eef7' } },
      axisLabel: { color: '#6b7280', fontFamily: chartFontFamily },
    },
    series: estratos.map((estrato) => ({
      name: estrato,
      type: 'bar',
      stack: 'qualis',
      emphasis: { focus: 'series' },
      data: props.data.map((item) => item.estratos[estrato] ?? 0),
    })),
  }, true)
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
  () => renderChart(),
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>
