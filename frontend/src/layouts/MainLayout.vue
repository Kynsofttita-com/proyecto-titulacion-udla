<template>
  <div class="min-h-screen bg-ink-50">
    <!-- ===== SIDEBAR FIJO ===== -->
    <aside :class="['sidebar-fixed scroll-thin flex flex-col', mobileOpen && 'open']">
      <div class="px-5 py-5 border-b border-ink-200 flex items-center justify-between">
        <router-link to="/dashboard" class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-brand-600 to-brand-800 flex items-center justify-center shadow-card">
            <i class="pi pi-car text-white text-sm" />
          </div>
          <div class="leading-tight">
            <p class="text-sm font-bold text-ink-900">Escuela Conducción</p>
            <p class="text-[11px] text-ink-500">Panel Administrativo</p>
          </div>
        </router-link>
        <button
          class="lg:hidden w-8 h-8 rounded-md hover:bg-ink-100 text-ink-500 flex items-center justify-center"
          @click="mobileOpen = false"
          aria-label="Cerrar menú"
        >
          <i class="pi pi-times text-sm" />
        </button>
      </div>

      <nav class="flex-1 p-3 space-y-0.5">
        <p class="px-3 py-2 text-[11px] uppercase tracking-wider text-ink-400 font-semibold">General</p>
        <NavItem to="/dashboard" icon="pi-th-large" label="Dashboard" />

        <p class="px-3 pt-4 pb-2 text-[11px] uppercase tracking-wider text-ink-400 font-semibold">Operación</p>
        <NavItem to="/estudiantes"   icon="pi-users"     label="Estudiantes" />
        <NavItem to="/instructores"  icon="pi-id-card"   label="Instructores" />
        <NavItem to="/vehiculos"     icon="pi-car"       label="Vehículos" />
        <NavItem to="/asignaciones"  icon="pi-calendar"  label="Asignaciones" />

        <p class="px-3 pt-4 pb-2 text-[11px] uppercase tracking-wider text-ink-400 font-semibold">Finanzas</p>
        <NavItem to="/cobros"        icon="pi-wallet"    label="Cobros y Pagos" />

        <p class="px-3 pt-4 pb-2 text-[11px] uppercase tracking-wider text-ink-400 font-semibold">Mi cuenta</p>
        <NavItem to="/perfil" icon="pi-user" label="Mi perfil" />

        <p v-if="isAdmin" class="px-3 pt-4 pb-2 text-[11px] uppercase tracking-wider text-ink-400 font-semibold">Sistema</p>
        <NavItem v-if="isAdmin" to="/usuarios"      icon="pi-users"  label="Usuarios" />
        <NavItem v-if="isAdmin" to="/configuracion" icon="pi-cog"    label="Configuración" />
      </nav>

      <div class="p-3 border-t border-ink-200">
        <div class="flex items-center gap-3 px-2 py-2">
          <Avatar :name="userName" size="md" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-ink-900 truncate">{{ userName }}</p>
            <p class="text-xs text-ink-500 truncate">{{ userEmail }}</p>
          </div>
          <button
            class="w-8 h-8 rounded-md hover:bg-ink-100 text-ink-500 flex items-center justify-center"
            @click="logout"
            title="Cerrar sesión"
          >
            <i class="pi pi-sign-out text-sm" />
          </button>
        </div>
      </div>
    </aside>

    <!-- Backdrop mobile -->
    <div
      v-if="mobileOpen"
      class="lg:hidden fixed inset-0 bg-ink-900/30 backdrop-blur-sm z-30"
      @click="mobileOpen = false"
    />

    <!-- ===== CONTENIDO ===== -->
    <div class="main-content-shift min-h-screen flex flex-col">
      <!-- TopBar -->
      <header class="sticky top-0 z-20 h-16 bg-white/80 backdrop-blur-md border-b border-ink-200 flex items-center justify-between px-4 lg:px-6">
        <div class="flex items-center gap-3 flex-1">
          <button
            class="lg:hidden w-9 h-9 rounded-lg hover:bg-ink-100 text-ink-700 flex items-center justify-center"
            @click="mobileOpen = true"
            aria-label="Abrir menú"
          >
            <i class="pi pi-bars" />
          </button>
          <div class="hidden md:flex items-center gap-2 flex-1 max-w-md px-3 py-2 rounded-lg bg-ink-100/60 border border-transparent hover:border-ink-200 focus-within:border-brand-400 focus-within:bg-white transition">
            <i class="pi pi-search text-sm text-ink-400" />
            <input
              type="text"
              placeholder="Buscar en el sistema..."
              class="flex-1 bg-transparent text-sm outline-none placeholder-ink-400"
            />
            <kbd class="hidden lg:inline text-[10px] font-mono px-1.5 py-0.5 rounded bg-white border border-ink-200 text-ink-500">⌘K</kbd>
          </div>
        </div>

        <div class="flex items-center gap-1">
          <button class="relative w-10 h-10 rounded-lg hover:bg-ink-100 text-ink-600 flex items-center justify-center transition">
            <i class="pi pi-bell" />
            <span class="absolute top-1.5 right-1.5 w-4 h-4 bg-danger-500 text-white text-[10px] font-semibold rounded-full flex items-center justify-center">2</span>
          </button>
          <button class="w-10 h-10 rounded-lg hover:bg-ink-100 text-ink-600 flex items-center justify-center transition" title="Ayuda">
            <i class="pi pi-question-circle" />
          </button>
          <!-- Switcher de rol (visible solo si tiene múltiples roles) -->
          <div v-if="authStore.hasMultipleRoles" class="hidden md:flex items-center">
            <div class="w-px h-6 bg-ink-200 mx-2" />
            <button
              @click="toggleRoleMenu"
              class="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-ink-100 transition group"
              v-tooltip.bottom="'Cambiar de rol activo'"
            >
              <div class="w-7 h-7 rounded-md bg-accent-50 text-accent-600 flex items-center justify-center">
                <i class="pi pi-sync text-xs" />
              </div>
              <div class="text-left">
                <p class="text-[10px] uppercase tracking-wider text-ink-500 leading-none">Actuando como</p>
                <p class="text-xs font-bold text-ink-900 leading-tight mt-0.5">{{ authStore.currentRole }}</p>
              </div>
              <i class="pi pi-angle-down text-xs text-ink-400" />
            </button>
            <Menu ref="roleMenuRef" :model="roleMenu" popup class="!min-w-[260px]" :pt="{ submenuHeader: { class: 'text-xs uppercase text-ink-500' } }" />
          </div>

          <div class="w-px h-6 bg-ink-200 mx-2" />
          <button @click="toggleUserMenu" class="flex items-center gap-2 pl-1 pr-3 py-1 rounded-lg hover:bg-ink-100 transition">
            <Avatar :name="userName" size="sm" />
            <div class="hidden lg:block text-left">
              <p class="text-xs font-semibold text-ink-900 leading-tight">{{ userName }}</p>
              <p class="text-[11px] text-ink-500 leading-tight">{{ authStore.currentRole }}</p>
            </div>
            <i class="pi pi-angle-down text-xs text-ink-400" />
          </button>
        </div>
        <Menu ref="userMenuRef" :model="userMenu" popup class="!min-w-[200px]" />
      </header>

      <!-- Contenido scrollable
           Nota: sin <transition> envolviendo el <component> porque interactuaba
           mal con componentes que usan Teleport interno (PrimeVue Password en
           PerfilView) — el leave quedaba colgado y la siguiente vista no se
           pintaba hasta recargar. El :key fuerza unmount/mount completo entre
           rutas, así no hay estado residual entre vistas. -->
      <main class="flex-1 p-4 lg:p-8">
        <router-view v-slot="{ Component, route }">
          <component :is="Component" :key="route.fullPath" />
        </router-view>
      </main>

      <footer class="border-t border-ink-200 px-6 py-3 bg-white/60">
        <p class="text-xs text-ink-500 text-center">
          © 2026 Universidad de las Américas — Sistema de Gestión Escuelas de Conducción
        </p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Menu from 'primevue/menu'
import Tooltip from 'primevue/tooltip'
import NavItem from '@/components/ui/NavItem.vue'
import Avatar from '@/components/ui/Avatar.vue'

const vTooltip = Tooltip

const router = useRouter()
const authStore = useAuthStore()

const mobileOpen = ref(false)
const userMenuRef = ref<any>(null)
const roleMenuRef = ref<any>(null)

const userName = computed(() => {
  const u: any = authStore.user
  return u?.nombreCompleto || `${u?.nombre || ''} ${u?.apellido || ''}`.trim() || 'Usuario'
})
const userEmail = computed(() => authStore.user?.email || '')
const isAdmin = computed(() => authStore.hasRole(['ADMIN']))

const toggleUserMenu = (e: Event) => userMenuRef.value?.toggle(e)
const toggleRoleMenu = (e: Event) => roleMenuRef.value?.toggle(e)

const ROLE_DESCRIPCIONES: Record<string, string> = {
  ADMIN: 'Acceso total al sistema',
  STAFF: 'Operaciones administrativas',
  INSTRUCTOR: 'Ver clases y alumnos asignados',
  ESTUDIANTE: 'Ver progreso académico'
}

const ROLE_ICONS: Record<string, string> = {
  ADMIN: 'pi pi-shield',
  STAFF: 'pi pi-briefcase',
  INSTRUCTOR: 'pi pi-id-card',
  ESTUDIANTE: 'pi pi-graduation-cap'
}

const roleMenu = computed(() => {
  return authStore.roles.map(rol => ({
    label: rol,
    icon: rol === authStore.currentRole ? 'pi pi-check-circle' : ROLE_ICONS[rol] || 'pi pi-user',
    command: () => cambiarRol(rol),
    class: rol === authStore.currentRole ? 'bg-brand-50 text-brand-700 font-semibold' : ''
  }))
})

const cambiarRol = (rol: string) => {
  if (rol === authStore.currentRole) return
  authStore.setActiveRole(rol)
  // Refrescar la ruta actual para que los guards revalúen permisos
  router.replace({ path: '/dashboard' })
}

const userMenu = [
  { label: 'Mi Perfil', icon: 'pi pi-user', command: () => router.push('/perfil') },
  { label: 'Configuración', icon: 'pi pi-cog', command: () => router.push('/configuracion') },
  { separator: true },
  { label: 'Cerrar sesión', icon: 'pi pi-sign-out', command: () => logout() }
]

const logout = async () => {
  await authStore.logout()
  // Forzar redirect inmediato sin importar el estado del router
  await router.push('/login')
  // Reload completo para garantizar que NINGÚN componente conserve estado anterior
  // (ej: stores con persist, cachés, intervalos abiertos)
  setTimeout(() => { window.location.href = '/login' }, 50)
}

// Cierra drawer mobile al cambiar de ruta
router.afterEach(() => { mobileOpen.value = false })

// Atajo ⌘K / Ctrl+K para buscador
const onKey = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    const input = document.querySelector('header input[type=text]') as HTMLInputElement | null
    input?.focus()
  }
}
onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>

