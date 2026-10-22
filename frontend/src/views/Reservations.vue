<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const auth = useAuthStore()
const manager = computed(() => auth.user?.role === 'LAB_ADMIN')
const list = ref([])
const labs = ref([])
const status = ref('')
const visible = ref(false)
const view = ref('list')
const calendarLabId = ref(null)
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const selectedDate = ref(formatDate(new Date()))
const calendarEvents = ref([])
const recommendations = ref([])
const recommendationVisible = ref(false)
const availability = ref([])
const timelineTrack = ref(null)
const selectedInterval = ref(null)
const compactTimeline = ref(window.innerWidth < 600)
const form = reactive({ labId: null, reserveDate: '', startTime: '', endTime: '', purpose: '', participants: 1, remark: '' })
const selectedLab = computed(() => labs.value.find(x => x.id === form.labId))
const openStart = computed(() => selectedLab.value?.openTime?.slice(0, 5) || '08:00')
const openEnd = computed(() => selectedLab.value?.closeTime?.slice(0, 5) || '21:30')
const openMinutes = computed(() => toMinutes(openStart.value))
const closeMinutes = computed(() => toMinutes(openEnd.value))
const timelineDuration = computed(() => Math.max(0, closeMinutes.value - openMinutes.value))
const slotOffsets = computed(() => {
  const offsets = []
  for (let offset = 0; offset < timelineDuration.value; offset += 30) offsets.push(offset)
  if (timelineDuration.value > 0 && offsets.at(-1) !== timelineDuration.value) offsets.push(timelineDuration.value)
  return offsets
})
const timelineTicks = computed(() => slotOffsets.value
  .map(offset => ({ offset, label: formatTime(openMinutes.value + offset) }))
  .filter((tick, index) => index === 0 || toMinutes(tick.label) % (compactTimeline.value ? 120 : 60) === 0))
const occupiedIntervals = computed(() => availability.value
  .filter(item => item.status === 'APPROVED')
  .map(item => ({
    start: Math.max(0, toMinutes(item.startTime) - openMinutes.value),
    end: Math.min(timelineDuration.value, toMinutes(item.endTime) - openMinutes.value),
    title: `${item.startTime.slice(0, 5)}—${item.endTime.slice(0, 5)}已占用`
  }))
  .filter(item => item.end > item.start))
const selectedStyle = computed(() => selectedInterval.value ? intervalStyle(selectedInterval.value.start, selectedInterval.value.end) : {})
const selectedDurationLabel = computed(() => {
  if (!selectedInterval.value) return ''
  const hours = (selectedInterval.value.end - selectedInterval.value.start) / 60
  return Number.isInteger(hours) ? `${hours}小时` : `${hours.toFixed(1)}小时`
})
const labels = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', CANCELLED: '已取消', COMPLETED: '已完成' }
const types = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info', COMPLETED: '' }
const weekdays = ['一', '二', '三', '四', '五', '六', '日']
let calendarRequest = 0

function formatDate(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function toMinutes(value) {
  const [hours = '0', minutes = '0'] = String(value || '0:0').split(':')
  return Number(hours) * 60 + Number(minutes)
}

function formatTime(totalMinutes) {
  const normalized = Math.max(0, Math.round(totalMinutes))
  return `${String(Math.floor(normalized / 60)).padStart(2, '0')}:${String(normalized % 60).padStart(2, '0')}`
}

function intervalStyle(start, end) {
  if (!timelineDuration.value) return {}
  return {
    left: `${start / timelineDuration.value * 100}%`,
    width: `${(end - start) / timelineDuration.value * 100}%`
  }
}

function monthBounds(date) {
  return { from: formatDate(new Date(date.getFullYear(), date.getMonth(), 1)), to: formatDate(new Date(date.getFullYear(), date.getMonth() + 1, 0)) }
}

const calendarDays = computed(() => {
  const first = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth(), 1)
  const mondayOffset = (first.getDay() + 6) % 7
  const start = new Date(first)
  start.setDate(start.getDate() - mondayOffset)
  return Array.from({ length: 42 }, (_, i) => {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    return { date, key: formatDate(date), inMonth: date.getMonth() === calendarMonth.value.getMonth() }
  })
})

const eventsByDate = computed(() => calendarEvents.value.reduce((groups, event) => {
  ;(groups[event.reserveDate] ||= []).push(event)
  return groups
}, {}))
const selectedEvents = computed(() => eventsByDate.value[selectedDate.value] || [])
const selectedLabForCalendar = computed(() => labs.value.find(lab => lab.id === calendarLabId.value))

async function load() {
  list.value = (await api.get(manager.value ? '/manage/reservations' : '/reservations/my', { params: { status: status.value || undefined } })).data
}

async function loadCalendar() {
  const requestId = ++calendarRequest
  if (!calendarLabId.value) {
    calendarEvents.value = []
    return
  }
  const { from, to } = monthBounds(calendarMonth.value)
  try {
    const data = (await api.get('/reservations/calendar', {
      params: { labId: calendarLabId.value, from, to }
    })).data
    if (requestId === calendarRequest) calendarEvents.value = data
  } catch (error) {
    if (requestId === calendarRequest) ElMessage.error(error.response?.data?.message || '预约日历加载失败')
  }
}

let availabilityRequest = 0
async function loadAvailability() {
  const requestId = ++availabilityRequest
  availability.value = []
  if (!visible.value || !form.labId || !form.reserveDate) return
  try {
    const data = (await api.get('/reservations/calendar', {
      params: { labId: form.labId, from: form.reserveDate, to: form.reserveDate }
    })).data
    if (requestId === availabilityRequest && visible.value) {
      availability.value = data.filter(item => item.status === 'APPROVED')
    }
  } catch (error) {
    if (requestId === availabilityRequest) {
      ElMessage.error(error.response?.data?.message || '预约占用数据加载失败')
    }
  }
}

function openBooking() {
  const today = formatDate(new Date())
  form.labId = labs.value.find(lab => lab.openStatus)?.id || null
  form.reserveDate = selectedDate.value >= today ? selectedDate.value : today
  form.startTime = ''
  form.endTime = ''
  form.purpose = ''
  form.participants = 1
  form.remark = ''
  selectedInterval.value = null
  availability.value = []
  visible.value = true
}

function moveMonth(amount) {
  calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + amount, 1)
}

function durationMinutes() {
  const [startHour, startMinute] = (form.startTime || '00:00').split(':').map(Number)
  const [endHour, endMinute] = (form.endTime || '00:00').split(':').map(Number)
  const duration = endHour * 60 + endMinute - startHour * 60 - startMinute
  return duration > 0 ? duration : 60
}

async function submit() {
  if (!selectedInterval.value || !isLegalInterval(selectedInterval.value.start, selectedInterval.value.end)) {
    ElMessage.warning('请在时间轴上选择预约时间')
    return
  }
  recommendations.value = []
  recommendationVisible.value = false
  try {
    await api.post('/reservations', form)
    visible.value = false
    ElMessage.success('预约申请已提交')
    await Promise.all([load(), loadCalendar()])
  } catch (error) {
    const message = error.response?.data?.message || ''
    if (message.includes('时段') || message.includes('冲突') || message.includes('占用')) {
      const duration = durationMinutes()
      recommendations.value = (await api.get('/reservations/recommendations', {
        params: { labId: form.labId, date: form.reserveDate, durationMinutes: duration }
      })).data
      recommendationVisible.value = true
      if (recommendations.value.length === 0) ElMessage.info('当天没有满足时长要求的可用时间')
    } else {
      ElMessage.error(message || '预约提交失败，请稍后重试')
    }
  }
}

async function cancel(id) {
  await ElMessageBox.confirm('确认取消该预约吗？', '提示')
  await api.put(`/reservations/${id}/cancel`)
  await Promise.all([load(), loadCalendar()])
}

async function audit(row, result) {
  let reason = ''
  if (result === 'REJECTED') reason = (await ElMessageBox.prompt('请填写拒绝原因', '审核预约')).value
  await api.put(`/manage/reservations/${row.id}/audit`, { status: result, reason })
  ElMessage.success('审核完成')
  await Promise.all([load(), loadCalendar()])
}

function useRecommendation(slot) {
  const start = toMinutes(slot.startTime) - openMinutes.value
  const end = toMinutes(slot.endTime) - openMinutes.value
  if (isLegalInterval(start, end)) setSelectedInterval(start, end)
  recommendationVisible.value = false
}

watch([calendarLabId, calendarMonth], loadCalendar)
watch([() => form.labId, () => form.reserveDate], () => {
  selectedInterval.value = null
  form.startTime = ''
  form.endTime = ''
  availability.value = []
  loadAvailability()
})
watch(visible, isVisible => {
  if (isVisible) loadAvailability()
  else {
    availabilityRequest++
    availability.value = []
  }
})
onMounted(async () => {
  window.addEventListener('resize', updateTimelineDensity)
  labs.value = (await api.get('/labs')).data
  const openLabs = labs.value.filter(lab => lab.openStatus)
  calendarLabId.value = openLabs[0]?.id || null
  await Promise.all([load(), loadCalendar()])
})
onBeforeUnmount(() => window.removeEventListener('resize', updateTimelineDensity))

function updateTimelineDensity() {
  compactTimeline.value = window.innerWidth < 600
}

function setSelectedInterval(start, end) {
  selectedInterval.value = { start, end }
  form.startTime = formatTime(openMinutes.value + start)
  form.endTime = formatTime(openMinutes.value + end)
}

function overlapsOccupied(start, end) {
  return occupiedIntervals.value.some(interval => start < interval.end && end > interval.start)
}

function isLegalInterval(start, end) {
  return Number.isFinite(start) && Number.isFinite(end) &&
    start >= 0 && end <= timelineDuration.value && end - start >= 30 &&
    (end - start) % 30 === 0 && slotOffsets.value.includes(start) && slotOffsets.value.includes(end) &&
    !overlapsOccupied(start, end)
}

function snapOffset(offset) {
  return slotOffsets.value.reduce((closest, candidate) =>
    Math.abs(candidate - offset) < Math.abs(closest - offset) ? candidate : closest, slotOffsets.value[0] || 0)
}

function pointerOffset(event) {
  const bounds = timelineTrack.value?.getBoundingClientRect()
  if (!bounds || bounds.width <= 0) return 0
  return snapOffset(Math.max(0, Math.min(timelineDuration.value, (event.clientX - bounds.left) / bounds.width * timelineDuration.value)))
}

let dragState = null
function onTimelinePointerDown(event) {
  if (event.button !== undefined && event.button !== 0) return
  if (event.target.closest('.timeline-occupied')) return
  const offset = pointerOffset(event)
  const dragMode = event.target.closest('[data-drag-mode]')?.dataset.dragMode
  let mode = dragMode || 'create'
  const maxStart = slotOffsets.value.filter(value => value + 30 <= timelineDuration.value).at(-1) || 0
  const initialStart = Math.min(offset, maxStart)
  if (mode === 'create' && overlapsOccupied(initialStart, initialStart + 30)) return

  const origin = selectedInterval.value ? { ...selectedInterval.value } : null
  if (mode === 'move' && !origin) mode = 'create'
  dragState = {
    mode,
    anchor: offset,
    origin,
    grabOffset: origin ? offset - origin.start : 0,
    lastValid: origin
  }
  event.preventDefault()
  event.currentTarget.setPointerCapture(event.pointerId)
  if (mode === 'create') {
    const start = initialStart
    setSelectedInterval(start, start + 30)
    dragState.lastValid = { start, end: start + 30 }
  }
}

function onTimelinePointerMove(event) {
  if (!dragState) return
  event.preventDefault()
  const offset = pointerOffset(event)
  let candidate
  if (dragState.mode === 'create') {
    let start = Math.min(dragState.anchor, offset)
    let end = Math.max(dragState.anchor, offset)
    if (end - start < 30) {
      if (offset < dragState.anchor) start = Math.max(0, dragState.anchor - 30)
      else end = Math.min(timelineDuration.value, dragState.anchor + 30)
    }
    candidate = { start, end }
  } else if (dragState.mode === 'resize-start') {
    candidate = { start: Math.min(offset, dragState.origin.end - 30), end: dragState.origin.end }
  } else if (dragState.mode === 'resize-end') {
    candidate = { start: dragState.origin.start, end: Math.max(offset, dragState.origin.start + 30) }
  } else {
    const duration = dragState.origin.end - dragState.origin.start
    const start = Math.max(0, Math.min(timelineDuration.value - duration, snapOffset(offset - dragState.grabOffset)))
    candidate = { start, end: start + duration }
  }
  if (isLegalInterval(candidate.start, candidate.end)) {
    dragState.lastValid = candidate
    setSelectedInterval(candidate.start, candidate.end)
  } else if (dragState.mode === 'create') {
    selectedInterval.value = dragState.lastValid
    form.startTime = formatTime(openMinutes.value + dragState.lastValid.start)
    form.endTime = formatTime(openMinutes.value + dragState.lastValid.end)
  }
}

function onTimelinePointerUp(event) {
  if (!dragState) return
  const state = dragState
  dragState = null
  if (state.lastValid) setSelectedInterval(state.lastValid.start, state.lastValid.end)
  if (event.currentTarget.hasPointerCapture(event.pointerId)) event.currentTarget.releasePointerCapture(event.pointerId)
}

watch(occupiedIntervals, intervals => {
  if (selectedInterval.value && intervals.some(item => selectedInterval.value.start < item.end && selectedInterval.value.end > item.start)) {
    selectedInterval.value = null
    form.startTime = ''
    form.endTime = ''
  }
})
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div><h2 class="page-title">{{ manager ? '预约审核' : '我的预约' }}</h2><div class="page-sub">查看预约安排、申请时段并跟踪审核状态</div></div>
      <div class="head-actions">
        <el-radio-group v-model="view" size="default">
          <el-radio-button label="list">预约列表</el-radio-button>
          <el-radio-button label="calendar">预约日历</el-radio-button>
        </el-radio-group>
        <el-button v-if="!manager" type="primary" @click="openBooking">申请预约</el-button>
      </div>
    </div>

    <template v-if="view === 'list'">
      <div class="panel">
        <div class="toolbar"><el-select v-model="status" clearable placeholder="全部状态" style="width: 160px" @change="load"><el-option v-for="(label, key) in labels" :key="key" :label="label" :value="key" /></el-select></div>
        <el-table :data="list">
          <el-table-column prop="labName" label="实验室" min-width="150" />
          <el-table-column v-if="manager" prop="applicantName" label="申请人" width="110" />
          <el-table-column prop="reserveDate" label="预约日期" width="120" />
          <el-table-column label="时间段" width="145"><template #default="scope">{{ scope.row.startTime?.slice(0, 5) }} - {{ scope.row.endTime?.slice(0, 5) }}</template></el-table-column>
          <el-table-column prop="purpose" label="用途" min-width="160" />
          <el-table-column prop="participants" label="人数" width="75" />
          <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="types[scope.row.status]">{{ labels[scope.row.status] }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="170"><template #default="scope">
            <template v-if="manager && scope.row.status === 'PENDING'"><el-button link type="success" @click="audit(scope.row, 'APPROVED')">通过</el-button><el-button link type="danger" @click="audit(scope.row, 'REJECTED')">拒绝</el-button></template>
            <el-button v-if="!manager && ['PENDING', 'APPROVED'].includes(scope.row.status)" link type="danger" @click="cancel(scope.row.id)">取消</el-button>
            <span v-if="scope.row.rejectReason" class="reason">{{ scope.row.rejectReason }}</span>
          </template></el-table-column>
        </el-table>
      </div>
    </template>

    <template v-else>
      <div class="calendar-layout">
        <section class="panel calendar-panel">
          <div class="calendar-toolbar">
            <el-select v-model="calendarLabId" placeholder="选择实验室" style="width: 240px"><el-option v-for="lab in labs.filter(x => x.openStatus)" :key="lab.id" :label="lab.name" :value="lab.id" /></el-select>
            <div class="month-controls"><el-button @click="moveMonth(-1)">上个月</el-button><b>{{ calendarMonth.getFullYear() }}年{{ calendarMonth.getMonth() + 1 }}月</b><el-button @click="moveMonth(1)">下个月</el-button></div>
          </div>
          <div class="calendar-grid weekday"><div v-for="day in weekdays" :key="day">星期{{ day }}</div></div>
          <div class="calendar-grid days">
            <button v-for="day in calendarDays" :key="day.key" class="day-cell" :class="{ outside: !day.inMonth, selected: selectedDate === day.key }" @click="selectedDate = day.key">
              <span>{{ day.date.getDate() }}</span>
              <small v-if="eventsByDate[day.key]?.length">{{ eventsByDate[day.key].length }} 个预约</small>
            </button>
          </div>
          <div class="legend"><span><i class="busy-dot"></i>已通过预约（占用）</span><span>选择日期查看占用情况</span></div>
        </section>
        <section class="panel day-panel">
          <div class="day-title"><div><b>{{ selectedDate }}</b><span>{{ selectedLabForCalendar?.name || '请选择实验室' }}</span></div></div>
          <div v-if="selectedEvents.length" class="event-list">
            <div v-for="(event, index) in selectedEvents" :key="`${event.startTime}-${index}`" class="event">
              <el-tag type="success">已通过</el-tag>
              <b>{{ event.startTime.slice(0, 5) }}–{{ event.endTime.slice(0, 5) }}</b>
            </div>
          </div>
          <el-empty v-else description="当天暂无预约" :image-size="70" />
          <div class="open-hours" v-if="selectedLabForCalendar">开放时间：{{ selectedLabForCalendar.openTime }}–{{ selectedLabForCalendar.closeTime }}</div>
        </section>
      </div>
    </template>

    <el-dialog v-model="visible" title="申请实验室预约" width="min(720px, 94vw)">
      <el-form label-width="100px">
        <el-form-item label="实验室"><el-select v-model="form.labId" style="width: 100%"><el-option v-for="lab in labs.filter(x => x.openStatus)" :key="lab.id" :label="`${lab.name}（${lab.openTime}–${lab.closeTime}）`" :value="lab.id" /></el-select></el-form-item>
        <el-form-item label="预约日期"><el-date-picker v-model="form.reserveDate" value-format="YYYY-MM-DD" :disabled-date="date => formatDate(date) < formatDate(new Date())" style="width: 100%" /></el-form-item>
        <el-form-item label="预约时间">
          <div v-if="!form.labId || !form.reserveDate" class="timeline-empty">请选择实验室和预约日期</div>
          <div v-else-if="timelineDuration < 30" class="timeline-empty">该实验室开放时长不足30分钟</div>
          <div v-else class="timeline-picker">
            <div class="timeline-axis">
              <span v-for="tick in timelineTicks" :key="tick.offset" :class="{ first: tick.offset === 0, last: tick.offset === timelineDuration }" :style="{ left: `${tick.offset / timelineDuration * 100}%` }">{{ tick.label }}</span>
            </div>
            <div
              ref="timelineTrack"
              class="timeline-track"
              @pointerdown="onTimelinePointerDown"
              @pointermove="onTimelinePointerMove"
              @pointerup="onTimelinePointerUp"
              @pointercancel="onTimelinePointerUp"
              @lostpointercapture="onTimelinePointerUp"
            >
              <i v-for="offset in slotOffsets" :key="`line-${offset}`" class="timeline-gridline" :style="{ left: `${offset / timelineDuration * 100}%` }"></i>
              <div
                v-for="(interval, index) in occupiedIntervals"
                :key="`busy-${index}`"
                class="timeline-occupied"
                :style="intervalStyle(interval.start, interval.end)"
                :title="interval.title"
                aria-label="已占用，不可预约"
                @pointerdown.stop
              ><span>已占用</span></div>
              <div v-if="selectedInterval" class="timeline-selection" :style="selectedStyle" data-drag-mode="move" title="拖动以移动预约时间段">
                <button class="timeline-handle start" type="button" data-drag-mode="resize-start" aria-label="拖动调整开始时间"></button>
                <span class="selection-label">{{ form.startTime }}—{{ form.endTime }}</span>
                <button class="timeline-handle end" type="button" data-drag-mode="resize-end" aria-label="拖动调整结束时间"></button>
              </div>
            </div>
            <div class="timeline-status">
              <span v-if="selectedInterval">当前选择：{{ form.startTime }}—{{ form.endTime }}，共{{ selectedDurationLabel }}</span>
              <span v-else>在浅色空闲区域按下并拖动，选择至少30分钟</span>
              <span>{{ openStart }}—{{ openEnd }} 开放</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="参与人数"><el-input-number v-model="form.participants" :min="1" /></el-form-item>
        <el-form-item label="使用用途"><el-input v-model="form.purpose" placeholder="请简要说明实验或活动内容" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <div v-if="recommendationVisible && recommendations.length" class="recommendations">
          <b>该时段已被预约，以下是相同时长的可用时段：</b>
          <div><el-button v-for="slot in recommendations" :key="slot.startTime" size="small" @click="useRecommendation(slot)">{{ slot.startTime.slice(0, 5) }}–{{ slot.endTime.slice(0, 5) }}</el-button></div>
        </div>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" @click="submit">提交申请</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head-actions{display:flex;align-items:center;gap:12px}.toolbar{padding:16px}
.calendar-layout{display:grid;grid-template-columns:minmax(0,2fr) minmax(260px,1fr);gap:16px}
.calendar-panel,.day-panel{padding:20px}.calendar-toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}
.month-controls{display:flex;align-items:center;gap:12px}.calendar-grid{display:grid;grid-template-columns:repeat(7,minmax(0,1fr))}
.weekday>div{text-align:center;padding:10px;color:#829399;font-size:12px;border-bottom:1px solid #edf2f3}
.day-cell{min-height:82px;border:0;border-bottom:1px solid #edf2f3;border-right:1px solid #edf2f3;background:#fff;text-align:left;padding:10px;cursor:pointer;color:var(--ink);display:flex;flex-direction:column;gap:9px}
.day-cell:nth-child(7n){border-right:0}.day-cell:hover,.day-cell.selected{background:#edf8f7}.day-cell.selected span{color:var(--primary);font-weight:700}.day-cell.outside{color:#b9c2c5}.day-cell small{color:#168a92;font-size:11px}
.legend{display:flex;justify-content:space-between;margin-top:16px;color:#819297;font-size:12px}.busy-dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:#168a92;margin-right:6px}
.day-title{padding:5px 0 15px;border-bottom:1px solid #edf2f3}.day-title div{display:flex;flex-direction:column;gap:6px}.day-title b{font-size:18px}.day-title span,.open-hours{font-size:12px;color:#829399}
.event-list{display:flex;flex-direction:column;gap:12px;padding:16px 0}.event{display:flex;align-items:center;gap:10px}.open-hours{border-top:1px solid #edf2f3;padding-top:14px}
.reason{font-size:12px;color:#9a6c6c}.recommendations{background:#f0f9f8;padding:12px;border-radius:8px;color:#386b6e}.recommendations>div{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px}
.timeline-picker{width:100%;min-width:0;padding:9px 5px 4px;overflow:hidden;user-select:none;-webkit-user-select:none}
.timeline-empty{width:100%;padding:22px 12px;border:1px dashed #d9e6e7;border-radius:8px;text-align:center;color:#829399;font-size:13px}
.timeline-axis{position:relative;height:27px;margin:0 0 2px;color:#607a80;font-size:11px}
.timeline-axis>span{position:absolute;top:0;white-space:nowrap;transform:translateX(-50%)}
.timeline-axis>span.first{transform:none}.timeline-axis>span.last{transform:translateX(-100%)}
.timeline-track{height:48px;position:relative;overflow:visible;border:1px solid #cfe3e4;border-radius:7px;background:#edf7f7;touch-action:none;cursor:crosshair;user-select:none;-webkit-user-select:none}
.timeline-gridline{position:absolute;z-index:1;top:-5px;bottom:-5px;width:1px;background:#c6dada;pointer-events:none}
.timeline-occupied{position:absolute;z-index:2;top:0;bottom:0;display:flex;justify-content:center;align-items:center;overflow:hidden;background:#d98787;border-left:1px solid #c96e6e;border-right:1px solid #c96e6e;color:#fff;font-size:11px;font-weight:700;white-space:nowrap;cursor:not-allowed}
.timeline-occupied span{overflow:hidden;text-overflow:ellipsis;padding:0 4px}
.timeline-selection{position:absolute;z-index:3;top:3px;bottom:3px;display:flex;align-items:center;justify-content:center;min-width:0;border:1px solid #168a92;border-radius:5px;background:#168a92;color:#fff;cursor:grab;touch-action:none}
.timeline-selection:active{cursor:grabbing}
.selection-label{display:block;overflow:hidden;padding:0 8px;text-overflow:ellipsis;white-space:nowrap;font-size:11px;font-weight:700;pointer-events:none}
.timeline-handle{position:absolute;z-index:4;top:50%;width:18px;height:34px;padding:0;transform:translateY(-50%);border:2px solid #fff;border-radius:7px;background:#126f79;box-shadow:0 1px 4px #174f5555;cursor:ew-resize;touch-action:none}
.timeline-handle.start{left:-9px}.timeline-handle.end{right:-9px}
.timeline-status{display:flex;justify-content:space-between;gap:8px;margin-top:10px;color:#557177;font-size:12px;line-height:1.5}
@media(max-width:520px){.timeline-status{flex-direction:column}.timeline-axis{font-size:10px}}
@media(max-width:900px){.calendar-layout{grid-template-columns:1fr}.calendar-toolbar{align-items:flex-start;gap:10px;flex-direction:column}}
@media(max-width:600px){.head-actions{align-items:flex-end;flex-direction:column}.day-cell{min-height:64px;padding:6px}.day-cell small{font-size:10px}.legend{gap:8px;flex-direction:column}}
</style>
