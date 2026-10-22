<script setup>
import { defineAsyncComponent } from 'vue'
import { useAuthStore } from '../stores/auth'
import StudentHome from './dashboard/StudentHome.vue'

const auth = useAuthStore()
const AdminDashboard = defineAsyncComponent(() => import('./dashboard/AdminDashboard.vue'))
</script>

<template>
  <StudentHome v-if="auth.user?.role === 'USER'" />
  <AdminDashboard v-else />
</template>
