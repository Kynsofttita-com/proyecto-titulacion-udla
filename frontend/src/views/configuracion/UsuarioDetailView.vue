<template>
  <div class="space-y-6">
    <PageHeader
      :title="`${usuario.nombre} ${usuario.apellido}`"
      :description="usuario.email"
      icon="pi pi-user"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Configuración', to: '/configuracion' },
        { label: 'Usuarios', to: '/usuarios' },
        { label: usuario.email }
      ]"
    >
      <template #actions>
        <Button label="Volver" icon="pi pi-arrow-left" outlined @click="$router.back()" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Panel izquierdo: Info básica -->
      <div class="lg:col-span-1 space-y-4">
        <!-- Card de perfil -->
        <div class="card p-5">
          <div class="flex flex-col items-center text-center mb-4">
            <Avatar :name="`${usuario.nombre} ${usuario.apellido}`" size="xlarge" class="mb-3" />
            <h2 class="text-lg font-bold text-ink-900">{{ usuario.nombre }} {{ usuario.apellido }}</h2>
            <p class="text-sm text-ink-500">{{ usuario.email }}</p>
          </div>

          <div class="space-y-2 border-t border-ink-200 pt-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Estado:</span>
              <StatusBadge :status="usuario.locked ? 'LOCKED' : (usuario.activo ? 'ACTIVO' : 'INACTIVO')" />
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Último acceso:</span>
              <span class="text-xs font-medium text-ink-700">
                {{ usuario.lastLogin ? new Date(usuario.lastLogin).toLocaleString('es-EC') : 'Nunca' }}
              </span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">ID:</span>
              <span class="text-xs font-mono text-ink-500">#{{ usuario.id }}</span>
            </div>
          </div>
        </div>

        <!-- Card de intentos fallidos -->
        <div class="card p-5">
          <div class="flex items-center gap-2 mb-3">
            <i class="pi pi-exclamation-triangle text-warning-600" />
            <h3 class="font-semibold text-ink-900">Intentos fallidos</h3>
          </div>
          <div class="space-y-3">
            <div class="flex items-center justify-between p-3 rounded-lg bg-ink-50">
              <div>
                <p class="text-xs text-ink-600">Intentos acumulados</p>
                <p class="text-xl font-bold text-ink-900">{{ usuario.intentosFallidos || 0 }}/3</p>
              </div>
              <ProgressBar :value="((usuario.intentosFallidos || 0) / 3) * 100" :show-value="false" style="width: 60px" />
            </div>
            <p class="text-xs text-ink-500 italic">La cuenta se bloquea después de 3 intentos fallidos.</p>
            <Button
              v-if="usuario.locked"
              label="Desbloquear cuenta"
              icon="pi pi-unlock"
              severity="warning"
              class="w-full"
              @click="desbloquear()"
              :loading="desbloqueando"
            />
            <Button
              v-else-if="(usuario.intentosFallidos || 0) > 0"
              label="Resetear intentos"
              icon="pi pi-refresh"
              severity="info"
              size="small"
              class="w-full"
              @click="resetearIntentos()"
              :loading="reseteando"
            />
          </div>
        </div>
      </div>

      <!-- Panel derecho: Configuración -->
      <div class="lg:col-span-2 space-y-4">
        <!-- Información personal -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-user-edit text-brand-600" />
            Información personal
          </h3>

          <div v-if="editando.info" class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre *</label>
                <InputText v-model="formInfo.nombre" placeholder="Juan" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Apellido *</label>
                <InputText v-model="formInfo.apellido" placeholder="Pérez" class="w-full" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Email (no editable)</label>
              <InputText v-model="formInfo.email" disabled class="w-full" />
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarInfo()" :loading="guardando.info" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.info = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Nombre" :value="`${usuario.nombre} ${usuario.apellido}`" />
            <DetailRow label="Email" :value="usuario.email" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarInfo()" size="small" />
          </div>
        </div>

        <!-- Roles y permisos -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-shield text-brand-600" />
            Roles y permisos
          </h3>

          <div v-if="editando.roles" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Roles asignados *</label>
              <MultiSelect
                v-model="formRoles.roles"
                :options="rolesDisponibles"
                optionLabel="label"
                optionValue="value"
                placeholder="Selecciona uno o más roles"
                class="w-full"
                display="chip"
              />
              <p class="text-xs text-ink-500 mt-1.5">Un usuario puede tener múltiples roles para acceder a diferentes módulos.</p>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarRoles()" :loading="guardando.roles" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.roles = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <div v-if="usuario.roles && usuario.roles.length > 0" class="flex flex-wrap gap-2">
              <span
                v-for="r in usuario.roles"
                :key="r"
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-brand-50 text-brand-700 text-sm font-medium border border-brand-200"
              >
                <i class="pi pi-check-circle text-xs" />
                {{ formatRol(r) }}
              </span>
            </div>
            <div v-else class="text-sm text-ink-500 italic">Sin roles asignados</div>
            <Button label="Cambiar roles" icon="pi pi-pencil" text @click="editarRoles()" size="small" />
          </div>
        </div>

        <!-- Información adicional -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-id-card text-brand-600" />
            Información adicional
          </h3>

          <!-- Información Personal -->
          <div class="space-y-4">
            <div v-if="editando.personalInfo" class="space-y-4 pb-4 border-b border-ink-200">
              <h4 class="text-sm font-semibold text-ink-900">Información Personal</h4>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Cédula</label>
                  <InputText v-model="formPersonalInfo.cedula" placeholder="1234567890" class="w-full" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Género</label>
                  <InputText v-model="formPersonalInfo.genero" placeholder="M/F" class="w-full" />
                </div>
                <div class="col-span-2">
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de nacimiento</label>
                  <InputText v-model="formPersonalInfo.fechaNacimiento" type="date" class="w-full" />
                </div>
              </div>
              <div class="flex gap-2">
                <Button label="Guardar" icon="pi pi-check" @click="guardarPersonalInfo()" :loading="guardando.personalInfo" />
                <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.personalInfo = false" />
              </div>
            </div>

            <div v-else class="pb-4 border-b border-ink-200">
              <h4 class="text-sm font-semibold text-ink-900 mb-3">Información Personal</h4>
              <div class="space-y-2 mb-3">
                <DetailRow label="Cédula" :value="usuario.cedula" />
                <DetailRow label="Fecha de nacimiento" :value="usuario.fechaNacimiento" type="date" />
                <DetailRow label="Género" :value="usuario.genero" />
              </div>
              <Button label="Editar información" icon="pi pi-pencil" text @click="editarPersonalInfo()" size="small" />
            </div>

            <!-- Información de Contacto -->
            <div v-if="editando.contactInfo" class="space-y-4 pt-4">
              <h4 class="text-sm font-semibold text-ink-900">Información de Contacto</h4>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Teléfono</label>
                  <InputText v-model="formContactInfo.telefono" placeholder="0987654321" class="w-full" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Ciudad</label>
                  <InputText v-model="formContactInfo.ciudad" placeholder="Quito" class="w-full" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Provincia</label>
                  <InputText v-model="formContactInfo.provincia" placeholder="Pichincha" class="w-full" />
                </div>
                <div class="col-span-2">
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Dirección</label>
                  <InputText v-model="formContactInfo.direccion" placeholder="Calle Principal 123" class="w-full" />
                </div>
              </div>
              <div class="flex gap-2">
                <Button label="Guardar" icon="pi pi-check" @click="guardarContactInfo()" :loading="guardando.contactInfo" />
                <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.contactInfo = false" />
              </div>
            </div>

            <div v-else class="pt-4">
              <h4 class="text-sm font-semibold text-ink-900 mb-3">Información de Contacto</h4>
              <div class="space-y-2 mb-3">
                <DetailRow label="Teléfono" :value="usuario.telefono" type="phone" />
                <DetailRow label="Dirección" :value="usuario.direccion" />
                <DetailRow label="Ciudad" :value="usuario.ciudad" />
                <DetailRow label="Provincia" :value="usuario.provincia" />
              </div>
              <Button label="Editar información" icon="pi pi-pencil" text @click="editarContactInfo()" size="small" />
            </div>
          </div>
        </div>

        <!-- Estado de la cuenta -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-toggle-on text-brand-600" />
            Estado de la cuenta
          </h3>

          <div class="space-y-3">
            <div class="flex items-center justify-between p-3 rounded-lg bg-ink-50">
              <div>
                <p class="text-sm font-medium text-ink-900">Cuenta activa</p>
                <p class="text-xs text-ink-500">Permite o bloquea el inicio de sesión</p>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-semibold" :class="usuario.activo ? 'text-success-600' : 'text-danger-600'">
                  {{ usuario.activo ? 'Activa' : 'Inactiva' }}
                </span>
                <Button
                  :icon="usuario.activo ? 'pi pi-times' : 'pi pi-check'"
                  :severity="usuario.activo ? 'danger' : 'success'"
                  :label="usuario.activo ? 'Desactivar' : 'Activar'"
                  size="small"
                  @click="toggleActivo()"
                  :loading="guardando.activo"
                />
              </div>
            </div>

            <div class="flex items-center justify-between p-3 rounded-lg bg-info-50 border border-info-200">
              <div>
                <p class="text-sm font-medium text-ink-900">Cambiar contraseña en próximo login</p>
                <p class="text-xs text-ink-500">Obliga al usuario a cambiar su contraseña</p>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-semibold" :class="usuario.passwordChangeRequired ? 'text-warning-600' : 'text-ink-600'">
                  {{ usuario.passwordChangeRequired ? 'Requerido' : 'Opcional' }}
                </span>
                <Button
                  :icon="usuario.passwordChangeRequired ? 'pi pi-times' : 'pi pi-check'"
                  :severity="usuario.passwordChangeRequired ? 'warning' : 'info'"
                  :label="usuario.passwordChangeRequired ? 'Desactivar' : 'Activar'"
                  size="small"
                  @click="togglePasswordChangeRequired()"
                  :loading="guardando.passwordChange"
                />
              </div>
            </div>

            <div class="flex items-center justify-between p-3 rounded-lg bg-info-50 border border-info-200">
              <div>
                <p class="text-sm font-medium text-ink-900">Cambiar contraseña</p>
                <p class="text-xs text-ink-500">Generar nueva contraseña temporal</p>
              </div>
              <Button
                label="Cambiar"
                icon="pi pi-key"
                severity="info"
                size="small"
                @click="abrirCambiarPassword()"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog: Cambiar contraseña como admin -->
    <Dialog v-model:visible="dlgCambiarPassword" modal header="Cambiar contraseña" :style="{ width: '480px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 p-3 flex items-start gap-2 text-sm text-info-700">
          <i class="pi pi-info-circle mt-0.5" />
          <span>Se generará una nueva contraseña temporal para <strong>{{ usuario.email }}</strong>. El usuario deberá cambiarla en el próximo login.</span>
        </div>

        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Nueva contraseña temporal *</label>
          <div class="flex gap-2">
            <InputText
              v-model="formPassword.nueva"
              placeholder="Ingresa nueva contraseña"
              :type="mostrarPasswordDialog ? 'text' : 'password'"
              class="w-full"
            />
            <Button
              :icon="mostrarPasswordDialog ? 'pi pi-eye-slash' : 'pi pi-eye'"
              rounded
              text
              severity="secondary"
              @click="mostrarPasswordDialog = !mostrarPasswordDialog"
            />
          </div>
        </div>

        <div class="flex items-center gap-2 p-3 rounded-lg bg-success-50 border border-success-200">
          <Checkbox v-model="formPassword.changeRequired" :binary="true" inputId="pwd-req" />
          <label for="pwd-req" class="text-sm text-success-700 font-medium">
            Forzar cambio en próximo login
          </label>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="dlgCambiarPassword = false" />
        <Button label="Cambiar" icon="pi pi-check" :loading="cambiandoPassword" @click="guardarCambioPassword()" />
      </template>
    </Dialog>

    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Dialog from 'primevue/dialog'
import Checkbox from 'primevue/checkbox'
import ProgressBar from 'primevue/progressbar'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import DetailRow from '@/components/ui/DetailRow.vue'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const usuarioId = route.params.id as string

const usuario = reactive<any>({
  id: null,
  nombre: '',
  apellido: '',
  email: '',
  roles: [],
  activo: true,
  locked: false,
  lastLogin: null,
  intentosFallidos: 0,
  passwordChangeRequired: false,
  cedula: null,
  fechaNacimiento: null,
  genero: null,
  telefono: null,
  direccion: null,
  ciudad: null,
  provincia: null
})

const rolesDisponibles = [
  { label: 'Administrador', value: 'ADMIN' },
  { label: 'Personal Administrativo', value: 'STAFF' },
  { label: 'Instructor', value: 'INSTRUCTOR' },
  { label: 'Estudiante', value: 'STUDENT' }
]

const formatRol = (rol: string) => {
  const map: any = {
    ADMIN: 'Administrador',
    STAFF: 'Personal Administrativo',
    INSTRUCTOR: 'Instructor',
    STUDENT: 'Estudiante'
  }
  return map[rol] || rol
}

// Estados de edición
const editando = reactive({ info: false, roles: false, personalInfo: false, contactInfo: false })
const guardando = reactive({ info: false, roles: false, activo: false, passwordChange: false, personalInfo: false, contactInfo: false })
const desbloqueando = ref(false)
const reseteando = ref(false)

// Formularios
const formInfo = reactive<any>({ nombre: '', apellido: '', email: '' })
const formRoles = reactive<any>({ roles: [] })
const formPersonalInfo = reactive<any>({ cedula: '', fechaNacimiento: '', genero: '' })
const formContactInfo = reactive<any>({ telefono: '', direccion: '', ciudad: '', provincia: '' })

const cargar = async () => {
  try {
    const { data } = await api.get(`/usuarios/${usuarioId}`)
    Object.assign(usuario, data)
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo cargar el usuario',
      life: 4000
    })
    setTimeout(() => router.back(), 1000)
  }
}

const editarInfo = () => {
  Object.assign(formInfo, {
    nombre: usuario.nombre,
    apellido: usuario.apellido,
    email: usuario.email
  })
  editando.info = true
}

const guardarInfo = async () => {
  if (!formInfo.nombre?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'El nombre es obligatorio', life: 3000 })
    return
  }
  if (!formInfo.apellido?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'El apellido es obligatorio', life: 3000 })
    return
  }

  guardando.info = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: formInfo.nombre.trim(),
      apellido: formInfo.apellido.trim(),
      email: usuario.email,
      roles: usuario.roles,
      activo: usuario.activo
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Información personal guardada',
      life: 3000
    })
    editando.info = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally { guardando.info = false }
}

const editarRoles = () => {
  formRoles.roles = [...usuario.roles]
  editando.roles = true
}

const guardarRoles = async () => {
  if (!formRoles.roles || formRoles.roles.length === 0) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Debe asignar al menos un rol', life: 3000 })
    return
  }

  guardando.roles = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      roles: formRoles.roles,
      activo: usuario.activo
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Roles asignados',
      life: 3000
    })
    editando.roles = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally { guardando.roles = false }
}

const editarPersonalInfo = () => {
  Object.assign(formPersonalInfo, {
    cedula: usuario.cedula || '',
    fechaNacimiento: usuario.fechaNacimiento || '',
    genero: usuario.genero || ''
  })
  editando.personalInfo = true
}

const guardarPersonalInfo = async () => {
  guardando.personalInfo = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      roles: usuario.roles,
      activo: usuario.activo,
      cedula: formPersonalInfo.cedula || null,
      fechaNacimiento: formPersonalInfo.fechaNacimiento || null,
      genero: formPersonalInfo.genero || null
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Información personal guardada',
      life: 3000
    })
    editando.personalInfo = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally { guardando.personalInfo = false }
}

const editarContactInfo = () => {
  Object.assign(formContactInfo, {
    telefono: usuario.telefono || '',
    direccion: usuario.direccion || '',
    ciudad: usuario.ciudad || '',
    provincia: usuario.provincia || ''
  })
  editando.contactInfo = true
}

const guardarContactInfo = async () => {
  guardando.contactInfo = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      roles: usuario.roles,
      activo: usuario.activo,
      telefono: formContactInfo.telefono || null,
      direccion: formContactInfo.direccion || null,
      ciudad: formContactInfo.ciudad || null,
      provincia: formContactInfo.provincia || null
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Información de contacto guardada',
      life: 3000
    })
    editando.contactInfo = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally { guardando.contactInfo = false }
}

const toggleActivo = async () => {
  guardando.activo = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      roles: usuario.roles,
      activo: !usuario.activo
    })
    toast.add({
      severity: 'success',
      summary: usuario.activo ? 'Desactivado' : 'Activado',
      detail: `${usuario.email}`,
      life: 3000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo cambiar el estado',
      life: 3000
    })
  } finally { guardando.activo = false }
}

const togglePasswordChangeRequired = async () => {
  guardando.passwordChange = true
  try {
    await api.put(`/usuarios/${usuarioId}`, {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      roles: usuario.roles,
      activo: usuario.activo,
      passwordChangeRequired: !usuario.passwordChangeRequired
    })
    toast.add({
      severity: 'success',
      summary: usuario.passwordChangeRequired ? 'Desactivado' : 'Activado',
      detail: 'Cambio de contraseña ' + (!usuario.passwordChangeRequired ? 'será requerido' : 'es opcional'),
      life: 3000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo cambiar',
      life: 3000
    })
  } finally { guardando.passwordChange = false }
}

const desbloquear = async () => {
  desbloqueando.value = true
  try {
    await api.post(`/usuarios/${usuarioId}/desbloquear`)
    toast.add({
      severity: 'success',
      summary: 'Desbloqueado',
      detail: 'La cuenta puede volver a iniciar sesión',
      life: 3000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo desbloquear',
      life: 3000
    })
  } finally { desbloqueando.value = false }
}

const resetearIntentos = async () => {
  reseteando.value = true
  try {
    await api.post(`/usuarios/${usuarioId}/resetear-intentos`)
    toast.add({
      severity: 'success',
      summary: 'Reseteado',
      detail: 'Los intentos fallidos se han limpiado',
      life: 3000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo resetear',
      life: 3000
    })
  } finally { reseteando.value = false }
}

// Cambiar contraseña como admin
const dlgCambiarPassword = ref(false)
const cambiandoPassword = ref(false)
const mostrarPasswordDialog = ref(false)
const formPassword = reactive<any>({
  nueva: '',
  changeRequired: true
})

const abrirCambiarPassword = () => {
  mostrarPasswordDialog.value = false
  formPassword.nueva = ''
  formPassword.changeRequired = true
  dlgCambiarPassword.value = true
}

const guardarCambioPassword = async () => {
  if (!formPassword.nueva?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Ingresa una contraseña', life: 3000 })
    return
  }

  cambiandoPassword.value = true
  try {
    await api.post(`/usuarios/${usuarioId}/cambiar-password`, {
      nuevaPassword: formPassword.nueva,
      passwordChangeRequired: formPassword.changeRequired
    })
    toast.add({
      severity: 'success',
      summary: 'Contraseña actualizada',
      detail: `${usuario.email} tendrá que usar la nueva contraseña`,
      life: 3000
    })
    dlgCambiarPassword.value = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo cambiar la contraseña',
      life: 3000
    })
  } finally { cambiandoPassword.value = false }
}

onMounted(cargar)
</script>
