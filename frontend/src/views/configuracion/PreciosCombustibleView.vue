<template>
  <div class="space-y-6 max-w-5xl mx-auto">
    <PageHeader
      title="Precios de combustible"
      description="Gestiona los precios oficiales vigentes. Se usan para autocalcular el costo cuando se registra carga de combustible."
      icon="pi pi-bolt"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Vehículos', to: '/vehiculos' },
        { label: 'Combustible' }
      ]"
    >
      <template #actions>
        <Button label="Volver" icon="pi pi-arrow-left" outlined @click="router.push('/vehiculos')" />
        <Button label="Nuevo tipo" icon="pi pi-plus" @click="abrirDialogCrear" />
      </template>
    </PageHeader>

    <div class="rounded-lg bg-info-50 border border-info-200 p-4 flex items-start gap-3 text-sm text-ink-700">
      <i class="pi pi-info-circle text-info-600 mt-0.5" />
      <div>
        <p class="font-semibold text-info-800 mb-1">¿Cómo funciona?</p>
        <p>
          Cada vehículo tiene un <strong>tipo de combustible</strong> asignado. Cuando registres una carga
          (en el detalle del vehículo → tab "Combustible"), el sistema calculará automáticamente
          <code class="px-1 py-0.5 bg-white border border-info-300 rounded text-xs">costoTotal = litros × precioActual</code>.
          Si el precio público de un combustible cambia, actualízalo aquí y los próximos registros lo usarán.
        </p>
      </div>
    </div>

    <div v-if="cargando" class="text-center py-12">
      <ProgressSpinner />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="tipo in tipos"
        :key="tipo.id"
        :class="['card p-5 transition-all',
          tipo.activo ? 'border-l-4 border-l-brand-500' : 'opacity-60 border-l-4 border-l-ink-300']"
      >
        <div class="flex items-start justify-between mb-4">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <i :class="iconoCombustible(tipo.codigo)" class="text-brand-600" />
              <h3 class="text-base font-bold text-ink-900">{{ tipo.nombre }}</h3>
            </div>
            <p class="text-xs text-ink-500 font-mono">{{ tipo.codigo }}</p>
            <p v-if="tipo.observaciones" class="text-xs text-ink-600 mt-1 italic">{{ tipo.observaciones }}</p>
          </div>
          <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
            tipo.activo ? 'bg-success-100 text-success-700' : 'bg-ink-100 text-ink-600']">
            {{ tipo.activo ? 'Activo' : 'Inactivo' }}
          </span>
        </div>

        <div class="space-y-3">
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1.5">Precio actual</label>
            <div class="flex items-center gap-2">
              <span class="text-2xl font-bold text-ink-900">$</span>
              <InputNumber
                v-model="precios[tipo.id]"
                mode="decimal"
                :minFractionDigits="2"
                :maxFractionDigits="4"
                :min="0.0001"
                class="flex-1"
              />
              <span class="text-sm text-ink-500">/{{ tipo.unidad.toLowerCase() }}</span>
            </div>
            <p v-if="precios[tipo.id] !== Number(tipo.precioActual)" class="text-xs text-warning-600 mt-1 flex items-center gap-1">
              <i class="pi pi-pencil" />
              Cambio pendiente (actual: ${{ Number(tipo.precioActual).toFixed(4) }})
            </p>
          </div>

          <div class="flex items-center justify-between pt-3 border-t border-ink-100">
            <div class="text-xs text-ink-500">
              <span v-if="tipo.updatedAt">
                Última actualización:<br/>
                {{ new Date(tipo.updatedAt).toLocaleString('es-EC') }}
                <span v-if="tipo.updatedBy" class="text-ink-400">por {{ tipo.updatedBy }}</span>
              </span>
              <span v-else>Sin cambios desde la carga inicial</span>
            </div>
            <Button
              v-if="precios[tipo.id] !== Number(tipo.precioActual) || activos[tipo.id] !== tipo.activo"
              label="Guardar"
              icon="pi pi-check"
              size="small"
              :loading="guardando[tipo.id]"
              @click="guardar(tipo)"
            />
          </div>

          <div class="flex items-center justify-between pt-2">
            <div class="flex items-center gap-2">
              <Checkbox v-model="activos[tipo.id]" :inputId="`activo-${tipo.id}`" :binary="true" />
              <label :for="`activo-${tipo.id}`" class="text-xs text-ink-700">
                Disponible para asignar
              </label>
            </div>
            <Button
              icon="pi pi-trash"
              text
              rounded
              size="small"
              severity="danger"
              v-tooltip="'Desactivar permanentemente'"
              @click="confirmarDesactivar(tipo)"
            />
          </div>
        </div>
      </div>
    </div>

    <Toast />
    <ConfirmDialog />

    <!-- Dialog: crear nuevo tipo de combustible -->
    <Dialog v-model:visible="dialogCrearVisible" modal header="Nuevo tipo de combustible" :style="{ width: '480px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700">
          <i class="pi pi-info-circle text-info-600 mr-1" />
          Útil para agregar combustibles especiales (ej: GLP de auto, biodiesel, etc.) o tipos regionales.
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Código *</label>
          <InputText
            v-model="formNuevo.codigo"
            @input="formNuevo.codigo = formNuevo.codigo.toUpperCase().replace(/[^A-Z0-9_]/g, '')"
            placeholder="GLP_AUTO"
            maxlength="20"
            class="w-full font-mono"
          />
          <p class="text-xs text-ink-500 mt-1">MAYÚSCULAS, letras/números/guión bajo, único en el sistema</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre completo *</label>
          <InputText v-model="formNuevo.nombre" placeholder="GLP automotriz" class="w-full" />
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Unidad *</label>
            <Dropdown
              v-model="formNuevo.unidad"
              :options="[{label:'Galón',value:'GALON'},{label:'Litro',value:'LITRO'},{label:'kWh (eléctrico)',value:'KWH'}]"
              option-label="label"
              option-value="value"
              class="w-full"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Precio actual *</label>
            <InputNumber
              v-model="formNuevo.precioActual"
              mode="decimal"
              :minFractionDigits="2"
              :maxFractionDigits="4"
              :min="0.0001"
              prefix="$ "
              class="w-full"
            />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Observaciones <span class="text-xs text-ink-500">(opcional)</span></label>
          <Textarea v-model="formNuevo.observaciones" rows="2" class="w-full" maxlength="500" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogCrearVisible = false" :disabled="creando" />
        <Button label="Crear tipo" icon="pi pi-check" :loading="creando" @click="crearTipo" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import Tooltip from 'primevue/tooltip'
import ProgressSpinner from 'primevue/progressspinner'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import vehiculosService, {
  type TipoCombustibleResponse,
  type UnidadCombustible
} from '@/services/vehiculos'

const vTooltip = Tooltip
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const tipos = ref<TipoCombustibleResponse[]>([])
const cargando = ref(false)
const precios = reactive<Record<number, number>>({})
const activos = reactive<Record<number, boolean>>({})
const guardando = reactive<Record<number, boolean>>({})

const iconoCombustible = (codigo: string): string => {
  if (codigo === 'ELECTRICO') return 'pi pi-bolt'
  if (codigo === 'DIESEL') return 'pi pi-truck'
  if (codigo === 'SUPER') return 'pi pi-star'
  return 'pi pi-cog'
}

const cargar = async () => {
  cargando.value = true
  try {
    tipos.value = await vehiculosService.listarTiposCombustible(false)
    tipos.value.forEach(t => {
      precios[t.id] = Number(t.precioActual)
      activos[t.id] = t.activo
    })
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudieron cargar los tipos de combustible',
      life: 4000
    })
  } finally {
    cargando.value = false
  }
}

const guardar = async (tipo: TipoCombustibleResponse) => {
  guardando[tipo.id] = true
  try {
    const actualizado = await vehiculosService.actualizarPrecioCombustible(tipo.id, {
      precioActual: Number(precios[tipo.id]),
      activo: activos[tipo.id]
    })
    // Reemplazar en la lista
    const idx = tipos.value.findIndex(t => t.id === tipo.id)
    if (idx >= 0) tipos.value[idx] = actualizado
    precios[tipo.id] = Number(actualizado.precioActual)
    activos[tipo.id] = actualizado.activo
    toast.add({
      severity: 'success',
      summary: 'Precio actualizado',
      detail: `${tipo.nombre}: $${Number(actualizado.precioActual).toFixed(4)}/${tipo.unidad.toLowerCase()}`,
      life: 3000
    })
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo actualizar el precio',
      life: 4000
    })
  } finally {
    guardando[tipo.id] = false
  }
}

// ----- Crear nuevo tipo -----
const dialogCrearVisible = ref(false)
const creando = ref(false)
const formNuevo = reactive<{ codigo: string; nombre: string; unidad: UnidadCombustible; precioActual: number | null; observaciones: string }>({
  codigo: '',
  nombre: '',
  unidad: 'GALON',
  precioActual: null,
  observaciones: ''
})

const abrirDialogCrear = () => {
  formNuevo.codigo = ''
  formNuevo.nombre = ''
  formNuevo.unidad = 'GALON'
  formNuevo.precioActual = null
  formNuevo.observaciones = ''
  dialogCrearVisible.value = true
}

const crearTipo = async () => {
  if (!formNuevo.codigo?.trim() || formNuevo.codigo.length < 2) {
    toast.add({ severity:'error', summary:'Error', detail:'Código requerido (mínimo 2 caracteres)', life:3000 })
    return
  }
  if (!formNuevo.nombre?.trim()) {
    toast.add({ severity:'error', summary:'Error', detail:'Nombre requerido', life:3000 })
    return
  }
  if (!formNuevo.precioActual || formNuevo.precioActual <= 0) {
    toast.add({ severity:'error', summary:'Error', detail:'Precio actual debe ser > 0', life:3000 })
    return
  }
  creando.value = true
  try {
    const nuevo = await vehiculosService.crearTipoCombustible({
      codigo: formNuevo.codigo.trim(),
      nombre: formNuevo.nombre.trim(),
      unidad: formNuevo.unidad,
      precioActual: Number(formNuevo.precioActual),
      observaciones: formNuevo.observaciones?.trim() || undefined
    })
    toast.add({ severity:'success', summary:'Creado', detail:`${nuevo.codigo} agregado`, life:3000 })
    dialogCrearVisible.value = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity:'error',
      summary:'Error',
      detail: e.response?.data?.detail || 'No se pudo crear el tipo',
      life: 4000
    })
  } finally { creando.value = false }
}

// ----- Desactivar tipo existente -----
const confirmarDesactivar = (tipo: TipoCombustibleResponse) => {
  confirm.require({
    message: `¿Desactivar "${tipo.nombre}"? No se podrá asignar a nuevos vehículos, pero los registros históricos se mantienen.`,
    header: 'Desactivar tipo',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Desactivar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.desactivarTipoCombustible(tipo.id)
        toast.add({ severity:'success', summary:'Desactivado', detail: tipo.codigo, life: 3000 })
        await cargar()
      } catch (e: any) {
        toast.add({
          severity:'error',
          summary:'Error',
          detail: e.response?.data?.detail || 'No se pudo desactivar',
          life: 4000
        })
      }
    }
  })
}

onMounted(cargar)
</script>
