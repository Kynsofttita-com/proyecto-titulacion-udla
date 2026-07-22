<template>
  <div class="space-y-6">
    <PageHeader
      title="Gastos"
      description="Registro de gastos administrativos y operativos. Cada gasto se asocia a una cuenta y a una categoría."
      icon="pi pi-arrow-down"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Finanzas' }, { label: 'Gastos' }]"
    >
      <template #actions>
        <Button label="Nueva categoría" icon="pi pi-tag" outlined @click="abrirDialogNuevaCategoria" />
        <Button label="Nuevo gasto" icon="pi pi-plus" @click="abrirDialogNuevo" />
      </template>
    </PageHeader>

    <!-- Stats -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total del período" :value="formatMoney(totalPeriodo)" icon="pi pi-wallet" color="danger" />
      <StatCard label="Registros" :value="String(gastos.length)" icon="pi pi-list" color="brand" />
      <StatCard label="Mayor gasto" :value="formatMoney(mayorGasto)" icon="pi pi-arrow-up" color="warning" />
      <StatCard label="Categorías usadas" :value="String(categoriasUsadas)" icon="pi pi-tag" color="info" />
    </div>

    <!-- Filtros + tabla -->
    <div class="card p-5 space-y-4">
      <div class="flex flex-wrap gap-3 items-end">
        <div>
          <label class="block text-xs font-medium text-ink-600 mb-1">Cuenta</label>
          <Dropdown
            v-model="filtros.cuentaId"
            :options="[{id:null,nombre:'Todas'}, ...cuentas]"
            optionLabel="nombre" optionValue="id"
            class="w-48" showClear
            @change="cargarGastos"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-ink-600 mb-1">Categoría</label>
          <Dropdown
            v-model="filtros.categoriaId"
            :options="[{id:null,nombre:'Todas'}, ...categoriasGasto]"
            optionLabel="nombre" optionValue="id"
            class="w-56" showClear
            @change="cargarGastos"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-ink-600 mb-1">Desde</label>
          <Calendar v-model="filtros.fechaInicio" dateFormat="yy-mm-dd" :showIcon="true" class="w-40" @date-select="cargarGastos" @clear-click="cargarGastos" showButtonBar />
        </div>
        <div>
          <label class="block text-xs font-medium text-ink-600 mb-1">Hasta</label>
          <Calendar v-model="filtros.fechaFin" dateFormat="yy-mm-dd" :showIcon="true" class="w-40" @date-select="cargarGastos" @clear-click="cargarGastos" showButtonBar />
        </div>
      </div>

      <div v-if="cargando" class="text-center py-12"><ProgressSpinner /></div>
      <EmptyState
        v-else-if="gastos.length === 0"
        icon="pi-inbox"
        title="Sin gastos registrados"
        description="Registra el primer gasto para empezar a llevar tu contabilidad."
      >
        <template #action>
          <Button label="Nuevo gasto" icon="pi pi-plus" @click="abrirDialogNuevo" />
        </template>
      </EmptyState>
      <DataTable v-else :value="gastos" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
        <Column header="Fecha" style="width: 100px">
          <template #body="{ data }">
            <span class="text-xs text-ink-600">{{ data.fecha }}</span>
          </template>
        </Column>
        <Column header="Categoría">
          <template #body="{ data }">
            <p class="text-sm font-medium text-ink-800">{{ data.categoriaNombre }}</p>
          </template>
        </Column>
        <Column header="Cuenta">
          <template #body="{ data }">
            <span class="text-sm">{{ data.cuentaNombre }}</span>
          </template>
        </Column>
        <Column header="Descripción">
          <template #body="{ data }">
            <p class="text-sm text-ink-700">{{ data.descripcion || '—' }}</p>
            <p v-if="data.referencia" class="text-[10px] text-ink-500 font-mono">Ref: {{ data.referencia }}</p>
          </template>
        </Column>
        <Column header="Origen" style="width: 140px">
          <template #body="{ data }">
            <router-link
              v-if="data.vehiculoId"
              :to="`/vehiculos/${data.vehiculoId}`"
              class="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-info-50 text-info-700 text-[10px] font-medium border border-info-200 hover:bg-info-100"
              v-tooltip.top="'Ver vehículo — este gasto se generó automáticamente'"
            >
              <i class="pi pi-car text-[9px]" />
              {{ data.placaVehiculo || `Vehículo #${data.vehiculoId}` }}
            </router-link>
            <span
              v-else
              class="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-ink-100 text-ink-600 text-[10px] font-medium"
            >
              Manual
            </span>
          </template>
        </Column>
        <Column header="Monto" style="width: 130px" bodyClass="text-right">
          <template #body="{ data }">
            <span class="text-sm font-bold text-danger-700">− {{ formatMoney(data.monto) }}</span>
          </template>
        </Column>
        <Column header="" style="width: 90px">
          <template #body="{ data }">
            <div class="flex items-center gap-1 justify-end">
              <Button
                icon="pi pi-pencil" text rounded size="small"
                v-tooltip.left="esOrigenVehiculo(data) ? 'Editable solo desde el módulo Vehículos' : 'Editar'"
                :disabled="esOrigenVehiculo(data)"
                @click="abrirDialogEditar(data)" />
              <Button
                icon="pi pi-ban" text rounded size="small" severity="danger"
                v-tooltip.left="esOrigenVehiculo(data) ? 'Anulable solo desde el módulo Vehículos' : 'Anular'"
                :disabled="esOrigenVehiculo(data)"
                @click="abrirDialogAnular(data)" />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <Toast />

    <!-- Dialog: Nueva categoría (rápido, solo crear; para editar/eliminar ir a Saldo → Categorías) -->
    <Dialog v-model:visible="dialogCatVisible" modal header="Nueva categoría" :style="{ width: '500px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-start gap-2 text-xs text-ink-700">
          <i class="pi pi-info-circle text-info-600 mt-0.5" />
          <span>
            Crea una categoría personalizada para clasificar mejor tus gastos.
            Para editar o eliminar categorías, ir a
            <router-link to="/finanzas/saldo" class="underline font-medium">Saldo → Categorías</router-link>.
          </span>
        </div>

        <div>
          <label for="field-catg-nombre" class="block text-sm font-medium text-ink-700 mb-1.5">
            Nombre <span class="text-danger-600 font-semibold">*</span>
          </label>
          <InputText
            id="field-catg-nombre"
            v-model="formCat.nombre"
            placeholder="Ej: Publicidad Meta, Limpieza oficina"
            maxlength="80"
            class="w-full"
            :class="errorsCat.nombre ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrCat('nombre')"
          />
          <p v-if="errorsCat.nombre" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCat.nombre }}
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label for="field-catg-codigo" class="block text-sm font-medium text-ink-700 mb-1.5">
              Código <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputText
              id="field-catg-codigo"
              v-model="formCat.codigo"
              placeholder="Ej: PUBLICIDAD_META"
              maxlength="40"
              class="w-full font-mono"
              :class="errorsCat.codigo ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="onCatCodigoInput"
            />
            <p v-if="errorsCat.codigo" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCat.codigo }}
            </p>
            <p v-else class="text-[11px] text-ink-500 mt-1">MAYÚSCULAS, sin espacios (2–40 chars)</p>
          </div>
          <div>
            <label for="field-catg-tipo" class="block text-sm font-medium text-ink-700 mb-1.5">
              Tipo <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Dropdown
              v-model="formCat.tipo"
              inputId="field-catg-tipo"
              :options="[{ label: 'Gasto', value: 'GASTO' }, { label: 'Ingreso', value: 'INGRESO' }]"
              optionLabel="label" optionValue="value"
              class="w-full"
              :class="errorsCat.tipo ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrCat('tipo')"
            />
            <p v-if="errorsCat.tipo" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCat.tipo }}
            </p>
          </div>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogCatVisible = false" :disabled="guardandoCat" />
        <Button label="Crear categoría" icon="pi pi-check" :loading="guardandoCat" @click="guardarCategoria" />
      </template>
    </Dialog>

    <!-- Dialog: Nuevo / Editar gasto -->
    <Dialog v-model:visible="dialogVisible" modal :header="formG.id ? 'Editar gasto' : 'Nuevo gasto'" :style="{ width: '600px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label for="field-gasto-fecha" class="block text-sm font-medium text-ink-700 mb-1.5">
              Fecha <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Calendar
              v-model="formG.fecha"
              inputId="field-gasto-fecha"
              dateFormat="yy-mm-dd" :showIcon="true"
              class="w-full"
              :maxDate="new Date()"
              :inputClass="errorsG.fecha ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrG('fecha')"
            />
            <p v-if="errorsG.fecha" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsG.fecha }}
            </p>
          </div>
          <div>
            <label for="field-gasto-monto" class="block text-sm font-medium text-ink-700 mb-1.5">
              Monto (USD) <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputNumber
              v-model="formG.monto"
              inputId="field-gasto-monto"
              mode="currency" currency="USD" locale="en-US"
              :min="0.01"
              class="w-full"
              :pt="{ input: { class: errorsG.monto ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
              @update:modelValue="clearErrG('monto')"
            />
            <p v-if="errorsG.monto" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsG.monto }}
            </p>
          </div>
        </div>

        <div>
          <label for="field-gasto-cuentaId" class="block text-sm font-medium text-ink-700 mb-1.5">
            Cuenta de donde sale <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Dropdown
            v-model="formG.cuentaId"
            inputId="field-gasto-cuentaId"
            :options="cuentasActivas"
            optionLabel="nombre" optionValue="id"
            placeholder="Selecciona la cuenta"
            class="w-full"
            :class="errorsG.cuentaId ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrG('cuentaId')"
          >
            <template #option="{ option }">
              <div class="flex items-center justify-between w-full">
                <div class="flex items-center gap-2">
                  <i :class="iconoCuenta(option.tipo)" class="text-brand-600 text-xs" />
                  <span class="text-sm">{{ option.nombre }}</span>
                </div>
                <span class="text-xs text-ink-500">{{ formatMoney(option.saldoActual) }}</span>
              </div>
            </template>
          </Dropdown>
          <p v-if="errorsG.cuentaId" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsG.cuentaId }}
          </p>
          <p v-else-if="cuentasActivas.length === 0" class="text-xs text-warning-700 mt-1">
            <i class="pi pi-exclamation-triangle mr-1" />
            No hay cuentas activas. <router-link to="/finanzas/saldo" class="underline font-medium">Crea una primero</router-link>.
          </p>
        </div>

        <div>
          <label for="field-gasto-categoriaId" class="block text-sm font-medium text-ink-700 mb-1.5">
            Categoría <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Dropdown
            v-model="formG.categoriaId"
            inputId="field-gasto-categoriaId"
            :options="categoriasGasto"
            optionLabel="nombre" optionValue="id"
            placeholder="Ej: Combustible, Sueldos, Alquiler..."
            class="w-full"
            :class="errorsG.categoriaId ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrG('categoriaId')"
          />
          <p v-if="errorsG.categoriaId" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsG.categoriaId }}
          </p>
        </div>

        <!-- Vehículo (aparece solo si la categoría es de vehículo) -->
        <div v-if="esCategoriaVehiculo" class="grid grid-cols-1 md:grid-cols-2 gap-3 p-3 rounded-lg bg-brand-50/50 border border-brand-200">
          <div class="md:col-span-2 flex items-center gap-2 text-xs text-brand-700">
            <i class="pi pi-car" />
            <span>Vinculá este gasto a un vehículo para que aparezca también en su detalle.</span>
          </div>
          <div>
            <label for="field-gasto-vehiculoId" class="block text-sm font-medium text-ink-700 mb-1.5">
              Vehículo <span class="text-xs text-ink-500">(opcional)</span>
            </label>
            <Dropdown
              v-model="formG.vehiculoId"
              inputId="field-gasto-vehiculoId"
              :options="vehiculosOpciones"
              optionLabel="etiqueta" optionValue="id"
              placeholder="Sin vehículo"
              class="w-full" showClear
              @update:modelValue="onVehiculoSelect"
            >
              <template #option="{ option }">
                <div class="flex items-center gap-2">
                  <i class="pi pi-car text-brand-600 text-xs" />
                  <span class="font-mono text-sm">{{ option.placa }}</span>
                  <span class="text-xs text-ink-500">— {{ option.marca }} {{ option.modelo }}</span>
                </div>
              </template>
            </Dropdown>
          </div>
          <div>
            <label for="field-gasto-kilometraje" class="block text-sm font-medium text-ink-700 mb-1.5">
              Kilometraje <span class="text-xs text-ink-500">(opcional)</span>
            </label>
            <InputNumber
              v-model="formG.kilometraje"
              inputId="field-gasto-kilometraje"
              :min="0"
              suffix=" km"
              class="w-full"
              :disabled="!formG.vehiculoId"
            />
          </div>
        </div>

        <div>
          <label for="field-gasto-descripcion" class="block text-sm font-medium text-ink-700 mb-1.5">
            Descripción <span class="text-xs text-ink-500">(opcional)</span>
          </label>
          <Textarea
            id="field-gasto-descripcion"
            v-model="formG.descripcion"
            rows="2"
            placeholder="Detalles del gasto..."
            maxlength="255"
            class="w-full"
          />
        </div>

        <div>
          <label for="field-gasto-referencia" class="block text-sm font-medium text-ink-700 mb-1.5">
            Referencia <span class="text-xs text-ink-500">(opcional)</span>
          </label>
          <InputText
            id="field-gasto-referencia"
            v-model="formG.referencia"
            placeholder="Nº factura proveedor, comprobante..."
            maxlength="80"
            class="w-full"
          />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogVisible = false" :disabled="guardando" />
        <Button
          :label="formG.id ? 'Guardar cambios' : 'Registrar gasto'"
          icon="pi pi-check"
          :loading="guardando"
          :disabled="cuentasActivas.length === 0"
          @click="guardar"
        />
      </template>
    </Dialog>

    <!-- Dialog: Anular -->
    <Dialog v-model:visible="dialogAnularVisible" modal header="Anular gasto" :style="{ width: '480px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-warning-50 border border-warning-500/20 p-3 flex items-start gap-2 text-sm text-warning-700">
          <i class="pi pi-exclamation-triangle mt-0.5" />
          <span>Se marcará como anulado y dejará de contarse en el saldo. La operación queda en el histórico.</span>
        </div>
        <div>
          <label for="field-anular-motivo" class="block text-sm font-medium text-ink-700 mb-1.5">
            Motivo <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Textarea
            id="field-anular-motivo"
            v-model="motivoAnulacion"
            rows="3"
            placeholder="Ej: Registro duplicado / Monto incorrecto / Gasto no correspondía..."
            maxlength="500"
            class="w-full"
            :class="errorAnular ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="errorAnular = ''"
          />
          <p v-if="errorAnular" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorAnular }}
          </p>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogAnularVisible = false" :disabled="anulando" />
        <Button label="Anular gasto" icon="pi pi-ban" severity="danger" :loading="anulando" @click="confirmarAnular" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import Toast from 'primevue/toast'
import Tooltip from 'primevue/tooltip'
import ProgressSpinner from 'primevue/progressspinner'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import finanzasService, {
  type CuentaResponse,
  type CategoriaMovimientoResponse,
  type MovimientoContableResponse,
  type TipoCuenta
} from '@/services/finanzas'
import api from '@/services/api'

const vTooltip = Tooltip
const toast = useToast()

const formatMoney = (n: any) =>
  `$${(parseFloat(n) || 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const iconoCuenta = (t: TipoCuenta) => ({
  EFECTIVO: 'pi pi-money-bill',
  BANCO: 'pi pi-building',
  TARJETA: 'pi pi-credit-card'
}[t] || 'pi pi-briefcase')

const esOrigenVehiculo = (m: MovimientoContableResponse) =>
  !!(m.registroCombustibleId || m.mantenimientoId)

const gastos = ref<MovimientoContableResponse[]>([])
const cuentas = ref<CuentaResponse[]>([])
const categoriasGasto = ref<CategoriaMovimientoResponse[]>([])
const cargando = ref(false)

// Vehículos disponibles (para vincular gastos de combustible/mantenimiento)
const vehiculos = ref<Array<{ id: number; placa: string; marca: string; modelo: string }>>([])
const vehiculosOpciones = computed(() =>
  vehiculos.value.map(v => ({ ...v, etiqueta: `${v.placa} — ${v.marca} ${v.modelo}` }))
)

// Categorías vinculables a vehículo (por código de sistema)
const CATEGORIAS_VEHICULO_CODIGOS = ['COMBUSTIBLE', 'MANTENIMIENTO_VEHICULO'] as const

const esCategoriaVehiculo = computed(() => {
  const cat = categoriasGasto.value.find(c => c.id === formG.categoriaId)
  return !!cat && CATEGORIAS_VEHICULO_CODIGOS.includes(cat.codigo as any)
})

const cuentasActivas = computed(() => cuentas.value.filter(c => c.activo))
const totalPeriodo = computed(() => gastos.value.reduce((s, g) => s + Number(g.monto), 0))
const mayorGasto = computed(() => gastos.value.reduce((max, g) => Math.max(max, Number(g.monto)), 0))
const categoriasUsadas = computed(() => new Set(gastos.value.map(g => g.categoriaId)).size)

const filtros = reactive<any>({
  cuentaId: null, categoriaId: null,
  fechaInicio: null, fechaFin: null
})

const fmtFecha = (d: any): string | undefined => {
  if (!d) return undefined
  if (typeof d === 'string') return d.substring(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const cargarCatalogos = async () => {
  try {
    const [ctas, cats] = await Promise.all([
      finanzasService.listarCuentas(false),
      finanzasService.listarCategorias('GASTO', true)
    ])
    cuentas.value = ctas
    categoriasGasto.value = cats
  } catch { /* ignore */ }
}

const cargarVehiculos = async () => {
  try {
    const { data } = await api.get('/vehiculos', { params: { size: 200 } })
    const raw = data?.content || data || []
    vehiculos.value = raw
      .filter((v: any) => !v.deletedAt)
      .map((v: any) => ({ id: v.id, placa: v.placa, marca: v.marca || '', modelo: v.modelo || '' }))
  } catch (e) {
    console.warn('No se pudieron cargar los vehículos', e)
    vehiculos.value = []
  }
}

const onVehiculoSelect = (id: number | null) => {
  if (!id) {
    formG.vehiculoId = null
    formG.placaVehiculo = null
    formG.kilometraje = null
    return
  }
  const v = vehiculos.value.find(x => x.id === id)
  formG.placaVehiculo = v?.placa || null
}

const cargarGastos = async () => {
  cargando.value = true
  try {
    const res = await finanzasService.buscarMovimientos({
      tipo: 'GASTO',
      cuentaId: filtros.cuentaId ?? undefined,
      categoriaId: filtros.categoriaId ?? undefined,
      fechaInicio: fmtFecha(filtros.fechaInicio),
      fechaFin: fmtFecha(filtros.fechaFin),
      size: 200
    })
    gastos.value = res.content
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los gastos', life: 4000 })
  } finally { cargando.value = false }
}

// ============ CREAR / EDITAR ============

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

const valG = useValidation()
const errorsG = valG.errors
const setErrG = valG.setError
const clearErrG = valG.clearError
const clearAllG = valG.clearAll

// ---------- Crear categoría rápido (dialog aparte) ----------
const valCat = useValidation()
const errorsCat = valCat.errors
const setErrCat = valCat.setError
const clearErrCat = valCat.clearError
const clearAllCat = valCat.clearAll

const dialogCatVisible = ref(false)
const guardandoCat = ref(false)
const formCat = reactive<any>({ codigo: '', nombre: '', tipo: 'GASTO' })

const abrirDialogNuevaCategoria = () => {
  Object.assign(formCat, { codigo: '', nombre: '', tipo: 'GASTO' })
  clearAllCat()
  dialogCatVisible.value = true
}

const onCatCodigoInput = (v: string) => {
  formCat.codigo = (v || '').toUpperCase().replace(/[^A-Z0-9_]/g, '').slice(0, 40)
  clearErrCat('codigo')
}

const validarCategoria = (): boolean => {
  clearAllCat()
  const nombre = formCat.nombre?.trim() ?? ''
  if (!nombre) setErrCat('nombre', 'El nombre es requerido')
  else if (nombre.length < 2) setErrCat('nombre', 'Mínimo 2 caracteres')

  const codigo = (formCat.codigo || '').trim()
  if (!codigo) setErrCat('codigo', 'El código es requerido')
  else if (!/^[A-Z][A-Z0-9_]{1,39}$/.test(codigo)) {
    setErrCat('codigo', 'Debe empezar con letra, solo MAYÚSCULAS, números y _ (2–40)')
  }
  if (!formCat.tipo) setErrCat('tipo', 'Selecciona el tipo')

  if (Object.keys(errorsCat).length > 0) {
    valCat.focusFirst(['nombre', 'codigo', 'tipo'], 'catg', true)
    return false
  }
  return true
}

const guardarCategoria = async () => {
  if (!validarCategoria()) return
  guardandoCat.value = true
  try {
    const payload = {
      codigo: formCat.codigo.trim().toUpperCase(),
      nombre: formCat.nombre.trim(),
      tipo: formCat.tipo,
      activo: true
    }
    const creada = await finanzasService.crearCategoria(payload)
    toast.add({ severity: 'success', summary: 'Categoría creada', detail: creada.nombre, life: 3000 })
    dialogCatVisible.value = false
    // Recargar catálogos para que la nueva aparezca en el filtro y en el dropdown del gasto
    await cargarCatalogos()
    // Si el dialog de "Nuevo gasto" está abierto y la categoría es GASTO, pre-seleccionarla
    if (dialogVisible.value && creada.tipo === 'GASTO') {
      formG.categoriaId = creada.id
    }
  } catch (e: any) {
    const detail = e.response?.data?.detail || e.response?.data?.message
    let mensaje = detail || 'No se pudo crear la categoría'
    if (e.response?.status === 409) mensaje = detail || 'Ya existe una categoría con ese código'
    toast.add({ severity: 'error', summary: 'Error', detail: mensaje, life: 4000 })
  } finally { guardandoCat.value = false }
}

const dialogVisible = ref(false)
const guardando = ref(false)
const formG = reactive<any>({
  id: null, fecha: new Date(), monto: null,
  cuentaId: null, categoriaId: null,
  descripcion: '', referencia: '',
  vehiculoId: null, placaVehiculo: null, kilometraje: null
})

const abrirDialogNuevo = () => {
  Object.assign(formG, {
    id: null, fecha: new Date(), monto: null,
    cuentaId: null, categoriaId: null,
    descripcion: '', referencia: '',
    vehiculoId: null, placaVehiculo: null, kilometraje: null
  })
  clearAllG()
  dialogVisible.value = true
}

const abrirDialogEditar = (g: MovimientoContableResponse) => {
  if (g.pagoId) {
    toast.add({
      severity: 'warn',
      summary: 'No se puede editar',
      detail: 'Este movimiento se generó automáticamente desde un pago. Edita el pago desde Cobros.',
      life: 5000
    })
    return
  }
  if (esOrigenVehiculo(g)) {
    toast.add({
      severity: 'warn',
      summary: 'No se puede editar',
      detail: 'Este gasto se generó automáticamente desde Vehículos. Modifica el registro de combustible o mantenimiento.',
      life: 5000
    })
    return
  }
  Object.assign(formG, {
    id: g.id,
    fecha: new Date(g.fecha + 'T00:00:00'),
    monto: Number(g.monto),
    cuentaId: g.cuentaId,
    categoriaId: g.categoriaId,
    descripcion: g.descripcion || '',
    referencia: g.referencia || '',
    vehiculoId: g.vehiculoId ?? null,
    placaVehiculo: g.placaVehiculo ?? null,
    kilometraje: g.kilometraje ?? null
  })
  clearAllG()
  dialogVisible.value = true
}

const validar = (): boolean => {
  clearAllG()
  if (!formG.fecha) setErrG('fecha', 'La fecha es requerida')
  if (!formG.monto || formG.monto <= 0) setErrG('monto', 'El monto debe ser mayor a $0')
  if (!formG.cuentaId) setErrG('cuentaId', 'Selecciona la cuenta')
  if (!formG.categoriaId) setErrG('categoriaId', 'Selecciona la categoría')
  if (Object.keys(errorsG).length > 0) {
    valG.focusFirst(['fecha', 'monto', 'cuentaId', 'categoriaId'], 'gasto', true)
    return false
  }
  return true
}

const guardar = async () => {
  if (!validar()) return
  guardando.value = true
  try {
    // Si la categoría no es de vehículo, limpiar los campos de vehículo por si el usuario cambió de categoría
    const usaVehiculo = esCategoriaVehiculo.value && formG.vehiculoId
    const payload = {
      fecha: fmtFecha(formG.fecha)!,
      tipo: 'GASTO' as const,
      monto: Number(formG.monto),
      cuentaId: formG.cuentaId,
      categoriaId: formG.categoriaId,
      descripcion: formG.descripcion?.trim() || undefined,
      referencia: formG.referencia?.trim() || undefined,
      vehiculoId: usaVehiculo ? formG.vehiculoId : null,
      placaVehiculo: usaVehiculo ? (formG.placaVehiculo || null) : null,
      kilometraje: usaVehiculo ? (formG.kilometraje ?? null) : null
    }
    if (formG.id) {
      await finanzasService.actualizarMovimiento(formG.id, payload)
      toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Gasto guardado', life: 3000 })
    } else {
      await finanzasService.crearMovimiento(payload)
      toast.add({ severity: 'success', summary: 'Registrado', detail: formatMoney(formG.monto), life: 3000 })
    }
    dialogVisible.value = false
    await Promise.all([cargarGastos(), cargarCatalogos()])   // recargar catálogos por el saldo de las cuentas
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar el gasto',
      life: 4000
    })
  } finally { guardando.value = false }
}

// ============ ANULAR ============
const dialogAnularVisible = ref(false)
const anulando = ref(false)
const gastoAAnular = ref<MovimientoContableResponse | null>(null)
const motivoAnulacion = ref('')
const errorAnular = ref('')

const abrirDialogAnular = (g: MovimientoContableResponse) => {
  if (g.pagoId) {
    toast.add({
      severity: 'warn',
      summary: 'No se puede anular',
      detail: 'Este movimiento se generó automáticamente desde un pago.',
      life: 5000
    })
    return
  }
  if (esOrigenVehiculo(g)) {
    toast.add({
      severity: 'warn',
      summary: 'No se puede anular',
      detail: 'Este gasto se generó automáticamente desde Vehículos. Elimina el registro origen desde ese módulo.',
      life: 5000
    })
    return
  }
  gastoAAnular.value = g
  motivoAnulacion.value = ''
  errorAnular.value = ''
  dialogAnularVisible.value = true
}

const confirmarAnular = async () => {
  errorAnular.value = ''
  if (!motivoAnulacion.value || motivoAnulacion.value.trim().length < 5) {
    errorAnular.value = 'El motivo debe tener al menos 5 caracteres'
    return
  }
  if (!gastoAAnular.value) return
  anulando.value = true
  try {
    await finanzasService.anularMovimiento(gastoAAnular.value.id, motivoAnulacion.value.trim())
    toast.add({ severity: 'success', summary: 'Anulado', detail: 'Gasto anulado', life: 3000 })
    dialogAnularVisible.value = false
    await Promise.all([cargarGastos(), cargarCatalogos()])
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo anular',
      life: 4000
    })
  } finally { anulando.value = false }
}

onMounted(async () => {
  await Promise.all([cargarCatalogos(), cargarVehiculos()])
  await cargarGastos()
})
</script>
