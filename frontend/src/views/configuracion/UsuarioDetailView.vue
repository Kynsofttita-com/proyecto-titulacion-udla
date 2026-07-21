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
                <label for="field-info-nombre" class="block text-sm font-medium text-ink-700 mb-1.5">
                  Nombre <span class="text-danger-600 font-semibold">*</span>
                </label>
                <InputText
                  id="field-info-nombre"
                  v-model="formInfo.nombre"
                  placeholder="Juan"
                  maxlength="50"
                  class="w-full"
                  :class="errorsInfo.nombre ? '!border-danger-500 !bg-danger-50' : ''"
                  @update:modelValue="clearErrInfo('nombre')"
                />
                <p v-if="errorsInfo.nombre" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                  <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsInfo.nombre }}
                </p>
              </div>
              <div>
                <label for="field-info-apellido" class="block text-sm font-medium text-ink-700 mb-1.5">
                  Apellido <span class="text-danger-600 font-semibold">*</span>
                </label>
                <InputText
                  id="field-info-apellido"
                  v-model="formInfo.apellido"
                  placeholder="Pérez"
                  maxlength="50"
                  class="w-full"
                  :class="errorsInfo.apellido ? '!border-danger-500 !bg-danger-50' : ''"
                  @update:modelValue="clearErrInfo('apellido')"
                />
                <p v-if="errorsInfo.apellido" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                  <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsInfo.apellido }}
                </p>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Email <span class="text-xs text-ink-500">(no editable)</span></label>
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
              <label for="field-rolinline-roles" class="block text-sm font-medium text-ink-700 mb-1.5">
                Roles asignados <span class="text-danger-600 font-semibold">*</span>
              </label>
              <MultiSelect
                v-model="formRoles.roles"
                inputId="field-rolinline-roles"
                :options="rolesDisponibles"
                optionLabel="label"
                optionValue="value"
                placeholder="Selecciona uno o más roles"
                class="w-full"
                :class="errorsRolInline.roles ? '!border-danger-500 !bg-danger-50' : ''"
                display="chip"
                @update:modelValue="clearErrRolInline('roles')"
              />
              <p v-if="errorsRolInline.roles" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsRolInline.roles }}
              </p>
              <p v-else class="text-xs text-ink-500 mt-1.5">Un usuario puede tener múltiples roles para acceder a diferentes módulos.</p>
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
                  <label for="field-per-cedula" class="block text-sm font-medium text-ink-700 mb-1.5">
                    Cédula <span class="text-xs text-ink-500">(opcional)</span>
                  </label>
                  <InputText
                    id="field-per-cedula"
                    v-model="formPersonalInfo.cedula"
                    placeholder="1234567890"
                    maxlength="10"
                    class="w-full"
                    :class="errorsPer.cedula ? '!border-danger-500 !bg-danger-50' : ''"
                    @update:modelValue="clearErrPer('cedula')"
                  />
                  <p v-if="errorsPer.cedula" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                    <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPer.cedula }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Género <span class="text-xs text-ink-500">(opcional)</span></label>
                  <InputText v-model="formPersonalInfo.genero" placeholder="M/F/O" maxlength="1" class="w-full" />
                </div>
                <div class="col-span-2">
                  <label for="field-per-fechaNacimiento" class="block text-sm font-medium text-ink-700 mb-1.5">
                    Fecha de nacimiento <span class="text-xs text-ink-500">(opcional)</span>
                  </label>
                  <InputText
                    id="field-per-fechaNacimiento"
                    v-model="formPersonalInfo.fechaNacimiento"
                    type="date"
                    class="w-full"
                    :class="errorsPer.fechaNacimiento ? '!border-danger-500 !bg-danger-50' : ''"
                    @update:modelValue="clearErrPer('fechaNacimiento')"
                  />
                  <p v-if="errorsPer.fechaNacimiento" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                    <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPer.fechaNacimiento }}
                  </p>
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
                  <label for="field-con-telefono" class="block text-sm font-medium text-ink-700 mb-1.5">
                    Teléfono <span class="text-xs text-ink-500">(opcional)</span>
                  </label>
                  <InputText
                    id="field-con-telefono"
                    v-model="formContactInfo.telefono"
                    placeholder="0987654321"
                    maxlength="10"
                    class="w-full"
                    :class="errorsCon.telefono ? '!border-danger-500 !bg-danger-50' : ''"
                    @update:modelValue="clearErrCon('telefono')"
                  />
                  <p v-if="errorsCon.telefono" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                    <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCon.telefono }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Ciudad <span class="text-xs text-ink-500">(opcional)</span></label>
                  <InputText v-model="formContactInfo.ciudad" placeholder="Quito" maxlength="80" class="w-full" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Provincia <span class="text-xs text-ink-500">(opcional)</span></label>
                  <InputText v-model="formContactInfo.provincia" placeholder="Pichincha" maxlength="80" class="w-full" />
                </div>
                <div class="col-span-2">
                  <label class="block text-sm font-medium text-ink-700 mb-1.5">Dirección <span class="text-xs text-ink-500">(opcional)</span></label>
                  <InputText v-model="formContactInfo.direccion" placeholder="Calle Principal 123" maxlength="200" class="w-full" />
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
          <span>Se generará una nueva contraseña temporal para <strong>{{ usuario.email }}</strong>. El usuario deberá cambiarla en el próximo login. Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.</span>
        </div>

        <div>
          <label for="field-pwd-nueva" class="block text-sm font-medium text-ink-700 mb-1.5">
            Nueva contraseña temporal <span class="text-danger-600 font-semibold">*</span>
          </label>
          <div class="flex gap-2">
            <InputText
              id="field-pwd-nueva"
              v-model="formPassword.nueva"
              placeholder="Al menos 8 caracteres"
              :type="mostrarPasswordDialog ? 'text' : 'password'"
              maxlength="100"
              class="w-full"
              :class="errorsPwd.nueva ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrPwd('nueva')"
            />
            <Button
              :icon="mostrarPasswordDialog ? 'pi pi-eye-slash' : 'pi pi-eye'"
              rounded
              text
              severity="secondary"
              @click="mostrarPasswordDialog = !mostrarPasswordDialog"
            />
          </div>
          <p v-if="errorsPwd.nueva" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsPwd.nueva }}
          </p>
          <p v-else class="text-xs text-ink-500 mt-1">Mínimo 8 caracteres.</p>
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

// -------- Helper factory de validación por campo --------
function useValidation() {
  const errors = reactive<Record<string, string>>({})
  const setError = (k: string, v: string) => { errors[k] = v }
  const clearError = (k: string) => { if (errors[k]) delete errors[k] }
  const clearAll = () => { Object.keys(errors).forEach(k => delete errors[k]) }
  const focusFirst = (orden: string[], prefijo: string, scroll = false) => {
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

const valInfo = useValidation()
const errorsInfo = valInfo.errors
const setErrInfo = valInfo.setError
const clearErrInfo = valInfo.clearError
const clearAllInfo = valInfo.clearAll

const valRolInline = useValidation()
const errorsRolInline = valRolInline.errors
const setErrRolInline = valRolInline.setError
const clearErrRolInline = valRolInline.clearError
const clearAllRolInline = valRolInline.clearAll

const valPer = useValidation()
const errorsPer = valPer.errors
const setErrPer = valPer.setError
const clearErrPer = valPer.clearError
const clearAllPer = valPer.clearAll

const valCon = useValidation()
const errorsCon = valCon.errors
const setErrCon = valCon.setError
const clearErrCon = valCon.clearError
const clearAllCon = valCon.clearAll

const valPwd = useValidation()
const errorsPwd = valPwd.errors
const setErrPwd = valPwd.setError
const clearErrPwd = valPwd.clearError
const clearAllPwd = valPwd.clearAll

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
  clearAllInfo()
  editando.info = true
}

const validarInfo = (): boolean => {
  clearAllInfo()
  const nombre = formInfo.nombre?.trim() ?? ''
  if (!nombre) setErrInfo('nombre', 'El nombre es requerido')
  else if (nombre.length < 2) setErrInfo('nombre', 'Mínimo 2 caracteres')
  const apellido = formInfo.apellido?.trim() ?? ''
  if (!apellido) setErrInfo('apellido', 'El apellido es requerido')
  else if (apellido.length < 2) setErrInfo('apellido', 'Mínimo 2 caracteres')
  if (Object.keys(errorsInfo).length > 0) {
    valInfo.focusFirst(['nombre', 'apellido'], 'info', false)
    return false
  }
  return true
}

const guardarInfo = async () => {
  if (!validarInfo()) return
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
  clearAllRolInline()
  editando.roles = true
}

const validarRolesInline = (): boolean => {
  clearAllRolInline()
  if (!formRoles.roles || formRoles.roles.length === 0) {
    setErrRolInline('roles', 'Debe asignar al menos un rol')
    valRolInline.focusFirst(['roles'], 'rolinline', false)
    return false
  }
  return true
}

const guardarRoles = async () => {
  if (!validarRolesInline()) return
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
  clearAllPer()
  editando.personalInfo = true
}

const validarPersonal = (): boolean => {
  clearAllPer()
  const cedula = formPersonalInfo.cedula?.trim() ?? ''
  if (cedula && (cedula.length !== 10 || !/^\d+$/.test(cedula))) {
    setErrPer('cedula', 'La cédula debe tener 10 dígitos numéricos')
  }
  if (formPersonalInfo.fechaNacimiento) {
    const fn = new Date(formPersonalInfo.fechaNacimiento)
    const hoy = new Date()
    if (fn > hoy) setErrPer('fechaNacimiento', 'No puede ser una fecha futura')
  }
  if (Object.keys(errorsPer).length > 0) {
    valPer.focusFirst(['cedula', 'fechaNacimiento'], 'per', false)
    return false
  }
  return true
}

const guardarPersonalInfo = async () => {
  if (!validarPersonal()) return
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
  clearAllCon()
  editando.contactInfo = true
}

const validarContacto = (): boolean => {
  clearAllCon()
  const tel = formContactInfo.telefono?.trim() ?? ''
  if (tel && (tel.length !== 10 || !/^\d+$/.test(tel))) {
    setErrCon('telefono', 'El teléfono debe tener 10 dígitos numéricos')
  }
  if (Object.keys(errorsCon).length > 0) {
    valCon.focusFirst(['telefono'], 'con', false)
    return false
  }
  return true
}

const guardarContactInfo = async () => {
  if (!validarContacto()) return
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
  clearAllPwd()
  dlgCambiarPassword.value = true
}

const validarPwd = (): boolean => {
  clearAllPwd()
  const pass = formPassword.nueva ?? ''
  if (!pass.trim()) setErrPwd('nueva', 'La contraseña es requerida')
  else if (pass.length < 8) setErrPwd('nueva', 'Mínimo 8 caracteres')
  if (Object.keys(errorsPwd).length > 0) {
    valPwd.focusFirst(['nueva'], 'pwd', false)
    return false
  }
  return true
}

const guardarCambioPassword = async () => {
  if (!validarPwd()) return
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
