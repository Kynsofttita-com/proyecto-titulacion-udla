<template>
  <div class="space-y-6">
    <PageHeader
      title="Saldo"
      description="Cuentas y movimientos consolidados de cobros y gastos."
      icon="pi pi-chart-pie"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Finanzas' }, { label: 'Saldo' }]"
    >
      <template #actions>
        <Button label="Nueva cuenta" icon="pi pi-plus" outlined @click="abrirDialogNuevaCuenta" v-if="esAdmin" />
      </template>
    </PageHeader>

    <!-- Total consolidado -->
    <div class="rounded-xl bg-gradient-to-br from-brand-600 to-brand-700 text-white p-6 shadow-card">
      <p class="text-sm text-white/80 uppercase tracking-wider">Saldo total consolidado</p>
      <p class="text-4xl font-bold mt-1">{{ formatMoney(saldoTotal) }}</p>
      <p class="text-xs text-white/70 mt-2">
        {{ cuentas.length }} cuenta{{ cuentas.length === 1 ? '' : 's' }} · Actualizado en tiempo real
      </p>
    </div>

    <!-- Tabs -->
    <div class="card overflow-hidden">
      <div class="flex border-b border-ink-200 bg-ink-50/40">
        <button
          v-for="t in tabs"
          :key="t.key"
          @click="tab = t.key"
          :class="['flex-1 px-4 py-3 text-sm font-medium transition border-b-2 flex items-center justify-center gap-2',
            tab === t.key ? 'border-brand-600 text-brand-700 bg-white' : 'border-transparent text-ink-600 hover:bg-ink-100/60']"
        >
          <i :class="['pi', t.icon]" />
          <span>{{ t.label }}</span>
        </button>
      </div>

      <!-- Tab: Cuentas -->
      <div v-if="tab === 'cuentas'" class="p-5">
        <div v-if="cargandoCuentas" class="text-center py-12">
          <ProgressSpinner />
        </div>
        <EmptyState
          v-else-if="cuentas.length === 0"
          icon="pi-briefcase"
          title="Aún no hay cuentas"
          description="Crea la primera cuenta para empezar a registrar movimientos."
        >
          <template #action>
            <Button label="Nueva cuenta" icon="pi pi-plus" @click="abrirDialogNuevaCuenta" v-if="esAdmin" />
          </template>
        </EmptyState>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="c in cuentas"
            :key="c.id"
            :class="['card p-5 transition-all border-l-4',
              c.saldoActual >= 0 ? 'border-l-success-500' : 'border-l-danger-500',
              !c.activo ? 'opacity-60' : '']"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <i :class="iconoCuenta(c.tipo)" class="text-brand-600" />
                  <h3 class="text-base font-bold text-ink-900 truncate">{{ c.nombre }}</h3>
                </div>
                <p class="text-xs text-ink-500 mt-0.5">{{ humanTipo(c.tipo) }}<span v-if="c.numeroCuenta"> · <span class="font-mono">{{ c.numeroCuenta }}</span></span></p>
              </div>
              <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
                c.activo ? 'bg-success-100 text-success-700' : 'bg-ink-100 text-ink-600']">
                {{ c.activo ? 'Activa' : 'Inactiva' }}
              </span>
            </div>

            <div class="space-y-3">
              <div>
                <p class="text-[10px] uppercase tracking-wider text-ink-500 font-semibold">Saldo actual</p>
                <p :class="['text-2xl font-bold', c.saldoActual >= 0 ? 'text-success-700' : 'text-danger-700']">
                  {{ formatMoney(c.saldoActual) }}
                </p>
                <p class="text-[11px] text-ink-500 mt-0.5">
                  Saldo inicial: {{ formatMoney(c.saldoInicial) }}
                </p>
              </div>
              <p v-if="c.observaciones" class="text-xs text-ink-600 italic border-t border-ink-100 pt-2">
                {{ c.observaciones }}
              </p>
              <div v-if="esAdmin" class="flex items-center gap-1 pt-2 border-t border-ink-100 justify-end">
                <Button icon="pi pi-pencil" text rounded size="small" v-tooltip="'Editar cuenta'" @click="abrirDialogEditarCuenta(c)" />
                <Button
                  v-if="c.activo"
                  icon="pi pi-ban" text rounded size="small" severity="danger"
                  v-tooltip="'Desactivar cuenta'"
                  @click="confirmarDesactivarCuenta(c)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Movimientos -->
      <div v-else-if="tab === 'movimientos'" class="p-5 space-y-4">
        <!-- Filtros -->
        <div class="flex flex-wrap gap-3 items-end">
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1">Cuenta</label>
            <Dropdown
              v-model="filtros.cuentaId"
              :options="[{id:null,nombre:'Todas'}, ...cuentas]"
              optionLabel="nombre" optionValue="id"
              class="w-48" showClear
              @change="cargarMovimientos"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1">Tipo</label>
            <Dropdown
              v-model="filtros.tipo"
              :options="[{label:'Todos',value:null},{label:'Ingresos',value:'INGRESO'},{label:'Gastos',value:'GASTO'}]"
              optionLabel="label" optionValue="value"
              class="w-36" showClear
              @change="cargarMovimientos"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1">Categoría</label>
            <Dropdown
              v-model="filtros.categoriaId"
              :options="categoriasFiltro"
              optionLabel="nombre" optionValue="id"
              class="w-56" showClear
              @change="cargarMovimientos"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1">Desde</label>
            <Calendar v-model="filtros.fechaInicio" dateFormat="yy-mm-dd" :showIcon="true" class="w-40" @date-select="cargarMovimientos" @clear-click="cargarMovimientos" showButtonBar />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-600 mb-1">Hasta</label>
            <Calendar v-model="filtros.fechaFin" dateFormat="yy-mm-dd" :showIcon="true" class="w-40" @date-select="cargarMovimientos" @clear-click="cargarMovimientos" showButtonBar />
          </div>
        </div>

        <!-- Tabla -->
        <div v-if="cargandoMov" class="text-center py-12"><ProgressSpinner /></div>
        <EmptyState
          v-else-if="movimientos.length === 0"
          icon="pi-inbox"
          title="Sin movimientos"
          description="No hay movimientos con esos filtros. Registra un pago desde Cobros o un gasto desde Gastos."
        />
        <DataTable v-else :value="movimientos" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
          <Column header="Fecha" style="width: 100px">
            <template #body="{ data }">
              <span class="text-xs text-ink-600">{{ data.fecha }}</span>
            </template>
          </Column>
          <Column header="Tipo" style="width: 90px">
            <template #body="{ data }">
              <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium',
                data.tipo === 'INGRESO' ? 'bg-success-50 text-success-700 border border-success-200' : 'bg-danger-50 text-danger-700 border border-danger-200']">
                <i :class="data.tipo === 'INGRESO' ? 'pi pi-arrow-up' : 'pi pi-arrow-down'" class="text-[10px]" />
                {{ data.tipo === 'INGRESO' ? 'Ingreso' : 'Gasto' }}
              </span>
            </template>
          </Column>
          <Column header="Categoría">
            <template #body="{ data }">
              <p class="text-sm font-medium text-ink-800">{{ data.categoriaNombre }}</p>
              <p class="text-[10px] text-ink-500 font-mono">{{ data.categoriaCodigo }}</p>
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
          <Column header="Monto" style="width: 140px" bodyClass="text-right">
            <template #body="{ data }">
              <span :class="['text-sm font-bold', data.tipo === 'INGRESO' ? 'text-success-700' : 'text-danger-700']">
                {{ data.tipo === 'INGRESO' ? '+' : '−' }} {{ formatMoney(data.monto) }}
              </span>
            </template>
          </Column>
          <Column header="Origen" style="width: 110px">
            <template #body="{ data }">
              <span v-if="data.pagoId" v-tooltip.left="'Auto-generado desde pago #' + data.pagoId" class="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-brand-50 text-brand-700 text-[10px] font-medium border border-brand-200">
                <i class="pi pi-link text-[9px]" /> Cobro
              </span>
              <span v-else class="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-ink-100 text-ink-600 text-[10px] font-medium">
                Manual
              </span>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <Toast />
    <ConfirmDialog />

    <!-- Dialog: Nueva/Editar cuenta -->
    <Dialog v-model:visible="dialogCuentaVisible" modal :header="formCuenta.id ? 'Editar cuenta' : 'Nueva cuenta'" :style="{ width: '520px' }">
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>

        <div>
          <label for="field-cta-nombre" class="block text-sm font-medium text-ink-700 mb-1.5">
            Nombre <span class="text-danger-600 font-semibold">*</span>
          </label>
          <InputText
            id="field-cta-nombre"
            v-model="formCuenta.nombre"
            placeholder="Ej: Caja, Banco Pichincha, Tarjeta Diners"
            maxlength="80"
            class="w-full"
            :class="errorsCta.nombre ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrCta('nombre')"
          />
          <p v-if="errorsCta.nombre" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCta.nombre }}
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label for="field-cta-tipo" class="block text-sm font-medium text-ink-700 mb-1.5">
              Tipo <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Dropdown
              v-model="formCuenta.tipo"
              inputId="field-cta-tipo"
              :options="[
                { label: 'Efectivo', value: 'EFECTIVO' },
                { label: 'Banco', value: 'BANCO' },
                { label: 'Tarjeta', value: 'TARJETA' }
              ]"
              optionLabel="label" optionValue="value"
              placeholder="Selecciona tipo"
              class="w-full"
              :class="errorsCta.tipo ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrCta('tipo'); clearErrCta('numeroCuenta')"
            />
            <p v-if="errorsCta.tipo" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCta.tipo }}
            </p>
          </div>
          <div>
            <label for="field-cta-saldoInicial" class="block text-sm font-medium text-ink-700 mb-1.5">
              Saldo inicial (USD) <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputNumber
              v-model="formCuenta.saldoInicial"
              inputId="field-cta-saldoInicial"
              mode="currency" currency="USD" locale="en-US"
              class="w-full"
              :pt="{ input: { class: errorsCta.saldoInicial ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
              @update:modelValue="clearErrCta('saldoInicial')"
            />
            <p v-if="errorsCta.saldoInicial" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCta.saldoInicial }}
            </p>
          </div>
        </div>

        <div v-if="formCuenta.tipo === 'BANCO' || formCuenta.tipo === 'TARJETA'">
          <label for="field-cta-numeroCuenta" class="block text-sm font-medium text-ink-700 mb-1.5">
            Número de cuenta <span class="text-danger-600 font-semibold">*</span>
          </label>
          <InputText
            id="field-cta-numeroCuenta"
            v-model="formCuenta.numeroCuenta"
            placeholder="Últimos 4 dígitos o número completo"
            maxlength="60"
            class="w-full"
            :class="errorsCta.numeroCuenta ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrCta('numeroCuenta')"
          />
          <p v-if="errorsCta.numeroCuenta" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsCta.numeroCuenta }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Observaciones <span class="text-xs text-ink-500">(opcional)</span></label>
          <Textarea v-model="formCuenta.observaciones" rows="2" maxlength="500" class="w-full" />
        </div>

        <div v-if="formCuenta.id" class="flex items-center gap-2 p-3 rounded-lg bg-ink-50 border border-ink-200">
          <Checkbox v-model="formCuenta.activo" :binary="true" inputId="field-cta-activo" />
          <label for="field-cta-activo" class="text-sm text-ink-700">Cuenta activa</label>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogCuentaVisible = false" :disabled="guardandoCuenta" />
        <Button :label="formCuenta.id ? 'Guardar cambios' : 'Crear cuenta'" icon="pi pi-check" :loading="guardandoCuenta" @click="guardarCuenta" />
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
import Checkbox from 'primevue/checkbox'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import Tooltip from 'primevue/tooltip'
import ProgressSpinner from 'primevue/progressspinner'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { useAuthStore } from '@/stores/auth'
import finanzasService, {
  type CuentaResponse,
  type CategoriaMovimientoResponse,
  type MovimientoContableResponse,
  type TipoCuenta
} from '@/services/finanzas'

const vTooltip = Tooltip
const toast = useToast()
const confirm = useConfirm()
const authStore = useAuthStore()

const esAdmin = computed(() => authStore.currentRole === 'ADMIN')

const tabs = [
  { key: 'cuentas',     label: 'Cuentas',     icon: 'pi-briefcase' },
  { key: 'movimientos', label: 'Movimientos', icon: 'pi-list' }
] as const
const tab = ref<'cuentas' | 'movimientos'>('cuentas')

const formatMoney = (n: any) =>
  `$${(parseFloat(n) || 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const humanTipo = (t: TipoCuenta) => ({ EFECTIVO: 'Efectivo', BANCO: 'Banco', TARJETA: 'Tarjeta' }[t] || t)
const iconoCuenta = (t: TipoCuenta) => ({
  EFECTIVO: 'pi pi-money-bill',
  BANCO: 'pi pi-building',
  TARJETA: 'pi pi-credit-card'
}[t] || 'pi pi-briefcase')

// ============ CUENTAS ============
const cuentas = ref<CuentaResponse[]>([])
const cargandoCuentas = ref(false)

const saldoTotal = computed(() =>
  cuentas.value.filter(c => c.activo).reduce((sum, c) => sum + Number(c.saldoActual || 0), 0)
)

const cargarCuentas = async () => {
  cargandoCuentas.value = true
  try {
    cuentas.value = await finanzasService.listarCuentas(false)
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las cuentas', life: 4000 })
  } finally { cargandoCuentas.value = false }
}

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

const valCta = useValidation()
const errorsCta = valCta.errors
const setErrCta = valCta.setError
const clearErrCta = valCta.clearError
const clearAllCta = valCta.clearAll

const dialogCuentaVisible = ref(false)
const guardandoCuenta = ref(false)
const formCuenta = reactive<any>({
  id: null, nombre: '', tipo: 'EFECTIVO', numeroCuenta: '',
  saldoInicial: 0, activo: true, observaciones: ''
})

const abrirDialogNuevaCuenta = () => {
  Object.assign(formCuenta, { id: null, nombre: '', tipo: 'EFECTIVO', numeroCuenta: '', saldoInicial: 0, activo: true, observaciones: '' })
  clearAllCta()
  dialogCuentaVisible.value = true
}

const abrirDialogEditarCuenta = (c: CuentaResponse) => {
  Object.assign(formCuenta, {
    id: c.id, nombre: c.nombre, tipo: c.tipo, numeroCuenta: c.numeroCuenta || '',
    saldoInicial: Number(c.saldoInicial), activo: c.activo, observaciones: c.observaciones || ''
  })
  clearAllCta()
  dialogCuentaVisible.value = true
}

const validarCuenta = (): boolean => {
  clearAllCta()
  const nombre = formCuenta.nombre?.trim() ?? ''
  if (!nombre) setErrCta('nombre', 'El nombre es requerido')
  else if (nombre.length < 2) setErrCta('nombre', 'Mínimo 2 caracteres')

  if (!formCuenta.tipo) setErrCta('tipo', 'Selecciona el tipo')

  if ((formCuenta.tipo === 'BANCO' || formCuenta.tipo === 'TARJETA')
      && !formCuenta.numeroCuenta?.trim()) {
    setErrCta('numeroCuenta', 'El número de cuenta es requerido para ' + (formCuenta.tipo === 'BANCO' ? 'Banco' : 'Tarjeta'))
  }
  if (formCuenta.saldoInicial == null) setErrCta('saldoInicial', 'El saldo inicial es requerido')

  if (Object.keys(errorsCta).length > 0) {
    valCta.focusFirst(['nombre', 'tipo', 'saldoInicial', 'numeroCuenta'], 'cta', true)
    return false
  }
  return true
}

const guardarCuenta = async () => {
  if (!validarCuenta()) return
  guardandoCuenta.value = true
  try {
    const payload = {
      nombre: formCuenta.nombre.trim(),
      tipo: formCuenta.tipo,
      numeroCuenta: formCuenta.numeroCuenta?.trim() || undefined,
      saldoInicial: Number(formCuenta.saldoInicial),
      activo: formCuenta.activo,
      observaciones: formCuenta.observaciones?.trim() || undefined
    }
    if (formCuenta.id) {
      await finanzasService.actualizarCuenta(formCuenta.id, payload)
      toast.add({ severity: 'success', summary: 'Actualizada', detail: formCuenta.nombre, life: 3000 })
    } else {
      await finanzasService.crearCuenta(payload)
      toast.add({ severity: 'success', summary: 'Creada', detail: formCuenta.nombre, life: 3000 })
    }
    dialogCuentaVisible.value = false
    await cargarCuentas()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar la cuenta',
      life: 4000
    })
  } finally { guardandoCuenta.value = false }
}

const confirmarDesactivarCuenta = (c: CuentaResponse) => {
  confirm.require({
    message: `¿Desactivar la cuenta "${c.nombre}"? No se podrán registrar movimientos nuevos en ella, pero los históricos se mantienen.`,
    header: 'Desactivar cuenta',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Desactivar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await finanzasService.desactivarCuenta(c.id)
        toast.add({ severity: 'success', summary: 'Desactivada', detail: c.nombre, life: 3000 })
        await cargarCuentas()
      } catch (e: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: e.response?.data?.detail || 'No se pudo desactivar',
          life: 4000
        })
      }
    }
  })
}

// ============ MOVIMIENTOS ============
const movimientos = ref<MovimientoContableResponse[]>([])
const categorias = ref<CategoriaMovimientoResponse[]>([])
const cargandoMov = ref(false)
const filtros = reactive<any>({
  cuentaId: null, categoriaId: null, tipo: null,
  fechaInicio: null, fechaFin: null
})

const categoriasFiltro = computed(() => {
  const base = [{ id: null, nombre: 'Todas', tipo: null } as any]
  if (!filtros.tipo) return [...base, ...categorias.value]
  return [...base, ...categorias.value.filter(c => c.tipo === filtros.tipo)]
})

const fmtFecha = (d: any): string | undefined => {
  if (!d) return undefined
  if (typeof d === 'string') return d.substring(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const cargarCategorias = async () => {
  try {
    categorias.value = await finanzasService.listarCategorias(undefined, true)
  } catch { /* ignore */ }
}

const cargarMovimientos = async () => {
  cargandoMov.value = true
  try {
    const res = await finanzasService.buscarMovimientos({
      cuentaId: filtros.cuentaId ?? undefined,
      categoriaId: filtros.categoriaId ?? undefined,
      tipo: filtros.tipo ?? undefined,
      fechaInicio: fmtFecha(filtros.fechaInicio),
      fechaFin: fmtFecha(filtros.fechaFin),
      size: 200
    })
    movimientos.value = res.content
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los movimientos', life: 4000 })
  } finally { cargandoMov.value = false }
}

onMounted(async () => {
  await Promise.all([cargarCuentas(), cargarCategorias(), cargarMovimientos()])
})
</script>
