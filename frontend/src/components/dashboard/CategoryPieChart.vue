<template>
  <div ref="chartRoot" class="chart-shell chart-shell-pie" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { CategoriaQuantidadeItem } from '../../types/dashboard'

const props = defineProps<{
  data: CategoriaQuantidadeItem[]
  label: string
}>()

const chartRoot = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const chartFontFamily = 'Poppins, "Segoe UI", Arial, sans-serif'

function formatCategoryLabel(categoria: string) {
  return categoria === 'Graduacao' ? 'Graduação' : categoria
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
    color: ['#29488d', '#6b82b0', '#5c8a62', '#b96565', '#7567a8', '#4f8d9b', '#b58a4a', '#73859f'],
    textStyle: {
      fontFamily: chartFontFamily,
      color: '#343a40',
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#ffffff',
      borderColor: '#dee2e6',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
      formatter: '{b}: <strong>{c}</strong> ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: '56%',
      top: 'middle',
      itemGap: 10,
      textStyle: {
        color: '#343a40',
        fontFamily: chartFontFamily,
      },
    },
    series: [
      {
        name: props.label,
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderColor: '#ffffff',
          borderWidth: 2,
          borderRadius: 2,
        },
        label: {
          show: false,
        },
        labelLine: {
          show: false,
        },
        data: props.data.map((item) => ({
          name: formatCategoryLabel(item.categoria),
          value: item.quantidade,
        })),
      },
    ],
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
