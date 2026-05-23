<template>
  <component :is="currentLayout">
    <RouterView />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

const route = useRoute()
const authStore = useAuthStore()

const currentLayout = computed(() => {
  if (route.meta.layout === 'auth') {
    return AuthLayout
  }
  return MainLayout
})
</script>

<style scoped>
</style>
