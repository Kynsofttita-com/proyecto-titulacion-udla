<template>
  <div class="space-y-6">
    <PageHeader
      title="Plantillas de Email"
      description="Crea y gestiona plantillas de correo electrónico configurables"
      icon="pi pi-envelope"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Plantillas' }
      ]"
    >
      <template #actions>
        <Button
          label="Nueva plantilla"
          icon="pi pi-plus"
          @click="abrirFormulario()"
          severity="primary"
        />
      </template>
    </PageHeader>

    <!-- Modal de Formulario -->
    <Dialog
      v-model:visible="mostrarModal"
      :header="plantillaEdicion ? 'Editar Plantilla' : 'Nueva Plantilla'"
      modal
      class="w-full md:w-11/12 lg:w-5/6 xl:w-4/5"
      @hide="limpiarFormulario"
    >
      <div class="space-y-4">
        <!-- Metadata -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-ink-900 mb-2">Código</label>
            <InputText
              v-model="formulario.codigo"
              placeholder="Ej: CONFIRMACION_INSCRIPCION"
              class="w-full"
              :disabled="!!plantillaEdicion"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-900 mb-2">Nombre</label>
            <InputText
              v-model="formulario.nombre"
              placeholder="Ej: Confirmación de Inscripción"
              class="w-full"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-ink-900 mb-2">Descripción</label>
          <Textarea
            v-model="formulario.descripcion"
            placeholder="Descripción breve de la plantilla"
            class="w-full"
            rows="2"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-ink-900 mb-2">Asunto del Email</label>
          <InputText
            v-model="formulario.asunto"
            placeholder="Ej: Bienvenido a la Escuela de Conducción"
            class="w-full"
          />
        </div>

        <!-- Editor visual + Preview lado a lado -->
        <div>
          <div class="flex items-center justify-between mb-2">
            <label class="block text-sm font-medium text-ink-900">Contenido del email</label>
            <button
              type="button"
              @click="mostrarPreview = !mostrarPreview"
              class="text-xs text-brand-700 hover:text-brand-800 flex items-center gap-1"
            >
              <i :class="mostrarPreview ? 'pi pi-eye-slash' : 'pi pi-eye'" class="text-xs" />
              {{ mostrarPreview ? 'Ocultar' : 'Mostrar' }} vista previa
            </button>
          </div>

          <div class="grid gap-4" :class="mostrarPreview ? 'grid-cols-1 lg:grid-cols-2' : 'grid-cols-1'">
            <HtmlEditor
              v-model="formulario.contenido"
              :variables="variablesDisponibles"
            />
            <EmailPreview
              v-if="mostrarPreview"
              :asunto="formulario.asunto"
              :cuerpo="formulario.contenido"
            />
          </div>

          <p class="text-xs text-ink-500 mt-2 flex items-start gap-1.5">
            <i class="pi pi-info-circle text-[10px] mt-0.5" />
            Usa la barra superior para dar formato al texto. Con el botón <span class="font-medium">"Insertar variable"</span> puedes agregar datos dinámicos como el nombre del estudiante, fecha de clase, etc.
          </p>
        </div>

        <div class="flex items-center gap-3">
          <Checkbox v-model="formulario.activa" binary />
          <label class="text-sm font-medium text-ink-900">Plantilla activa</label>
        </div>
      </div>

      <template #footer>
        <Button
          label="Cancelar"
          icon="pi pi-times"
          severity="secondary"
          @click="mostrarModal = false"
        />
        <Button
          label="Guardar"
          icon="pi pi-check"
          @click="guardarPlantilla"
          :loading="guardando"
        />
      </template>
    </Dialog>

    <DataTableCard title="Plantillas disponibles">
      <div v-if="loading" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="plantillas.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin plantillas"
          description="Crea la primera plantilla para comenzar"
        />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Código</th>
            <th class="px-4 py-3 text-left font-semibold">Nombre</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
            <th class="px-4 py-3 text-center font-semibold">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="p in plantillas" :key="p.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono text-xs">{{ p.codigo }}</td>
            <td class="px-4 py-3">{{ p.nombre }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="p.activa ? 'ACTIVA' : 'INACTIVA'" />
            </td>
            <td class="px-4 py-3 text-center">
              <div class="flex gap-2 justify-center">
                <button
                  @click="abrirPreview(p)"
                  class="text-info-600 hover:text-info-700"
                  title="Vista previa"
                >
                  <i class="pi pi-eye text-sm" />
                </button>
                <button
                  @click="abrirFormulario(p)"
                  class="text-warning-600 hover:text-warning-700"
                  title="Editar"
                >
                  <i class="pi pi-pencil text-sm" />
                </button>
                <button
                  @click="eliminar(p.id)"
                  class="text-danger-600 hover:text-danger-700"
                  title="Eliminar"
                >
                  <i class="pi pi-trash text-sm" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import HtmlEditor from '@/components/ui/HtmlEditor.vue'
import EmailPreview from '@/components/ui/EmailPreview.vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Checkbox from 'primevue/checkbox'
import plantillasService, { Plantilla, CreatePlantillaRequest, UpdatePlantillaRequest } from '@/services/plantillas'

const plantillas = ref<Plantilla[]>([])
const loading = ref(false)
const guardando = ref(false)
const mostrarModal = ref(false)
const mostrarPreview = ref(true)
const plantillaEdicion = ref<Plantilla | null>(null)

// Variables disponibles para las plantillas de email (uso comun del sistema)
const variablesDisponibles = [
  { key: 'nombre',           descripcion: 'Nombre del estudiante' },
  { key: 'apellido',         descripcion: 'Apellido del estudiante' },
  { key: 'nombreCompleto',   descripcion: 'Nombre completo del destinatario' },
  { key: 'email',            descripcion: 'Correo del destinatario' },
  { key: 'telefono',         descripcion: 'Telefono del destinatario' },
  { key: 'cedula',           descripcion: 'Cedula del destinatario' },
  { key: 'fecha',            descripcion: 'Fecha del evento (clase, factura, etc.)' },
  { key: 'hora',             descripcion: 'Hora del evento' },
  { key: 'instructor',       descripcion: 'Nombre del instructor asignado' },
  { key: 'vehiculo',         descripcion: 'Vehiculo asignado a la clase' },
  { key: 'monto',            descripcion: 'Monto de la factura o pago' },
  { key: 'numeroFactura',    descripcion: 'Numero de la factura' },
  { key: 'categoria',        descripcion: 'Categoria de licencia' },
  { key: 'escuela',          descripcion: 'Nombre de la escuela' }
]

const formulario = ref<CreatePlantillaRequest>({
  codigo: '',
  nombre: '',
  descripcion: '',
  asunto: '',
  contenido: '',
  activa: true
})

async function cargar() {
  loading.value = true
  try {
    plantillas.value = await plantillasService.obtenerPlantillas()
  } catch (error) {
    console.error('Error cargando plantillas:', error)
  } finally {
    loading.value = false
  }
}

function abrirFormulario(plantilla?: Plantilla) {
  if (plantilla) {
    plantillaEdicion.value = plantilla
    formulario.value = {
      codigo: plantilla.codigo,
      nombre: plantilla.nombre,
      descripcion: plantilla.descripcion,
      asunto: plantilla.asunto,
      contenido: plantilla.contenido,
      activa: plantilla.activa
    }
  } else {
    limpiarFormulario()
  }
  mostrarModal.value = true
}

function limpiarFormulario() {
  plantillaEdicion.value = null
  formulario.value = {
    codigo: '',
    nombre: '',
    descripcion: '',
    asunto: '',
    contenido: '',
    activa: true
  }
}

async function guardarPlantilla() {
  if (!formulario.value.codigo || !formulario.value.nombre || !formulario.value.asunto || !formulario.value.contenido) {
    alert('Por favor completa todos los campos requeridos')
    return
  }

  guardando.value = true
  try {
    if (plantillaEdicion.value) {
      // Editar plantilla existente
      const updateData: UpdatePlantillaRequest = {
        nombre: formulario.value.nombre,
        descripcion: formulario.value.descripcion,
        asunto: formulario.value.asunto,
        contenido: formulario.value.contenido,
        activa: formulario.value.activa
      }
      await plantillasService.actualizarPlantilla(plantillaEdicion.value.id, updateData)
      alert('Plantilla actualizada correctamente')
    } else {
      // Crear nueva plantilla
      await plantillasService.crearPlantilla(formulario.value)
      alert('Plantilla creada correctamente')
    }
    mostrarModal.value = false
    await cargar()
  } catch (error) {
    console.error('Error guardando plantilla:', error)
    alert('Error al guardar la plantilla. Intenta de nuevo.')
  } finally {
    guardando.value = false
  }
}

async function abrirPreview(plantilla: Plantilla) {
  try {
    const preview = await plantillasService.previewPlantilla(plantilla.id, {})
    console.log('Preview:', preview)
  } catch (error) {
    console.error('Error preview:', error)
  }
}

async function eliminar(id: number) {
  if (!confirm('¿Eliminar esta plantilla?')) return
  try {
    await plantillasService.eliminarPlantilla(id)
    alert('Plantilla eliminada correctamente')
    await cargar()
  } catch (error) {
    console.error('Error eliminando:', error)
    alert('Error al eliminar la plantilla. Intenta de nuevo.')
  }
}

onMounted(() => {
  cargar()
})
</script>
