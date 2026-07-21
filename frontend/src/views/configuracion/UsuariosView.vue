<template>
  <div class="space-y-6">
    <PageHeader
      title="Usuarios del sistema"
      description="Gestión de cuentas, roles, estado y permisos de todos los usuarios."
      icon="pi pi-users"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Configuración', to: '/configuracion' },
        { label: 'Usuarios' }
      ]"
    >
      <template #actions>
        <Button label="Nuevo usuario" icon="pi pi-plus" @click="abrirFormUsuario()" />
        <Button label="Recargar" icon="pi pi-refresh" outlined @click="cargar" :loading="loading" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      <StatCard label="Total usuarios" :value="stats.total" icon="pi pi-users" color="brand" />
      <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      <StatCard label="Inactivos" :value="stats.inactivos" icon="pi pi-times-circle" color="danger" />
      <StatCard label="Bloqueados" :value="stats.bloqueados" icon="pi pi-lock" color="warning" />
      <StatCard label="Admins" :value="stats.admins" icon="pi pi-shield" color="info" />
    </div>

    <DataTableCard>
      <template #toolbar>
        <span class="p-input-icon-left">
          <i class="pi pi-search text-ink-400" />
          <InputText v-model="busqueda" placeholder="Buscar por nombre o email..." class="!pl-10 w-72" />
        </span>
        <MultiSelect
          v-model="filtroRoles"
          :options="rolesDisponibles"
          optionLabel="label"
          optionValue="value"
          placeholder="Filtrar por roles..."
          class="w-80"
          :maxSelectedLabels="2"
          display="chip"
        />
        <Dropdown
          v-model="filtroEstado"
          :options="[
            { label: 'Todos', value: 'todos' },
            { label: 'Activos', value: 'activos' },
            { label: 'Inactivos', value: 'inactivos' },
            { label: 'Bloqueados', value: 'bloqueados' }
          ]"
          optionLabel="label"
          optionValue="value"
          class="w-48"
        />
      </template>

      <DataTable
        :value="usuariosFiltrados"
        :loading="loading"
        striped-rows
        :pt="{
          table: { style: 'min-width: 75rem' },
          bodyRow: { style: 'padding: 0.25rem 0' },
          bodyCell: { style: 'padding: 0.5rem 0.75rem' },
          headerCell: { style: 'padding: 0.75rem' }
        }"
      >
        <Column header="Usuario" style="width: 30%">
          <template #body="{ data }">
            <router-link :to="`/usuarios/${data.id}`" class="flex items-center gap-1.5 hover:opacity-80 transition">
              <Avatar :name="`${data.nombre} ${data.apellido}`" size="sm" />
              <div>
                <p class="text-sm font-semibold text-brand-700 hover:underline">{{ data.nombre }} {{ data.apellido }}</p>
                <p class="text-xs text-ink-500">{{ data.email }}</p>
              </div>
            </router-link>
          </template>
        </Column>
        <Column header="Roles" style="width: 28%">
          <template #body="{ data }">
            <div class="flex gap-0.5 flex-wrap">
              <span
                v-for="r in (data.roles || [])"
                :key="r"
                class="inline-flex items-center px-1.5 py-0.5 rounded-md bg-brand-50 text-brand-700 text-xs font-medium border border-brand-200"
              >{{ r }}</span>
            </div>
          </template>
        </Column>
        <Column header="Último acceso" style="width: 18%">
          <template #body="{ data }">
            <span class="text-xs text-ink-600">
              {{ data.lastLogin ? new Date(data.lastLogin).toLocaleString('es-EC') : 'Nunca' }}
            </span>
          </template>
        </Column>
        <Column header="Estado" style="width: 12%">
          <template #body="{ data }">
            <div class="space-y-0.5">
              <StatusBadge v-if="data.locked" status="LOCKED" label="Bloqueado" />
              <StatusBadge v-else-if="data.activo" status="ACTIVO" />
              <StatusBadge v-else status="INACTIVO" />
            </div>
          </template>
        </Column>
        <Column header="Acciones" style="width: 12%" frozen alignFrozen="right">
          <template #body="{ data }">
            <div>
              <Button
                icon="pi pi-ellipsis-v"
                rounded
                text
                severity="secondary"
                size="small"
                @click="mostrarMenu(data, $event)"
              />
              <Menu ref="menuRef" :model="menuItems" :popup="true" />
            </div>
          </template>
        </Column>
      </DataTable>
    </DataTableCard>

    <!-- Dialog: Nuevo/Editar usuario -->
    <Dialog v-model:visible="dlgUsuario" modal :header="formUsuario.id ? 'Editar usuario' : 'Nuevo usuario'" :style="{ width: '680px' }" class="max-h-[90vh] overflow-y-auto">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos marcados con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>

        <!-- Datos Personales -->
        <div>
          <p class="text-sm font-semibold text-ink-900 mb-3 pb-2 border-b border-ink-200">Datos Personales</p>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label for="field-usr-nombre" class="block text-sm font-medium text-ink-700 mb-1.5">
                Nombre <span class="text-danger-600 font-semibold">*</span>
              </label>
              <InputText
                id="field-usr-nombre"
                v-model="formUsuario.nombre"
                placeholder="Juan"
                maxlength="50"
                class="w-full border border-ink-300 bg-ink-50"
                :class="errorsU.nombre ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrU('nombre')"
              />
              <p v-if="errorsU.nombre" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.nombre }}
              </p>
            </div>
            <div>
              <label for="field-usr-apellido" class="block text-sm font-medium text-ink-700 mb-1.5">
                Apellido <span class="text-danger-600 font-semibold">*</span>
              </label>
              <InputText
                id="field-usr-apellido"
                v-model="formUsuario.apellido"
                placeholder="Pérez"
                maxlength="50"
                class="w-full border border-ink-300 bg-ink-50"
                :class="errorsU.apellido ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrU('apellido')"
              />
              <p v-if="errorsU.apellido" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.apellido }}
              </p>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label for="field-usr-cedula" class="block text-sm font-medium text-ink-700 mb-1.5">
                Cédula <span class="text-xs text-ink-500">(opcional)</span>
              </label>
              <InputText
                id="field-usr-cedula"
                v-model="formUsuario.cedula"
                placeholder="1234567890"
                maxlength="10"
                class="w-full border border-ink-300 bg-ink-50"
                :class="errorsU.cedula ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrU('cedula')"
              />
              <p v-if="errorsU.cedula" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.cedula }}
              </p>
            </div>
            <div>
              <label for="field-usr-fechaNacimiento" class="block text-sm font-medium text-ink-700 mb-1.5">
                Fecha de Nacimiento <span class="text-xs text-ink-500">(opcional)</span>
              </label>
              <Calendar
                v-model="formUsuario.fechaNacimiento"
                inputId="field-usr-fechaNacimiento"
                dateFormat="dd/mm/yy"
                class="w-full border border-ink-300 bg-ink-50"
                :inputClass="errorsU.fechaNacimiento ? '!border-danger-500 !bg-danger-50' : 'border border-ink-300 bg-ink-50'"
                :maxDate="new Date()"
                @update:modelValue="clearErrU('fechaNacimiento')"
              />
              <p v-if="errorsU.fechaNacimiento" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.fechaNacimiento }}
              </p>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Género <span class="text-xs text-ink-500">(opcional)</span></label>
            <Dropdown
              v-model="formUsuario.genero"
              :options="[
                { label: 'Masculino', value: 'M' },
                { label: 'Femenino', value: 'F' },
                { label: 'Otro', value: 'O' }
              ]"
              optionLabel="label"
              optionValue="value"
              placeholder="Selecciona género"
              class="w-full border border-ink-300 bg-ink-50"
            />
          </div>
        </div>

        <!-- Datos de Contacto -->
        <div>
          <p class="text-sm font-semibold text-ink-900 mb-3 pb-2 border-b border-ink-200">Datos de Contacto</p>
          <div class="mb-4">
            <label for="field-usr-email" class="block text-sm font-medium text-ink-700 mb-1.5">
              Email <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputText
              id="field-usr-email"
              v-model="formUsuario.email"
              placeholder="juan@escuela.com"
              type="email"
              maxlength="120"
              class="w-full border border-ink-300 bg-ink-50"
              :class="errorsU.email ? '!border-danger-500 !bg-danger-50' : ''"
              :disabled="!!formUsuario.id"
              @update:modelValue="clearErrU('email')"
            />
            <p v-if="errorsU.email" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.email }}
            </p>
            <p v-else-if="formUsuario.id" class="text-xs text-ink-500 mt-1">El email no se puede cambiar tras crear el usuario.</p>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label for="field-usr-telefono" class="block text-sm font-medium text-ink-700 mb-1.5">
                Teléfono <span class="text-xs text-ink-500">(opcional)</span>
              </label>
              <InputText
                id="field-usr-telefono"
                v-model="formUsuario.telefono"
                placeholder="0991234567"
                maxlength="10"
                class="w-full border border-ink-300 bg-ink-50"
                :class="errorsU.telefono ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrU('telefono')"
              />
              <p v-if="errorsU.telefono" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.telefono }}
              </p>
            </div>
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Dirección <span class="text-xs text-ink-500">(opcional)</span></label>
            <InputText v-model="formUsuario.direccion" placeholder="Calle Principal 123" maxlength="200" class="w-full border border-ink-300 bg-ink-50" />
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Ciudad <span class="text-xs text-ink-500">(opcional)</span></label>
              <InputText v-model="formUsuario.ciudad" placeholder="Quito" maxlength="80" class="w-full border border-ink-300 bg-ink-50" />
            </div>
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Provincia <span class="text-xs text-ink-500">(opcional)</span></label>
              <InputText v-model="formUsuario.provincia" placeholder="Pichincha" maxlength="80" class="w-full border border-ink-300 bg-ink-50" />
            </div>
          </div>
        </div>

        <!-- Datos de Cuenta -->
        <div>
          <p class="text-sm font-semibold text-ink-900 mb-3 pb-2 border-b border-ink-200">Datos de Cuenta</p>
          <div class="mb-4">
            <label for="field-usr-roles" class="block text-sm font-medium text-ink-700 mb-1.5">
              Roles <span class="text-danger-600 font-semibold">*</span>
            </label>
            <MultiSelect
              v-model="formUsuario.roles"
              inputId="field-usr-roles"
              :options="rolesDisponibles"
              optionLabel="label"
              optionValue="value"
              placeholder="Selecciona uno o más roles"
              class="w-full border border-ink-300 bg-ink-50"
              :class="errorsU.roles ? '!border-danger-500 !bg-danger-50' : ''"
              display="chip"
              @update:modelValue="clearErrU('roles')"
            />
            <p v-if="errorsU.roles" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.roles }}
            </p>
            <p v-else class="text-xs text-ink-500 mt-1.5">Un usuario puede tener múltiples roles.</p>
          </div>
        </div>

        <!-- Seguridad (solo en creación) -->
        <div v-if="!formUsuario.id">
          <p class="text-sm font-semibold text-ink-900 mb-3 pb-2 border-b border-ink-200">Seguridad</p>
          <div class="mb-4">
            <label for="field-usr-password" class="block text-sm font-medium text-ink-700 mb-1.5">
              Contraseña temporal <span class="text-danger-600 font-semibold">*</span>
            </label>
            <div class="flex gap-2">
              <InputText
                id="field-usr-password"
                v-model="formUsuario.password"
                placeholder="Al menos 8 caracteres"
                :type="showPassword ? 'text' : 'password'"
                maxlength="100"
                class="w-full border border-ink-300 bg-ink-50"
                :class="errorsU.password ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrU('password')"
              />
              <Button
                :icon="showPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"
                rounded
                text
                severity="secondary"
                @click="showPassword = !showPassword"
              />
            </div>
            <p v-if="errorsU.password" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsU.password }}
            </p>
            <p v-else class="text-xs text-ink-500 mt-1">Mínimo 8 caracteres. El usuario podrá cambiarla en el primer login.</p>
          </div>
          <div class="flex items-center gap-2 p-3 rounded-lg bg-success-50 border border-success-200">
            <Checkbox v-model="formUsuario.passwordChangeRequired" :binary="true" inputId="pwd-change" />
            <label for="pwd-change" class="text-sm text-success-700 font-medium">
              Solicitar cambio de contraseña en el primer login
            </label>
          </div>
        </div>

        <!-- Estado (solo en edición) -->
        <div v-if="formUsuario.id">
          <p class="text-sm font-semibold text-ink-900 mb-3 pb-2 border-b border-ink-200">Estado</p>
          <div class="flex items-center gap-2 p-3 rounded-lg bg-ink-50 border border-ink-200">
            <Checkbox v-model="formUsuario.activo" :binary="true" inputId="usr-activo" />
            <label for="usr-activo" class="text-sm text-ink-700">Cuenta activa (permite iniciar sesión)</label>
          </div>
        </div>

        <!-- Error de servidor (409 email duplicado, red, etc.) -->
        <div v-if="errUsuario" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ errUsuario }}</span>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="dlgUsuario = false" />
        <Button :label="formUsuario.id ? 'Guardar' : 'Crear'" icon="pi pi-check" :loading="guardandoUsuario" @click="guardarUsuario" />
      </template>
    </Dialog>

    <!-- Dialog: Cambiar roles -->
    <Dialog v-model:visible="dlgRoles" modal header="Cambiar roles" :style="{ width: '480px' }">
      <div class="space-y-4">
        <div>
          <p class="text-sm font-semibold text-ink-900 mb-3">{{ usuarioRolesEdit?.nombre }} {{ usuarioRolesEdit?.apellido }}</p>
          <label for="field-rol-roles" class="block text-sm font-medium text-ink-700 mb-1.5">
            Roles <span class="text-danger-600 font-semibold">*</span>
          </label>
          <MultiSelect
            v-model="formRoles"
            inputId="field-rol-roles"
            :options="rolesDisponibles"
            optionLabel="label"
            optionValue="value"
            placeholder="Selecciona uno o más roles"
            class="w-full"
            :class="errorsR.roles ? '!border-danger-500 !bg-danger-50' : ''"
            display="chip"
            @update:modelValue="clearErrR('roles')"
          />
          <p v-if="errorsR.roles" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsR.roles }}
          </p>
          <p v-else class="text-xs text-ink-500 mt-1.5">Un usuario puede tener múltiples roles.</p>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="dlgRoles = false" />
        <Button label="Guardar" icon="pi pi-check" :loading="guarandoRoles" @click="guardarRolesEdit()" />
      </template>
    </Dialog>

    <!-- Dialog: Confirmar eliminación -->
    <Dialog v-model:visible="dlgConfirmEliminar" modal header="Eliminar usuario" :style="{ width: '420px' }">
      <div class="space-y-4">
        <div class="flex items-start gap-3">
          <div class="w-10 h-10 rounded-lg bg-danger-50 text-danger-600 flex items-center justify-center flex-shrink-0">
            <i class="pi pi-exclamation-triangle" />
          </div>
          <div>
            <p class="font-semibold text-ink-900">¿Eliminar este usuario?</p>
            <p class="text-sm text-ink-600 mt-1">
              Se eliminará la cuenta de <strong>{{ usuarioAEliminar?.nombre }} {{ usuarioAEliminar?.apellido }}</strong>
              ({{ usuarioAEliminar?.email }})
            </p>
            <p class="text-xs text-danger-600 mt-2">⚠️ Esta acción no se puede deshacer.</p>
          </div>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="dlgConfirmEliminar = false" />
        <Button label="Eliminar" severity="danger" icon="pi pi-trash" :loading="eliminando" @click="eliminarUsuario()" />
      </template>
    </Dialog>

    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Dialog from 'primevue/dialog'
import Menu from 'primevue/menu'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Tooltip from 'primevue/tooltip'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import api from '@/services/api'

const vTooltip = Tooltip
const toast = useToast()

const menuRef = ref()
const menuItems = ref<any[]>([])
const showPassword = ref(false)

const usuarios = ref<any[]>([])
const loading = ref(false)
const busqueda = ref('')
const filtroRoles = ref<string[]>([])
const filtroEstado = ref<'todos' | 'activos' | 'inactivos' | 'bloqueados'>('todos')
const desbloqueandoId = ref<number | null>(null)
const toggleandoId = ref<number | null>(null)

const rolesDisponibles = [
  { label: 'Administrador', value: 'ADMIN' },
  { label: 'Personal Administrativo', value: 'STAFF' },
  { label: 'Instructor', value: 'INSTRUCTOR' },
  { label: 'Estudiante', value: 'STUDENT' }
]

const stats = reactive({ total: 0, activos: 0, inactivos: 0, bloqueados: 0, admins: 0 })

const usuariosFiltrados = computed(() => {
  let result = usuarios.value

  // Filtrar por estado
  if (filtroEstado.value === 'activos') result = result.filter(u => u.activo && !u.locked)
  else if (filtroEstado.value === 'inactivos') result = result.filter(u => !u.activo)
  else if (filtroEstado.value === 'bloqueados') result = result.filter(u => u.locked)

  // Filtrar por roles
  if (filtroRoles.value.length > 0) {
    result = result.filter(u =>
      u.roles?.some((r: string) => filtroRoles.value.includes(r))
    )
  }

  // Filtrar por búsqueda
  if (busqueda.value.trim()) {
    const q = busqueda.value.toLowerCase()
    result = result.filter(u =>
      (u.email || '').toLowerCase().includes(q) ||
      (u.nombre || '').toLowerCase().includes(q) ||
      (u.apellido || '').toLowerCase().includes(q)
    )
  }

  return result
})

const cargar = async () => {
  try {
    loading.value = true
    const { data } = await api.get('/usuarios', { params: { size: 200 } })
    usuarios.value = data.content || []
    stats.total = usuarios.value.length
    stats.activos = usuarios.value.filter(u => u.activo && !u.locked).length
    stats.inactivos = usuarios.value.filter(u => !u.activo).length
    stats.bloqueados = usuarios.value.filter(u => u.locked).length
    stats.admins = usuarios.value.filter(u => u.roles?.includes('ADMIN')).length
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los usuarios', life: 4000 })
  } finally { loading.value = false }
}

const desbloquear = async (u: any) => {
  try {
    desbloqueandoId.value = u.id
    await api.post(`/usuarios/${u.id}/desbloquear`)
    toast.add({
      severity: 'success',
      summary: 'Cuenta desbloqueada',
      detail: `${u.email} puede iniciar sesión nuevamente`,
      life: 4000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'No se pudo desbloquear',
      detail: e.response?.data?.detail || 'Error en el servidor',
      life: 4000
    })
  } finally { desbloqueandoId.value = null }
}

const toggleActivo = async (u: any) => {
  try {
    toggleandoId.value = u.id
    await api.put(`/usuarios/${u.id}`, {
      nombre: u.nombre,
      apellido: u.apellido,
      email: u.email,
      roles: u.roles,
      activo: !u.activo,
      cedula: u.cedula || null,
      fechaNacimiento: u.fechaNacimiento || null,
      genero: u.genero || null,
      telefono: u.telefono || null,
      direccion: u.direccion || null,
      ciudad: u.ciudad || null,
      provincia: u.provincia || null
    })
    toast.add({
      severity: 'success',
      summary: u.activo ? 'Usuario desactivado' : 'Usuario activado',
      detail: `${u.email}`,
      life: 4000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo cambiar el estado',
      life: 4000
    })
  } finally { toggleandoId.value = null }
}

// -------- Helper factory de validación por campo --------
function useValidation() {
  const errors = reactive<Record<string, string>>({})
  const setError = (k: string, v: string) => { errors[k] = v }
  const clearError = (k: string) => { if (errors[k]) delete errors[k] }
  const clearAll = () => { Object.keys(errors).forEach(k => delete errors[k]) }
  const focusFirst = (orden: string[], prefijo: string, scroll = true) => {
    const p = orden.find(k => errors[k])
    if (!p) return
    setTimeout(() => {
      const el = document.getElementById(`field-${prefijo}-${p}`)
      if (!el) return
      if (scroll) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      ;(el as HTMLElement).focus?.()
    }, scroll ? 300 : 100)
  }
  return { errors, setError, clearError, clearAll, focusFirst }
}

const valU = useValidation()
const errorsU = valU.errors
const setErrU = valU.setError
const clearErrU = valU.clearError
const clearAllU = valU.clearAll

const valR = useValidation()
const errorsR = valR.errors
const setErrR = valR.setError
const clearErrR = valR.clearError
const clearAllR = valR.clearAll

// Dialog y formulario
const dlgUsuario = ref(false)
const guardandoUsuario = ref(false)
const errUsuario = ref('')
const formUsuario = reactive<any>({
  id: null,
  nombre: '',
  apellido: '',
  email: '',
  roles: [],
  password: '',
  passwordChangeRequired: true,
  activo: true,
  cedula: '',
  fechaNacimiento: null,
  genero: '',
  telefono: '',
  direccion: '',
  ciudad: '',
  provincia: ''
})

const abrirFormUsuario = (u?: any) => {
  errUsuario.value = ''
  clearAllU()
  showPassword.value = false
  if (u) {
    Object.assign(formUsuario, {
      id: u.id,
      nombre: u.nombre,
      apellido: u.apellido,
      email: u.email,
      roles: [...(u.roles || [])],
      password: '',
      passwordChangeRequired: false,
      activo: u.activo,
      cedula: u.cedula || '',
      fechaNacimiento: u.fechaNacimiento ? new Date(u.fechaNacimiento) : null,
      genero: u.genero || '',
      telefono: u.telefono || '',
      direccion: u.direccion || '',
      ciudad: u.ciudad || '',
      provincia: u.provincia || ''
    })
  } else {
    Object.assign(formUsuario, {
      id: null,
      nombre: '',
      apellido: '',
      email: '',
      roles: [],
      password: '',
      passwordChangeRequired: true,
      activo: true,
      cedula: '',
      fechaNacimiento: null,
      genero: '',
      telefono: '',
      direccion: '',
      ciudad: '',
      provincia: ''
    })
  }
  dlgUsuario.value = true
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validarUsuario = (): boolean => {
  clearAllU()
  const nombre = formUsuario.nombre?.trim() ?? ''
  if (!nombre) setErrU('nombre', 'El nombre es requerido')
  else if (nombre.length < 2) setErrU('nombre', 'Mínimo 2 caracteres')

  const apellido = formUsuario.apellido?.trim() ?? ''
  if (!apellido) setErrU('apellido', 'El apellido es requerido')
  else if (apellido.length < 2) setErrU('apellido', 'Mínimo 2 caracteres')

  const cedula = formUsuario.cedula?.trim() ?? ''
  if (cedula && (cedula.length !== 10 || !/^\d+$/.test(cedula))) {
    setErrU('cedula', 'La cédula debe tener 10 dígitos numéricos')
  }

  if (formUsuario.fechaNacimiento) {
    const fn = new Date(formUsuario.fechaNacimiento)
    const hoy = new Date()
    if (fn > hoy) setErrU('fechaNacimiento', 'No puede ser una fecha futura')
  }

  const email = formUsuario.email?.trim() ?? ''
  if (!email) setErrU('email', 'El email es requerido')
  else if (!EMAIL_REGEX.test(email)) setErrU('email', 'Formato de email inválido')

  const telefono = formUsuario.telefono?.trim() ?? ''
  if (telefono && (telefono.length !== 10 || !/^\d+$/.test(telefono))) {
    setErrU('telefono', 'El teléfono debe tener 10 dígitos numéricos')
  }

  if (!formUsuario.roles || formUsuario.roles.length === 0) {
    setErrU('roles', 'Debe seleccionar al menos un rol')
  }

  if (!formUsuario.id) {
    const pass = formUsuario.password ?? ''
    if (!pass.trim()) setErrU('password', 'La contraseña es requerida')
    else if (pass.length < 8) setErrU('password', 'Mínimo 8 caracteres')
  }

  if (Object.keys(errorsU).length > 0) {
    const orden = ['nombre','apellido','cedula','fechaNacimiento','email','telefono','roles','password']
    valU.focusFirst(orden, 'usr', true)
    return false
  }
  return true
}

const guardarUsuario = async () => {
  errUsuario.value = ''
  if (!validarUsuario()) return

  guardandoUsuario.value = true
  try {
    const payload = {
      nombre: formUsuario.nombre.trim(),
      apellido: formUsuario.apellido.trim(),
      email: formUsuario.email.trim(),
      roles: formUsuario.roles,
      activo: formUsuario.activo,
      cedula: formUsuario.cedula?.trim() || null,
      fechaNacimiento: formUsuario.fechaNacimiento ? new Date(formUsuario.fechaNacimiento).toISOString().split('T')[0] : null,
      genero: formUsuario.genero || null,
      telefono: formUsuario.telefono?.trim() || null,
      direccion: formUsuario.direccion?.trim() || null,
      ciudad: formUsuario.ciudad?.trim() || null,
      provincia: formUsuario.provincia?.trim() || null
    }

    if (formUsuario.id) {
      await api.put(`/usuarios/${formUsuario.id}`, payload)
      toast.add({
        severity: 'success',
        summary: 'Usuario actualizado',
        detail: `${formUsuario.email}`,
        life: 4000
      })
    } else {
      await api.post('/usuarios', {
        ...payload,
        password: formUsuario.password,
        passwordChangeRequired: formUsuario.passwordChangeRequired
      })
      toast.add({
        severity: 'success',
        summary: 'Usuario creado',
        detail: `${formUsuario.email}`,
        life: 4000
      })
    }

    dlgUsuario.value = false
    cargar()
  } catch (e: any) {
    errUsuario.value = e.response?.data?.detail || e.response?.data?.message || 'Error al guardar'
  } finally { guardandoUsuario.value = false }
}

// Cambiar roles
const dlgRoles = ref(false)
const guarandoRoles = ref(false)
const usuarioRolesEdit = ref<any>(null)
const formRoles = ref<string[]>([])

const abrirRoles = (u: any) => {
  usuarioRolesEdit.value = u
  formRoles.value = [...(u.roles || [])]
  clearAllR()
  dlgRoles.value = true
}

const guardarRolesEdit = async () => {
  clearAllR()
  if (!formRoles.value || formRoles.value.length === 0) {
    setErrR('roles', 'Debe seleccionar al menos un rol')
    valR.focusFirst(['roles'], 'rol', false)
    return
  }

  guarandoRoles.value = true
  try {
    await api.put(`/usuarios/${usuarioRolesEdit.value.id}`, {
      nombre: usuarioRolesEdit.value.nombre,
      apellido: usuarioRolesEdit.value.apellido,
      email: usuarioRolesEdit.value.email,
      roles: formRoles.value,
      activo: usuarioRolesEdit.value.activo,
      cedula: usuarioRolesEdit.value.cedula || null,
      fechaNacimiento: usuarioRolesEdit.value.fechaNacimiento || null,
      genero: usuarioRolesEdit.value.genero || null,
      telefono: usuarioRolesEdit.value.telefono || null,
      direccion: usuarioRolesEdit.value.direccion || null,
      ciudad: usuarioRolesEdit.value.ciudad || null,
      provincia: usuarioRolesEdit.value.provincia || null
    })
    toast.add({
      severity: 'success',
      summary: 'Roles actualizado',
      detail: `${usuarioRolesEdit.value.email}`,
      life: 3000
    })
    dlgRoles.value = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally { guarandoRoles.value = false }
}

// Eliminar usuario
const dlgConfirmEliminar = ref(false)
const eliminando = ref(false)
const usuarioAEliminar = ref<any>(null)

const abrirConfirmEliminar = (u: any) => {
  usuarioAEliminar.value = u
  dlgConfirmEliminar.value = true
}

const eliminarUsuario = async () => {
  eliminando.value = true
  try {
    await api.delete(`/usuarios/${usuarioAEliminar.value.id}`)
    toast.add({
      severity: 'success',
      summary: 'Usuario eliminado',
      detail: `${usuarioAEliminar.value.email}`,
      life: 3000
    })
    dlgConfirmEliminar.value = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo eliminar',
      life: 3000
    })
  } finally { eliminando.value = false }
}

// Menú de acciones
const mostrarMenu = (usuario: any, event: any) => {
  const items: any[] = [
    {
      label: 'Editar información',
      icon: 'pi pi-user-edit',
      command: () => abrirFormUsuario(usuario)
    },
    {
      label: 'Cambiar roles',
      icon: 'pi pi-shield',
      command: () => abrirRoles(usuario)
    },
    { separator: true },
    {
      label: usuario.activo ? 'Desactivar cuenta' : 'Activar cuenta',
      icon: usuario.activo ? 'pi pi-lock' : 'pi pi-lock-open',
      command: () => toggleActivo(usuario)
    }
  ]

  if (usuario.locked) {
    items.push({
      label: 'Desbloquear cuenta',
      icon: 'pi pi-unlock',
      command: () => desbloquear(usuario)
    })
  }

  items.push({
    separator: true
  })
  items.push({
    label: 'Eliminar usuario',
    icon: 'pi pi-trash',
    command: () => abrirConfirmEliminar(usuario)
  })

  menuItems.value = items
  menuRef.value?.toggle(event)
}

onMounted(cargar)
</script>
