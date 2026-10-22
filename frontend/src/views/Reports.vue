<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import api from '../api'

const labs = ref([])
const dateRange = ref([])
const labId = ref('')
const report = ref({ summary: {}, daily: [], statuses: [], labs: [] })
const trendRef = ref(), statusRef = ref(), labRef = ref()
let trendChart, statusChart, labChart
const params = computed(() => ({
  fromDate: dateRange.value?.[0] || undefined,
  toDate: dateRange.value?.[1] || undefined,
  labId: labId.value || undefined
}))
const summary = computed(() => report.value.summary || {})

function setCharts() {
  trendChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 42, right: 18, top: 20, bottom: 48 },
    xAxis: { type: 'category', data: report.value.daily.map(x => x.reportDate) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, data: report.value.daily.map(x => x.amount), areaStyle: { opacity: 0.12 }, itemStyle: { color: '#168a92' } }]
  })
  statusChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['42%', '70%'], data: report.value.statuses.map(x => ({ name: ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', CANCELLED: '已取消', COMPLETED: '已完成' })[x.name] || x.name, value: x.amount })) }]
  })
  labChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 42, right: 18, top: 20, bottom: 48 },
    xAxis: { type: 'category', data: report.value.labs.map(x => x.name), axisLabel: { interval: 0, rotate: 20 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: report.value.labs.map(x => x.amount), itemStyle: { color: '#3f8fb2', borderRadius: [5, 5, 0, 0] } }]
  })
}

async function load() {
  report.value = (await api.get('/manage/reports', { params: params.value })).data
  setCharts()
}

async function exportExcel() {
  const blob = await api.get('/manage/reports/export', { params: params.value, responseType: 'blob' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '实验室预约报表.xlsx'
  link.click()
  URL.revokeObjectURL(url)
}

function resizeCharts() {
  trendChart?.resize()
  statusChart?.resize()
  labChart?.resize()
}

onMounted(async () => {
  const today = new Date()
  const from = new Date(today)
  from.setDate(from.getDate() - 29)
  const format = date => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  dateRange.value = [format(from), format(today)]
  labs.value = (await api.get('/labs')).data
  trendChart = echarts.init(trendRef.value)
  statusChart = echarts.init(statusRef.value)
  labChart = echarts.init(labRef.value)
  await load()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  statusChart?.dispose()
  labChart?.dispose()
})
</script>

<template>
  <div class="page reports">
    <div class="page-head">
      <div><h2 class="page-title">统计报表</h2><div class="page-sub">按日期和实验室分析预约情况，并导出 Excel 报表</div></div>
      <el-button type="success" @click="exportExcel">导出 Excel</el-button>
    </div>
    <section class="panel filters">
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-select v-model="labId" clearable placeholder="全部实验室" style="width: 220px">
        <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </section>
    <section class="metrics">
      <div class="metric"><span>预约总数</span><b>{{ summary.total || 0 }}</b></div>
      <div class="metric"><span>待审核</span><b>{{ summary.pending || 0 }}</b></div>
      <div class="metric"><span>已通过</span><b>{{ summary.approved || 0 }}</b></div>
      <div class="metric"><span>已完成</span><b>{{ summary.completed || 0 }}</b></div>
      <div class="metric"><span>已取消</span><b>{{ summary.cancelled || 0 }}</b></div>
    </section>
    <section class="charts">
      <div class="panel chart-panel"><h3>预约趋势</h3><div ref="trendRef" class="chart"></div></div>
      <div class="panel chart-panel"><h3>预约状态分布</h3><div ref="statusRef" class="chart"></div></div>
      <div class="panel chart-panel wide"><h3>各实验室预约数量</h3><div ref="labRef" class="chart"></div></div>
    </section>
  </div>
</template>

<style scoped>
.filters{display:flex;align-items:center;gap:12px;padding:16px;margin:18px 0}
.metrics{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:18px}
.metric{background:#fff;border:1px solid var(--border);border-radius:12px;padding:18px 20px;display:flex;flex-direction:column;gap:9px;color:#71858c}
.metric b{font-size:26px;color:var(--ink)}
.charts{display:grid;grid-template-columns:1fr 1fr;gap:16px}
.chart-panel{padding:18px 20px}
.chart-panel h3{margin:0;font-size:15px}
.chart{height:300px}
.wide{grid-column:1 / -1}
@media(max-width:900px){.metrics{grid-template-columns:repeat(2,1fr)}}
@media(max-width:650px){.filters{align-items:stretch;flex-direction:column}.charts{grid-template-columns:1fr}.wide{grid-column:auto}}
</style>
