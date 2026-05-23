<template>
  <div
    :class="['inline-flex items-center justify-center rounded-full font-semibold flex-shrink-0', sizeCls, toneCls]"
    :title="name"
  >
    {{ initials }}
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  name?: string
  size?: 'sm' | 'md' | 'lg'
}>(), { size: 'md', name: '' })

const initials = computed(() => {
  return (props.name || '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(s => s[0]?.toUpperCase())
    .join('')
})

const sizeCls = computed(() => ({
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-base'
}[props.size]))

// Color determinístico por hash del nombre
const palette = [
  'bg-brand-100 text-brand-700',
  'bg-success-50 text-success-600',
  'bg-warning-50 text-warning-600',
  'bg-info-50 text-info-600',
  'bg-purple-100 text-purple-700',
  'bg-pink-100 text-pink-700'
]
const toneCls = computed(() => {
  let hash = 0
  for (const c of (props.name || '')) hash = (hash * 31 + c.charCodeAt(0)) >>> 0
  return palette[hash % palette.length]
})
</script>
