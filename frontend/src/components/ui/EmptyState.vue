<template>
  <div class="flex flex-col items-center justify-center py-16 px-6 text-center">
    <div class="w-16 h-16 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
      <i :class="icon || 'pi pi-inbox'" class="text-2xl" />
    </div>
    <h3 class="heading-3 mb-1">{{ title }}</h3>
    <p class="muted max-w-md">{{ description }}</p>
    <div v-if="$slots.action" class="mt-5">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  icon?: string
}>()
</script>
