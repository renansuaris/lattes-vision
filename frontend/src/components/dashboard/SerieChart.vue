<template>
  <div ref="chartRoot" class="chart-shell" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { SerieTemporalItem } from '../../types/dashboard'

const props = withDefaults(defineProps<{
  data: SerieTemporalItem[]
  label: string
  chartType?: 'bar' | 'line'
  interactive?: boolean
}>(), {
  chartType: 'bar',
  interactive: false,
})

const emit = defineEmits<{
  selectYear: [year: number]
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

  const values = props.data.map((item) => item.quantidade)
  const series = props.chartType === 'line'
    ? {
        name: props.label,
        type: 'line' as const,
        data: values,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#29488d',
        },
        itemStyle: {
          color: '#29488d',
          borderColor: '#ffffff',
          borderWidth: 2,
        },
        cursor: props.interactive ? 'pointer' : 'default',
      }
    : {
        name: props.label,
        type: 'bar' as const,
        data: values,
        barWidth: '42%',
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
          color: '#29488d',
        },
        cursor: props.interactive ? 'pointer' : 'default',
      }

  chart.setOption({
    animationDuration: 500,
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#343a40',
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
    series: [series],
  }, true)
}

function resizeChart() {
  chart?.resize()
}

function handleChartClick(event: { dataIndex?: number }) {
  if (!props.interactive || event.dataIndex == null) {
    return
  }

  const item = props.data[event.dataIndex]
  if (item) {
    emit('selectYear', item.ano)
  }
}

onMounted(() => {
  renderChart()
  chart?.on('click', handleChartClick)
  window.addEventListener('resize', resizeChart)
})

watch(
  () => [props.data, props.label, props.chartType],
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
