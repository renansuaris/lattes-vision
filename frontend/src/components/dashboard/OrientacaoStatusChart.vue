<template>
  <div ref="chartRoot" class="chart-shell" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { SerieOrientacaoStatusItem } from '../../types/dashboard'

const props = defineProps<{
  data: SerieOrientacaoStatusItem[]
  interactive?: boolean
}>()

const emit = defineEmits<{
  selectOrientation: [payload: { ano: number; status: 'CONCLUIDA' | 'EM_ANDAMENTO' }]
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = 'Poppins, "Segoe UI", Arial, sans-serif'

function renderChart() {
  if (!chartRoot.value) {
    return
  }

  if (!chart) {
    chart = echarts.init(chartRoot.value)
  }

  chart.setOption({
    animationDuration: 500,
    color: ['#639922', '#C0DD97'],
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#343a40',
    },
    grid: {
      top: 24,
      left: 40,
      right: 16,
      bottom: 72,
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      backgroundColor: '#ffffff',
      borderColor: '#dee2e6',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
    },
    legend: {
      bottom: 8,
      left: 'center',
      itemGap: 18,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
    },
    xAxis: {
      type: 'category',
      data: props.data.map((item) => item.ano),
      axisLine: {
        lineStyle: {
          color: '#dee2e6',
        },
      },
      axisLabel: {
        color: '#6c757d',
        fontFamily: chartFontFamily,
      },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: {
        lineStyle: {
          color: '#e9ecef',
        },
      },
      axisLabel: {
        color: '#6c757d',
        fontFamily: chartFontFamily,
      },
    },
    series: [
      {
        name: 'Concluídas',
        type: 'bar',
        data: props.data.map((item) => item.concluidas),
        barMaxWidth: 28,
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
          color: '#639922',
        },
        cursor: props.interactive ? 'pointer' : 'default',
      },
      {
        name: 'Em andamento',
        type: 'bar',
        data: props.data.map((item) => item.emAndamento),
        barMaxWidth: 28,
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
          color: '#C0DD97',
        },
        cursor: props.interactive ? 'pointer' : 'default',
      },
    ],
  }, true)
}

function resizeChart() {
  chart?.resize()
}

function handleChartClick(event: { dataIndex?: number; seriesName?: string }) {
  if (!props.interactive || event.dataIndex == null) {
    return
  }

  const ano = props.data[event.dataIndex]?.ano
  const status = event.seriesName === 'Concluídas'
    ? 'CONCLUIDA'
    : event.seriesName === 'Em andamento'
      ? 'EM_ANDAMENTO'
      : null
  if (ano != null && status) {
    emit('selectOrientation', { ano, status })
  }
}

onMounted(() => {
  renderChart()
  chart?.on('click', handleChartClick)
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
