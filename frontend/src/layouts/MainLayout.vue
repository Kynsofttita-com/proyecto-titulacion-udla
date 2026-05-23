<template>
  <div class="main-layout">
    <div class="layout-container">
      <Sidebar :visible="sidebarVisible" @hide="sidebarVisible = false" class="sidebar">
        <template #header>
          <div class="logo">
            <span class="text-xl font-bold">Gestión Escuelas</span>
          </div>
        </template>

        <PanelMenu :model="menuItems" class="w-full md:w-64" />
      </Sidebar>

      <div class="flex flex-col flex-1">
        <div class="navbar p-4 bg-white shadow-md flex items-center justify-between">
          <div class="flex items-center gap-4">
            <Button
              icon="pi pi-bars"
              class="p-button-rounded p-button-text md:hidden"
              @click="sidebarVisible = true"
            />
            <h1 class="text-2xl font-bold">Sistema de Gestión</h1>
          </div>

          <div class="flex items-center gap-4">
            <Button
              icon="pi pi-bell"
              class="p-button-rounded p-button-text"
              badge="2"
              badgeSeverity="danger"
            />
            <Menu :model="profileMenu" :popup="true">
              <template #start>
                <Button
                  :label="authStore.user?.nombreCompleto || 'Usuario'"
                  icon="pi pi-user"
                  class="p-button-text"
                  @click="$refs.profileButton?.$el.click()"
                />
              </template>
            </Menu>
          </div>
        </div>

        <div class="p-6 flex-1 overflow-auto bg-gray-50">
          <Breadcrumb :model="breadcrumbs" class="mb-6" />
          <RouterView />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Sidebar from 'primevue/sidebar'
import PanelMenu from 'primevue/panelmenu'
import Button from 'primevue/button'
import Menu from 'primevue/menu'
import Breadcrumb from 'primevue/breadcrumb'

const router = useRouter()
const authStore = useAuthStore()

const sidebarVisible = ref(false)

const menuItems = computed(() => [
  {
    label: 'Dashboard',
    icon: 'pi pi-home',
    command: () => router.push('/dashboard')
  },
  {
    label: 'Estudiantes',
    icon: 'pi pi-users',
    command: () => router.push('/estudiantes')
  },
  {
    label: 'Instructores',
    icon: 'pi pi-user-edit',
    command: () => router.push('/instructores')
  },
  {
    label: 'Vehículos',
    icon: 'pi pi-car',
    command: () => router.push('/vehiculos')
  },
  {
    label: 'Asignaciones',
    icon: 'pi pi-calendar',
    command: () => router.push('/asignaciones')
  },
  {
    label: 'Cobros',
    icon: 'pi pi-money-bill',
    command: () => router.push('/cobros')
  },
  authStore.hasRole(['ADMIN']) ? {
    label: 'Configuración',
    icon: 'pi pi-cog',
    command: () => router.push('/configuracion')
  } : null
].filter(Boolean))

const profileMenu = [
  {
    label: 'Mi Perfil',
    icon: 'pi pi-user',
    command: () => router.push('/perfil')
  },
  {
    label: 'Configuración',
    icon: 'pi pi-cog',
    command: () => router.push('/configuracion')
  },
  { separator: true },
  {
    label: 'Logout',
    icon: 'pi pi-sign-out',
    command: () => authStore.logout().then(() => router.push('/login'))
  }
]

const breadcrumbs = [
  { label: 'Home', icon: 'pi pi-home' }
]
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.layout-container {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 250px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
  }
}
</style>
