<template>
  <div class="space-y-4">
    <p class="text-sm text-gray-600">Ingresa tu nueva contraseña</p>
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label for="password" class="block text-sm font-medium mb-1">Nueva Contraseña</label>
        <Password
          id="password"
          v-model="password"
          class="w-full"
          placeholder="••••••••"
          required
        />
      </div>
      <Button type="submit" label="Cambiar Contraseña" class="w-full" />
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Password from 'primevue/password'
import Button from 'primevue/button'

const password = ref('')

const handleSubmit = async () => {
  console.log('Reset password')
}
</script>
