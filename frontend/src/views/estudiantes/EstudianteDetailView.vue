<template>
  <div class="space-y-6">
    <PageHeader
      :title="`${estudiante.nombre} ${estudiante.apellido}`"
      :description="estudiante.cedula"
      icon="pi pi-user"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Estudiantes', to: '/estudiantes' },
        { label: estudiante.email }
      ]"
    >
      <template #actions>
        <Button label="Volver" icon="pi pi-arrow-left" outlined @click="$router.back()" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Panel izquierdo: Cards resumen -->
      <div class="lg:col-span-1 space-y-4">
        <!-- Card de perfil -->
        <div class="card p-5">
          <div class="flex flex-col items-center text-center mb-4">
            <Avatar :name="`${estudiante.nombre} ${estudiante.apellido}`" size="xlarge" class="mb-3" />
            <h2 class="text-lg font-bold text-ink-900">{{ estudiante.nombre }} {{ estudiante.apellido }}</h2>
            <p class="text-sm text-ink-500">{{ estudiante.email }}</p>
          </div>

          <div class="space-y-2 border-t border-ink-200 pt-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Estado académico:</span>
              <div class="flex items-center gap-2">
                <StatusBadge :status="estudiante.estado" />
                <Button
                  v-if="estadosPermitidos.length > 0"
                  icon="pi pi-pencil"
                  text
                  rounded
                  size="small"
                  severity="info"
                  v-tooltip="'Cambiar estado'"
                  @click="abrirDialogEstado"
                />
              </div>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Estado pago:</span>
              <StatusBadge :status="estudiante.situacionPago" />
            </div>
            <div v-if="estudiante.fechaMatricula" class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Matrícula:</span>
              <span class="text-xs font-medium text-ink-700">
                {{ new Date(estudiante.fechaMatricula).toLocaleDateString('es-EC') }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Panel derecho: Información detallada -->
      <div class="lg:col-span-2 space-y-4">
        <!-- Información personal -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-user-edit text-brand-600" />
            Información personal
          </h3>

          <div v-if="editando.personal" class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre *</label>
                <InputText v-model="formPersonal.nombre" placeholder="Juan" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Apellido *</label>
                <InputText v-model="formPersonal.apellido" placeholder="Pérez" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Cédula *</label>
                <InputText v-model="formPersonal.cedula" maxlength="10" placeholder="1234567890" class="w-full" disabled />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Género</label>
                <Dropdown
                  v-model="formPersonal.genero"
                  :options="[
                    { label: 'Masculino', value: 'M' },
                    { label: 'Femenino', value: 'F' },
                    { label: 'Otro', value: 'O' }
                  ]"
                  option-label="label"
                  option-value="value"
                  placeholder="Selecciona"
                  class="w-full"
                  showClear
                />
              </div>
              <div class="col-span-2">
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de nacimiento</label>
                <Calendar v-model="formPersonal.fechaNacimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarPersonal()" :loading="guardando.personal" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.personal = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Nombre" :value="`${estudiante.nombre} ${estudiante.apellido}`" />
            <DetailRow label="Cédula" :value="estudiante.cedula" />
            <DetailRow label="Fecha de nacimiento" :value="estudiante.fechaNacimiento" type="date" />
            <DetailRow label="Género" :value="estudiante.genero" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarPersonal()" size="small" />
          </div>
        </div>

        <!-- Información de contacto -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-phone text-brand-600" />
            Información de contacto
          </h3>

          <div v-if="editando.contacto" class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Email *</label>
                <InputText v-model="formContacto.email" type="email" placeholder="estudiante@correo.com" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Teléfono *</label>
                <InputText v-model="formContacto.telefono" placeholder="0987654321" maxlength="10" class="w-full" />
              </div>
              <div class="col-span-2">
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Dirección</label>
                <Textarea v-model="formContacto.direccion" rows="3" placeholder="Calle, número, sector, ciudad" class="w-full" />
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarContactoInfo()" :loading="guardando.contacto" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.contacto = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Email" :value="estudiante.email" type="email" />
            <DetailRow label="Teléfono" :value="estudiante.telefono" type="phone" />
            <DetailRow label="Dirección" :value="estudiante.direccion" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarContactoInfo()" size="small" />
          </div>
        </div>

        <!-- Información académica -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-graduation-cap text-brand-600" />
            Información académica
          </h3>

          <div class="space-y-3">
            <DetailRow label="Tipo de curso" :value="nombreCurso || 'No especificado'" />
            <DetailRow label="Categoría licencia" :value="estudiante.categoriaLicenciaId ? `Categoría ${estudiante.categoriaLicenciaId}` : 'No especificado'" />
            <DetailRow label="Fecha de matrícula" :value="estudiante.fechaMatricula" type="date" />
            <DetailRow label="Observaciones" :value="estudiante.observaciones || 'Sin observaciones'" />
          </div>
        </div>

        <!-- Contactos de emergencia -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-phone text-brand-600" />
            Contactos de emergencia
          </h3>

          <div v-if="editandoContacto.id" class="space-y-4 mb-4 p-4 rounded-lg bg-info-50 border border-info-200">
            <h4 class="text-sm font-semibold text-ink-900">{{ editandoContacto.id === 'nuevo' ? 'Nuevo contacto' : 'Editar contacto' }}</h4>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre *</label>
                <InputText v-model="formContactoEmergencia.nombre" placeholder="Nombre completo" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Teléfono *</label>
                <InputText v-model="formContactoEmergencia.telefono" placeholder="0987654321" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Parentesco</label>
                <InputText v-model="formContactoEmergencia.parentesco" placeholder="Ej: Padre, Hermano..." class="w-full" />
              </div>
              <div class="flex items-end">
                <label class="flex items-center gap-2 text-sm font-medium text-ink-700">
                  <Checkbox v-model="formContactoEmergencia.esPrincipal" :binary="true" />
                  Es principal
                </label>
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" size="small" @click="guardarContactoEmergencia()" :loading="guardandoContacto" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined size="small" @click="cancelarContactoEmergencia()" />
            </div>
          </div>

          <div v-if="contactosEmergencia && contactosEmergencia.length > 0" class="space-y-3 mb-4">
            <div
              v-for="contacto in contactosEmergencia"
              :key="contacto.id"
              class="p-3 rounded-lg bg-ink-50 border border-ink-200 flex items-start justify-between gap-2"
            >
              <div class="flex-1">
                <p class="text-sm font-medium text-ink-900">{{ contacto.nombre }}</p>
                <p class="text-xs text-ink-600">{{ contacto.parentesco || 'Parentesco no especificado' }}</p>
                <p class="text-xs text-ink-600 mt-1">{{ contacto.telefono }}</p>
                <span v-if="contacto.esPrincipal" class="inline-block mt-1.5 px-2 py-1 bg-brand-50 text-brand-700 text-xs font-medium rounded">
                  Principal
                </span>
              </div>
              <div class="flex gap-1 flex-shrink-0">
                <Button
                  icon="pi pi-pencil"
                  severity="info"
                  text
                  size="small"
                  @click="editarContacto(contacto)"
                  v-tooltip="'Editar'"
                />
                <Button
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  size="small"
                  @click="confirmarEliminarContacto(contacto)"
                  v-tooltip="'Eliminar'"
                />
              </div>
            </div>
          </div>
          <div v-else-if="!editandoContacto.id" class="text-sm text-ink-500 italic mb-4">Sin contactos de emergencia registrados</div>

          <Button
            v-if="!editandoContacto.id"
            label="Agregar contacto"
            icon="pi pi-plus"
            severity="success"
            text
            size="small"
            @click="agregarContacto()"
          />
        </div>

        <!-- Documentos -->
        <div class="card p-5">
          <div class="flex items-center justify-between mb-4">
            <h3 class="heading-3 flex items-center gap-2">
              <i class="pi pi-file text-brand-600" />
              Documentos
            </h3>
            <Button label="Subir" icon="pi pi-upload" size="small" severity="success" text @click="abrirDialogSubida" />
          </div>

          <div v-if="documentos.length > 0" class="space-y-3">
            <div
              v-for="doc in documentos"
              :key="doc.id"
              class="p-3 rounded-lg bg-ink-50 border border-ink-200 flex items-start justify-between gap-3"
            >
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-ink-900">{{ humanizarTipo(doc.tipo) }}</p>
                <p class="text-xs text-ink-600 mt-0.5">Subido {{ new Date(doc.fechaSubida).toLocaleString('es-EC') }}</p>
                <p class="text-xs text-ink-500">{{ formatearTamano(doc.tamanoBytes) }} · {{ doc.mimeType || 'tipo desconocido' }}</p>
              </div>
              <div class="flex gap-1 flex-shrink-0">
                <Button
                  icon="pi pi-download"
                  severity="info"
                  text
                  size="small"
                  :loading="descargandoId === doc.id"
                  @click="descargarDocumento(doc.id)"
                  v-tooltip="'Descargar'"
                />
                <Button
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  size="small"
                  @click="confirmarEliminarDocumento(doc.id)"
                  v-tooltip="'Eliminar'"
                />
              </div>
            </div>
          </div>
          <div v-else class="text-sm text-ink-500 italic">Sin documentos registrados</div>
        </div>

        <!-- Progreso académico -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-chart-bar text-brand-600" />
            Progreso académico
          </h3>

          <div v-if="progreso" class="space-y-4">
            <div class="grid grid-cols-3 gap-3">
              <div class="p-3 bg-brand-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">Horas completadas</p>
                <p class="text-2xl font-bold text-brand-700">{{ progreso.horasCompletadas }}</p>
              </div>
              <div class="p-3 bg-brand-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">Horas requeridas</p>
                <p class="text-2xl font-bold text-brand-700">{{ progreso.horasRequeridas }}</p>
              </div>
              <div class="p-3 bg-success-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">% Completado</p>
                <p class="text-2xl font-bold text-success-700">{{ progreso.porcentajeComplecion }}%</p>
              </div>
            </div>

            <ProgressBar :value="progreso.porcentajeComplecion" :showValue="false" />

            <div class="grid grid-cols-2 gap-3">
              <div class="p-3 bg-ink-50 rounded-lg">
                <p class="text-xs text-ink-600">Asignaciones completadas</p>
                <p class="text-xl font-bold text-ink-900 mt-1">{{ progreso.asignacionesCompletadas }}</p>
              </div>
              <div class="p-3 bg-warning-50 rounded-lg">
                <p class="text-xs text-ink-600">Asignaciones pendientes</p>
                <p class="text-xl font-bold text-warning-700 mt-1">{{ progreso.asignacionesPendientes }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Historial de asignaciones -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-calendar text-brand-600" />
            Historial de asignaciones
            <span v-if="historialAsignaciones.length > 0" class="text-xs font-normal text-ink-500">
              ({{ historialAsignaciones.length }})
            </span>
          </h3>

          <div v-if="cargandoHistorialAsignaciones" class="text-sm text-ink-500 italic">Cargando...</div>
          <div v-else-if="historialAsignaciones.length === 0" class="text-sm text-ink-500 italic">
            Sin asignaciones registradas
          </div>
          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-ink-200 text-left text-xs text-ink-600 uppercase">
                  <th class="py-2 pr-3 font-medium">Fecha</th>
                  <th class="py-2 px-3 font-medium">Horario</th>
                  <th class="py-2 px-3 font-medium">Instructor</th>
                  <th class="py-2 px-3 font-medium">Vehículo</th>
                  <th class="py-2 pl-3 font-medium">Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="asig in historialAsignaciones"
                  :key="asig.id"
                  class="border-b border-ink-100 hover:bg-ink-50"
                >
                  <td class="py-2 pr-3 text-ink-900 whitespace-nowrap">
                    {{ formatearFechaAsignacion(asig.fecha) }}
                  </td>
                  <td class="py-2 px-3 text-ink-700 whitespace-nowrap">
                    {{ formatearHora(asig.horaInicio) }} - {{ formatearHora(asig.horaFin) }}
                  </td>
                  <td class="py-2 px-3 text-ink-700">{{ nombreInstructor(asig.instructorId) }}</td>
                  <td class="py-2 px-3 text-ink-700 font-mono text-xs">{{ placaVehiculo(asig.vehiculoId) }}</td>
                  <td class="py-2 pl-3"><StatusBadge :status="asig.estado" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Historial de pagos -->
        <div class="card p-5">
          <div class="flex items-center justify-between mb-4">
            <h3 class="heading-3 flex items-center gap-2">
              <i class="pi pi-wallet text-brand-600" />
              Historial de pagos
              <span v-if="historialPagos.length > 0" class="text-xs font-normal text-ink-500">
                ({{ historialPagos.length }})
              </span>
            </h3>
            <span v-if="historialPagos.length > 0" class="text-sm font-semibold text-success-700">
              Total: ${{ totalPagado.toFixed(2) }}
            </span>
          </div>

          <div v-if="cargandoHistorialPagos" class="text-sm text-ink-500 italic">Cargando...</div>
          <div v-else-if="historialPagos.length === 0" class="text-sm text-ink-500 italic">
            Sin pagos registrados
          </div>
          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-ink-200 text-left text-xs text-ink-600 uppercase">
                  <th class="py-2 pr-3 font-medium">Fecha</th>
                  <th class="py-2 px-3 font-medium">Factura</th>
                  <th class="py-2 px-3 font-medium text-right">Monto</th>
                  <th class="py-2 px-3 font-medium">Método</th>
                  <th class="py-2 pl-3 font-medium">Referencia</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="pago in historialPagos"
                  :key="pago.id"
                  class="border-b border-ink-100 hover:bg-ink-50"
                >
                  <td class="py-2 pr-3 text-ink-900 whitespace-nowrap">
                    {{ formatearFechaPago(pago.fechaPago) }}
                  </td>
                  <td class="py-2 px-3 text-ink-700">#{{ pago.facturaId }}</td>
                  <td class="py-2 px-3 text-right font-semibold text-ink-900">
                    ${{ Number(pago.monto).toFixed(2) }}
                  </td>
                  <td class="py-2 px-3 text-ink-700">{{ pago.metodoPago }}</td>
                  <td class="py-2 pl-3 text-ink-500 text-xs">{{ pago.referenciaTransaccion || '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <Toast />
    <ConfirmDialog />

    <!-- Dialog: cambiar estado académico -->
    <Dialog
      v-model:visible="dialogEstadoVisible"
      modal
      header="Cambiar estado académico"
      :style="{ width: '480px' }"
      :closable="!cambiandoEstado"
    >
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700">
          <p>Estado actual: <strong>{{ estudiante.estado }}</strong></p>
          <p class="mt-1 text-ink-600">Las transiciones desde PRE_MATRICULADO se hacen automáticamente al registrar el primer pago.</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Nuevo estado *</label>
          <Dropdown
            v-model="formEstado.estado"
            :options="estadosPermitidos"
            option-label="label"
            option-value="value"
            placeholder="Selecciona estado"
            class="w-full border border-ink-300 bg-ink-50"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">
            Motivo
            <span v-if="formEstado.estado === 'RETIRADO'" class="text-danger-600">*</span>
            <span v-else class="text-xs text-ink-500">(opcional)</span>
          </label>
          <Textarea
            v-model="formEstado.motivo"
            rows="3"
            placeholder="Ej: Curso completado satisfactoriamente / Retiro voluntario por motivos personales..."
            class="w-full border border-ink-300 bg-ink-50"
            maxlength="500"
          />
          <p class="text-xs text-ink-500 mt-1">Se anexará a las observaciones del estudiante con fecha.</p>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" outlined @click="cerrarDialogEstado" :disabled="cambiandoEstado" />
        <Button label="Cambiar estado" icon="pi pi-check" @click="cambiarEstado" :loading="cambiandoEstado" :disabled="!formEstado.estado" />
      </template>
    </Dialog>

    <!-- Dialog: subir documento -->
    <Dialog
      v-model:visible="dialogSubidaVisible"
      modal
      header="Subir documento"
      :style="{ width: '480px' }"
      :closable="!subiendoDocumento"
    >
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Tipo de documento *</label>
          <Dropdown
            v-model="formSubida.tipo"
            :options="tiposDocumento"
            option-label="label"
            option-value="value"
            placeholder="Selecciona el tipo"
            class="w-full border border-ink-300 bg-ink-50"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Archivo * <span class="text-xs text-ink-500">(máx. 10 MB)</span></label>
          <input
            ref="fileInputRef"
            type="file"
            class="block w-full text-sm text-ink-700 file:mr-3 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 cursor-pointer"
            @change="onArchivoSeleccionado"
          />
          <p v-if="formSubida.archivo" class="text-xs text-ink-600 mt-1.5">
            <i class="pi pi-paperclip mr-1" />
            {{ formSubida.archivo.name }} · {{ formatearTamano(formSubida.archivo.size) }}
          </p>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" outlined @click="cerrarDialogSubida" :disabled="subiendoDocumento" />
        <Button label="Subir" icon="pi pi-upload" @click="subirDocumento" :loading="subiendoDocumento" :disabled="!formSubida.tipo || !formSubida.archivo" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import ProgressBar from 'primevue/progressbar'
import Toast from 'primevue/toast'
import Checkbox from 'primevue/checkbox'
import ConfirmDialog from 'primevue/confirmdialog'
import Dialog from 'primevue/dialog'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import DetailRow from '@/components/ui/DetailRow.vue'
import api from '@/services/api'
import estudiantesService, { EstudianteDetailResponse, ProgresoAcademico } from '@/services/estudiantes'
import asignacionesService, { type HistorialAsignacionItem } from '@/services/asignaciones'
import cobrosService, { type HistorialPagoItem } from '@/services/cobros'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const estudianteId = route.params.id as string

const estudiante = reactive<EstudianteDetailResponse>({
  id: 0,
  nombre: '',
  apellido: '',
  cedula: '',
  email: '',
  telefono: '',
  direccion: '',
  fechaNacimiento: '',
  genero: '',
  estado: '',
  situacionPago: '',
  fechaMatricula: null,
  tipoCursoId: null,
  categoriaLicenciaId: null,
  usuarioId: null,
  observaciones: '',
  documentos: [],
  contactosEmergencia: [],
  createdAt: '',
  updatedAt: ''
})

const progreso = ref<ProgresoAcademico | null>(null)
const loading = ref(false)
const contactosEmergencia = ref<any[]>([])
const guardandoContacto = ref(false)

const editando = reactive({ personal: false, contacto: false })
const guardando = reactive({ personal: false, contacto: false })

const formPersonal = reactive<any>({ nombre: '', apellido: '', cedula: '', fechaNacimiento: '', genero: '' })
const formContacto = reactive<any>({ email: '', telefono: '', direccion: '' })
const editandoContacto = reactive<any>({ id: null })
const formContactoEmergencia = reactive<any>({ nombre: '', telefono: '', parentesco: '', esPrincipal: false })

const nombreCurso = computed(() => {
  // En una aplicación real, esto vendría de un catálogo o endpoint
  return estudiante.tipoCursoId ? `Curso ${estudiante.tipoCursoId}` : null
})

const formatearTamano = (bytes?: number) => {
  if (!bytes) return 'Tamaño desconocido'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// ------- Historiales (asignaciones + pagos) -------
const historialAsignaciones = ref<HistorialAsignacionItem[]>([])
const historialPagos = ref<HistorialPagoItem[]>([])
const cargandoHistorialAsignaciones = ref(false)
const cargandoHistorialPagos = ref(false)

// Maps id -> nombre/placa para mostrar nombres reales (no IDs) en el historial.
const mapInstructores = ref<Map<number, string>>(new Map())
const mapVehiculos    = ref<Map<number, string>>(new Map())
const nombreInstructor = (id: number) => mapInstructores.value.get(id) || `Instructor #${id}`
const placaVehiculo    = (id: number) => mapVehiculos.value.get(id) || `Vehículo #${id}`

const cargarCatalogos = async () => {
  try {
    const [insRes, vehRes] = await Promise.all([
      api.get('/instructores', { params: { size: 200 } }),
      api.get('/vehiculos',    { params: { size: 200 } })
    ])
    const ins = insRes.data.content || []
    const veh = vehRes.data.content || []
    mapInstructores.value = new Map(ins.map((i: any) => [
      i.id,
      i.nombreCompleto || `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim() || `Instructor #${i.id}`
    ]))
    mapVehiculos.value = new Map(veh.map((v: any) => [
      v.id,
      v.placa ? `${v.placa}${v.marca ? ` · ${v.marca} ${v.modelo ?? ''}`.trim() : ''}` : `Vehículo #${v.id}`
    ]))
  } catch (e) {
    console.warn('No se pudo cargar catalogos para historial', e)
  }
}

const totalPagado = computed(() =>
  historialPagos.value.reduce((sum, p) => sum + Number(p.monto || 0), 0)
)

const formatearFechaAsignacion = (fecha: string) => {
  if (!fecha) return '—'
  const fechaSolo = fecha.length >= 10 ? fecha.substring(0, 10) : fecha
  const [y, m, d] = fechaSolo.split('-')
  if (!y || !m || !d) return fecha
  return `${d}/${m}/${y}`
}

const formatearFechaPago = (iso: string) => {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString('es-EC', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  } catch {
    return iso
  }
}

const formatearHora = (hora: string) => {
  if (!hora) return '—'
  return hora.length >= 5 ? hora.substring(0, 5) : hora
}

const cargarHistorialAsignaciones = async () => {
  cargandoHistorialAsignaciones.value = true
  try {
    historialAsignaciones.value = await asignacionesService.obtenerHistorial(parseInt(estudianteId))
  } catch (e) {
    console.warn('No se pudo cargar historial de asignaciones', e)
    historialAsignaciones.value = []
  } finally {
    cargandoHistorialAsignaciones.value = false
  }
}

const cargarHistorialPagos = async () => {
  cargandoHistorialPagos.value = true
  try {
    historialPagos.value = await cobrosService.obtenerPagosPorEstudiante(parseInt(estudianteId))
  } catch (e) {
    console.warn('No se pudo cargar historial de pagos', e)
    historialPagos.value = []
  } finally {
    cargandoHistorialPagos.value = false
  }
}

// ------- Cambio de estado académico -------
const TRANSICIONES_MANUALES: Record<string, { value: string; label: string }[]> = {
  MATRICULADO: [
    { value: 'COMPLETADO', label: 'Completado (terminó el curso)' },
    { value: 'RETIRADO', label: 'Retirado' }
  ],
  CURSANDO: [
    { value: 'COMPLETADO', label: 'Completado (terminó el curso)' },
    { value: 'RETIRADO', label: 'Retirado' }
  ],
  COMPLETADO: [
    { value: 'CURSANDO', label: 'Cursando (corrección)' }
  ],
  RETIRADO: [
    { value: 'MATRICULADO', label: 'Matriculado (reactivar)' },
    { value: 'CURSANDO', label: 'Cursando (reactivar)' }
  ]
}

const dialogEstadoVisible = ref(false)
const cambiandoEstado = ref(false)
const formEstado = reactive<{ estado: string | null; motivo: string }>({ estado: null, motivo: '' })

const estadosPermitidos = computed(() => TRANSICIONES_MANUALES[estudiante.estado] || [])

const abrirDialogEstado = () => {
  formEstado.estado = null
  formEstado.motivo = ''
  dialogEstadoVisible.value = true
}

const cerrarDialogEstado = () => {
  dialogEstadoVisible.value = false
}

const cambiarEstado = async () => {
  if (!formEstado.estado) return
  if (formEstado.estado === 'RETIRADO' && !formEstado.motivo.trim()) {
    toast.add({
      severity: 'warn',
      summary: 'Motivo requerido',
      detail: 'Debes ingresar un motivo al marcar el estudiante como RETIRADO',
      life: 4000
    })
    return
  }

  cambiandoEstado.value = true
  try {
    await estudiantesService.cambiarEstadoEstudiante(
      parseInt(estudianteId),
      formEstado.estado,
      formEstado.motivo.trim() || undefined
    )
    toast.add({
      severity: 'success',
      summary: 'Estado actualizado',
      detail: `Estudiante ahora está en ${formEstado.estado}`,
      life: 3000
    })
    cerrarDialogEstado()
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo cambiar el estado',
      life: 4000
    })
  } finally {
    cambiandoEstado.value = false
  }
}

// ------- Documentos -------
const tiposDocumento = [
  { label: 'Cédula', value: 'CEDULA' },
  { label: 'Certificado médico', value: 'CERTIFICADO_MEDICO' },
  { label: 'Foto', value: 'FOTO' },
  { label: 'Comprobante de pago', value: 'COMPROBANTE_PAGO' },
  { label: 'Otro', value: 'OTRO' }
]

const documentos = ref<any[]>([])
const dialogSubidaVisible = ref(false)
const subiendoDocumento = ref(false)
const descargandoId = ref<number | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const formSubida = reactive<{ tipo: string | null; archivo: File | null }>({ tipo: null, archivo: null })

const humanizarTipo = (tipo: string) => {
  const found = tiposDocumento.find(t => t.value === tipo)
  return found ? found.label : tipo
}

const cargarDocumentos = async () => {
  try {
    documentos.value = await estudiantesService.listarDocumentos(parseInt(estudianteId))
  } catch (e) {
    console.warn('No se pudieron cargar documentos', e)
  }
}

const abrirDialogSubida = () => {
  formSubida.tipo = null
  formSubida.archivo = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  dialogSubidaVisible.value = true
}

const cerrarDialogSubida = () => {
  dialogSubidaVisible.value = false
}

const onArchivoSeleccionado = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] || null
  if (file && file.size > 10 * 1024 * 1024) {
    toast.add({ severity: 'error', summary: 'Archivo muy grande', detail: 'El tamaño máximo es 10 MB', life: 4000 })
    input.value = ''
    formSubida.archivo = null
    return
  }
  formSubida.archivo = file
}

const subirDocumento = async () => {
  if (!formSubida.tipo || !formSubida.archivo) return
  subiendoDocumento.value = true
  try {
    await estudiantesService.subirDocumento(parseInt(estudianteId), formSubida.tipo, formSubida.archivo)
    toast.add({ severity: 'success', summary: 'Subido', detail: 'Documento subido correctamente', life: 3000 })
    cerrarDialogSubida()
    await cargarDocumentos()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo subir el documento',
      life: 4000
    })
  } finally {
    subiendoDocumento.value = false
  }
}

const descargarDocumento = async (documentoId: number) => {
  descargandoId.value = documentoId
  try {
    const { blob, filename } = await estudiantesService.descargarDocumento(parseInt(estudianteId), documentoId)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo descargar el documento',
      life: 4000
    })
  } finally {
    descargandoId.value = null
  }
}

const confirmarEliminarDocumento = (documentoId: number) => {
  confirm.require({
    message: '¿Eliminar este documento? Esta acción no se puede deshacer.',
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await estudiantesService.eliminarDocumento(parseInt(estudianteId), documentoId)
        toast.add({ severity: 'success', summary: 'Eliminado', detail: 'Documento eliminado', life: 3000 })
        await cargarDocumentos()
      } catch (e: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: e.response?.data?.detail || 'No se pudo eliminar',
          life: 4000
        })
      }
    }
  })
}

const cargar = async () => {
  loading.value = true
  try {
    const [est, prog] = await Promise.all([
      estudiantesService.obtenerEstudiante(parseInt(estudianteId)),
      estudiantesService.obtenerProgreso(parseInt(estudianteId))
    ])
    Object.assign(estudiante, est)
    progreso.value = prog
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo cargar el estudiante',
      life: 4000
    })
    setTimeout(() => router.back(), 1000)
  } finally {
    loading.value = false
  }
}

const editarPersonal = () => {
  Object.assign(formPersonal, {
    nombre: estudiante.nombre,
    apellido: estudiante.apellido,
    cedula: estudiante.cedula,
    fechaNacimiento: estudiante.fechaNacimiento ? new Date(estudiante.fechaNacimiento) : null,
    genero: estudiante.genero
  })
  editando.personal = true
}

const guardarPersonal = async () => {
  if (!formPersonal.nombre?.trim() || !formPersonal.apellido?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Nombre y apellido son requeridos', life: 3000 })
    return
  }

  guardando.personal = true
  try {
    await estudiantesService.actualizarEstudiante(parseInt(estudianteId), {
      nombre: formPersonal.nombre.trim(),
      apellido: formPersonal.apellido.trim(),
      fechaNacimiento: formPersonal.fechaNacimiento ? formPersonal.fechaNacimiento.toISOString().split('T')[0] : undefined,
      genero: formPersonal.genero
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Información personal guardada',
      life: 3000
    })
    editando.personal = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally {
    guardando.personal = false
  }
}

const editarContactoInfo = () => {
  Object.assign(formContacto, {
    email: estudiante.email,
    telefono: estudiante.telefono,
    direccion: estudiante.direccion
  })
  editando.contacto = true
}

const guardarContactoInfo = async () => {
  if (!formContacto.email?.trim() || !formContacto.telefono?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Email y teléfono son requeridos', life: 3000 })
    return
  }

  guardando.contacto = true
  try {
    await estudiantesService.actualizarEstudiante(parseInt(estudianteId), {
      email: formContacto.email.trim(),
      telefono: formContacto.telefono.trim(),
      direccion: formContacto.direccion
    })
    toast.add({
      severity: 'success',
      summary: 'Actualizado',
      detail: 'Información de contacto guardada',
      life: 3000
    })
    editando.contacto = false
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally {
    guardando.contacto = false
  }
}

const cargarContactosEmergencia = async () => {
  try {
    contactosEmergencia.value = await estudiantesService.listarContactosEmergencia(parseInt(estudianteId))
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudieron cargar los contactos',
      life: 3000
    })
  }
}

const agregarContacto = () => {
  editandoContacto.id = 'nuevo'
  Object.assign(formContactoEmergencia, {
    nombre: '',
    telefono: '',
    parentesco: '',
    esPrincipal: false
  })
}

const editarContacto = (contacto: any) => {
  editandoContacto.id = contacto.id
  Object.assign(formContactoEmergencia, {
    nombre: contacto.nombre,
    telefono: contacto.telefono,
    parentesco: contacto.parentesco || '',
    esPrincipal: contacto.esPrincipal || false
  })
}

const cancelarContactoEmergencia = () => {
  editandoContacto.id = null
  Object.assign(formContactoEmergencia, {
    nombre: '',
    telefono: '',
    parentesco: '',
    esPrincipal: false
  })
}

const guardarContactoEmergencia = async () => {
  if (!formContactoEmergencia.nombre?.trim() || !formContactoEmergencia.telefono?.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Nombre y teléfono son requeridos',
      life: 3000
    })
    return
  }

  guardandoContacto.value = true
  try {
    if (editandoContacto.id === 'nuevo') {
      await estudiantesService.crearContactoEmergencia(parseInt(estudianteId), {
        nombre: formContactoEmergencia.nombre.trim(),
        telefono: formContactoEmergencia.telefono.trim(),
        parentesco: formContactoEmergencia.parentesco?.trim() || undefined,
        esPrincipal: formContactoEmergencia.esPrincipal
      })
      toast.add({
        severity: 'success',
        summary: 'Agregado',
        detail: 'Contacto de emergencia agregado',
        life: 3000
      })
    } else {
      await estudiantesService.actualizarContactoEmergencia(parseInt(estudianteId), editandoContacto.id, {
        nombre: formContactoEmergencia.nombre.trim(),
        telefono: formContactoEmergencia.telefono.trim(),
        parentesco: formContactoEmergencia.parentesco?.trim() || undefined,
        esPrincipal: formContactoEmergencia.esPrincipal
      })
      toast.add({
        severity: 'success',
        summary: 'Actualizado',
        detail: 'Contacto de emergencia actualizado',
        life: 3000
      })
    }
    cancelarContactoEmergencia()
    cargarContactosEmergencia()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally {
    guardandoContacto.value = false
  }
}

const confirmarEliminarContacto = (contacto: any) => {
  confirm.require({
    message: `¿Eliminar contacto "${contacto.nombre}"?`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    accept: () => eliminarContacto(contacto.id),
    reject: () => {}
  })
}

const eliminarContacto = async (contactoId: number) => {
  try {
    await estudiantesService.eliminarContactoEmergencia(parseInt(estudianteId), contactoId)
    toast.add({
      severity: 'success',
      summary: 'Eliminado',
      detail: 'Contacto de emergencia eliminado',
      life: 3000
    })
    cargarContactosEmergencia()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo eliminar',
      life: 3000
    })
  }
}

onMounted(() => {
  cargar()
  cargarCatalogos()
  cargarContactosEmergencia()
  cargarDocumentos()
  cargarHistorialAsignaciones()
  cargarHistorialPagos()
})
</script>
