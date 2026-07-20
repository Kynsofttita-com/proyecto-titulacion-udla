<template>
  <div class="flex flex-col sm:flex-row sm:items-center justify-between py-3 border-b border-ink-100 last:border-b-0">
    <label class="text-sm font-medium text-ink-600 mb-1 sm:mb-0">{{ label }}</label>
    <span v-if="value" class="text-sm text-ink-900 font-medium">{{ formattedValue }}</span>
    <span v-else class="text-sm text-ink-400 italic">No especificado</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { fmtFechaLocal } from '@/utils/fechas'

interface Props {
  label: string
  value?: string | number | boolean | Date | null
  type?: 'text' | 'date' | 'email' | 'phone'
}

const props = withDefaults(defineProps<Props>(), { type: 'text' })

const formattedValue = computed(() => {
  if (!props.value) return ''

  if (props.type === 'date' && props.value instanceof Date) {
    return props.value.toLocaleDateString('es-EC')
  }

  if (props.type === 'date' && typeof props.value === 'string') {
    // fmtFechaLocal parsea "YYYY-MM-DD" en local (evita corrimiento -1 dia por TZ)
    // y delega en Date para strings con hora explicita.
    return fmtFechaLocal(props.value)
  }

  if (typeof props.value === 'boolean') {
    return props.value ? 'Sí' : 'No'
  }

  return String(props.value)
})
</script>
