<template>
  <span :class="['inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border', tone]">
    <span :class="['w-1.5 h-1.5 rounded-full', dot]" />
    {{ label || status }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  status?: string
  label?: string
}>(), { status: '' })

const map: Record<string, { tone: string; dot: string }> = {
  ACTIVO:           { tone: 'bg-success-50 text-success-600 border-success-500/20', dot: 'bg-success-500' },
  LOCKED:           { tone: 'bg-warning-50 text-warning-600 border-warning-500/20', dot: 'bg-warning-500' },
  CONFIRMADA:       { tone: 'bg-success-50 text-success-600 border-success-500/20', dot: 'bg-success-500' },
  COMPLETADA:       { tone: 'bg-info-50 text-info-600 border-info-500/20',          dot: 'bg-info-500' },
  COMPLETADO:       { tone: 'bg-info-50 text-info-600 border-info-500/20',          dot: 'bg-info-500' },
  PAGADO:           { tone: 'bg-success-50 text-success-600 border-success-500/20', dot: 'bg-success-500' },
  MATRICULADO:      { tone: 'bg-success-50 text-success-600 border-success-500/20', dot: 'bg-success-500' },
  PRE_MATRICULADO:  { tone: 'bg-warning-50 text-warning-600 border-warning-500/20', dot: 'bg-warning-500' },
  PROGRAMADA:       { tone: 'bg-warning-50 text-warning-600 border-warning-500/20', dot: 'bg-warning-500' },
  PARCIAL:          { tone: 'bg-warning-50 text-warning-600 border-warning-500/20', dot: 'bg-warning-500' },
  PENDIENTE:        { tone: 'bg-warning-50 text-warning-600 border-warning-500/20', dot: 'bg-warning-500' },
  EN_CURSO:         { tone: 'bg-info-50 text-info-600 border-info-500/20',          dot: 'bg-info-500' },
  INACTIVO:         { tone: 'bg-ink-100 text-ink-600 border-ink-300',               dot: 'bg-ink-400' },
  RETIRADO:         { tone: 'bg-ink-100 text-ink-600 border-ink-300',               dot: 'bg-ink-400' },
  CANCELADA:        { tone: 'bg-danger-50 text-danger-600 border-danger-500/20',    dot: 'bg-danger-500' },
  NO_ASISTIO:       { tone: 'bg-danger-50 text-danger-600 border-danger-500/20',    dot: 'bg-danger-500' }
}

const tone = computed(() => map[props.status]?.tone || 'bg-ink-100 text-ink-700 border-ink-300')
const dot  = computed(() => map[props.status]?.dot  || 'bg-ink-400')
</script>
