<template>
  <div class="border border-ink-300 rounded-lg overflow-hidden bg-white flex flex-col">
    <!-- Barra superior estilo Gmail -->
    <div class="bg-ink-50 border-b border-ink-200 px-4 py-2.5 flex items-center gap-3">
      <div class="w-8 h-8 rounded-full bg-brand-100 flex items-center justify-center flex-shrink-0">
        <i class="pi pi-envelope text-brand-700 text-xs" />
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-xs text-ink-500">Vista previa del email</p>
        <p class="text-sm font-semibold text-ink-900 truncate">{{ asuntoPreview || 'Sin asunto' }}</p>
      </div>
    </div>

    <!-- Cuerpo simulado del email -->
    <div class="p-4 flex-1 overflow-y-auto max-h-[480px]">
      <div
        v-if="cuerpoPreview"
        class="email-body-preview text-sm text-ink-900"
        v-html="cuerpoPreview"
      />
      <div v-else class="flex flex-col items-center justify-center py-12 text-ink-400">
        <i class="pi pi-eye text-4xl mb-2" />
        <p class="text-sm">Escribe algo en el editor para ver la vista previa</p>
      </div>
    </div>

    <!-- Nota inferior -->
    <div class="bg-ink-50 border-t border-ink-200 px-3 py-1.5 text-[10px] text-ink-500 flex items-center gap-1.5">
      <i class="pi pi-info-circle text-[10px]" />
      Las variables se muestran con datos de ejemplo
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  asunto?: string
  cuerpo: string
  datosEjemplo?: Record<string, string>
}>()

const ejemplo = computed<Record<string, string>>(() => ({
  nombre: 'Juan Carlos',
  apellido: 'Perez Gomez',
  nombreCompleto: 'Juan Carlos Perez Gomez',
  email: 'juan.perez@ejemplo.com',
  telefono: '0991234567',
  cedula: '1712345678',
  fecha: new Date().toLocaleDateString('es-EC'),
  hora: '10:00',
  instructor: 'Pedro Flores',
  vehiculo: 'Toyota Corolla ABC-1234',
  monto: '$250.00',
  numeroFactura: 'FAC-0001',
  categoria: 'B',
  escuela: 'Escuela de Conduccion',
  ...(props.datosEjemplo || {})
}))

function reemplazarVariables(texto: string): string {
  if (!texto) return ''
  return texto.replace(/\{\{\s*([a-zA-Z0-9_]+)\s*\}\}/g, (match, key) => {
    const valor = ejemplo.value[key]
    if (valor !== undefined) return valor
    // Si no hay valor de ejemplo, lo dejamos resaltado
    return `<span style="background:#fef3c7;color:#92400e;padding:1px 4px;border-radius:3px;font-family:monospace;font-size:11px">${match}</span>`
  })
}

const asuntoPreview = computed(() => reemplazarVariables(props.asunto || ''))
const cuerpoPreview = computed(() => reemplazarVariables(props.cuerpo))
</script>

<style scoped>
.email-body-preview :deep(h1) {
  font-size: 22px;
  font-weight: 700;
  margin: 8px 0;
}
.email-body-preview :deep(h2) {
  font-size: 18px;
  font-weight: 700;
  margin: 8px 0;
}
.email-body-preview :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0;
}
.email-body-preview :deep(p) {
  margin: 0 0 8px 0;
  line-height: 1.6;
}
.email-body-preview :deep(ul) {
  list-style: disc;
  padding-left: 24px;
  margin: 6px 0;
}
.email-body-preview :deep(ol) {
  list-style: decimal;
  padding-left: 24px;
  margin: 6px 0;
}
.email-body-preview :deep(a) {
  color: #2563eb;
  text-decoration: underline;
}
</style>
