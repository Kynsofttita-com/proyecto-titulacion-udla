<template>
  <div class="space-y-4">
    <p class="text-sm text-gray-600">Ingresa tu email para recibir instrucciones de recuperación</p>
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label for="email" class="block text-sm font-medium mb-1">Email</label>
        <InputText
          id="email"
          v-model="email"
          type="email"
          class="w-full"
          placeholder="tu@email.com"
          required
        />
      </div>
      <Button type="submit" label="Enviar" class="w-full" />
    </form>
    <RouterLink to="/login" class="text-sm text-blue-600 hover:underline block text-center">
      Volver al login
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const email = ref('')

const handleSubmit = async () => {
  console.log('Forgot password:', email.value)
}
</script>
