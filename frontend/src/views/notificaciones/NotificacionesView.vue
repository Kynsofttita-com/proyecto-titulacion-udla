<template>
  <div class="space-y-6">
    <PageHeader
      title="Notificaciones"
      description="Bandeja completa de notificaciones del sistema"
      icon="pi pi-bell"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Notificaciones' }
      ]"
    >
      <template #actions>
        <Button
          v-if="contadorNoLeidas > 0"
          label="Marcar todas como leídas"
          icon="pi pi-check"
          outlined
          @click="handleMarcarTodas"
          :loading="accionEnCurso"
        />
        <Button
          v-if="notificaciones.length > 0"
          label="Eliminar todas"
          icon="pi pi-trash"
          severity="danger"
          outlined
          @click="handleEliminarTodas"
          :loading="accionEnCurso"
        />
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="rounded-lg border border-ink-200 bg-white p-4">
        <p class="text-xs uppercase tracking-wider text-ink-500">Total</p>
        <p class="text-2xl font-bold text-ink-900 mt-1">{{ totalElementos }}</p>
      </div>
      <div class="rounded-lg border border-ink-200 bg-brand-50 p-4">
        <p class="text-xs uppercase tracking-wider text-brand-600">No leídas</p>
        <p class="text-2xl font-bold text-brand-700 mt-1">{{ contadorNoLeidas }}</p>
      </div>
      <div class="rounded-lg border border-red-200 bg-red-50 p-4">
        <p class="text-xs uppercase tracking-wider text-red-600">Prioridad alta</p>
        <p class="text-2xl font-bold text-red-700 mt-1">{{ contadorAlta }}</p>
      </div>
      <div class="rounded-lg border border-ink-200 bg-white p-4">
        <p class="text-xs uppercase tracking-wider text-ink-500">Tipos únicos</p>
        <p class="text-2xl font-bold text-ink-900 mt-1">{{ tiposUnicos.length }}</p>
      </div>
    </div>

    <!-- Filtros -->
    <div class="rounded-lg border border-ink-200 bg-white p-4 flex flex-wrap items-end gap-3">
      <div class="flex flex-col gap-1">
        <label class="text-xs font-medium text-ink-600">Estado</label>
        <select v-model="filtroLeida" class="px-3 py-2 rounded border border-ink-300 text-sm min-w-[140px]">
          <option :value="undefined">Todas</option>
          <option :value="false">No leídas</option>
          <option :value="true">Leídas</option>
        </select>
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-xs font-medium text-ink-600">Tipo</label>
        <select v-model="filtroTipo" class="px-3 py-2 rounded border border-ink-300 text-sm min-w-[180px]">
          <option value="">Todos</option>
          <option v-for="t in tiposUnicos" :key="t" :value="t">{{ formatTipo(t) }}</option>
        </select>
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-xs font-medium text-ink-600">Prioridad</label>
        <select v-model="filtroPrioridad" class="px-3 py-2 rounded border border-ink-300 text-sm min-w-[140px]">
          <option value="">Todas</option>
          <option value="ALTA">Alta</option>
          <option value="NORMAL">Normal</option>
          <option value="BAJA">Baja</option>
        </select>
      </div>
      <Button label="Limpiar" outlined size="small" @click="limpiarFiltros" />
      <div class="ml-auto text-xs text-ink-500">
        Mostrando {{ notificacionesFiltradas.length }} de {{ totalElementos }}
      </div>
    </div>

    <!-- Lista -->
    <div class="rounded-lg border border-ink-200 bg-white overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center py-16">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="notificacionesFiltradas.length === 0" class="py-16">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin notificaciones"
          :description="hayFiltros ? 'Ningún registro coincide con los filtros aplicados' : 'No tienes notificaciones aún'"
        />
      </div>

      <ul v-else class="divide-y divide-ink-200">
        <li
          v-for="notif in notificacionesFiltradas"
          :key="notif.id"
          class="p-4 hover:bg-ink-50 transition"
          :class="!notif.leida && 'bg-brand-50/40'"
        >
          <div class="flex items-start gap-4">
            <div
              class="w-11 h-11 rounded-lg flex items-center justify-center flex-shrink-0"
              :class="clasesIcono(notif)"
            >
              <i :class="iconoTipo(notif.tipo)" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-start gap-2 flex-wrap">
                <p class="text-sm font-semibold text-ink-900 flex-1 min-w-[240px]">
                  {{ notif.titulo }}
                </p>
                <span
                  class="text-[10px] px-2 py-0.5 rounded uppercase font-semibold"
                  :class="clasesPrioridad(notif.prioridad)"
                >
                  {{ notif.prioridad }}
                </span>
                <span class="text-[10px] px-2 py-0.5 rounded uppercase font-medium bg-ink-100 text-ink-600">
                  {{ formatTipo(notif.tipo) }}
                </span>
                <span
                  v-if="!notif.leida"
                  class="text-[10px] px-2 py-0.5 rounded uppercase font-semibold bg-brand-500 text-white"
                >
                  Nueva
                </span>
              </div>
              <p class="text-sm text-ink-600 mt-1.5">
                {{ notif.mensaje }}
              </p>
              <p class="text-[11px] text-ink-400 mt-2">
                <i class="pi pi-clock text-[10px] mr-1" />
                {{ formatearFecha(notif.createdAt) }}
                <span v-if="notif.leida && notif.fechaLectura" class="ml-3">
                  <i class="pi pi-check text-[10px] mr-1" />
                  Leída: {{ formatearFecha(notif.fechaLectura) }}
                </span>
              </p>
            </div>

            <div class="flex flex-col gap-1 flex-shrink-0">
              <button
                v-if="!notif.leida"
                @click="marcarLeida(notif.id)"
                class="text-xs px-3 py-1.5 rounded border border-brand-500 text-brand-700 hover:bg-brand-50 flex items-center gap-1"
                title="Marcar como leída"
              >
                <i class="pi pi-check text-[10px]" />
                Marcar leída
              </button>
              <button
                @click="eliminar(notif.id)"
                class="text-xs px-3 py-1.5 rounded border border-ink-200 text-ink-500 hover:bg-red-50 hover:text-red-600 hover:border-red-300 flex items-center gap-1"
                title="Eliminar"
              >
                <i class="pi pi-trash text-[10px]" />
                Eliminar
              </button>
            </div>
          </div>
        </li>
      </ul>
    </div>

    <ConfirmDialog />
    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { useNotificacionesStore } from '@/stores/notificaciones'
import { useAuthStore } from '@/stores/auth'
import type { Notificacion } from '@/services/notificaciones'

const notificacionesStore = useNotificacionesStore()
const authStore = useAuthStore()
const toast = useToast()
const confirm = useConfirm()

const loading = computed(() => notificacionesStore.loading)
const notificaciones = computed(() => notificacionesStore.notificaciones as Notificacion[])
const contadorNoLeidas = computed(() => notificacionesStore.contadorNoLeidas)
const accionEnCurso = ref(false)

const filtroLeida = ref<boolean | undefined>(undefined)
const filtroTipo = ref<string>('')
const filtroPrioridad = ref<string>('')

const totalElementos = computed(() => notificaciones.value.length)
const contadorAlta = computed(() => notificaciones.value.filter((n) => n.prioridad === 'ALTA').length)
const tiposUnicos = computed(() => Array.from(new Set(notificaciones.value.map((n) => n.tipo))).sort())

const hayFiltros = computed(
  () => filtroLeida.value !== undefined || !!filtroTipo.value || !!filtroPrioridad.value
)

const notificacionesFiltradas = computed(() => {
  return notificaciones.value.filter((n) => {
    if (filtroLeida.value !== undefined && n.leida !== filtroLeida.value) return false
    if (filtroTipo.value && n.tipo !== filtroTipo.value) return false
    if (filtroPrioridad.value && n.prioridad !== filtroPrioridad.value) return false
    return true
  })
})

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleString('es-ES', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatTipo(tipo: string): string {
  const map: Record<string, string> = {
    SOAT_VENCIMIENTO: 'SOAT',
    LICENCIA_VENCIMIENTO: 'Licencia',
    CURSO_COMPLETADO: 'Curso',
    PAGO_ATRASADO: 'Pago',
    PASSWORD_RESET: 'Password',
    ACCOUNT_LOCKED: 'Cuenta',
    USUARIO_CREADO: 'Bienvenida'
  }
  return map[tipo] || tipo
}

function iconoTipo(tipo: string): string {
  const map: Record<string, string> = {
    SOAT_VENCIMIENTO: 'pi pi-car',
    LICENCIA_VENCIMIENTO: 'pi pi-id-card',
    CURSO_COMPLETADO: 'pi pi-graduation-cap',
    PAGO_ATRASADO: 'pi pi-wallet',
    PASSWORD_RESET: 'pi pi-key',
    ACCOUNT_LOCKED: 'pi pi-lock',
    USUARIO_CREADO: 'pi pi-user-plus'
  }
  return map[tipo] || 'pi pi-bell'
}

function clasesIcono(n: Notificacion): string {
  if (n.prioridad === 'ALTA') return 'bg-red-100 text-red-600'
  if (n.prioridad === 'NORMAL') return 'bg-brand-100 text-brand-600'
  return 'bg-ink-100 text-ink-500'
}

function clasesPrioridad(prio: string): string {
  if (prio === 'ALTA') return 'bg-red-100 text-red-700'
  if (prio === 'NORMAL') return 'bg-brand-100 text-brand-700'
  return 'bg-ink-100 text-ink-600'
}

function limpiarFiltros() {
  filtroLeida.value = undefined
  filtroTipo.value = ''
  filtroPrioridad.value = ''
}

async function marcarLeida(id: number) {
  try {
    await notificacionesStore.marcarComoLeida(id)
    toast.add({ severity: 'success', summary: 'Marcada como leída', life: 2000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo marcar', life: 3000 })
  }
}

function eliminar(id: number) {
  confirm.require({
    message: '¿Eliminar esta notificación?',
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await notificacionesStore.eliminarNotificacion(id)
        toast.add({ severity: 'success', summary: 'Eliminada', life: 2000 })
      } catch {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar', life: 3000 })
      }
    }
  })
}

function handleMarcarTodas() {
  confirm.require({
    message: `¿Marcar las ${contadorNoLeidas.value} notificaciones no leídas como leídas?`,
    header: 'Confirmar',
    icon: 'pi pi-check',
    accept: async () => {
      accionEnCurso.value = true
      try {
        await notificacionesStore.marcarTodasComoLeidas(authStore.userId!)
        toast.add({ severity: 'success', summary: 'Marcadas todas como leídas', life: 2500 })
      } catch {
        toast.add({ severity: 'error', summary: 'Error', life: 3000 })
      } finally {
        accionEnCurso.value = false
      }
    }
  })
}

function handleEliminarTodas() {
  confirm.require({
    message: `¿Eliminar TODAS las ${totalElementos.value} notificaciones? Esta acción no se puede deshacer.`,
    header: 'Confirmar eliminación masiva',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: async () => {
      accionEnCurso.value = true
      try {
        await notificacionesStore.eliminarTodasNotificaciones(authStore.userId!)
        toast.add({ severity: 'success', summary: 'Todas eliminadas', life: 2500 })
      } catch {
        toast.add({ severity: 'error', summary: 'Error', life: 3000 })
      } finally {
        accionEnCurso.value = false
      }
    }
  })
}

onMounted(() => {
  if (authStore.userId) {
    notificacionesStore.obtenerNotificaciones(authStore.userId)
  }
})
</script>
