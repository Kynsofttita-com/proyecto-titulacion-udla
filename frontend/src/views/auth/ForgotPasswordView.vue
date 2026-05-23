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
        <label class="label mb-1.5 block">Correo electrónico</label>
        <span class="p-input-icon-left w-full">
          <i class="pi pi-envelope text-ink-400" />
          <InputText
            v-model="email"
            type="email"
            placeholder="tu@correo.com"
            class="w-full !pl-10"
            required
          />
        </span>
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
import { ref } from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import api from '@/services/api'

const email = ref('')
const isLoading = ref(false)
const submitted = ref(false)
const error = ref('')

const handleSubmit = async () => {
  isLoading.value = true
  error.value = ''
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    submitted.value = true
  } catch (err: any) {
    error.value = err.response?.data?.message || err.response?.data?.detail || 'Error al enviar el correo'
  } finally {
    isLoading.value = false
  }
}
</script>
