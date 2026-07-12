<template>
  <div class="relative">
    <!-- Botón con badge -->
    <button
      @click="mostrarDropdown = !mostrarDropdown"
      class="relative w-9 h-9 rounded-md hover:bg-ink-100 text-ink-600 flex items-center justify-center transition"
      title="Notificaciones"
    >
      <i class="pi pi-bell text-sm" />
      <span
        v-if="contadorNoLeidas > 0"
        class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center"
      >
        {{ contadorNoLeidas > 9 ? '9+' : contadorNoLeidas }}
      </span>
    </button>

    <!-- Dropdown panel -->
    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="mostrarDropdown"
        class="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-lg border border-ink-200 z-50"
        @click.stop
      >
        <!-- Header -->
        <div class="px-4 py-3 border-b border-ink-200 flex items-center justify-between">
          <h3 class="font-semibold text-ink-900">Notificaciones</h3>
          <div class="flex gap-2">
            <button
              v-if="contadorNoLeidas > 0"
              @click="marcarTodasComoLeidas"
              class="text-xs text-brand-600 hover:text-brand-700 font-medium"
              :disabled="marcandoTodas"
            >
              Marcar todas como leídas
            </button>
            <button
              @click="mostrarDropdown = false"
              class="text-ink-400 hover:text-ink-600"
            >
              <i class="pi pi-times text-sm" />
            </button>
          </div>
        </div>

        <!-- Lista de notificaciones -->
        <div
          class="max-h-96 overflow-y-auto"
          :class="loading && 'flex items-center justify-center py-8'"
        >
          <div v-if="loading" class="text-center py-8">
            <i class="pi pi-spin pi-spinner text-brand-600" />
            <p class="text-sm text-ink-500 mt-2">Cargando...</p>
          </div>

          <div v-else-if="notificacionesRecientes.length === 0" class="px-4 py-8 text-center">
            <i class="pi pi-inbox text-2xl text-ink-300 mb-2" />
            <p class="text-sm text-ink-500">Sin notificaciones</p>
          </div>

          <ul v-else class="divide-y divide-ink-100">
            <li
              v-for="notif in notificacionesRecientes"
              :key="notif.id"
              class="px-4 py-3 hover:bg-ink-50 transition cursor-pointer"
              :class="!notif.leida && 'bg-brand-50 border-l-2 border-brand-500'"
              @click="handleClickNotificacion(notif)"
            >
              <div class="flex gap-3">
                <div class="flex-1 min-w-0">
                  <div class="flex items-start gap-2">
                    <p class="text-sm font-medium text-ink-900 flex-1 truncate">
                      {{ notif.titulo }}
                    </p>
                    <span
                      v-if="!notif.leida"
                      class="w-2 h-2 rounded-full bg-brand-500 flex-shrink-0 mt-1"
                    />
                  </div>
                  <p class="text-xs text-ink-500 mt-1 line-clamp-2">
                    {{ notif.contenido }}
                  </p>
                  <p class="text-[11px] text-ink-400 mt-1">
                    {{ formatearFecha(notif.createdAt) }}
                  </p>
                </div>
                <button
                  @click.stop="eliminarNotificacion(notif.id)"
                  class="text-ink-400 hover:text-red-600 flex-shrink-0"
                  title="Eliminar"
                >
                  <i class="pi pi-trash text-xs" />
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- Footer -->
        <div v-if="notificacionesRecientes.length > 0" class="px-4 py-3 border-t border-ink-200 text-center">
          <router-link
            to="/notificaciones"
            class="text-sm text-brand-600 hover:text-brand-700 font-medium"
          >
            Ver todas las notificaciones →
          </router-link>
        </div>
      </div>
    </transition>

    <!-- Overlay para cerrar dropdown -->
    <div
      v-if="mostrarDropdown"
      @click="mostrarDropdown = false"
      class="fixed inset-0 z-40"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNotificacionesStore } from '@/stores/notificaciones'
import { useAuthStore } from '@/stores/auth'

const notificacionesStore = useNotificacionesStore()
const authStore = useAuthStore()

const mostrarDropdown = ref(false)
const marcandoTodas = ref(false)

const loading = computed(() => notificacionesStore.loading)
const contadorNoLeidas = computed(() => notificacionesStore.contadorNoLeidas)
const notificacionesRecientes = computed(() => notificacionesStore.notificacionesRecientes)

function formatearFecha(fecha: string): string {
  const date = new Date(fecha)
  const ahora = new Date()
  const diffMs = ahora.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHoras = Math.floor(diffMins / 60)
  const diffDias = Math.floor(diffHoras / 24)

  if (diffMins < 1) return 'Justo ahora'
  if (diffMins < 60) return `Hace ${diffMins} min`
  if (diffHoras < 24) return `Hace ${diffHoras} h`
  if (diffDias < 7) return `Hace ${diffDias}d`

  return date.toLocaleDateString('es-ES')
}

async function handleClickNotificacion(notif: any) {
  if (!notif.leida) {
    await notificacionesStore.marcarComoLeida(notif.id)
  }
}

async function marcarTodasComoLeidas() {
  marcandoTodas.value = true
  try {
    await notificacionesStore.marcarTodasComoLeidas(authStore.userId!)
  } finally {
    marcandoTodas.value = false
  }
}

async function eliminarNotificacion(id: number) {
  await notificacionesStore.eliminarNotificacion(id)
}

onMounted(() => {
  if (authStore.userId) {
    notificacionesStore.iniciarPolling(authStore.userId)
  }
})

onUnmounted(() => {
  notificacionesStore.detenerPolling()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
