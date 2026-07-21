<template>
  <!-- ============================================================
       MODO PANTALLA ÚNICA: usuario debe cambiar contraseña (primer login)
       Se muestra dentro del AuthLayout, sin sidebar, sin distracciones.
       ============================================================ -->
  <div v-if="authStore.mustChangePassword" class="space-y-6 w-full max-w-md">
    <div class="space-y-7">
      <div>
        <h1 class="text-3xl font-bold text-ink-900 tracking-tight">Establece tu contraseña</h1>
        <p class="text-ink-500 mt-2">Antes de continuar, define una contraseña personal segura.</p>
      </div>

      <div class="rounded-lg bg-warning-50 border border-warning-500/30 p-4 flex items-start gap-3">
        <i class="pi pi-exclamation-triangle text-warning-600 mt-0.5" />
        <div>
          <p class="text-sm font-medium text-warning-700">Cambio de contraseña obligatorio</p>
          <p class="text-xs text-warning-700/80 mt-0.5">
            Tu cuenta fue creada por un administrador con una contraseña temporal.
            Define una nueva que solo tú conozcas para activar tu cuenta.
          </p>
        </div>
      </div>

      <div v-if="success" class="rounded-lg bg-success-50 border border-success-500/20 p-3 flex items-start gap-2 text-sm text-success-700">
        <i class="pi pi-check-circle mt-0.5" />
        <span>¡Contraseña actualizada! Te redirigiremos al inicio de sesión para que ingreses con tu nueva contraseña...</span>
      </div>

      <form @submit.prevent="cambiar" class="space-y-5" autocomplete="off">
        <div>
          <label for="field-pwd-currentPassword" class="label mb-1.5 block">
            Contraseña temporal <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Password
            v-model="form.currentPassword"
            :feedback="false"
            toggleMask
            class="w-full"
            :inputClass="errorsPwd.currentPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
            placeholder="La que te dieron"
            :inputProps="{ id: 'field-pwd-currentPassword', autocomplete: 'new-password' }"
            @update:modelValue="clearErrPwd('currentPassword')"
          />
          <p v-if="errorsPwd.currentPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.currentPassword }}
          </p>
        </div>
        <div>
          <label for="field-pwd-newPassword" class="label mb-1.5 block">
            Nueva contraseña <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Password
            v-model="form.newPassword"
            toggleMask
            class="w-full"
            :inputClass="errorsPwd.newPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
            placeholder="Mínimo 8 caracteres"
            :inputProps="{ id: 'field-pwd-newPassword', autocomplete: 'new-password' }"
            @update:modelValue="clearErrPwd('newPassword')"
          />
          <p v-if="errorsPwd.newPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.newPassword }}
          </p>
          <p v-else class="text-xs text-ink-500 mt-1.5">Mínimo 8 caracteres con mayúscula, minúscula y dígito.</p>
        </div>
        <div>
          <label for="field-pwd-confirmPassword" class="label mb-1.5 block">
            Confirmar nueva contraseña <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Password
            v-model="form.confirmPassword"
            :feedback="false"
            toggleMask
            class="w-full"
            :inputClass="errorsPwd.confirmPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
            placeholder="Repítela"
            :inputProps="{ id: 'field-pwd-confirmPassword', autocomplete: 'new-password' }"
            @update:modelValue="clearErrPwd('confirmPassword')"
          />
          <p v-if="errorsPwd.confirmPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.confirmPassword }}
          </p>
        </div>

        <div v-if="error" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ error }}</span>
        </div>

        <Button
          type="submit"
          :loading="isLoading"
          class="w-full !py-3 !text-base !font-semibold"
        >
          <span class="flex items-center gap-2">
            Establecer contraseña y continuar
            <i class="pi pi-arrow-right text-sm" />
          </span>
        </Button>
      </form>

      <div class="pt-4 border-t border-ink-200 text-center">
        <button @click="cerrarSesion" class="text-xs text-ink-500 hover:text-danger-600 transition">
          ¿Prefieres salir? Cerrar sesión
        </button>
      </div>
    </div>
  </div>

  <!-- ============================================================
       MODO NORMAL: usuario ya activado, ve perfil completo
       ============================================================ -->
  <div v-else class="space-y-6 max-w-3xl mx-auto">
    <PageHeader
      title="Mi perfil"
      description="Información de tu cuenta y configuración de seguridad."
      icon="pi pi-user"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Mi perfil' }]"
    >
      <template #actions>
        <Button label="Recargar perfil" icon="pi pi-refresh" outlined :loading="recargando" @click="recargarPerfil" />
      </template>
    </PageHeader>

    <!-- Banner URGENTE si el usuario debe cambiar contraseña (NO se llega aquí, pero por defensa) -->
    <div v-if="false" class="rounded-xl bg-gradient-to-r from-warning-50 to-warning-50/50 border-2 border-warning-500/30 p-5 animate-fade-up flex items-start gap-4">
      <div class="w-12 h-12 rounded-xl bg-warning-500 text-white flex items-center justify-center flex-shrink-0 shadow-card">
        <i class="pi pi-exclamation-triangle text-xl" />
      </div>
      <div class="flex-1">
        <h3 class="text-lg font-bold text-warning-700 mb-1">Debes cambiar tu contraseña antes de continuar</h3>
        <p class="text-sm text-warning-700/90">
          Tu cuenta fue creada por un administrador con una contraseña temporal.
          Por tu seguridad, debes establecer una contraseña nueva que solo tú conozcas
          antes de acceder al resto del sistema.
        </p>
        <p class="text-xs text-warning-700/70 mt-2 flex items-center gap-1.5">
          <i class="pi pi-lock" />
          <span>Esta restricción se levantará automáticamente al guardar tu nueva contraseña.</span>
        </p>
      </div>
    </div>

    <!-- Card info del usuario -->
    <section class="card animate-fade-up overflow-hidden">
      <!-- Hero gradient sólo decorativo arriba -->
      <div class="h-3 bg-gradient-to-r from-brand-700 via-brand-600 to-brand-500"></div>

      <!-- Contenido principal -->
      <div class="p-6">
        <div class="flex items-center gap-5">
          <!-- Avatar grande con gradient teal interno -->
          <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-brand-600 to-brand-800 flex items-center justify-center text-2xl font-bold text-white shadow-card flex-shrink-0">
            {{ initials }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-2xl font-bold text-ink-900 truncate">{{ userName }}</h2>
              <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-success-50 text-success-600 text-xs font-medium border border-success-500/20">
                <span class="w-1.5 h-1.5 rounded-full bg-success-500" />
                Activo
              </span>
            </div>
            <p class="text-sm text-ink-500 truncate mt-1">{{ userEmail }}</p>
          </div>
        </div>

        <!-- Info en grid -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mt-6 pt-6 border-t border-ink-200">
          <Info label="Correo electrónico" :value="userEmail" icon="pi-envelope" />
          <Info label="Rol activo" :value="authStore.currentRole" icon="pi-shield" tone="brand" />
          <Info label="Estado de cuenta" value="Activo" tone="success" icon="pi-check-circle" />
        </div>
      </div>
    </section>

    <!-- Card: Selector de rol activo (solo si tiene múltiples roles) -->
    <FormCard
      v-if="authStore.hasMultipleRoles"
      title="Cambiar rol activo"
      description="Tienes más de un rol asignado. Selecciona con cuál quieres trabajar — la vista del sistema se ajustará a los permisos de ese rol."
      icon="pi pi-sync"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <button
          v-for="rol in authStore.roles"
          :key="rol"
          type="button"
          @click="seleccionarRol(rol)"
          :class="['flex items-start gap-3 p-4 rounded-lg border-2 text-left transition',
            rol === authStore.currentRole
              ? 'border-brand-600 bg-brand-50/40 shadow-card'
              : 'border-ink-200 hover:border-brand-300 hover:bg-brand-50/20']"
        >
          <div :class="['w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0',
            rol === authStore.currentRole ? 'bg-brand-600 text-white' : 'bg-ink-100 text-ink-600']">
            <i :class="ROLE_ICONS[rol] || 'pi pi-user'" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <p class="text-sm font-bold text-ink-900">{{ rol }}</p>
              <span
                v-if="rol === authStore.currentRole"
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-success-50 text-success-600 text-[10px] font-medium border border-success-500/20"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-success-500" />
                Activo
              </span>
            </div>
            <p class="text-xs text-ink-500 mt-1">{{ ROLE_DESCRIPCIONES[rol] || 'Acceso al sistema' }}</p>
          </div>
          <i v-if="rol === authStore.currentRole" class="pi pi-check-circle text-brand-700 text-lg" />
        </button>
      </div>
      <p class="text-xs text-ink-500 mt-4 flex items-start gap-2">
        <i class="pi pi-info-circle text-info-600 mt-0.5" />
        <span>El sidebar y los menús se adaptarán automáticamente. Útil para probar cómo verá el sistema un usuario con menos permisos.</span>
      </p>
    </FormCard>

    <!-- Card: Roles asignados (informativa) -->
    <section v-else class="card animate-fade-up p-5">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center">
          <i class="pi pi-shield" />
        </div>
        <div class="flex-1">
          <p class="text-sm font-semibold text-ink-900">Rol asignado</p>
          <p class="text-xs text-ink-500">Solo tienes un rol asignado. Si necesitas otro, pide al administrador.</p>
        </div>
        <span class="inline-flex items-center px-3 py-1 rounded-full bg-brand-50 text-brand-700 text-xs font-bold border border-brand-200">
          {{ authStore.currentRole }}
        </span>
      </div>
    </section>

    <!-- Card cambio de contraseña -->
    <FormCard
      title="Cambiar contraseña"
      description="Necesitas tu contraseña actual para cambiarla. Se cerrarán las demás sesiones."
      icon="pi pi-lock"
    >
      <div v-if="success" class="rounded-lg bg-success-50 border border-success-500/20 p-3 mb-5 flex items-start gap-2 text-sm text-success-700">
        <i class="pi pi-check-circle mt-0.5" />
        <span>Contraseña actualizada correctamente. Vuelve a iniciar sesión.</span>
      </div>

      <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 mb-5 flex items-center gap-2">
        <i class="pi pi-info-circle text-info-600" />
        <p class="text-sm text-ink-700">
          Los campos marcados con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
        </p>
      </div>

      <form @submit.prevent="cambiar" class="space-y-5" autocomplete="off">
        <div>
          <label for="field-pwd-currentPassword" class="label mb-1.5 block">
            Contraseña actual <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Password
            v-model="form.currentPassword"
            :feedback="false"
            toggleMask
            class="w-full"
            :inputClass="errorsPwd.currentPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
            placeholder="••••••••"
            :inputProps="{ id: 'field-pwd-currentPassword', autocomplete: 'new-password' }"
            @update:modelValue="clearErrPwd('currentPassword')"
          />
          <p v-if="errorsPwd.currentPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.currentPassword }}
          </p>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div>
            <label for="field-pwd-newPassword" class="label mb-1.5 block">
              Nueva contraseña <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Password
              v-model="form.newPassword"
              toggleMask
              class="w-full"
              :inputClass="errorsPwd.newPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
              placeholder="••••••••"
              :inputProps="{ id: 'field-pwd-newPassword', autocomplete: 'new-password' }"
              @update:modelValue="clearErrPwd('newPassword')"
            />
            <p v-if="errorsPwd.newPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.newPassword }}
            </p>
            <p v-else class="text-xs text-ink-500 mt-1.5">Mínimo 8 caracteres con mayúscula, minúscula y dígito.</p>
          </div>
          <div>
            <label for="field-pwd-confirmPassword" class="label mb-1.5 block">
              Confirmar nueva <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Password
              v-model="form.confirmPassword"
              :feedback="false"
              toggleMask
              class="w-full"
              :inputClass="errorsPwd.confirmPassword ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full'"
              placeholder="••••••••"
              :inputProps="{ id: 'field-pwd-confirmPassword', autocomplete: 'new-password' }"
              @update:modelValue="clearErrPwd('confirmPassword')"
            />
            <p v-if="errorsPwd.confirmPassword" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.confirmPassword }}
            </p>
          </div>
        </div>

        <div v-if="error" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ error }}</span>
        </div>
      </form>

      <template #actions>
        <Button label="Cancelar" outlined type="button" @click="resetForm" />
        <Button
          label="Cambiar contraseña"
          icon="pi pi-check"
          :loading="isLoading"
          @click="cambiar"
        />
      </template>
    </FormCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'
import Password from 'primevue/password'
import Button from 'primevue/button'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import { useAuthStore } from '@/stores/auth'
import usuariosService from '@/services/usuarios'

const Info = defineComponent({
  props: ['label', 'value', 'tone', 'icon'],
  setup(props) {
    return () =>
      h('div', { class: 'flex items-start gap-3' }, [
        props.icon && h('div', {
          class: 'w-9 h-9 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0 mt-0.5'
        }, [h('i', { class: `pi ${props.icon} text-sm` })]),
        h('div', { class: 'min-w-0 flex-1' }, [
          h('p', { class: 'text-[11px] uppercase tracking-wider text-ink-500 font-semibold mb-1' }, props.label),
          h('p', {
            class: `text-sm font-medium truncate ${
              props.tone === 'success' ? 'text-success-600' :
              props.tone === 'brand'   ? 'text-brand-700 font-bold' :
              'text-ink-800'
            }`
          }, props.value)
        ])
      ])
  }
})

const ROLE_DESCRIPCIONES: Record<string, string> = {
  ADMIN: 'Acceso total al sistema. Puedes crear/editar usuarios, configurar la escuela y ver todos los reportes.',
  STAFF: 'Operaciones administrativas. Gestión diaria de estudiantes, instructores, vehículos y cobros.',
  INSTRUCTOR: 'Ver las clases que tienes asignadas y los datos de tus alumnos.',
  ESTUDIANTE: 'Ver tu progreso académico, asignaciones de clases y estado de cuenta.'
}

const ROLE_ICONS: Record<string, string> = {
  ADMIN: 'pi pi-shield',
  STAFF: 'pi pi-briefcase',
  INSTRUCTOR: 'pi pi-id-card',
  ESTUDIANTE: 'pi pi-graduation-cap'
}

const seleccionarRol = (rol: string) => {
  if (rol === authStore.currentRole) return
  authStore.setActiveRole(rol)
  // Refrescar para que el sidebar/router revaluen permisos
  router.replace('/dashboard')
}

const recargando = ref(false)
const recargarPerfil = async () => {
  recargando.value = true
  try {
    await authStore.refreshProfile()
  } finally {
    recargando.value = false
  }
}

const cerrarSesion = async () => {
  await authStore.logout()
  router.push('/login')
}

const router = useRouter()
const authStore = useAuthStore()

const userName = computed(() => {
  const u: any = authStore.user
  return u?.nombreCompleto || `${u?.nombre ?? ''} ${u?.apellido ?? ''}`.trim() || 'Usuario'
})
const userEmail = computed(() => authStore.user?.email || '')
const initials = computed(() => userName.value.split(' ').slice(0, 2).map(s => s[0]?.toUpperCase()).join(''))

const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const isLoading = ref(false)
const error = ref('')
const success = ref(false)

// -------- Helper factory de validación por campo --------
function useValidation() {
  const errors = reactive<Record<string, string>>({})
  const setError = (k: string, v: string) => { errors[k] = v }
  const clearError = (k: string) => { if (errors[k]) delete errors[k] }
  const clearAll = () => { Object.keys(errors).forEach(k => delete errors[k]) }
  const focusFirst = (orden: string[], prefijo: string, scroll = false) => {
    const p = orden.find(k => errors[k])
    if (!p) return
    setTimeout(() => {
      const el = document.getElementById(`field-${prefijo}-${p}`)
      if (!el) return
      if (scroll) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      ;(el as HTMLElement).focus?.()
    }, scroll ? 300 : 100)
  }
  return { errors, setError, clearError, clearAll, focusFirst }
}

const valPwd = useValidation()
const errorsPwd = valPwd.errors
const setErrPwd = valPwd.setError
const clearErrPwd = valPwd.clearError
const clearAllPwd = valPwd.clearAll

const validarPwd = (): boolean => {
  clearAllPwd()
  if (!form.currentPassword) setErrPwd('currentPassword', 'La contraseña actual es requerida')

  const pass = form.newPassword ?? ''
  if (!pass) {
    setErrPwd('newPassword', 'La nueva contraseña es requerida')
  } else if (pass.length < 8) {
    setErrPwd('newPassword', 'Mínimo 8 caracteres')
  } else if (!/[A-Z]/.test(pass) || !/[a-z]/.test(pass) || !/\d/.test(pass)) {
    setErrPwd('newPassword', 'Debe incluir mayúscula, minúscula y dígito')
  } else if (pass === form.currentPassword) {
    setErrPwd('newPassword', 'La nueva contraseña debe ser diferente a la actual')
  }

  if (!form.confirmPassword) {
    setErrPwd('confirmPassword', 'Confirma la nueva contraseña')
  } else if (form.newPassword && form.confirmPassword !== form.newPassword) {
    setErrPwd('confirmPassword', 'Las contraseñas no coinciden')
  }

  if (Object.keys(errorsPwd).length > 0) {
    valPwd.focusFirst(['currentPassword', 'newPassword', 'confirmPassword'], 'pwd', false)
    return false
  }
  return true
}

const resetForm = () => {
  form.currentPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  error.value = ''
  clearAllPwd()
}

const cambiar = async () => {
  error.value = ''
  if (!validarPwd()) return
  success.value = false
  isLoading.value = true
  try {
    await usuariosService.cambiarPassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })
    success.value = true
    resetForm()
    // SIEMPRE: cerrar sesión y forzar nuevo login con la nueva contraseña.
    // Esto garantiza que el JWT se regenere con el flag mustChangePassword=false
    // y que el sistema cargue limpio sin estado corrupto.
    setTimeout(async () => {
      await authStore.logout()
      router.push('/login')
    }, 1800)
  } catch (e: any) {
    const data = e.response?.data
    error.value = data?.errors
      ? Object.values(data.errors).join(' · ')
      : (data?.detail || data?.message || 'No se pudo cambiar la contraseña')
  } finally { isLoading.value = false }
}
</script>
