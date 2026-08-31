<template>
  <div ref="chartRoot" class="chart-shell chart-shell-ranking" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { RankingPesquisadorItem } from '../../types/dashboard'

const props = defineProps<{
  data: RankingPesquisadorItem[]
  metricLabel: string
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = 'Poppins, "Segoe UI", Arial, sans-serif'

function shortName(item: RankingPesquisadorItem) {
  return item.nomeCompleto || `ID ${item.idLattes}`
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
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#343a40',
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      backgroundColor: '#ffffff',
      borderColor: '#dee2e6',
      borderWidth: 1,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
      formatter(params: unknown) {
        const [item] = params as Array<{ dataIndex: number }>
        const rankingItem = props.data[item.dataIndex]
        return `${shortName(rankingItem)}<br/>${props.metricLabel}: <strong>${rankingItem.quantidade}</strong>`
      },
    },
    grid: {
      top: 18,
      left: 170,
      right: 42,
      bottom: 30,
    },
    xAxis: {
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
    yAxis: {
      type: 'category',
      inverse: true,
      data: props.data.map(shortName),
      axisLine: {
        lineStyle: {
          color: '#dee2e6',
        },
      },
      axisLabel: {
        color: '#495057',
        fontFamily: chartFontFamily,
        width: 150,
        overflow: 'truncate',
      },
    },
    series: [
      {
        name: props.metricLabel,
        type: 'bar',
        data: props.data.map((item) => item.quantidade),
        barMaxWidth: 28,
        itemStyle: {
          color: '#29488d',
          borderRadius: [0, 2, 2, 0],
        },
        label: {
          show: true,
          position: 'right',
          color: '#343a40',
          fontFamily: chartFontFamily,
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
  () => [props.data, props.metricLabel],
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
