<template>
  <div class="p-4 space-y-4">
    <div class="flex justify-between items-center">
      <h3 class="text-xl font-bold">Usuarios del Sistema</h3>
      <Button label="Nuevo Usuario" icon="pi pi-plus" @click="showNewUserDialog = true" />
    </div>

    <DataTable :value="usuarios" striped-rows table-style="min-width: 50rem" paginator :rows="10">
      <Column field="email" header="Email"></Column>
      <Column field="nombreCompleto" header="Nombre Completo"></Column>
      <Column field="roles" header="Roles">
        <template #body="slotProps">
          <span v-for="rol in slotProps.data.roles" :key="rol" class="inline-block bg-blue-100 text-blue-800 text-xs font-semibold mr-2 px-2 py-1 rounded">
            {{ rol }}
          </span>
        </template>
      </Column>
      <Column field="estado" header="Estado">
        <template #body="slotProps">
          <span :class="slotProps.data.estado === 'ACTIVO' ? 'text-green-600' : 'text-red-600'">
            {{ slotProps.data.estado }}
          </span>
        </template>
      </Column>
      <Column header="Acciones" style="width: 200px">
        <template #body="slotProps">
          <Button
            icon="pi pi-pencil"
            class="p-button-rounded p-button-text p-button-sm mr-2"
            @click="editUsuario(slotProps.data)"
          />
          <Button
            icon="pi pi-trash"
            class="p-button-rounded p-button-text p-button-sm p-button-danger"
            @click="deleteUsuario(slotProps.data.id)"
          />
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="showNewUserDialog" header="Nuevo Usuario" :modal="true" style="width: 50vw">
      <form @submit.prevent="saveUsuario" class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-2">Email</label>
          <InputText v-model="formUsuario.email" type="email" class="w-full" required />
        </div>
        <div>
          <label class="block text-sm font-medium mb-2">Nombre Completo</label>
          <InputText v-model="formUsuario.nombreCompleto" class="w-full" required />
        </div>
        <div>
          <label class="block text-sm font-medium mb-2">Roles</label>
          <MultiSelect
            v-model="formUsuario.roles"
            :options="rolesDisponibles"
            option-label="label"
            option-value="value"
            class="w-full"
            placeholder="Selecciona roles"
          />
        </div>
        <div class="flex gap-2 justify-end">
          <Button label="Cancelar" severity="secondary" @click="showNewUserDialog = false" />
          <Button label="Guardar" type="submit" />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import usuariosService from '@/services/usuarios'

interface Usuario {
  id: number
  email: string
  nombreCompleto: string
  roles: string[]
  estado: 'ACTIVO' | 'INACTIVO'
}

const usuarios = ref<Usuario[]>([])
const isLoading = ref(false)
const editingUsuarioId = ref<number | null>(null)

const rolesDisponibles = [
  { label: 'Admin', value: 'ADMIN' },
  { label: 'Personal Administrativo', value: 'STAFF' },
  { label: 'Instructor', value: 'INSTRUCTOR' },
  { label: 'Estudiante', value: 'ESTUDIANTE' }
]

const showNewUserDialog = ref(false)
const formUsuario = reactive({
  email: '',
  nombreCompleto: '',
  roles: [] as string[]
})

const loadUsuarios = async () => {
  try {
    isLoading.value = true
    usuarios.value = await usuariosService.obtenerUsuarios()
  } catch (error) {
    console.error('Error loading usuarios:', error)
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  formUsuario.email = ''
  formUsuario.nombreCompleto = ''
  formUsuario.roles = []
  editingUsuarioId.value = null
}

const editUsuario = (usuario: Usuario) => {
  editingUsuarioId.value = usuario.id
  Object.assign(formUsuario, usuario)
  showNewUserDialog.value = true
}

const saveUsuario = async () => {
  try {
    if (editingUsuarioId.value) {
      await usuariosService.actualizarUsuario(editingUsuarioId.value, {
        id: editingUsuarioId.value,
        ...formUsuario
      })
    } else {
      await usuariosService.crearUsuario(formUsuario)
    }
    await loadUsuarios()
    showNewUserDialog.value = false
    resetForm()
  } catch (error) {
    console.error('Error saving usuario:', error)
  }
}

const deleteUsuario = async (id: number) => {
  try {
    await usuariosService.eliminarUsuario(id)
    await loadUsuarios()
  } catch (error) {
    console.error('Error deleting usuario:', error)
  }
}

onMounted(() => {
  loadUsuarios()
})
</script>
