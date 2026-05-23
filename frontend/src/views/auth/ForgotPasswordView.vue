<template>
  <div class="space-y-6">
    <div>
      <p class="text-sm text-gray-600">
        Ingresa tu email registrado y te enviaremos instrucciones para recuperar tu contraseña.
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div class="space-y-2">
        <label for="email" class="block text-sm font-semibold text-gray-700">Email</label>
        <InputText
          id="email"
          v-model="email"
          type="email"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="tu@email.com"
          required
        />
      </div>

      <div v-if="submitted && !error" class="p-4 bg-green-50 border border-green-200 rounded-lg">
        <p class="text-sm text-green-700">
          ✓ Email enviado. Revisa tu bandeja de entrada para las instrucciones.
        </p>
      </div>

      <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-sm text-red-700">{{ error }}</p>
      </div>

      <Button
        type="submit"
        label="Enviar Instrucciones"
        class="w-full py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
        :loading="isLoading"
        :disabled="isLoading || submitted"
      />
    </form>

    <RouterLink to="/login" class="text-sm text-blue-600 hover:text-blue-700 block text-center">
      ← Volver al login
    </RouterLink>
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
    error.value = err.response?.data?.message || 'Error al enviar email'
  } finally {
    isLoading.value = false
  }
}
</script>
