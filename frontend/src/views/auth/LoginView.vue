<template>
  <div class="space-y-7">
    <div>
      <h1 class="text-3xl font-bold text-ink-900 tracking-tight">Bienvenido de vuelta</h1>
      <p class="text-ink-500 mt-2">Ingresa tus credenciales para acceder al panel.</p>
    </div>

    <div v-if="authStore.error" class="rounded-lg bg-danger-50 border border-danger-500/20 p-4 flex items-start gap-3">
      <i class="pi pi-exclamation-circle text-danger-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-danger-600">No se pudo iniciar sesión</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ authStore.error }}</p>
      </div>
    </div>

    <form @submit.prevent="handleLogin" class="space-y-5">
      <div>
        <label for="field-login-email" class="label mb-1.5 block">
          Correo electrónico <span class="text-danger-600 font-semibold">*</span>
        </label>
        <span class="p-input-icon-left w-full">
          <i class="pi pi-envelope text-ink-400" />
          <InputText
            id="field-login-email"
            v-model="email"
            type="email"
            placeholder="tu@correo.com"
            maxlength="120"
            class="w-full !pl-10"
            :class="errors.email ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErr('email')"
          />
        </span>
        <p v-if="errors.email" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
          <i class="pi pi-exclamation-circle text-[10px]" />{{ errors.email }}
        </p>
      </div>

      <div>
        <div class="flex items-center justify-between mb-1.5">
          <label for="field-login-password" class="label">
            Contraseña <span class="text-danger-600 font-semibold">*</span>
          </label>
          <router-link to="/forgot-password" class="text-xs text-brand-700 font-medium hover:text-brand-800">
            ¿Olvidaste tu contraseña?
          </router-link>
        </div>
        <span class="p-input-icon-left w-full">
          <i class="pi pi-lock text-ink-400" />
          <InputText
            id="field-login-password"
            v-model="password"
            type="password"
            placeholder="••••••••"
            maxlength="100"
            class="w-full !pl-10"
            :class="errors.password ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErr('password')"
          />
        </span>
        <p v-if="errors.password" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
          <i class="pi pi-exclamation-circle text-[10px]" />{{ errors.password }}
        </p>
      </div>

      <Button
        type="submit"
        :loading="authStore.isLoading"
        class="w-full !py-3 !text-base !font-semibold"
      >
        <span class="flex items-center gap-2">
          Iniciar sesión
          <i class="pi pi-arrow-right text-sm" />
        </span>
      </Button>
    </form>

    <div class="rounded-lg bg-ink-50 border border-ink-200 p-4">
      <p class="text-xs font-semibold text-ink-700 mb-2 flex items-center gap-1.5">
        <i class="pi pi-info-circle text-brand-600" />
        Credenciales de demostración
      </p>
      <div class="space-y-1 font-mono text-xs text-ink-600">
        <p><span class="text-ink-400">Usuario:</span> admin@escuela.local</p>
        <p><span class="text-ink-400">Clave:</span> Admin123!</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')

// -------- Helper factory de validación por campo --------
function useValidation() {
  const errors = reactive<Record<string, string>>({})
  const setError = (k: string, v: string) => { errors[k] = v }
  const clearError = (k: string) => { if (errors[k]) delete errors[k] }
  const clearAll = () => { Object.keys(errors).forEach(k => delete errors[k]) }
  const focusFirst = (orden: string[], prefijo: string) => {
    const p = orden.find(k => errors[k])
    if (!p) return
    setTimeout(() => document.getElementById(`field-${prefijo}-${p}`)?.focus?.(), 100)
  }
  return { errors, setError, clearError, clearAll, focusFirst }
}

const val = useValidation()
const errors = val.errors
const setErr = val.setError
const clearErr = val.clearError
const clearAll = val.clearAll

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validar = (): boolean => {
  clearAll()
  const em = email.value?.trim() ?? ''
  if (!em) setErr('email', 'El correo es requerido')
  else if (!EMAIL_REGEX.test(em)) setErr('email', 'Formato de email inválido')
  if (!password.value) setErr('password', 'La contraseña es requerida')
  if (Object.keys(errors).length > 0) {
    val.focusFirst(['email', 'password'], 'login')
    return false
  }
  return true
}

const handleLogin = async () => {
  if (!validar()) return
  authStore.error = null
  try {
    await authStore.login(email.value, password.value)
    // Siempre va al dashboard, independientemente de la URL previa
    router.push('/dashboard')
  } catch (_) {
    // mensaje viene del store
  }
}
</script>
