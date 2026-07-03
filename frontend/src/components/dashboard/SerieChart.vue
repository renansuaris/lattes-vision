<template>
  <div ref="chartRoot" class="chart-shell" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { SerieTemporalItem } from '../../types/dashboard'

const props = defineProps<{
  data: SerieTemporalItem[]
  label: string
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = '"Segoe UI", SegoeUI, "Helvetica Neue", Helvetica, Arial, sans-serif'

function renderChart() {
  if (!chartRoot.value) {
    return
  }

  if (!chart) {
    chart = echarts.init(chartRoot.value)
  }

  chart.setOption({
    animationDuration: 500,
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#1f2937',
    },
    grid: {
      top: 24,
      left: 40,
      right: 16,
      bottom: 36,
    },
    tooltip: {
      trigger: 'axis',
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
    series: [
      {
        name: props.label,
        type: 'bar',
        data: props.data.map((item) => item.quantidade),
        barWidth: '42%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: '#1976c9',
        },
      },
    ],
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
  () => [props.data, props.label],
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
