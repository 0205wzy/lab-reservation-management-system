<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const list = ref([])
const labs = ref([])
const assignments = ref({})
const roles = { USER: '普通用户', LAB_ADMIN: '实验室管理员', SYSTEM_ADMIN: '系统管理员' }
function availableRoles(user) {
  return Object.entries(roles)
    .filter(([role]) => role !== 'SYSTEM_ADMIN' || user.role === 'SYSTEM_ADMIN')
    .map(([value, label]) => ({ value, label }))
}

async function load() {
  const [users, allLabs, assignmentRows] = await Promise.all([
    api.get('/admin/users'),
    api.get('/labs'),
    api.get('/admin/lab-assignments')
  ])
  list.value = users.data
  labs.value = allLabs.data
  assignments.value = {}
  for (const row of assignmentRows.data) {
    ;(assignments.value[row.userId] ||= []).push(row.labId)
  }
}

async function save(user) {
  const selectedLabs = assignments.value[user.id] || []
  if (user.role === 'LAB_ADMIN') {
    if (selectedLabs.length < 1 || selectedLabs.length > 2) {
      ElMessage.warning('每位实验室管理员必须分配1至2个实验室')
      return
    }
    await api.put(`/admin/users/${user.id}/labs`, { labIds: selectedLabs })
  }
  await api.put(`/admin/users/${user.id}`, user)
  ElMessage.success('用户信息与权限已更新')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户与权限管理</h2>
        <div class="page-sub">系统管理员可维护用户角色及实验室管理员的实验室范围</div>
      </div>
    </div>
    <div class="panel">
      <el-table :data="list">
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="150">
          <template #default="s"><el-input v-model="s.row.realName" :disabled="s.row.id === auth.user?.id" /></template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="150">
          <template #default="s"><el-input v-model="s.row.phone" :disabled="s.row.id === auth.user?.id" /></template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="s">
            <el-select v-model="s.row.role" :disabled="s.row.id === auth.user?.id">
              <el-option v-for="role in availableRoles(s.row)" :key="role.value" :value="role.value" :label="role.label" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="管理实验室（最多2个）" min-width="260">
          <template #default="s">
            <el-select
              v-model="assignments[s.row.id]"
              multiple
              collapse-tags
              :multiple-limit="2"
              :disabled="s.row.id === auth.user?.id || s.row.role !== 'LAB_ADMIN'"
              placeholder="选择1至2个实验室"
            >
              <el-option v-for="lab in labs" :key="lab.id" :value="lab.id" :label="lab.name" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" min-width="125">
          <template #default="s">
            <el-switch v-model="s.row.status" :active-value="1" :inactive-value="0" :disabled="s.row.id === auth.user?.id" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="s">
            <el-button type="primary" link :disabled="s.row.id === auth.user?.id" @click="save(s.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p class="hint">实验室管理员仅能访问分配范围内的实验室、预约、设备、维修及统计数据；分配至少1个、至多2个实验室。</p>
    </div>
  </div>
</template>

<style scoped>
.hint{padding:14px 18px 0;color:#81949a;font-size:12px}
</style>
