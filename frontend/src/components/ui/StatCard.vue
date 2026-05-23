<template>
  <div class="card card-hover p-5 animate-fade-up">
    <div class="flex items-start justify-between mb-3">
      <div :class="['w-11 h-11 rounded-lg flex items-center justify-center', iconBg]">
        <i :class="[icon, 'text-lg', iconColor]" />
      </div>
      <span
        v-if="trend != null"
        :class="['inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full',
                 trend >= 0 ? 'bg-success-50 text-success-600' : 'bg-danger-50 text-danger-600']"
      >
        <i :class="trend >= 0 ? 'pi pi-arrow-up' : 'pi pi-arrow-down'" class="text-[10px]" />
        {{ Math.abs(trend) }}%
      </span>
    </div>
    <p class="text-sm font-medium text-ink-500 mb-1">{{ label }}</p>
    <div class="flex items-baseline gap-2">
      <span class="text-3xl font-bold text-ink-900 tracking-tight">{{ value }}</span>
      <span v-if="unit" class="text-sm text-ink-500">{{ unit }}</span>
    </div>
    <p v-if="hint" class="muted text-xs mt-2">{{ hint }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  value: string | number
  icon: string
  color?: 'brand' | 'success' | 'warning' | 'danger' | 'info'
  unit?: string
  trend?: number | null
  hint?: string
}>(), { color: 'brand' })

const iconBg = computed(() => ({
  brand:   'bg-brand-50',
  success: 'bg-success-50',
  warning: 'bg-warning-50',
  danger:  'bg-danger-50',
  info:    'bg-info-50'
}[props.color]))

const iconColor = computed(() => ({
  brand:   'text-brand-700',
  success: 'text-success-600',
  warning: 'text-warning-600',
  danger:  'text-danger-600',
  info:    'text-info-600'
}[props.color]))
</script>
