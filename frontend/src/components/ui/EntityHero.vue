<template>
  <section class="card animate-fade-up overflow-hidden">
    <div class="relative h-24 bg-gradient-to-r from-brand-700 via-brand-600 to-brand-500" />
    <div class="px-6 pb-6 -mt-12">
      <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
        <div class="flex items-end gap-4">
          <div class="w-24 h-24 rounded-2xl bg-white border-4 border-white shadow-elev flex items-center justify-center text-3xl font-bold text-brand-700">
            <slot name="avatar">
              {{ avatarText }}
            </slot>
          </div>
          <div class="pb-2">
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-2xl font-bold text-ink-900">{{ title }}</h2>
              <slot name="badge" />
            </div>
            <p v-if="subtitle" class="text-ink-500 text-sm mt-1">{{ subtitle }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2 flex-wrap pb-2">
          <slot name="actions" />
        </div>
      </div>

      <div v-if="$slots.stats" class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-ink-200">
        <slot name="stats" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  title: string
  subtitle?: string
  initials?: string
}>()

const avatarText = computed(() => {
  if (props.initials) return props.initials
  return (props.title || '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(s => s[0]?.toUpperCase())
    .join('')
})
</script>
