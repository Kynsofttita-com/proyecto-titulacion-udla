<template>
  <div class="space-y-6">
    <h2 class="text-3xl font-bold">Configuración del Sistema</h2>

    <TabView>
      <TabPanel header="Datos de la Escuela" :header-style="{ padding: 0 }">
        <div class="p-4 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Nombre de la Escuela</label>
              <InputText v-model="schoolName" class="w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">RUC</label>
              <InputText v-model="schoolRuc" class="w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Email</label>
              <InputText v-model="schoolEmail" type="email" class="w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Teléfono</label>
              <InputText v-model="schoolPhone" class="w-full" />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium mb-2">Dirección</label>
              <Textarea v-model="schoolAddress" rows="3" class="w-full" />
            </div>
          </div>
          <Button label="Guardar Cambios" icon="pi pi-save" @click="saveSchoolConfig" />
        </div>
      </TabPanel>

      <TabPanel header="Usuarios y Roles" :header-style="{ padding: 0 }">
        <UsuariosTab />
      </TabPanel>

      <TabPanel header="Conceptos de Facturación" :header-style="{ padding: 0 }">
        <div class="p-4">
          <p class="text-gray-600">Conceptos de facturación será implementado en T7.2</p>
        </div>
      </TabPanel>

      <TabPanel header="Plantillas de Email" :header-style="{ padding: 0 }">
        <div class="p-4">
          <p class="text-gray-600">Plantillas de email será implementado en T7.2</p>
        </div>
      </TabPanel>
    </TabView>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import UsuariosTab from '@/components/admin/UsuariosTab.vue'
import configuracionService from '@/services/configuracion'

const schoolName = ref('')
const schoolRuc = ref('')
const schoolEmail = ref('')
const schoolPhone = ref('')
const schoolAddress = ref('')
const isLoading = ref(false)

const loadConfig = async () => {
  try {
    isLoading.value = true
    const config = await configuracionService.obtenerConfiguracion()
    schoolName.value = config.nombre
    schoolRuc.value = config.ruc
    schoolEmail.value = config.email
    schoolPhone.value = config.telefono
    schoolAddress.value = config.direccion
  } catch (error) {
    console.error('Error loading configuration:', error)
  } finally {
    isLoading.value = false
  }
}

const saveSchoolConfig = async () => {
  try {
    isLoading.value = true
    await configuracionService.actualizarConfiguracion({
      nombre: schoolName.value,
      ruc: schoolRuc.value,
      email: schoolEmail.value,
      telefono: schoolPhone.value,
      direccion: schoolAddress.value
    })
  } catch (error) {
    console.error('Error saving configuration:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>
