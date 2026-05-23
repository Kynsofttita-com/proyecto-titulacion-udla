<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Estudiantes</h2>
      <Button label="Nuevo Estudiante" icon="pi pi-plus" @click="$router.push('/estudiantes/nuevo')" />
    </div>
    <Card>
      <template #content>
        <p class="text-gray-600">Lista de estudiantes será implementada en T7.3</p>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import Card from 'primevue/card'
</script>
