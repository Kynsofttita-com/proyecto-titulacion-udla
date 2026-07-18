<template>
  <div class="bg-white border border-ink-200 rounded-lg p-3 flex flex-wrap items-end gap-3">
    <!-- Selector de campo -->
    <div class="flex-shrink-0 min-w-[180px]">
      <label class="text-xs font-semibold text-ink-500 block mb-1">Filtrar por</label>
      <select
        v-model="campoSeleccionado"
        class="w-full px-3 py-2 border border-ink-300 bg-white rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
      >
        <option
          v-for="c in campos"
          :key="c.key"
          :value="c.key"
        >{{ c.label }}</option>
      </select>
    </div>

    <!-- Input del valor (segun tipo del campo) -->
    <div class="flex-1 min-w-[200px]">
      <label class="text-xs font-semibold text-ink-500 block mb-1">Valor</label>

      <!-- Tipo TEXT: buscar por substring -->
      <input
        v-if="campoActual?.tipo === 'text'"
        v-model="valorTexto"
        type="text"
        :placeholder="`Buscar por ${campoActual.label.toLowerCase()}...`"
        class="w-full px-3 py-2 border border-ink-300 bg-white rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
      />

      <!-- Tipo SELECT: seleccionar de opciones -->
      <select
        v-else-if="campoActual?.tipo === 'select'"
        v-model="valorSelect"
        class="w-full px-3 py-2 border border-ink-300 bg-white rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
      >
        <option value="">Todos</option>
        <option
          v-for="opt in campoActual.opciones || []"
          :key="typeof opt === 'string' ? opt : opt.valor"
          :value="typeof opt === 'string' ? opt : opt.valor"
        >{{ typeof opt === 'string' ? opt : opt.label }}</option>
      </select>

      <!-- Tipo DATE: fecha exacta -->
      <input
        v-else-if="campoActual?.tipo === 'date'"
        v-model="valorFecha"
        type="date"
        class="w-full px-3 py-2 border border-ink-300 bg-white rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
      />

      <!-- Tipo NUMBER: numero -->
      <input
        v-else-if="campoActual?.tipo === 'number'"
        v-model="valorNumero"
        type="number"
        :placeholder="`Buscar ${campoActual.label.toLowerCase()}...`"
        class="w-full px-3 py-2 border border-ink-300 bg-white rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
      />
    </div>

    <!-- Boton limpiar -->
    <button
      type="button"
      @click="limpiar"
      :disabled="!tieneFiltroAplicado"
      class="px-3 py-2 border border-ink-300 rounded-lg text-sm text-ink-700 hover:bg-ink-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      title="Limpiar filtro"
    >
      <i class="pi pi-times mr-1 text-xs" />
      Limpiar
    </button>

    <!-- Info: cuantos registros muestra -->
    <div
      v-if="tieneFiltroAplicado"
      class="w-full text-xs text-brand-700 flex items-center gap-1.5 pt-1"
    >
      <i class="pi pi-filter text-xs" />
      Mostrando <strong>{{ datosFiltrados.length }}</strong> de {{ datos.length }} registros
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

export interface CampoFiltro {
  /** Nombre de la propiedad en cada objeto de la lista (ej: 'nombreCompleto') */
  key: string
  /** Etiqueta visible al usuario (ej: 'Nombre') */
  label: string
  /** Tipo del campo (define el input a mostrar) */
  tipo: 'text' | 'select' | 'date' | 'number'
  /** Opciones cuando tipo = 'select' */
  opciones?: (string | { valor: string; label: string })[]
}

const props = defineProps<{
  campos: CampoFiltro[]
  datos: Record<string, any>[]
}>()

const emit = defineEmits<{
  'update:datosFiltrados': [datos: Record<string, any>[]]
}>()

const campoSeleccionado = ref<string>(props.campos[0]?.key || '')
const valorTexto = ref('')
const valorSelect = ref('')
const valorFecha = ref('')
const valorNumero = ref<number | null>(null)

const campoActual = computed(() =>
  props.campos.find(c => c.key === campoSeleccionado.value)
)

const valorActual = computed(() => {
  switch (campoActual.value?.tipo) {
    case 'text': return valorTexto.value.trim()
    case 'select': return valorSelect.value
    case 'date': return valorFecha.value
    case 'number': return valorNumero.value != null ? String(valorNumero.value) : ''
    default: return ''
  }
})

const tieneFiltroAplicado = computed(() => valorActual.value !== '' && valorActual.value != null)

const datosFiltrados = computed(() => {
  if (!tieneFiltroAplicado.value || !campoActual.value) return props.datos

  const key = campoActual.value.key
  const val = valorActual.value

  return props.datos.filter(item => {
    const raw = obtenerValor(item, key)
    if (raw == null) return false

    switch (campoActual.value?.tipo) {
      case 'text':
        return String(raw).toLowerCase().includes(val.toLowerCase())
      case 'select':
        return String(raw).toUpperCase() === val.toUpperCase()
      case 'date': {
        const fechaItem = String(raw).substring(0, 10)
        return fechaItem === val
      }
      case 'number':
        return String(raw) === val
      default:
        return false
    }
  })
})

// Emitir cambios cuando el filtro cambia
watch([datosFiltrados], () => {
  emit('update:datosFiltrados', datosFiltrados.value)
}, { immediate: true })

// Cuando cambia la fuente de datos, emitimos filtrado inmediatamente
watch(() => props.datos, () => {
  emit('update:datosFiltrados', datosFiltrados.value)
})

// Al cambiar campo seleccionado, resetear valores
watch(campoSeleccionado, () => {
  limpiarValores()
})

function limpiarValores() {
  valorTexto.value = ''
  valorSelect.value = ''
  valorFecha.value = ''
  valorNumero.value = null
}

function limpiar() {
  limpiarValores()
}

/**
 * Obtiene un valor por 'key' que puede incluir puntos (ej: 'usuario.nombre').
 * Tambien soporta variantes snake_case si no encuentra camelCase.
 */
function obtenerValor(obj: any, key: string): any {
  if (!obj) return null
  if (obj[key] !== undefined) return obj[key]
  // Fallback snake_case
  const snake = key.replace(/([A-Z])/g, '_$1').toLowerCase()
  if (obj[snake] !== undefined) return obj[snake]
  // Dot notation
  if (key.includes('.')) {
    return key.split('.').reduce((acc, k) => (acc ? acc[k] : null), obj)
  }
  return null
}
</script>
