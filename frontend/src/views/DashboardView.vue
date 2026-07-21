<template>
  <!-- =============================================================
       DASHBOARD INSTRUCTOR
       Vista simplificada con lo que un instructor necesita ver:
       sus clases del dia, sus estudiantes, sus minutos dictados.
       ============================================================= -->
  <div v-if="esInstructor" class="space-y-6">
    <PageHeader
      title="Mi panel"
      :description="`Bienvenido/a, ${instructorNombre} · Vista INSTRUCTOR`"
      icon="pi pi-id-card"
      :breadcrumbs="[{ label: 'Inicio' }]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined :loading="loading" @click="cargar" />
        <Button label="Ver mi calendario" icon="pi pi-calendar" @click="$router.push('/asignaciones')" />
      </template>
    </PageHeader>

    <!-- KPIs del instructor -->
    <div>
      <h3 class="heading-3 mb-3 flex items-center gap-2">
        <i class="pi pi-chart-line text-brand-700" />
        Mi actividad
      </h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Mis clases hoy"
          :value="kpisInst.clasesHoy"
          icon="pi pi-calendar"
          color="brand"
          :hint="`${kpisInst.clasesEnCurso} en curso · ${kpisInst.clasesCompletadasHoy} completadas`"
        />
        <StatCard
          label="Esta semana"
          :value="kpisInst.clasesSemana"
          icon="pi pi-calendar-plus"
          color="info"
          hint="Clases programadas"
        />
        <StatCard
          label="Mis estudiantes"
          :value="kpisInst.estudiantesActivos"
          icon="pi pi-users"
          color="success"
          :hint="`${kpisInst.estudiantesTotal} histórico total`"
        />
        <StatCard
          label="Minutos dictados (mes)"
          :value="kpisInst.minutosMes"
          icon="pi pi-clock"
          color="warning"
          :hint="`${(kpisInst.minutosMes / 60).toFixed(1)} horas este mes`"
        />
      </div>
    </div>

    <!-- Mis clases de hoy con acciones -->
    <DataTableCard
      title="Mis clases de hoy"
      description="Inicia o finaliza cada clase desde acá"
    >
      <template #toolbar>
        <Button label="Ver calendario completo" icon="pi pi-calendar" text @click="$router.push('/asignaciones')" />
      </template>
      <div v-if="misClasesHoy.length === 0">
        <EmptyState
          icon="pi pi-calendar-times"
          title="Sin clases para hoy"
          description="Disfruta tu día libre. Cuando tengas asignaciones aparecerán acá."
        />
      </div>
      <ul v-else class="divide-y divide-ink-200">
        <li v-for="c in misClasesHoy" :key="c.id" class="px-6 py-4 hover:bg-ink-50/40 transition">
          <div class="flex items-center gap-4 flex-wrap">
            <div class="w-20 text-center flex-shrink-0">
              <p class="text-xl font-bold text-brand-700">{{ c.hora }}</p>
              <p class="text-[10px] text-ink-500 uppercase tracking-wider">{{ c.duracion }}min</p>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-ink-900 truncate">
                Estudiante: {{ c.estudianteNombre }}
              </p>
              <p class="text-xs text-ink-500 truncate">
                <i class="pi pi-car text-[10px]" /> {{ c.vehiculoPlaca }} · Clase #{{ c.id }}
              </p>
            </div>
            <StatusBadge :status="c.estado" />
            <div class="flex gap-2 ml-auto">
              <Button
                v-if="c.estado === 'PROGRAMADA' || c.estado === 'CONFIRMADA'"
                icon="pi pi-play"
                label="Iniciar"
                size="small"
                severity="success"
                @click="iniciarClase(c.id)"
                :loading="c.processing"
              />
              <Button
                v-if="c.estado === 'EN_CURSO'"
                icon="pi pi-check"
                label="Finalizar"
                size="small"
                @click="finalizarClase(c.id)"
                :loading="c.processing"
              />
              <Button
                icon="pi pi-eye"
                outlined
                size="small"
                @click="$router.push('/asignaciones')"
                title="Ver en calendario"
              />
            </div>
          </div>
        </li>
      </ul>
    </DataTableCard>

    <!-- Próximas clases (sin hoy) -->
    <DataTableCard
      title="Próximas clases"
      description="Tus siguientes 5 asignaciones programadas"
    >
      <DataTable :value="proximasClases" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 50rem' } }">
        <Column header="Cuándo">
          <template #body="{ data }">
            <p class="text-sm font-medium">{{ formatearFechaCorta(data.fecha) }}</p>
            <p class="text-xs text-ink-500">{{ data.hora }} · {{ data.duracion }}min</p>
          </template>
        </Column>
        <Column header="Estudiante">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <Avatar :name="data.estudianteNombre" size="sm" />
              <p class="text-sm">{{ data.estudianteNombre }}</p>
            </div>
          </template>
        </Column>
        <Column header="Vehículo">
          <template #body="{ data }">
            <span class="text-sm font-mono">{{ data.vehiculoPlaca }}</span>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
      </DataTable>
    </DataTableCard>
  </div>

  <!-- =============================================================
       DASHBOARD ESTUDIANTE
       Vista solo-lectura con su progreso, próximas clases y saldo.
       ============================================================= -->
  <div v-else-if="esEstudiante" class="space-y-6">
    <PageHeader
      title="Mi panel"
      :description="`Bienvenido/a, ${estudianteNombre} · Vista ESTUDIANTE`"
      icon="pi pi-graduation-cap"
      :breadcrumbs="[{ label: 'Inicio' }]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined :loading="loading" @click="cargar" />
        <Button label="Ver mi calendario" icon="pi pi-calendar" @click="$router.push('/asignaciones')" />
      </template>
    </PageHeader>

    <!-- Estado del curso + progreso -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <div class="card p-5 lg:col-span-2">
        <div class="flex items-start gap-4 flex-wrap">
          <div class="w-12 h-12 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0">
            <i class="pi pi-graduation-cap text-xl" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-xs text-ink-500 uppercase tracking-wider">Estado académico</p>
            <div class="flex items-center gap-3 flex-wrap mt-1">
              <StatusBadge :status="datosEst.estado || 'PRE_MATRICULADO'" />
              <span v-if="datosEst.tipoCursoNombre" class="text-sm text-ink-700">
                · {{ datosEst.tipoCursoNombre }}
              </span>
              <span v-if="datosEst.categoriaLicencia" class="text-sm text-ink-700">
                · Categoría {{ datosEst.categoriaLicencia }}
              </span>
            </div>
            <p v-if="datosEst.fechaMatricula" class="text-xs text-ink-500 mt-1">
              Matriculado el {{ formatearFechaCorta(datosEst.fechaMatricula) }}
            </p>
          </div>
        </div>

        <!-- Progreso de horas -->
        <div class="mt-5">
          <div class="flex items-baseline justify-between mb-2">
            <p class="text-sm font-semibold text-ink-900">Progreso del curso</p>
            <p class="text-sm font-bold text-brand-700">
              {{ kpisEst.horasCompletadas.toFixed(1) }} / {{ kpisEst.horasRequeridas.toFixed(0) }} horas
            </p>
          </div>
          <div class="w-full h-3 rounded-full bg-ink-100 overflow-hidden">
            <div
              class="h-full bg-brand-600 transition-all"
              :style="{ width: kpisEst.porcentajeAvance + '%' }"
            />
          </div>
          <p class="text-xs text-ink-500 mt-1.5">
            {{ kpisEst.porcentajeAvance.toFixed(0) }}% completado
            <span v-if="kpisEst.horasFaltantes > 0">
              · te faltan {{ kpisEst.horasFaltantes.toFixed(1) }} horas
            </span>
            <span v-else class="text-success-700 font-medium">
              · ¡Completaste todas las horas requeridas!
            </span>
          </p>
        </div>
      </div>

      <!-- Saldo financiero -->
      <div class="card p-5">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-warning-50 text-warning-700 flex items-center justify-center flex-shrink-0">
            <i class="pi pi-wallet" />
          </div>
          <div>
            <p class="text-xs text-ink-500 uppercase tracking-wider">Saldo pendiente</p>
            <p class="text-2xl font-bold text-ink-900 mt-0.5">{{ formatMoney(kpisEst.saldoPendiente) }}</p>
          </div>
        </div>
        <div class="mt-4 space-y-1.5 text-xs">
          <div class="flex justify-between">
            <span class="text-ink-500">Total facturado</span>
            <span class="font-medium text-ink-700">{{ formatMoney(kpisEst.totalFacturado) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-ink-500">Total pagado</span>
            <span class="font-medium text-success-700">{{ formatMoney(kpisEst.totalPagado) }}</span>
          </div>
          <div v-if="kpisEst.facturasPendientes > 0" class="flex justify-between">
            <span class="text-ink-500">Facturas pendientes</span>
            <span class="font-medium text-warning-700">{{ kpisEst.facturasPendientes }}</span>
          </div>
        </div>
        <Button
          label="Ver mis pagos"
          icon="pi pi-arrow-right"
          text
          size="small"
          class="!mt-3"
          @click="$router.push('/mis-pagos')"
        />
      </div>
    </div>

    <!-- KPIs rápidos -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <StatCard
        label="Clases programadas"
        :value="kpisEst.clasesProgramadas"
        icon="pi pi-calendar"
        color="brand"
        hint="Pendientes a futuro"
      />
      <StatCard
        label="Clases completadas"
        :value="kpisEst.clasesCompletadas"
        icon="pi pi-check-circle"
        color="success"
        hint="Asistencia registrada"
      />
      <StatCard
        label="Clases canceladas"
        :value="kpisEst.clasesCanceladas"
        icon="pi pi-times-circle"
        :color="kpisEst.clasesCanceladas > 0 ? 'warning' : 'success'"
        hint="Histórico"
      />
      <StatCard
        label="Próxima clase"
        :value="kpisEst.proximaClaseFecha || '—'"
        icon="pi pi-clock"
        color="info"
        :hint="kpisEst.proximaClaseHora || 'Sin clases programadas'"
      />
    </div>

    <!-- Próximas clases -->
    <DataTableCard
      title="Mis próximas clases"
      description="Tus siguientes asignaciones programadas"
    >
      <template #toolbar>
        <Button label="Ver calendario" icon="pi pi-calendar" text @click="$router.push('/asignaciones')" />
      </template>
      <div v-if="proximasClasesEst.length === 0">
        <EmptyState
          icon="pi pi-calendar-times"
          title="Sin clases programadas"
          description="Cuando tengas asignaciones aparecerán aquí."
        />
      </div>
      <ul v-else class="divide-y divide-ink-200">
        <li v-for="c in proximasClasesEst" :key="c.id" class="px-6 py-3 hover:bg-ink-50/40 transition">
          <div class="flex items-center gap-4 flex-wrap">
            <div class="w-20 text-center flex-shrink-0">
              <p class="text-base font-bold text-brand-700">{{ formatearFechaCorta(c.fecha) }}</p>
              <p class="text-[10px] text-ink-500 uppercase tracking-wider">{{ c.hora }} · {{ c.duracion }}min</p>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-ink-900 truncate">
                Con {{ c.instructorNombre }}
              </p>
              <p class="text-xs text-ink-500 truncate">
                <i class="pi pi-car text-[10px]" /> {{ c.vehiculoPlaca }} · Clase #{{ c.id }}
              </p>
            </div>
            <StatusBadge :status="c.estado" />
          </div>
        </li>
      </ul>
    </DataTableCard>

    <!-- Últimas clases realizadas -->
    <DataTableCard
      title="Últimas clases realizadas"
      description="Tu historial de asistencia más reciente"
    >
      <div v-if="ultimasClasesEst.length === 0">
        <EmptyState
          icon="pi pi-history"
          title="Aún no has completado clases"
          description="Tus clases completadas se mostrarán aquí."
        />
      </div>
      <DataTable v-else :value="ultimasClasesEst" striped-rows :pt="{ table: { style: 'min-width: 50rem' } }">
        <Column header="Fecha">
          <template #body="{ data }">
            <p class="text-sm font-medium">{{ formatearFechaCorta(data.fecha) }}</p>
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
            <p class="text-sm">{{ data.duracion }}min</p>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
      </DataTable>
    </DataTableCard>
  </div>

  <!-- =============================================================
       DASHBOARD ADMIN / STAFF
       Vista ejecutiva con KPIs operativos y financieros.
       ============================================================= -->
  <div v-else class="space-y-6">
    <PageHeader
      title="Panel General"
      :description="`Resumen ejecutivo del estado de la escuela · Vista ${authStore.currentRole || 'ADMIN'}`"
      icon="pi pi-th-large"
      :breadcrumbs="[{ label: 'Inicio' }]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined :loading="loading" @click="cargar" />
        <Button label="Programar clase" icon="pi pi-plus" @click="$router.push('/asignaciones')" />
      </template>
    </PageHeader>

    <!-- ================== BANNER DE ALERTAS CRITICAS ================== -->
    <div
      v-if="alertas.length > 0"
      class="card animate-fade-up p-4 border-l-4 !border-l-warning-600"
    >
      <div class="flex items-start gap-3 flex-wrap">
        <div class="w-10 h-10 rounded-lg bg-warning-50 text-warning-700 flex items-center justify-center flex-shrink-0">
          <i class="pi pi-exclamation-triangle" />
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-sm font-semibold text-ink-900">Atención requerida</h3>
          <p class="text-xs text-ink-600 mt-0.5">
            Hay {{ alertas.length }} {{ alertas.length === 1 ? 'asunto pendiente' : 'asuntos pendientes' }} que necesitan revisión.
          </p>
          <div class="flex gap-2 mt-2 flex-wrap">
            <button
              v-for="a in alertas"
              :key="a.label"
              type="button"
              @click="router.push(a.to)"
              :class="['inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all',
                a.severity === 'danger'
                  ? 'bg-danger-100 text-danger-800 hover:bg-danger-200'
                  : 'bg-warning-100 text-warning-800 hover:bg-warning-200']"
            >
              <i :class="`pi ${a.icon} text-xs`" />
              {{ a.count }} {{ a.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ================== SECCION 1: RESUMEN EJECUTIVO ================== -->
    <div>
      <h3 class="heading-3 mb-3 flex items-center gap-2">
        <i class="pi pi-chart-line text-brand-700" />
        Resumen ejecutivo
      </h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Estudiantes activos"
          :value="kpis.estudiantesActivos"
          icon="pi pi-users"
          color="brand"
          :hint="`${kpis.estudiantesTotal} total en sistema`"
        />
        <StatCard
          label="Ingresos del mes"
          :value="formatMoney(kpis.ingresosMes)"
          icon="pi pi-arrow-up-right"
          color="success"
          :hint="`${kpis.facturasPagadasMes} ${kpis.facturasPagadasMes === 1 ? 'factura pagada' : 'facturas pagadas'}`"
        />
        <StatCard
          label="Saldo por cobrar"
          :value="formatMoney(kpis.saldoPorCobrar)"
          icon="pi pi-wallet"
          color="warning"
          :hint="`${kpis.facturasPendientes} ${kpis.facturasPendientes === 1 ? 'factura pendiente' : 'facturas pendientes'}`"
        />
        <StatCard
          label="Clases hoy"
          :value="kpis.clasesHoy"
          icon="pi pi-calendar"
          color="info"
          :hint="`${kpis.clasesSemana} programadas esta semana`"
        />
      </div>
    </div>

    <!-- ================== SECCION 2: DISTRIBUCION ACADEMICA ================== -->
    <div>
      <h3 class="heading-3 mb-3 flex items-center gap-2">
        <i class="pi pi-graduation-cap text-brand-700" />
        Estudiantes por estado académico
      </h3>
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
        <StatCard label="Pre-matriculados" :value="dist.preMatriculados" icon="pi pi-clock" color="warning" hint="Pendientes de pago" />
        <StatCard label="Matriculados" :value="dist.matriculados" icon="pi pi-check-circle" color="success" hint="Pagaron matrícula" />
        <StatCard label="Cursando" :value="dist.cursando" icon="pi pi-book" color="info" hint="Con clases activas" />
        <StatCard label="Completados" :value="dist.completados" icon="pi pi-verified" color="brand" hint="Curso terminado" />
        <StatCard label="Retirados" :value="dist.retirados" icon="pi pi-times-circle" color="danger" hint="Baja del sistema" />
      </div>
    </div>

    <!-- ================== SECCION 3: OPERATIVO ================== -->
    <div>
      <h3 class="heading-3 mb-3 flex items-center gap-2">
        <i class="pi pi-cog text-brand-700" />
        Operativo
      </h3>
      <div class="grid grid-cols-2 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Instructores"
          :value="kpis.instructoresActivos"
          icon="pi pi-id-card"
          color="info"
          :hint="`${kpis.instructoresTotal} total · ${kpis.instructoresTotal - kpis.instructoresActivos} inactivos`"
        />
        <StatCard
          label="Vehículos disponibles"
          :value="kpis.vehiculosActivos"
          icon="pi pi-car"
          color="success"
          :hint="`${kpis.vehiculosTotal} total en flota`"
        />
        <StatCard
          label="SOAT por vencer (30d)"
          :value="kpis.soatPorVencer"
          icon="pi pi-shield"
          :color="kpis.soatPorVencer > 0 ? 'warning' : 'success'"
          :hint="kpis.soatVencido > 0 ? `${kpis.soatVencido} ya vencido(s)` : 'Flota al día'"
        />
        <StatCard
          label="Facturas en crédito"
          :value="kpis.facturasCredito"
          icon="pi pi-credit-card"
          color="brand"
          :hint="`${kpis.facturasContado} en contado`"
        />
      </div>
    </div>

    <!-- ================== SECCION 4: CLASES + ACCESOS ================== -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-5">
      <DataTableCard
        title="Clases programadas para hoy"
        description="Asignaciones del día de hoy"
        class="xl:col-span-2"
      >
        <template #toolbar>
          <Button label="Ver calendario" icon="pi pi-calendar" text @click="$router.push('/asignaciones')" />
        </template>
        <div v-if="clasesHoy.length === 0">
          <EmptyState
            icon="pi pi-calendar-times"
            title="Sin clases para hoy"
            description="Cuando se programen asignaciones aparecerán acá."
          >
            <template #action>
              <Button label="Crear asignación" icon="pi pi-plus" @click="$router.push('/asignaciones')" />
            </template>
          </EmptyState>
        </div>
        <ul v-else class="divide-y divide-ink-200">
          <li v-for="c in clasesHoy" :key="c.id" class="flex items-center gap-4 px-6 py-3 hover:bg-ink-50/50 transition">
            <div class="w-14 text-center flex-shrink-0">
              <p class="text-lg font-bold text-brand-700">{{ c.hora }}</p>
              <p class="text-[10px] text-ink-500 uppercase">{{ c.duracion }}min</p>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-ink-900 truncate">
                {{ c.estudianteNombre }} <span class="text-ink-400 font-normal">con</span> {{ c.instructorNombre }}
              </p>
              <p class="text-xs text-ink-500 truncate">
                <i class="pi pi-car text-[10px]" /> {{ c.vehiculoPlaca }} · Clase #{{ c.id }}
              </p>
            </div>
            <StatusBadge :status="c.estado" />
          </li>
        </ul>
      </DataTableCard>

      <section class="card animate-fade-up p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-bolt text-brand-700" />
          Accesos rápidos
        </h3>
        <div class="space-y-2">
          <QuickAction icon="pi-user-plus"     label="Matricular estudiante" to="/estudiantes/nuevo" />
          <QuickAction icon="pi-id-card"       label="Gestionar instructores" to="/instructores" />
          <QuickAction icon="pi-car"           label="Gestionar vehículos"    to="/vehiculos" />
          <QuickAction icon="pi-calendar-plus" label="Programar clase"        to="/asignaciones" />
          <QuickAction icon="pi-file-edit"     label="Emitir factura"         to="/cobros" />
          <QuickAction icon="pi-cog"           label="Configuración escuela"  to="/configuracion" v-if="esAdmin" />
        </div>
      </section>
    </div>

    <!-- ================== SECCION 5: ESTUDIANTES RECIENTES ================== -->
    <DataTableCard
      title="Estudiantes recientes"
      description="Últimos estudiantes registrados en el sistema"
    >
      <template #toolbar>
        <Button label="Ver todos" icon="pi pi-arrow-right" iconPos="right" text @click="$router.push('/estudiantes')" />
      </template>
      <DataTable :value="ultimosEstudiantes" striped-rows :loading="loading" :pt="{ table: { style: 'min-width: 50rem' } }">
        <Column header="Estudiante">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <Avatar :name="data.nombreCompleto || `${data.nombre} ${data.apellido}`" size="sm" />
              <div>
                <p class="text-sm font-medium text-ink-900">{{ data.nombreCompleto || `${data.nombre} ${data.apellido}` }}</p>
                <p class="text-xs text-ink-500">{{ data.email }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column field="cedula" header="Cédula" />
        <Column field="telefono" header="Teléfono" />
        <Column header="Estado académico">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="Situación pago">
          <template #body="{ data }">
            <StatusBadge :status="data.situacionPago" />
          </template>
        </Column>
      </DataTable>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import { useAuthStore } from '@/stores/auth'
import { useDashboardStore } from '@/stores/dashboard'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()
const dashboardStore = useDashboardStore()
const esAdmin = computed(() => authStore.currentRole === 'ADMIN')
const esInstructor = computed(() => authStore.currentRole === 'INSTRUCTOR')
const esEstudiante = computed(() => authStore.currentRole === 'ESTUDIANTE')
const instructorNombre = computed(() => {
  const u: any = authStore.user
  return u?.nombreCompleto || `${u?.nombre ?? ''} ${u?.apellido ?? ''}`.trim() || 'Instructor'
})
const estudianteNombre = computed(() => {
  const u: any = authStore.user
  return u?.nombreCompleto || `${u?.nombre ?? ''} ${u?.apellido ?? ''}`.trim() || 'Estudiante'
})

// KPIs y datos para vista de ESTUDIANTE
const kpisEst = reactive({
  horasCompletadas: 0,
  horasRequeridas: 0,
  horasFaltantes: 0,
  porcentajeAvance: 0,
  clasesProgramadas: 0,
  clasesCompletadas: 0,
  clasesCanceladas: 0,
  proximaClaseFecha: '',
  proximaClaseHora: '',
  saldoPendiente: 0,
  totalFacturado: 0,
  totalPagado: 0,
  facturasPendientes: 0
})
const datosEst = reactive<{ estado: string; tipoCursoNombre: string; categoriaLicencia: string; fechaMatricula: string }>({
  estado: '',
  tipoCursoNombre: '',
  categoriaLicencia: '',
  fechaMatricula: ''
})
const proximasClasesEst = ref<any[]>([])
const ultimasClasesEst = ref<any[]>([])

// KPIs para vista de INSTRUCTOR
const kpisInst = reactive({
  clasesHoy: 0,
  clasesEnCurso: 0,
  clasesCompletadasHoy: 0,
  clasesSemana: 0,
  estudiantesActivos: 0,
  estudiantesTotal: 0,
  minutosMes: 0
})
const misClasesHoy = ref<any[]>([])
const proximasClases = ref<any[]>([])

const formatearFechaCorta = (f: string) => {
  if (!f) return '—'
  try {
    const d = new Date(f + 'T00:00:00')
    return d.toLocaleDateString('es-EC', { weekday: 'short', day: 'numeric', month: 'short' })
  } catch { return f }
}

const QuickAction = defineComponent({
  props: ['icon', 'label', 'to'],
  setup(props) {
    const r = useRouter()
    return () =>
      h('button',
        {
          class: 'w-full flex items-center gap-3 p-3 rounded-lg border border-ink-200 hover:border-brand-400 hover:bg-brand-50/50 transition group text-left',
          onClick: () => r.push(props.to)
        },
        [
          h('div', { class: 'w-9 h-9 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center group-hover:bg-brand-100 transition' },
            [h('i', { class: `pi ${props.icon}` })]
          ),
          h('span', { class: 'text-sm font-medium text-ink-700 flex-1' }, props.label),
          h('i', { class: 'pi pi-angle-right text-ink-400 group-hover:text-brand-600 group-hover:translate-x-0.5 transition' })
        ]
      )
  }
})

const loading = ref(false)

// KPIs principales
const kpis = reactive({
  estudiantesActivos: 0,
  estudiantesTotal: 0,
  instructoresActivos: 0,
  instructoresTotal: 0,
  vehiculosActivos: 0,
  vehiculosTotal: 0,
  soatPorVencer: 0,
  soatVencido: 0,
  ingresosMes: 0,
  facturasPagadasMes: 0,
  saldoPorCobrar: 0,
  facturasPendientes: 0,
  facturasContado: 0,
  facturasCredito: 0,
  clasesHoy: 0,
  clasesSemana: 0
})

// Distribución académica
const dist = reactive({
  preMatriculados: 0,
  matriculados: 0,
  cursando: 0,
  completados: 0,
  retirados: 0
})

const clasesHoy = ref<any[]>([])
const ultimosEstudiantes = ref<any[]>([])

// Alertas computadas
const alertas = computed(() => {
  const list: any[] = []
  if (kpis.soatVencido > 0) {
    list.push({
      count: kpis.soatVencido,
      label: kpis.soatVencido === 1 ? 'vehículo con SOAT vencido' : 'vehículos con SOAT vencido',
      icon: 'pi-shield',
      to: '/vehiculos',
      severity: 'danger'
    })
  }
  if (kpis.soatPorVencer > 0) {
    list.push({
      count: kpis.soatPorVencer,
      label: kpis.soatPorVencer === 1 ? 'SOAT por vencer en 30 días' : 'SOAT por vencer en 30 días',
      icon: 'pi-clock',
      to: '/vehiculos',
      severity: 'warning'
    })
  }
  if (kpis.facturasPendientes > 0 && kpis.saldoPorCobrar > 0) {
    list.push({
      count: kpis.facturasPendientes,
      label: kpis.facturasPendientes === 1 ? 'factura por cobrar' : 'facturas por cobrar',
      icon: 'pi-wallet',
      to: '/cobros',
      severity: 'warning'
    })
  }
  return list
})

const formatMoney = (n: number) =>
  `$${(n ?? 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

// =============================================================
// Cargar datos para vista INSTRUCTOR
// =============================================================
const cargarInstructor = async () => {
  // Asegurar que tenemos el instructorId
  if (!authStore.currentInstructorId) {
    await authStore.loadInstructorId()
  }
  const miId = authStore.currentInstructorId
  if (!miId) {
    console.warn('Usuario INSTRUCTOR sin instructor asociado')
    return
  }

  const [asigRes, estRes, vehRes] = await Promise.allSettled([
    api.get('/asignaciones', { params: { size: 500 } }),
    api.get('/estudiantes',  { params: { size: 200 } }),
    api.get('/vehiculos',    { params: { size: 100 } })
  ])

  const asignaciones = asigRes.status === 'fulfilled'
    ? (asigRes.value.data.content || []).filter((a: any) => a.instructorId === miId)
    : []
  const estudiantes = estRes.status === 'fulfilled' ? (estRes.value.data.content || []) : []
  const vehiculos = vehRes.status === 'fulfilled' ? (vehRes.value.data.content || []) : []

  const mapEst = new Map(estudiantes.map((e: any) => [
    e.id, e.nombreCompleto || `${e.nombre ?? ''} ${e.apellido ?? ''}`.trim()
  ]))
  const mapVeh = new Map(vehiculos.map((v: any) => [v.id, v.placa || `Veh #${v.id}`]))

  const hoy = new Date()
  const hoyStr = hoy.toISOString().substring(0, 10)
  const inicioSemana = new Date(hoy); inicioSemana.setDate(hoy.getDate() - hoy.getDay()); inicioSemana.setHours(0, 0, 0, 0)
  const finSemana = new Date(inicioSemana); finSemana.setDate(inicioSemana.getDate() + 7)
  const inicioMes = new Date(); inicioMes.setDate(1); inicioMes.setHours(0, 0, 0, 0)

  // KPIs
  const hoyArr = asignaciones.filter((a: any) => a.fecha === hoyStr)
  kpisInst.clasesHoy = hoyArr.length
  kpisInst.clasesEnCurso = hoyArr.filter((a: any) => a.estado === 'EN_CURSO').length
  kpisInst.clasesCompletadasHoy = hoyArr.filter((a: any) => a.estado === 'COMPLETADA').length
  kpisInst.clasesSemana = asignaciones.filter((a: any) => {
    const d = new Date((a.fecha || '') + 'T' + (a.horaInicio || '00:00'))
    return d >= inicioSemana && d < finSemana
  }).length

  const estIdsAsignados = new Set(asignaciones.map((a: any) => a.estudianteId))
  kpisInst.estudiantesTotal = estIdsAsignados.size
  kpisInst.estudiantesActivos = estudiantes.filter((e: any) =>
    estIdsAsignados.has(e.id) && (e.estado === 'CURSANDO' || e.estado === 'MATRICULADO')
  ).length

  // Minutos del mes = duracion (en min) de clases COMPLETADAS del mes
  kpisInst.minutosMes = asignaciones
    .filter((a: any) => a.estado === 'COMPLETADA')
    .filter((a: any) => {
      const d = new Date((a.fecha || '') + 'T' + (a.horaInicio || '00:00'))
      return d >= inicioMes
    })
    .reduce((acc: number, a: any) => {
      if (a.horaInicio && a.horaFin) {
        const [hi, mi] = a.horaInicio.substring(0, 5).split(':').map(Number)
        const [hf, mf] = a.horaFin.substring(0, 5).split(':').map(Number)
        return acc + ((hf * 60 + mf) - (hi * 60 + mi))
      }
      return acc + (a.duracionMinutos || 60)
    }, 0)

  // Mis clases de hoy con datos enriquecidos
  misClasesHoy.value = hoyArr
    .map((a: any) => ({
      id: a.id,
      hora: (a.horaInicio || '').substring(0, 5) || '--:--',
      duracion: (() => {
        if (!a.horaInicio || !a.horaFin) return a.duracionMinutos || 60
        const [hi, mi] = a.horaInicio.substring(0, 5).split(':').map(Number)
        const [hf, mf] = a.horaFin.substring(0, 5).split(':').map(Number)
        return (hf * 60 + mf) - (hi * 60 + mi)
      })(),
      estudianteNombre: mapEst.get(a.estudianteId) || `Estudiante #${a.estudianteId}`,
      vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`,
      estado: a.estado,
      processing: false
    }))
    .sort((a: any, b: any) => a.hora.localeCompare(b.hora))

  // Proximas 5 clases (futuras, NO de hoy)
  proximasClases.value = asignaciones
    .filter((a: any) => {
      if (!a.fecha) return false
      return a.fecha > hoyStr
    })
    .sort((a: any, b: any) => {
      const ka = (a.fecha || '') + 'T' + (a.horaInicio || '00:00')
      const kb = (b.fecha || '') + 'T' + (b.horaInicio || '00:00')
      return ka.localeCompare(kb)
    })
    .slice(0, 5)
    .map((a: any) => ({
      id: a.id,
      fecha: a.fecha,
      hora: (a.horaInicio || '').substring(0, 5) || '--:--',
      duracion: a.duracionMinutos || 60,
      estudianteNombre: mapEst.get(a.estudianteId) || `Estudiante #${a.estudianteId}`,
      vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`,
      estado: a.estado
    }))
}

// Acciones del instructor sobre sus clases
const iniciarClase = async (id: number) => {
  const c = misClasesHoy.value.find(x => x.id === id)
  if (!c) return
  c.processing = true
  try {
    await api.post(`/asignaciones/${id}/iniciar`, { kmInicial: 0 })
    await cargar()
  } catch (e: any) {
    console.error('Error iniciando clase', e?.response?.data || e)
    alert(e?.response?.data?.detail || 'No se pudo iniciar la clase')
  } finally {
    if (c) c.processing = false
  }
}
const finalizarClase = async (id: number) => {
  const c = misClasesHoy.value.find(x => x.id === id)
  if (!c) return
  const kmFinalStr = window.prompt('Ingresa el kilometraje final del vehículo:')
  if (!kmFinalStr) return
  const kmFinal = parseInt(kmFinalStr, 10)
  if (isNaN(kmFinal) || kmFinal < 0) {
    alert('Kilometraje inválido')
    return
  }
  c.processing = true
  try {
    await api.post(`/asignaciones/${id}/finalizar`, { kmFinal })
    await cargar()
  } catch (e: any) {
    console.error('Error finalizando clase', e?.response?.data || e)
    alert(e?.response?.data?.detail || 'No se pudo finalizar la clase')
  } finally {
    if (c) c.processing = false
  }
}

// =============================================================
// Cargar datos para vista ADMIN / STAFF
// =============================================================
const cargarAdmin = async () => {
  // Intentar cargar KPIs desde reportes (Sprint 11)
  try {
    await dashboardStore.obtenerKPIs()
  } catch (err) {
    console.log('KPIs no disponibles aún', err)
  }

  const [est, ins, veh, asig, fact] = await Promise.allSettled([
      api.get('/estudiantes', { params: { size: 200 } }),
      api.get('/instructores', { params: { size: 200 } }),
      api.get('/vehiculos', { params: { size: 200 } }),
      api.get('/asignaciones', { params: { size: 200 } }),
      api.get('/facturas', { params: { size: 200 } })
    ])

    // ========== ESTUDIANTES ==========
    let estudiantesData: any[] = []
    if (est.status === 'fulfilled') {
      estudiantesData = est.value.data.content || est.value.data || []
      kpis.estudiantesTotal = est.value.data.totalElements ?? estudiantesData.length
      kpis.estudiantesActivos = estudiantesData.filter(
        (e: any) => e.estado === 'MATRICULADO' || e.estado === 'CURSANDO'
      ).length

      // Distribución
      dist.preMatriculados = estudiantesData.filter(e => e.estado === 'PRE_MATRICULADO').length
      dist.matriculados = estudiantesData.filter(e => e.estado === 'MATRICULADO').length
      dist.cursando = estudiantesData.filter(e => e.estado === 'CURSANDO').length
      dist.completados = estudiantesData.filter(e => e.estado === 'COMPLETADO').length
      dist.retirados = estudiantesData.filter(e => e.estado === 'RETIRADO').length

      // Últimos 5 por id descendente
      ultimosEstudiantes.value = [...estudiantesData]
        .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
        .slice(0, 5)
    }

    // ========== INSTRUCTORES ==========
    let instructoresData: any[] = []
    if (ins.status === 'fulfilled') {
      instructoresData = ins.value.data.content || ins.value.data || []
      kpis.instructoresTotal = ins.value.data.totalElements ?? instructoresData.length
      kpis.instructoresActivos = instructoresData.filter((i: any) => i.estado === 'ACTIVO').length
    }

    // ========== VEHICULOS + SOAT ==========
    let vehiculosData: any[] = []
    if (veh.status === 'fulfilled') {
      vehiculosData = veh.value.data.content || veh.value.data || []
      kpis.vehiculosTotal = veh.value.data.totalElements ?? vehiculosData.length
      kpis.vehiculosActivos = vehiculosData.filter((v: any) => v.estado === 'ACTIVO').length

      // SOAT por vencer (próximos 30 días) y vencidos
      const hoy = new Date()
      const limite = new Date()
      limite.setDate(hoy.getDate() + 30)
      kpis.soatPorVencer = vehiculosData.filter((v: any) => {
        if (!v.soatVencimiento) return false
        const fecha = new Date(v.soatVencimiento)
        return fecha >= hoy && fecha <= limite
      }).length
      kpis.soatVencido = vehiculosData.filter((v: any) => {
        if (!v.soatVencimiento) return false
        return new Date(v.soatVencimiento) < hoy
      }).length
    }

    // ========== FACTURAS ==========
    if (fact.status === 'fulfilled') {
      const facturas = fact.value.data.content || fact.value.data || []
      const inicioMes = new Date()
      inicioMes.setDate(1)
      inicioMes.setHours(0, 0, 0, 0)

      kpis.facturasContado = facturas.filter((f: any) => f.tipoPago === 'CONTADO').length
      kpis.facturasCredito = facturas.filter((f: any) => f.tipoPago === 'CREDITO').length

      // Ingresos del mes = facturas pagadas en el mes actual
      const pagadasMes = facturas.filter((f: any) => {
        const estado = (f.estado || '').toUpperCase()
        if (estado !== 'PAGADA' && estado !== 'PAGADO') return false
        const fecha = f.fechaPago || f.fechaEmision
        if (!fecha) return false
        return new Date(fecha) >= inicioMes
      })
      kpis.facturasPagadasMes = pagadasMes.length
      kpis.ingresosMes = pagadasMes.reduce(
        (s: number, f: any) => s + parseFloat(f.montoPagado ?? f.montoOriginal ?? 0),
        0
      )

      // Saldo por cobrar = suma de saldos de facturas PENDIENTE/PARCIAL/VENCIDA
      const pendientes = facturas.filter((f: any) => {
        const estado = (f.estado || '').toUpperCase()
        return estado === 'PENDIENTE' || estado === 'PARCIAL' || estado === 'VENCIDA'
      })
      kpis.facturasPendientes = pendientes.length
      kpis.saldoPorCobrar = pendientes.reduce(
        (s: number, f: any) => s + parseFloat(f.saldo ?? f.montoOriginal ?? 0),
        0
      )
    }

    // ========== ASIGNACIONES ==========
    if (asig.status === 'fulfilled') {
      const asignaciones = asig.value.data.content || asig.value.data || []
      const hoy = new Date()
      const hoyStr = hoy.toISOString().substring(0, 10)
      const inicioSemana = new Date(hoy)
      inicioSemana.setDate(hoy.getDate() - hoy.getDay()) // domingo
      inicioSemana.setHours(0, 0, 0, 0)
      const finSemana = new Date(inicioSemana)
      finSemana.setDate(inicioSemana.getDate() + 7)

      kpis.clasesSemana = asignaciones.filter((a: any) => {
        const fecha = new Date((a.fecha || '') + 'T' + (a.horaInicio || '00:00'))
        return fecha >= inicioSemana && fecha < finSemana
      }).length

      // Maps para lookup de nombres
      const mapEst = new Map(estudiantesData.map((e: any) => [
        e.id,
        e.nombreCompleto || `${e.nombre ?? ''} ${e.apellido ?? ''}`.trim()
      ]))
      const mapIns = new Map(instructoresData.map((i: any) => [
        i.id,
        i.nombreCompleto || `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim()
      ]))
      const mapVeh = new Map(vehiculosData.map((v: any) => [
        v.id,
        v.placa || `Vehículo #${v.id}`
      ]))

      const clasesDelDia = asignaciones
        .filter((a: any) => a.fecha === hoyStr)
        .map((a: any) => {
          const hi = (a.horaInicio || '').substring(0, 5)
          const hf = (a.horaFin || '').substring(0, 5)
          // duración derivada (en min) entre horaInicio y horaFin si vienen
          let dur = a.duracionMinutos
          if (!dur && hi && hf) {
            const [h1, m1] = hi.split(':').map(Number)
            const [h2, m2] = hf.split(':').map(Number)
            dur = (h2 * 60 + m2) - (h1 * 60 + m1)
          }
          return {
            id: a.id,
            hora: hi || '--:--',
            duracion: dur || 60,
            estudianteNombre: mapEst.get(a.estudianteId) || `Estudiante #${a.estudianteId}`,
            instructorNombre: mapIns.get(a.instructorId) || `Instructor #${a.instructorId}`,
            vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`,
            estado: a.estado
          }
        })
        .sort((a: any, b: any) => a.hora.localeCompare(b.hora))

      kpis.clasesHoy = clasesDelDia.length
      clasesHoy.value = clasesDelDia.slice(0, 8)
    }
}

// =============================================================
// Cargar datos para vista ESTUDIANTE
// =============================================================
const cargarEstudiante = async () => {
  // Asegurar que tenemos el estudianteId
  if (!authStore.currentEstudianteId) {
    await authStore.loadEstudianteId()
  }
  const miId = authStore.currentEstudianteId
  if (!miId) {
    console.warn('Usuario ESTUDIANTE sin registro de estudiante asociado')
    return
  }

  const [meRes, asigRes, facRes, pagRes, instRes, vehRes, cursosRes, catsRes] = await Promise.allSettled([
    api.get('/estudiantes/me'),
    api.get('/asignaciones', { params: { size: 500, estudianteId: miId } }),
    api.get('/facturas',     { params: { size: 200, estudianteId: miId } }),
    api.get('/pagos',        { params: { size: 200, estudianteId: miId } }),
    api.get('/instructores', { params: { size: 100 } }),
    api.get('/vehiculos',    { params: { size: 100 } }),
    api.get('/tipos-curso'),
    api.get('/categorias-licencia')
  ])

  const me = meRes.status === 'fulfilled' ? meRes.value.data : null
  // /asignaciones del backend no acepta filtro estudianteId — filtramos aquí
  const asignaciones = asigRes.status === 'fulfilled'
    ? (asigRes.value.data.content || []).filter((a: any) => a.estudianteId === miId)
    : []
  const facturas = facRes.status === 'fulfilled'
    ? (facRes.value.data.content || []).filter((f: any) => f.estudianteId === miId)
    : []
  const pagos = pagRes.status === 'fulfilled'
    ? (pagRes.value.data.content || []).filter((p: any) => p.estudianteId === miId)
    : []
  const instructores = instRes.status === 'fulfilled' ? (instRes.value.data.content || []) : []
  const vehiculos = vehRes.status === 'fulfilled' ? (vehRes.value.data.content || []) : []
  const cursos = cursosRes.status === 'fulfilled'
    ? (Array.isArray(cursosRes.value.data) ? cursosRes.value.data : (cursosRes.value.data.content || []))
    : []
  const categorias = catsRes.status === 'fulfilled'
    ? (Array.isArray(catsRes.value.data) ? catsRes.value.data : (catsRes.value.data.content || []))
    : []

  const mapInst = new Map(instructores.map((i: any) => [
    i.id, i.nombreCompleto || `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim()
  ]))
  const mapVeh = new Map(vehiculos.map((v: any) => [v.id, v.placa || `Veh #${v.id}`]))

  // Estado del estudiante (de /estudiantes/me)
  if (me) {
    datosEst.estado = me.estado || ''
    datosEst.fechaMatricula = me.fechaMatricula || ''
    const curso = cursos.find((c: any) => c.id === me.tipoCursoId)
    datosEst.tipoCursoNombre = curso?.nombre || ''
    const cat = categorias.find((c: any) => c.id === me.categoriaLicenciaId)
    datosEst.categoriaLicencia = cat?.codigo || ''

    // El total requerido viene del curso (duracionTotalHoras, en horas).
    // El acumulado del estudiante esta en minutosCompletados.
    const horasRequeridas = Number(curso?.duracionTotalHoras || 0)
    const totalMin = horasRequeridas * 60
    const completadosMin = Number(me.minutosCompletados || 0)
    kpisEst.horasCompletadas = completadosMin / 60
    kpisEst.horasRequeridas = horasRequeridas
    kpisEst.horasFaltantes = Math.max(0, kpisEst.horasRequeridas - kpisEst.horasCompletadas)
    kpisEst.porcentajeAvance = totalMin > 0
      ? Math.min(100, (completadosMin / totalMin) * 100)
      : 0
  }

  const hoyStr = new Date().toISOString().substring(0, 10)

  kpisEst.clasesProgramadas = asignaciones.filter((a: any) =>
    (a.estado === 'PROGRAMADA' || a.estado === 'CONFIRMADA' || a.estado === 'EN_CURSO')
    && a.fecha >= hoyStr
  ).length
  kpisEst.clasesCompletadas = asignaciones.filter((a: any) => a.estado === 'COMPLETADA').length
  kpisEst.clasesCanceladas = asignaciones.filter((a: any) => a.estado === 'CANCELADA' || a.estado === 'AUSENTE').length

  // Financiero
  kpisEst.totalFacturado = facturas.reduce((s: number, f: any) => s + Number(f.montoOriginal || 0), 0)
  kpisEst.totalPagado = pagos.reduce((s: number, p: any) => s + Number(p.monto || 0), 0)
  kpisEst.saldoPendiente = Math.max(0, kpisEst.totalFacturado - kpisEst.totalPagado)
  kpisEst.facturasPendientes = facturas.filter((f: any) =>
    f.estado === 'PENDIENTE' || f.estado === 'PARCIAL' || f.estado === 'VENCIDA'
  ).length

  // Próximas clases
  const proxs = asignaciones
    .filter((a: any) => {
      if (!a.fecha) return false
      if (a.estado === 'CANCELADA' || a.estado === 'COMPLETADA' || a.estado === 'AUSENTE') return false
      return a.fecha >= hoyStr
    })
    .sort((a: any, b: any) => {
      const ka = (a.fecha || '') + 'T' + (a.horaInicio || '00:00')
      const kb = (b.fecha || '') + 'T' + (b.horaInicio || '00:00')
      return ka.localeCompare(kb)
    })

  proximasClasesEst.value = proxs.slice(0, 5).map((a: any) => ({
    id: a.id,
    fecha: a.fecha,
    hora: (a.horaInicio || '').substring(0, 5) || '--:--',
    duracion: a.duracionMinutos || 60,
    instructorNombre: mapInst.get(a.instructorId) || `Instructor #${a.instructorId}`,
    vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`,
    estado: a.estado
  }))

  if (proxs[0]) {
    kpisEst.proximaClaseFecha = formatearFechaCorta(proxs[0].fecha)
    kpisEst.proximaClaseHora = (proxs[0].horaInicio || '').substring(0, 5)
  } else {
    kpisEst.proximaClaseFecha = ''
    kpisEst.proximaClaseHora = ''
  }

  // Últimas clases realizadas (completadas, ordenadas desc por fecha)
  ultimasClasesEst.value = asignaciones
    .filter((a: any) => a.estado === 'COMPLETADA')
    .sort((a: any, b: any) => {
      const ka = (a.fecha || '') + 'T' + (a.horaInicio || '00:00')
      const kb = (b.fecha || '') + 'T' + (b.horaInicio || '00:00')
      return kb.localeCompare(ka)
    })
    .slice(0, 8)
    .map((a: any) => {
      let duracion = a.duracionMinutos || 60
      if (a.horaInicio && a.horaFin) {
        const [hi, mi] = a.horaInicio.substring(0, 5).split(':').map(Number)
        const [hf, mf] = a.horaFin.substring(0, 5).split(':').map(Number)
        duracion = (hf * 60 + mf) - (hi * 60 + mi)
      }
      return {
        id: a.id,
        fecha: a.fecha,
        hora: (a.horaInicio || '').substring(0, 5) || '--:--',
        duracion,
        instructorNombre: mapInst.get(a.instructorId) || `Instructor #${a.instructorId}`,
        vehiculoPlaca: mapVeh.get(a.vehiculoId) || `#${a.vehiculoId}`,
        estado: a.estado
      }
    })
}

// =============================================================
// Dispatcher: elige rama segun el rol activo
// =============================================================
const cargar = async () => {
  loading.value = true
  try {
    if (esInstructor.value) {
      await cargarInstructor()
    } else if (esEstudiante.value) {
      await cargarEstudiante()
    } else {
      await cargarAdmin()
    }
  } finally {
    loading.value = false
  }
}

onMounted(cargar)
</script>
