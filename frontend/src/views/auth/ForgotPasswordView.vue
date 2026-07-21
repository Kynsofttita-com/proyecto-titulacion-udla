<template>
  <div class="space-y-7">
    <div>
      <h1 class="text-3xl font-bold text-ink-900 tracking-tight">Recuperar contraseña</h1>
      <p class="text-ink-500 mt-2">
        Ingresa tu correo registrado y te enviaremos un enlace para restablecer tu contraseña.
      </p>
    </div>

    <div v-if="submitted" class="rounded-lg bg-success-50 border border-success-500/20 p-4 flex items-start gap-3 animate-fade-up">
      <i class="pi pi-check-circle text-success-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-success-700">Correo enviado</p>
        <p class="text-xs text-success-700/80 mt-0.5">
          Revisa tu bandeja de entrada. Si el correo está registrado, recibirás el enlace de recuperación.
        </p>
      </div>
    </div>

    <div v-if="error" class="rounded-lg bg-danger-50 border border-danger-500/20 p-4 flex items-start gap-3">
      <i class="pi pi-exclamation-circle text-danger-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-danger-600">No se pudo procesar</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ error }}</p>
      </div>
    </div>

    <form v-if="!submitted" @submit.prevent="handleSubmit" class="space-y-5">
      <div>
        <label for="field-forgot-email" class="label mb-1.5 block">
          Correo electrónico <span class="text-danger-600 font-semibold">*</span>
        </label>
        <span class="p-input-icon-left w-full">
          <i class="pi pi-envelope text-ink-400" />
          <InputText
            id="field-forgot-email"
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

      <Button
        type="submit"
        :loading="isLoading"
        class="w-full !py-3 !text-base !font-semibold"
      >
        <span class="flex items-center gap-2">
          Enviar enlace de recuperación
          <i class="pi pi-send text-sm" />
        </span>
      </Button>
    </form>

    <div class="pt-4 border-t border-ink-200 text-center">
      <RouterLink to="/login" class="inline-flex items-center gap-1.5 text-sm font-medium text-brand-700 hover:text-brand-800">
        <i class="pi pi-arrow-left text-xs" />
        Volver al inicio de sesión
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import api from '@/services/api'

const email = ref('')
const isLoading = ref(false)
const submitted = ref(false)
const error = ref('')

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
  if (Object.keys(errors).length > 0) {
    val.focusFirst(['email'], 'forgot')
    return false
  }
  return true
}

const handleSubmit = async () => {
  error.value = ''
  if (!validar()) return
  isLoading.value = true
  try {
    await api.post('/auth/forgot-password', { email: email.value.trim() })
    submitted.value = true
  } catch (err: any) {
    error.value = err.response?.data?.message || err.response?.data?.detail || 'Error al enviar el correo'
  } finally {
    isLoading.value = false
  }
}
</script>
