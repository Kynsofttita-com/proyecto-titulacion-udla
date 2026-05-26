<template>
  <div class="space-y-6">
    <PageHeader
      title="Mi progreso"
      description="Detalle de tu avance académico en el curso"
      icon="pi pi-chart-line"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Mi progreso' }
      ]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined :loading="loading" @click="cargar" />
      </template>
    </PageHeader>

    <div v-if="loading" class="card p-10 text-center text-ink-500">
      <i class="pi pi-spinner animate-spin text-2xl mb-2" />
      <p>Cargando tu progreso…</p>
    </div>

    <div v-else-if="!miId" class="card p-10 text-center">
      <i class="pi pi-info-circle text-warning-600 text-3xl mb-2" />
      <p class="text-sm text-ink-700">Tu cuenta aún no está vinculada a un registro de estudiante.</p>
      <p class="text-xs text-ink-500 mt-1">Contacta con la administración para completar tu matrícula.</p>
    </div>

    <template v-else>
      <!-- Resumen general -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div class="card p-5 lg:col-span-2">
          <p class="text-xs text-ink-500 uppercase tracking-wider mb-2">Estado del curso</p>
          <div class="flex items-center gap-3 flex-wrap">
            <StatusBadge :status="me.estado || 'PRE_MATRICULADO'" />
            <span v-if="tipoCursoNombre" class="text-sm text-ink-800 font-medium">
              {{ tipoCursoNombre }}
            </span>
            <span v-if="categoriaCodigo" class="text-sm text-ink-700">
              · Categoría {{ categoriaCodigo }}
            </span>
          </div>
          <div class="mt-4 space-y-1 text-sm">
            <div class="flex justify-between text-ink-600">
              <span>Fecha de matrícula</span>
              <span class="font-medium text-ink-800">{{ me.fechaMatricula ? formatearFecha(me.fechaMatricula) : '—' }}</span>
            </div>
            <div class="flex justify-between text-ink-600">
              <span>Horas requeridas</span>
              <span class="font-medium text-ink-800">{{ horasRequeridas.toFixed(0) }} horas</span>
            </div>
            <div class="flex justify-between text-ink-600">
              <span>Horas completadas</span>
              <span class="font-medium text-ink-800">{{ horasCompletadas.toFixed(1) }} horas</span>
            </div>
            <div class="flex justify-between text-ink-600">
              <span>Horas restantes</span>
              <span class="font-semibold" :class="horasFaltantes > 0 ? 'text-warning-700' : 'text-success-700'">
                {{ horasFaltantes.toFixed(1) }} horas
              </span>
            </div>
          </div>
        </div>

        <div class="card p-5 flex flex-col">
          <p class="text-xs text-ink-500 uppercase tracking-wider mb-1">Avance global</p>
          <div class="flex-1 flex items-center justify-center py-4">
            <div class="relative w-32 h-32">
              <svg class="w-32 h-32 -rotate-90" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="44" stroke-width="10" class="stroke-ink-100" fill="none" />
                <circle
                  cx="50" cy="50" r="44" stroke-width="10" fill="none" stroke-linecap="round"
                  class="stroke-brand-600 transition-all"
                  :stroke-dasharray="276.46"
                  :stroke-dashoffset="276.46 * (1 - porcentajeAvance / 100)"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-2xl font-bold text-brand-700">{{ porcentajeAvance.toFixed(0) }}%</span>
                <span class="text-[10px] text-ink-500 uppercase tracking-wider">completado</span>
              </div>
            </div>
          </div>
          <p v-if="porcentajeAvance >= 100" class="text-center text-success-700 text-xs font-medium">
            <i class="pi pi-verified" /> ¡Todas las horas completadas!
          </p>
          <p v-else class="text-center text-ink-500 text-xs">
            Faltan {{ horasFaltantes.toFixed(1) }} horas
          </p>
        </div>
      </div>

      <!-- Breakdown teórico vs práctico (estimado por categoría) -->
      <DataTableCard
        title="Detalle de horas"
        description="Distribución de las horas requeridas según tu plan de estudios"
      >
        <div class="p-6 space-y-5">
          <div v-for="bucket in bucketsHoras" :key="bucket.label" class="space-y-1.5">
            <div class="flex items-baseline justify-between">
              <p class="text-sm font-medium text-ink-900">
                <i :class="`pi ${bucket.icon} text-[11px] text-${bucket.color}-600 mr-1.5`" />
                {{ bucket.label }}
              </p>
              <p class="text-sm font-semibold" :class="`text-${bucket.color}-700`">
                {{ bucket.completadas.toFixed(1) }} / {{ bucket.requeridas.toFixed(0) }} h
              </p>
            </div>
            <div class="w-full h-2 rounded-full bg-ink-100 overflow-hidden">
              <div
                class="h-full transition-all"
                :class="`bg-${bucket.color}-500`"
                :style="{ width: bucket.porcentaje + '%' }"
              />
            </div>
            <p class="text-[11px] text-ink-500">{{ bucket.porcentaje.toFixed(0) }}% · {{ bucket.descripcion }}</p>
          </div>
        </div>
      </DataTableCard>

      <!-- Historial de clases completadas -->
      <DataTableCard
        title="Clases completadas"
        :description="`${historial.length} ${historial.length === 1 ? 'clase' : 'clases'} con asistencia confirmada`"
      >
        <EmptyState
          v-if="historial.length === 0"
          icon="pi pi-history"
          title="Aún no completas ninguna clase"
          description="Cuando tu instructor finalice una clase aparecerá registrada aquí."
        />
        <DataTable v-else :value="historial" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
          <Column header="#">
            <template #body="{ data }">
              <span class="font-mono text-xs text-ink-500">#{{ data.id }}</span>
            </template>
          </Column>
          <Column header="Fecha">
            <template #body="{ data }">
              <p class="text-sm font-medium">{{ formatearFecha(data.fecha) }}</p>
              <p class="text-xs text-ink-500">{{ data.hora }}</p>
            </template>
          </Column>
          <Column header="Instructor">
            <template #body="{ data }">
              <p class="text-sm">{{ data.instructorNombre }}</p>
            </template>
          </Column>
          <Column header="Vehículo">
            <template #body="{ data }">
              <p class="text-sm font-mono">{{ data.vehiculoPlaca }}</p>
            </template>
          </Column>
          <Column header="Duración">
            <template #body="{ data }">
              <p class="text-sm font-semibold">{{ data.duracion }} min</p>
              <p class="text-[11px] text-ink-500">{{ (data.duracion / 60).toFixed(1) }} horas</p>
            </template>
          </Column>
          <Column header="Km recorridos">
            <template #body="{ data }">
              <p class="text-sm">{{ data.kmRecorridos != null ? data.kmRecorridos + ' km' : '—' }}</p>
            </template>
          </Column>
        </DataTable>
      </DataTableCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const authStore = useAuthStore()
const loading = ref(false)
const me = reactive<any>({})
const tipoCurso = ref<any>(null)
const categoria = ref<any>(null)
const historial = ref<any[]>([])

const miId = computed(() => authStore.currentEstudianteId)
const tipoCursoNombre = computed(() => tipoCurso.value?.nombre || '')
const categoriaCodigo = computed(() => categoria.value?.codigo || '')

const horasRequeridas = computed(() => {
  // duracionTotalHoras viene en horas. El estudiante no tiene un campo de
  // "total requerido" propio; siempre proviene del tipo_curso asociado.
  return Number(tipoCurso.value?.duracionTotalHoras || 0)
})
const horasCompletadas = computed(() => Number(me.minutosCompletados || 0) / 60)
const horasFaltantes = computed(() => Math.max(0, horasRequeridas.value - horasCompletadas.value))
const porcentajeAvance = computed(() => {
  if (horasRequeridas.value === 0) return 0
  return Math.min(100, (horasCompletadas.value / horasRequeridas.value) * 100)
})

// Split conceptual de horas en teóricas vs prácticas. El backend MS-Estudiantes
// almacena un acumulado único (minutos_completados) sin distinción; aquí lo
// representamos como una división estimada 30%/70% (teoría/práctica) basada en
// el plan estándar de la ANT para Ecuador. Si el backend agrega desglose
// específico, este cálculo se debe actualizar.
const bucketsHoras = computed(() => {
  const requeridasTotal = horasRequeridas.value
  const completadasTotal = horasCompletadas.value
  const teoricasReq = requeridasTotal * 0.3
  const practicasReq = requeridasTotal * 0.7
  // Distribución proporcional de las completadas
  const teoricasComp = Math.min(teoricasReq, completadasTotal * 0.3)
  const practicasComp = Math.min(practicasReq, completadasTotal * 0.7)
  return [
    {
      label: 'Horas teóricas',
      icon: 'pi-book',
      color: 'brand',
      requeridas: teoricasReq,
      completadas: teoricasComp,
      porcentaje: teoricasReq > 0 ? Math.min(100, (teoricasComp / teoricasReq) * 100) : 0,
      descripcion: 'Clases en aula sobre normativa, señalización y seguridad vial'
    },
    {
      label: 'Horas prácticas',
      icon: 'pi-car',
      color: 'info',
      requeridas: practicasReq,
      completadas: practicasComp,
      porcentaje: practicasReq > 0 ? Math.min(100, (practicasComp / practicasReq) * 100) : 0,
      descripcion: 'Conducción en pista y vías reales acompañado de instructor'
    }
  ]
})

const formatearFecha = (fecha: string) => {
  if (!fecha) return '—'
  const f = fecha.length >= 10 ? fecha.substring(0, 10) : fecha
  const [y, m, d] = f.split('-')
  if (!y || !m || !d) return fecha
  return `${d}/${m}/${y}`
}

const cargar = async () => {
  loading.value = true
  try {
    if (!authStore.currentEstudianteId) {
      await authStore.loadEstudianteId()
    }
    if (!authStore.currentEstudianteId) {
      return
    }

    const [meRes, asigRes, instRes, vehRes, cursosRes, catsRes] = await Promise.allSettled([
      api.get('/estudiantes/me'),
      api.get('/asignaciones', { params: { size: 500 } }),
      api.get('/instructores', { params: { size: 100 } }),
      api.get('/vehiculos',    { params: { size: 100 } }),
      api.get('/tipos-curso'),
      api.get('/categorias-licencia')
    ])

    if (meRes.status === 'fulfilled') {
      Object.assign(me, meRes.value.data)
    }
    const cursos = cursosRes.status === 'fulfilled'
      ? (Array.isArray(cursosRes.value.data) ? cursosRes.value.data : (cursosRes.value.data.content || []))
      : []
    tipoCurso.value = cursos.find((c: any) => c.id === me.tipoCursoId) || null

    const categorias = catsRes.status === 'fulfilled'
      ? (Array.isArray(catsRes.value.data) ? catsRes.value.data : (catsRes.value.data.content || []))
      : []
    categoria.value = categorias.find((c: any) => c.id === me.categoriaLicenciaId) || null

    const instructores = instRes.status === 'fulfilled' ? (instRes.value.data.content || []) : []
    const vehiculos = vehRes.status === 'fulfilled' ? (vehRes.value.data.content || []) : []
    const mapInst = new Map(instructores.map((i: any) => [
      i.id, i.nombreCompleto || `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim()
    ]))
    const mapVeh = new Map(vehiculos.map((v: any) => [v.id, v.placa || `Veh #${v.id}`]))

    const miEstId = authStore.currentEstudianteId
    const asignaciones = asigRes.status === 'fulfilled' ? (asigRes.value.data.content || []) : []
    historial.value = asignaciones
      .filter((a: any) => a.estudianteId === miEstId && a.estado === 'COMPLETADA')
      .sort((a: any, b: any) => {
        const ka = (a.fecha || '') + 'T' + (a.horaInicio || '00:00')
        const kb = (b.fecha || '') + 'T' + (b.horaInicio || '00:00')
        return kb.localeCompare(ka)
      })
      .map((a: any) => {
        let duracion = a.duracionMinutos || 60
        if (a.horaInicio && a.horaFin) {
          const [hi, mi] = a.horaInicio.substring(0, 5).split(':').map(Number)
          const [hf, mf] = a.horaFin.substring(0, 5).split(':').map(Number)
          duracion = (hf * 60 + mf) - (hi * 60 + mi)
        }
        const kmRec = (a.kmInicial != null && a.kmFinal != null) ? (a.kmFinal - a.kmInicial) : null
        return {
          id: a.id,
          fecha: a.fecha,
          hora: (a.horaInicio || '').substring(0, 5) || '--:--',
          duracion,
          kmRecorridos: kmRec,
          instructorNombre: mapInst.get(a.instructorId) || `Instructor #${a.instructorId}`,
          vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`
        }
      })
  } finally {
    loading.value = false
  }
}

onMounted(cargar)
</script>
