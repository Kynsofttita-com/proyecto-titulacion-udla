<template>
  <div class="space-y-6">
    <h2 class="text-3xl font-bold">Dashboard</h2>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <Card class="bg-gradient-to-br from-blue-500 to-blue-600 text-white">
        <template #content>
          <div class="space-y-2">
            <div class="text-3xl font-bold">125</div>
            <div class="text-blue-100">Estudiantes Activos</div>
          </div>
        </template>
      </Card>

      <Card class="bg-gradient-to-br from-green-500 to-green-600 text-white">
        <template #content>
          <div class="space-y-2">
            <div class="text-3xl font-bold">28</div>
            <div class="text-green-100">Instructores</div>
          </div>
        </template>
      </Card>

      <Card class="bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
        <template #content>
          <div class="space-y-2">
            <div class="text-3xl font-bold">15</div>
            <div class="text-yellow-100">Vehículos</div>
          </div>
        </template>
      </Card>

      <Card class="bg-gradient-to-br from-purple-500 to-purple-600 text-white">
        <template #content>
          <div class="space-y-2">
            <div class="text-3xl font-bold">$45K</div>
            <div class="text-purple-100">Ingresos Mes</div>
          </div>
        </template>
      </Card>
    </div>

    <Card>
      <template #content>
        <h3 class="text-xl font-bold mb-4">Clases de Hoy</h3>
        <p class="text-gray-600">No hay clases programadas para hoy</p>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import Card from 'primevue/card'
</script>
