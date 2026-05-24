<template>
  <div class="space-y-6">
    <PageHeader
      title="Cobros y facturación"
      description="Gestión de facturas (contado / crédito) y pagos recibidos."
      icon="pi pi-wallet"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Cobros' }]"
    >
      <template #actions>
        <Button
          v-tooltip.bottom="'Recalcula situacion_pago de todos los estudiantes consultando MS-Cobros'"
          label="Sincronizar"
          icon="pi pi-refresh"
          severity="secondary" outlined
          :loading="sincronizando"
          @click="sincronizarSituacion"
        />
        <Button label="Registrar pago" icon="pi pi-dollar" outlined @click="abrirFormPago()" />
        <Button label="Nueva factura" icon="pi pi-plus" @click="abrirFormFactura()" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total facturado" :value="formatMoney(stats.totalFacturado)" icon="pi pi-file-edit" color="brand" />
      <StatCard label="Total pagado"    :value="formatMoney(stats.totalPagado)"    icon="pi pi-check-circle" color="success" />
      <StatCard label="Saldo pendiente" :value="formatMoney(stats.saldoPendiente)" icon="pi pi-clock" color="warning" />
      <StatCard label="Facturas activas" :value="stats.facturas" icon="pi pi-receipt" color="info" :hint="`${stats.pagos} pagos registrados`" />
    </div>

    <!-- ====== ESTUDIANTES POR SITUACIÓN DE PAGO ====== -->
    <div class="card overflow-hidden">
      <div class="px-5 pt-5 pb-2 flex items-center justify-between flex-wrap gap-3">
        <div>
          <h3 class="heading-3">Estudiantes por estado de pago</h3>
          <p class="text-xs text-ink-500 mt-0.5">
            Identifica de un vistazo a quién cobrar matrícula, quién tiene saldo y quién ya pagó todo.
          </p>
        </div>
      </div>

      <div class="flex border-b border-ink-200 bg-ink-50/40 mt-2">
        <button
          v-for="t in tabsEstudiantes"
          :key="t.key"
          @click="tabEst = t.key"
          :class="['flex-1 px-4 py-3 text-sm font-medium transition border-b-2 flex items-center justify-center gap-2',
            tabEst === t.key
              ? 'border-brand-600 text-brand-700 bg-white'
              : 'border-transparent text-ink-600 hover:bg-ink-100/60']"
        >
          <i :class="['pi', t.icon]" />
          <span>{{ t.label }}</span>
          <span :class="['inline-flex items-center justify-center min-w-[20px] h-5 px-1.5 rounded-full text-[10px] font-bold',
            tabEst === t.key ? t.activeBadge : 'bg-ink-200 text-ink-600']">
            {{ estudiantesPorTab(t.key).length }}
          </span>
        </button>
      </div>

      <div class="p-5">
        <EmptyState
          v-if="estudiantesPorTab(tabEst).length === 0"
          :icon="tabEst === 'sin_pagar' ? 'pi-check-circle' : tabEst === 'con_saldo' ? 'pi-thumbs-up' : 'pi-users'"
          :title="tabEst === 'sin_pagar' ? 'Nadie sin facturar' : tabEst === 'con_saldo' ? 'Nadie con saldo pendiente' : 'Nadie al día aún'"
          :description="tabEst === 'sin_pagar' ? 'Todos los estudiantes activos ya tienen facturas emitidas.' : tabEst === 'con_saldo' ? 'Todos los estudiantes pagaron o no tienen facturas.' : 'Los estudiantes que paguen todas sus facturas aparecerán aquí.'"
        />
        <ul v-else class="divide-y divide-ink-200">
          <li
            v-for="est in estudiantesPorTab(tabEst)"
            :key="est.id"
            class="flex items-center gap-4 py-3 hover:bg-ink-50/50 transition rounded-lg px-3 -mx-3"
          >
            <div class="w-10 h-10 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-xs font-semibold flex-shrink-0">
              {{ initials(est.nombreCompleto) }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-ink-900 truncate">{{ est.nombreCompleto }}</p>
              <p class="text-[11px] text-ink-500">{{ est.cedula }} · <StatusBadge :status="est.estado" /></p>
            </div>
            <div class="hidden md:block text-right">
              <p class="text-[10px] uppercase tracking-wider text-ink-500 font-semibold">Situación</p>
              <StatusBadge :status="est.situacionPago || 'SIN_DEUDA'" />
            </div>
            <Button
              :label="tabEst === 'sin_pagar' ? 'Facturar' : tabEst === 'con_saldo' ? 'Cobrar' : 'Ver'"
              :icon="tabEst === 'sin_pagar' ? 'pi pi-plus' : tabEst === 'con_saldo' ? 'pi pi-dollar' : 'pi pi-eye'"
              size="small"
              :outlined="tabEst === 'al_dia'"
              @click="accionEstudiante(est, tabEst)"
            />
          </li>
        </ul>
      </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- ============================= FACTURAS ============================ -->
      <DataTableCard title="Facturas" :description="`${facturas.length} facturas en el sistema`">
        <template #toolbar>
          <Button label="Nueva" icon="pi pi-plus" size="small" @click="abrirFormFactura()" />
        </template>
        <EmptyState
          v-if="facturas.length === 0"
          icon="pi pi-receipt"
          title="Sin facturas emitidas"
          description="Emite la primera factura para empezar a registrar cobros."
        >
          <template #action>
            <Button label="Nueva factura" icon="pi pi-plus" @click="abrirFormFactura()" />
          </template>
        </EmptyState>
        <DataTable v-else :value="facturas" striped-rows :pt="{ table: { style: 'min-width: 30rem' } }">
          <Column header="#">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700 font-semibold">{{ data.numeroFactura }}</span>
            </template>
          </Column>
          <Column header="Estudiante">
            <template #body="{ data }">
              <p class="text-sm">{{ nombreEstudiante(data.estudianteId) }}</p>
              <p class="text-[11px] text-ink-500">ID {{ data.estudianteId }}</p>
            </template>
          </Column>
          <Column header="Monto">
            <template #body="{ data }">
              <p class="text-sm font-semibold">{{ formatMoney(data.montoOriginal) }}</p>
              <p class="text-[11px] text-ink-500">Pagado: {{ formatMoney(data.montoPagado) }}</p>
            </template>
          </Column>
          <Column header="Saldo" style="width: 110px">
            <template #body="{ data }">
              <span
                v-if="saldoFactura(data) > 0"
                class="inline-flex items-center px-2 py-0.5 rounded-md bg-warning-50 text-warning-700 text-xs font-bold border border-warning-500/20"
              >
                {{ formatMoney(saldoFactura(data)) }}
              </span>
              <span v-else class="inline-flex items-center gap-1 text-xs text-success-600 font-medium">
                <i class="pi pi-check-circle text-[10px]" /> Pagada
              </span>
            </template>
          </Column>
          <Column header="Pago">
            <template #body="{ data }">
              <StatusBadge :status="data.tipoPago || 'CONTADO'" />
              <p v-if="data.tipoPago === 'CREDITO'" class="text-[11px] text-ink-500 mt-1">
                {{ data.cuotasPagadas || 0 }}/{{ data.numeroCuotas }} cuotas
              </p>
            </template>
          </Column>
          <Column header="Estado">
            <template #body="{ data }">
              <StatusBadge :status="data.estado" />
            </template>
          </Column>
          <Column header="" style="width: 60px">
            <template #body="{ data }">
              <Button
                v-if="data.tipoPago === 'CREDITO'"
                v-tooltip.left="'Ver cuotas'"
                icon="pi pi-list"
                text rounded
                @click="abrirCuotas(data)"
              />
            </template>
          </Column>
        </DataTable>
      </DataTableCard>

      <!-- ============================= PAGOS ============================ -->
      <DataTableCard title="Pagos recibidos" :description="`${pagos.length} pagos registrados`">
        <EmptyState
          v-if="pagos.length === 0"
          icon="pi pi-dollar"
          title="Sin pagos registrados"
          description="Los pagos aparecerán aquí al ser registrados."
        />
        <DataTable v-else :value="pagos" striped-rows :pt="{ table: { style: 'min-width: 30rem' } }">
          <Column field="id" header="#" style="width: 60px" />
          <Column field="facturaId" header="Factura" style="width: 90px">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700">{{ numFactura(data.facturaId) }}</span>
            </template>
          </Column>
          <Column header="Monto">
            <template #body="{ data }">
              <span class="font-semibold text-success-600">{{ formatMoney(data.monto) }}</span>
              <span v-if="data.numeroCuota" class="ml-2 text-[10px] text-ink-500">cuota {{ data.numeroCuota }}</span>
            </template>
          </Column>
          <Column field="metodoPago" header="Método">
            <template #body="{ data }">
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-ink-100 text-xs font-medium text-ink-700">
                <i :class="metodoIcon(data.metodoPago)" class="text-[10px]" />
                {{ data.metodoPago }}
              </span>
            </template>
          </Column>
          <Column header="Fecha">
            <template #body="{ data }">
              <span class="text-xs text-ink-600">{{ (data.fechaPago || '').substring(0, 10) }}</span>
            </template>
          </Column>
        </DataTable>
      </DataTableCard>
    </div>

    <!-- ============================ DIALOG: NUEVA FACTURA ============================ -->
    <Dialog
      v-model:visible="mostrarFormFactura"
      modal header="Nueva factura"
      :style="{ width: '680px' }"
      :pt="{ content: { class: '!pb-2' } }"
    >
      <div class="space-y-5">
        <div v-if="errF" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errF }}</div>

        <!-- ESTUDIANTE -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-user text-brand-600" /> Estudiante *
            </span>
          </label>
          <AutoComplete
            v-model="selEstudiante"
            :suggestions="estudiantesFiltered"
            @complete="filterEstudiantes"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre, cédula o email..."
            class="w-full"
            :pt="{ input: { class: 'w-full' } }"
          >
            <template #option="{ option }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-9 h-9 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-xs font-semibold">
                  {{ initials(option.nombreCompleto) }}
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-ink-900">{{ option.nombreCompleto }}</p>
                  <p class="text-xs text-ink-500">{{ option.cedula }} · {{ option.email }}</p>
                </div>
                <StatusBadge :status="option.estado" />
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">Sin coincidencias</p>
            </template>
          </AutoComplete>

          <div v-if="selEstudiante" class="mt-3 rounded-lg border border-brand-200 bg-brand-50/40 p-3 animate-fade-up">
            <div class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
              <DetailRow label="Cédula" :value="selEstudiante.cedula" />
              <DetailRow label="Estado académico" :value="humanLabel(selEstudiante.estado)" />
              <DetailRow label="Situación pago" :value="humanLabel(selEstudiante.situacionPago)" />
              <DetailRow label="Email" :value="selEstudiante.email" />
            </div>
          </div>

          <!-- Resumen académico-financiero -->
          <div v-if="cargandoResumen" class="mt-3 rounded-lg bg-ink-50 p-3 text-center text-xs text-ink-500">
            <i class="pi pi-spin pi-spinner mr-2" /> Cargando resumen financiero del estudiante...
          </div>
          <div v-else-if="resumenAcad && resumenAcad.tieneCursoAsignado" class="mt-3 rounded-xl border-2 border-brand-300 bg-gradient-to-br from-brand-50/60 to-brand-50/20 p-4 animate-fade-up">
            <div class="flex items-start justify-between gap-3 mb-3">
              <div>
                <p class="text-[10px] uppercase tracking-wider text-brand-700 font-semibold">Curso contratado</p>
                <p class="text-sm font-bold text-ink-900">{{ resumenAcad.tipoCursoNombre }}</p>
                <p class="text-[11px] text-ink-500 mt-0.5">Categoría {{ resumenAcad.categoriaLicenciaCodigo }} · {{ resumenAcad.duracionHoras }} h</p>
              </div>
              <div class="text-right">
                <p class="text-[10px] uppercase tracking-wider text-ink-500 font-semibold">Precio</p>
                <p class="text-base font-bold text-brand-700">{{ formatMoney(resumenAcad.precioCurso) }}</p>
              </div>
            </div>

            <!-- Barra de progreso -->
            <div class="mb-3">
              <div class="flex items-center justify-between text-[11px] text-ink-600 mb-1.5">
                <span>Pagado <strong>{{ formatMoney(resumenAcad.totalPagado) }}</strong></span>
                <span>Saldo <strong class="text-warning-700">{{ formatMoney(resumenAcad.saldoTotal) }}</strong></span>
              </div>
              <div class="h-2 rounded-full bg-ink-200/80 overflow-hidden">
                <div
                  class="h-full bg-success-500 rounded-full transition-all"
                  :style="{ width: progresoPct + '%' }"
                />
              </div>
              <p class="text-[10px] text-ink-500 mt-1">{{ progresoPct }}% pagado del curso completo</p>
            </div>

            <div class="grid grid-cols-3 gap-2 text-[11px]">
              <div class="rounded-md bg-white/60 px-2 py-1.5 text-center">
                <p class="text-ink-500 uppercase tracking-wider">Facturas</p>
                <p class="font-bold text-ink-900">{{ resumenAcad.cantidadFacturas }}</p>
              </div>
              <div class="rounded-md bg-white/60 px-2 py-1.5 text-center">
                <p class="text-ink-500 uppercase tracking-wider">Facturado</p>
                <p class="font-bold text-ink-900">{{ formatMoney(resumenAcad.totalFacturado) }}</p>
              </div>
              <div class="rounded-md bg-white/60 px-2 py-1.5 text-center">
                <p class="text-ink-500 uppercase tracking-wider">Por facturar</p>
                <p class="font-bold text-info-700">{{ formatMoney(precioRestantePorFacturar) }}</p>
              </div>
            </div>
          </div>
          <div v-else-if="resumenAcad && !resumenAcad.tieneCursoAsignado" class="mt-3 rounded-lg border border-warning-500/30 bg-warning-50/60 p-3 text-sm text-warning-700 flex items-start gap-2 animate-fade-up">
            <i class="pi pi-exclamation-triangle mt-0.5" />
            <span>
              <strong>Este estudiante no tiene tipo de curso asignado.</strong>
              Asígnale uno desde
              <router-link to="/estudiantes" class="font-semibold underline">Estudiantes → Editar</router-link>
              antes de facturar, o factura un monto manual (ej: examen, material extra).
            </span>
          </div>
        </div>

        <!-- CONCEPTO + MONTO -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div class="md:col-span-2">
            <label class="label mb-1.5 block">
              <span class="flex items-center gap-2">
                <i class="pi pi-tag text-brand-600" /> Concepto *
              </span>
            </label>
            <Dropdown
              v-model="formF.conceptoFacturacionId"
              :options="conceptos"
              optionLabel="nombre"
              optionValue="id"
              placeholder="Selecciona un concepto"
              class="w-full"
              @change="onConceptoChange"
            >
              <template #option="{ option }">
                <div class="flex items-center justify-between gap-3 w-full">
                  <span class="text-sm font-medium">{{ option.nombre }}</span>
                  <span class="text-xs text-brand-700 font-semibold">{{ formatMoney(option.montoBase) }}</span>
                </div>
              </template>
            </Dropdown>
          </div>
          <div>
            <label class="label mb-1.5 block">Monto (USD) *</label>
            <InputNumber
              v-model="formF.montoOriginal"
              mode="currency" currency="USD" locale="en-US"
              class="w-full" :min="0.01" :pt="{ input: { class: 'w-full' } }"
            />
          </div>
        </div>

        <!-- TIPO DE PAGO -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-credit-card text-brand-600" /> Modalidad de pago
            </span>
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              @click="formF.tipoPago = 'CONTADO'"
              :class="['flex items-start gap-3 p-4 rounded-lg border-2 text-left transition',
                formF.tipoPago === 'CONTADO'
                  ? 'border-brand-600 bg-brand-50/40 shadow-card'
                  : 'border-ink-200 hover:border-brand-300']"
            >
              <i class="pi pi-wallet text-xl mt-0.5" :class="formF.tipoPago === 'CONTADO' ? 'text-brand-700' : 'text-ink-500'" />
              <div>
                <p class="text-sm font-bold text-ink-900">Contado</p>
                <p class="text-xs text-ink-500 mt-0.5">Pago único, en un solo movimiento.</p>
              </div>
            </button>
            <button
              type="button"
              @click="formF.tipoPago = 'CREDITO'"
              :class="['flex items-start gap-3 p-4 rounded-lg border-2 text-left transition',
                formF.tipoPago === 'CREDITO'
                  ? 'border-brand-600 bg-brand-50/40 shadow-card'
                  : 'border-ink-200 hover:border-brand-300']"
            >
              <i class="pi pi-calendar-plus text-xl mt-0.5" :class="formF.tipoPago === 'CREDITO' ? 'text-brand-700' : 'text-ink-500'" />
              <div>
                <p class="text-sm font-bold text-ink-900">Crédito</p>
                <p class="text-xs text-ink-500 mt-0.5">N cuotas con frecuencia fija.</p>
              </div>
            </button>
          </div>
        </div>

        <!-- CRÉDITO: cuotas + frecuencia + fecha primera cuota + preview -->
        <div v-if="formF.tipoPago === 'CREDITO'" class="rounded-xl border border-accent-200 bg-accent-50/30 p-4 space-y-4 animate-fade-up">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label class="label mb-1.5 block">Nº de cuotas *</label>
              <InputNumber
                v-model="formF.numeroCuotas"
                :min="2" :max="24"
                showButtons buttonLayout="horizontal"
                class="w-full" :pt="{ input: { class: 'w-full text-center' } }"
              />
            </div>
            <div>
              <label class="label mb-1.5 block">Frecuencia *</label>
              <Dropdown
                v-model="formF.frecuenciaCuota"
                :options="[
                  { label: 'Mensual', value: 'MENSUAL' },
                  { label: 'Quincenal', value: 'QUINCENAL' },
                  { label: 'Semanal', value: 'SEMANAL' }
                ]"
                optionLabel="label" optionValue="value"
                class="w-full"
              />
            </div>
            <div>
              <label class="label mb-1.5 block">1ra cuota *</label>
              <Calendar v-model="formF.fechaPrimeraCuota" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
            </div>
          </div>

          <!-- Preview cronograma -->
          <div v-if="cronogramaPreview.length" class="rounded-lg bg-white border border-accent-200/60 overflow-hidden">
            <div class="px-4 py-2 bg-accent-50/40 border-b border-accent-200/60 flex items-center justify-between">
              <p class="text-xs font-semibold text-accent-700 uppercase tracking-wider">Cronograma estimado</p>
              <p class="text-xs text-ink-600">
                <span class="font-semibold">{{ formatMoney(valorCuotaCalc) }}</span> por cuota
              </p>
            </div>
            <div class="divide-y divide-ink-100 max-h-48 overflow-y-auto">
              <div v-for="c in cronogramaPreview" :key="c.numero" class="flex items-center justify-between px-4 py-2 text-xs">
                <span class="font-medium text-ink-700">Cuota {{ c.numero }}</span>
                <span class="text-ink-500">{{ c.fecha }}</span>
                <span class="font-semibold">{{ formatMoney(c.monto) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- FECHA VENCIMIENTO + DESCRIPCIÓN -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label class="label mb-1.5 block">Fecha vencimiento *</label>
            <Calendar v-model="formF.fechaVencimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </div>
          <div>
            <label class="label mb-1.5 block">Descripción</label>
            <InputText v-model="formF.descripcion" placeholder="Ej: Curso categoría B" class="w-full" />
          </div>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormFactura = false" />
        <Button label="Crear factura" icon="pi pi-check" :loading="creandoF" :disabled="!puedeCrearFactura" @click="crearFactura" />
      </template>
    </Dialog>

    <!-- ============================ DIALOG: REGISTRAR PAGO ============================ -->
    <Dialog
      v-model:visible="mostrarFormPago"
      modal header="Registrar pago"
      :style="{ width: '560px' }"
    >
      <div class="space-y-5">
        <div v-if="errP" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errP }}</div>

        <!-- ESTUDIANTE -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2"><i class="pi pi-user text-brand-600" /> Estudiante *</span>
          </label>
          <AutoComplete
            v-model="selEstudiantePago"
            :suggestions="estudiantesFiltered"
            @complete="filterEstudiantes"
            @item-select="onEstudiantePagoSelect"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre, cédula o email..."
            class="w-full"
            :pt="{ input: { class: 'w-full' } }"
          >
            <template #option="{ option }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-9 h-9 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-xs font-semibold">
                  {{ initials(option.nombreCompleto) }}
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium">{{ option.nombreCompleto }}</p>
                  <p class="text-xs text-ink-500">{{ option.cedula }}</p>
                </div>
                <StatusBadge :status="option.estado" />
              </div>
            </template>
          </AutoComplete>
        </div>

        <!-- FACTURA DEL ESTUDIANTE -->
        <div v-if="selEstudiantePago">
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2"><i class="pi pi-receipt text-brand-600" /> Factura pendiente *</span>
          </label>
          <Dropdown
            v-model="formP.facturaId"
            :options="facturasEstudiante"
            optionValue="id"
            optionLabel="numeroFactura"
            placeholder="Selecciona una factura con saldo"
            class="w-full"
            @change="onFacturaPagoChange"
          >
            <template #value="{ value, placeholder }">
              <span v-if="!value" class="text-ink-500">{{ placeholder }}</span>
              <span v-else class="text-sm">
                <span class="font-mono font-semibold text-brand-700">{{ facturaSeleccionadaPago?.numeroFactura }}</span>
                · Saldo {{ formatMoney(saldoFacturaPago) }}
              </span>
            </template>
            <template #option="{ option }">
              <div class="flex items-center justify-between gap-3 w-full">
                <div>
                  <p class="text-sm font-semibold font-mono text-brand-700">{{ option.numeroFactura }}</p>
                  <p class="text-[11px] text-ink-500">
                    {{ option.tipoPago || 'CONTADO' }}
                    <span v-if="option.tipoPago === 'CREDITO'">· {{ option.cuotasPagadas }}/{{ option.numeroCuotas }} cuotas</span>
                  </p>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">{{ formatMoney(option.montoOriginal - option.montoPagado) }}</p>
                  <StatusBadge :status="option.estado" />
                </div>
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">Este estudiante no tiene facturas con saldo pendiente.</p>
            </template>
          </Dropdown>

          <!-- Detalle de la factura seleccionada (crédito → cuota siguiente) -->
          <div v-if="facturaSeleccionadaPago" class="mt-3 rounded-lg border border-brand-200 bg-brand-50/40 p-3 text-xs space-y-1.5 animate-fade-up">
            <div class="flex items-center justify-between">
              <span class="text-ink-500 uppercase tracking-wider text-[10px] font-semibold">Saldo factura</span>
              <span class="font-bold text-base text-ink-900">{{ formatMoney(saldoFacturaPago) }}</span>
            </div>
            <div v-if="facturaSeleccionadaPago.tipoPago === 'CREDITO' && proximaCuota" class="pt-2 border-t border-brand-200/60 flex items-center justify-between">
              <div>
                <p class="text-ink-500 uppercase tracking-wider text-[10px] font-semibold">Próxima cuota</p>
                <p class="font-semibold text-accent-700">Cuota {{ proximaCuota.numeroCuota }} · vence {{ proximaCuota.fechaVencimiento }}</p>
              </div>
              <button
                type="button"
                class="text-xs font-semibold text-brand-700 hover:underline"
                @click="formP.monto = proximaCuota.monto - proximaCuota.montoPagado"
              >
                Usar saldo de la cuota: {{ formatMoney(proximaCuota.monto - proximaCuota.montoPagado) }}
              </button>
            </div>
          </div>
        </div>

        <!-- MONTO + MÉTODO -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label class="label mb-1.5 block">Monto (USD) *</label>
            <InputNumber
              v-model="formP.monto"
              mode="currency" currency="USD" locale="en-US"
              :min="0.01" class="w-full" :pt="{ input: { class: 'w-full' } }"
            />
          </div>
          <div>
            <label class="label mb-1.5 block">Método de pago *</label>
            <Dropdown
              v-model="formP.metodoPago"
              :options="[
                { label: 'Efectivo', value: 'EFECTIVO' },
                { label: 'Transferencia', value: 'TRANSFERENCIA' },
                { label: 'Tarjeta', value: 'TARJETA' }
              ]"
              optionLabel="label" optionValue="value" class="w-full"
            />
          </div>
        </div>

        <div>
          <label class="label mb-1.5 block">Referencia / Observaciones</label>
          <InputText v-model="formP.observaciones" placeholder="Comprobante, nº transacción..." class="w-full" />
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormPago = false" />
        <Button label="Registrar pago" icon="pi pi-check" :loading="creandoP" :disabled="!puedeCrearPago" @click="crearPago" />
      </template>
    </Dialog>

    <!-- ============================ DIALOG: CUOTAS DE FACTURA ============================ -->
    <Dialog
      v-model:visible="mostrarCuotas"
      modal :header="`Cronograma · ${facturaCuotasDetalle?.numeroFactura || ''}`"
      :style="{ width: '640px' }"
    >
      <div v-if="cargandoCuotas" class="py-8 text-center text-ink-500">
        <i class="pi pi-spin pi-spinner text-2xl" />
        <p class="text-sm mt-2">Cargando cuotas...</p>
      </div>
      <div v-else class="space-y-4">
        <div class="grid grid-cols-3 gap-3">
          <div class="rounded-lg bg-ink-100/60 p-3">
            <p class="text-[10px] uppercase tracking-wider text-ink-500 font-semibold">Total factura</p>
            <p class="text-base font-bold mt-0.5">{{ formatMoney(facturaCuotasDetalle?.montoOriginal) }}</p>
          </div>
          <div class="rounded-lg bg-success-50 p-3">
            <p class="text-[10px] uppercase tracking-wider text-success-600 font-semibold">Pagado</p>
            <p class="text-base font-bold mt-0.5 text-success-700">{{ formatMoney(facturaCuotasDetalle?.montoPagado) }}</p>
          </div>
          <div class="rounded-lg bg-warning-50 p-3">
            <p class="text-[10px] uppercase tracking-wider text-warning-600 font-semibold">Saldo</p>
            <p class="text-base font-bold mt-0.5 text-warning-700">{{ formatMoney(saldoFacturaCuotasDetalle) }}</p>
          </div>
        </div>

        <DataTable :value="cuotas" striped-rows :pt="{ table: { style: 'min-width: 100%' } }">
          <Column header="#" style="width: 60px">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700 font-semibold">{{ data.numeroCuota }}</span>
            </template>
          </Column>
          <Column header="Vence">
            <template #body="{ data }">
              <span class="text-xs">{{ data.fechaVencimiento }}</span>
            </template>
          </Column>
          <Column header="Monto">
            <template #body="{ data }">
              <span class="text-sm font-semibold">{{ formatMoney(data.monto) }}</span>
            </template>
          </Column>
          <Column header="Pagado">
            <template #body="{ data }">
              <span class="text-sm text-success-700">{{ formatMoney(data.montoPagado) }}</span>
            </template>
          </Column>
          <Column header="Saldo">
            <template #body="{ data }">
              <span class="text-sm">{{ formatMoney(data.saldo) }}</span>
            </template>
          </Column>
          <Column header="Estado">
            <template #body="{ data }">
              <StatusBadge :status="data.estado" />
            </template>
          </Column>
        </DataTable>
      </div>
      <template #footer>
        <Button label="Cerrar" outlined @click="mostrarCuotas = false" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, defineComponent, h } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import AutoComplete from 'primevue/autocomplete'
import Tooltip from 'primevue/tooltip'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import api from '@/services/api'
import { humanLabel } from '@/utils/labels'

const vTooltip = Tooltip

// ============ Estado general ============
const facturas = ref<any[]>([])
const pagos = ref<any[]>([])
const conceptos = ref<any[]>([])
const estudiantes = ref<any[]>([])
const stats = reactive({ totalFacturado: 0, totalPagado: 0, saldoPendiente: 0, facturas: 0, pagos: 0 })

const estudiantesPorId = computed(() => {
  const m: Record<number, any> = {}
  estudiantes.value.forEach(e => { m[e.id] = e })
  return m
})
const facturasPorId = computed(() => {
  const m: Record<number, any> = {}
  facturas.value.forEach(f => { m[f.id] = f })
  return m
})

const nombreEstudiante = (id: number) => {
  const e = estudiantesPorId.value[id]
  return e?.nombreCompleto || `Estudiante #${id}`
}
const numFactura = (id: number) => facturasPorId.value[id]?.numeroFactura || `#${id}`

// ============ Diálogo Nueva Factura ============
const mostrarFormFactura = ref(false)
const creandoF = ref(false)
const errF = ref('')

const selEstudiante = ref<any>(null)
const estudiantesFiltered = ref<any[]>([])

const formF = reactive<any>({
  estudianteId: null,
  conceptoFacturacionId: null,
  montoOriginal: 0,
  fechaVencimiento: null,
  descripcion: '',
  tipoPago: 'CONTADO',
  numeroCuotas: 3,
  frecuenciaCuota: 'MENSUAL',
  fechaPrimeraCuota: null
})

const abrirFormFactura = () => {
  errF.value = ''
  selEstudiante.value = null
  resumenAcad.value = null
  Object.assign(formF, {
    estudianteId: null, conceptoFacturacionId: null, montoOriginal: 0,
    fechaVencimiento: null, descripcion: '', tipoPago: 'CONTADO',
    numeroCuotas: 3, frecuenciaCuota: 'MENSUAL', fechaPrimeraCuota: null
  })
  mostrarFormFactura.value = true
}

const onConceptoChange = (e: any) => {
  const c = conceptos.value.find(x => x.id === e.value)
  if (c && (!formF.montoOriginal || formF.montoOriginal === 0)) {
    formF.montoOriginal = Number(c.montoBase)
  }
  if (c && !formF.descripcion) {
    formF.descripcion = c.nombre
  }
}

const valorCuotaCalc = computed(() => {
  if (!formF.montoOriginal || !formF.numeroCuotas) return 0
  return Math.round((formF.montoOriginal / formF.numeroCuotas) * 100) / 100
})

const cronogramaPreview = computed(() => {
  if (formF.tipoPago !== 'CREDITO') return []
  if (!formF.numeroCuotas || !formF.fechaPrimeraCuota || !formF.frecuenciaCuota || !formF.montoOriginal) return []
  const list: any[] = []
  const base = new Date(formF.fechaPrimeraCuota)
  const valor = valorCuotaCalc.value
  const total = Number(formF.montoOriginal)
  const residuo = Math.round((total - valor * formF.numeroCuotas) * 100) / 100
  for (let i = 0; i < formF.numeroCuotas; i++) {
    const d = new Date(base)
    if (formF.frecuenciaCuota === 'MENSUAL') d.setMonth(d.getMonth() + i)
    else if (formF.frecuenciaCuota === 'QUINCENAL') d.setDate(d.getDate() + 15 * i)
    else if (formF.frecuenciaCuota === 'SEMANAL') d.setDate(d.getDate() + 7 * i)
    const monto = (i === formF.numeroCuotas - 1) ? Math.round((valor + residuo) * 100) / 100 : valor
    list.push({ numero: i + 1, fecha: fmtFecha(d), monto })
  }
  return list
})

const puedeCrearFactura = computed(() => {
  if (!selEstudiante.value) return false
  if (!formF.conceptoFacturacionId) return false
  if (!formF.montoOriginal || formF.montoOriginal <= 0) return false
  if (!formF.fechaVencimiento) return false
  if (formF.tipoPago === 'CREDITO') {
    if (!formF.numeroCuotas || formF.numeroCuotas < 2) return false
    if (!formF.frecuenciaCuota) return false
    if (!formF.fechaPrimeraCuota) return false
  }
  return true
})

const crearFactura = async () => {
  errF.value = ''
  try {
    creandoF.value = true
    const payload: any = {
      estudianteId: selEstudiante.value.id,
      conceptoFacturacionId: formF.conceptoFacturacionId,
      montoOriginal: formF.montoOriginal,
      fechaVencimiento: fmtFecha(formF.fechaVencimiento),
      descripcion: formF.descripcion || null,
      tipoPago: formF.tipoPago,
      numeroCuotas: formF.tipoPago === 'CREDITO' ? formF.numeroCuotas : 1,
      frecuenciaCuota: formF.tipoPago === 'CREDITO' ? formF.frecuenciaCuota : null,
      fechaPrimeraCuota: formF.tipoPago === 'CREDITO' ? fmtFecha(formF.fechaPrimeraCuota) : null
    }
    await api.post('/facturas', payload)
    mostrarFormFactura.value = false
    await cargar()
  } catch (e: any) {
    const data = e.response?.data
    errF.value = data?.detail || data?.message || data?.errors
      ? (typeof data?.errors === 'object' ? Object.values(data.errors).join(' · ') : data?.detail)
      : 'No se pudo crear la factura'
  } finally { creandoF.value = false }
}

// ============ Diálogo Registrar Pago ============
const mostrarFormPago = ref(false)
const creandoP = ref(false)
const errP = ref('')

const selEstudiantePago = ref<any>(null)
const facturasEstudiante = ref<any[]>([])
const cuotasFactura = ref<any[]>([])

const formP = reactive<any>({ facturaId: null, monto: 0, metodoPago: 'EFECTIVO', observaciones: '' })

const facturaSeleccionadaPago = computed(() =>
  facturasEstudiante.value.find(f => f.id === formP.facturaId) || null
)

const saldoFacturaPago = computed(() =>
  facturaSeleccionadaPago.value
    ? Number(facturaSeleccionadaPago.value.montoOriginal) - Number(facturaSeleccionadaPago.value.montoPagado)
    : 0
)

const proximaCuota = computed(() => {
  if (!cuotasFactura.value.length) return null
  return cuotasFactura.value.find(c => c.estado === 'PENDIENTE' || c.estado === 'PARCIAL') || null
})

const abrirFormPago = () => {
  errP.value = ''
  selEstudiantePago.value = null
  facturasEstudiante.value = []
  cuotasFactura.value = []
  Object.assign(formP, { facturaId: null, monto: 0, metodoPago: 'EFECTIVO', observaciones: '' })
  mostrarFormPago.value = true
}

const onEstudiantePagoSelect = async (e: any) => {
  const est = e.value
  if (!est) return
  formP.facturaId = null
  cuotasFactura.value = []
  // Cargar facturas con saldo del estudiante
  try {
    const { data } = await api.get(`/facturas/estudiante/${est.id}`, { params: { size: 100 } })
    facturasEstudiante.value = (data.content || []).filter((f: any) =>
      Number(f.montoOriginal) > Number(f.montoPagado) && f.estado !== 'ANULADA'
    )
  } catch { facturasEstudiante.value = [] }
}

const onFacturaPagoChange = async (e: any) => {
  cuotasFactura.value = []
  if (!e.value) return
  const fact = facturasEstudiante.value.find(f => f.id === e.value)
  if (!fact || fact.tipoPago !== 'CREDITO') return
  try {
    const { data } = await api.get(`/facturas/${e.value}/cuotas`)
    cuotasFactura.value = data || []
  } catch { cuotasFactura.value = [] }
}

const puedeCrearPago = computed(() =>
  !!formP.facturaId && formP.monto > 0 && !!formP.metodoPago
)

const crearPago = async () => {
  errP.value = ''
  try {
    creandoP.value = true
    await api.post('/pagos', { ...formP })
    mostrarFormPago.value = false
    await cargar()
  } catch (e: any) {
    errP.value = e.response?.data?.detail || e.response?.data?.message || 'No se pudo registrar el pago'
  } finally { creandoP.value = false }
}

// ============ Diálogo Cuotas ============
const mostrarCuotas = ref(false)
const cargandoCuotas = ref(false)
const facturaCuotasDetalle = ref<any>(null)
const cuotas = ref<any[]>([])

const saldoFacturaCuotasDetalle = computed(() =>
  facturaCuotasDetalle.value
    ? Number(facturaCuotasDetalle.value.montoOriginal) - Number(facturaCuotasDetalle.value.montoPagado)
    : 0
)

const abrirCuotas = async (factura: any) => {
  facturaCuotasDetalle.value = factura
  cuotas.value = []
  mostrarCuotas.value = true
  cargandoCuotas.value = true
  try {
    const { data } = await api.get(`/facturas/${factura.id}/cuotas`)
    cuotas.value = data || []
  } finally { cargandoCuotas.value = false }
}

// ============ AutoComplete filter compartido ============
const filterEstudiantes = (e: any) => {
  const q = (e.query || '').toLowerCase().trim()
  if (!q) { estudiantesFiltered.value = estudiantes.value.slice(0, 20); return }
  estudiantesFiltered.value = estudiantes.value.filter(es => {
    const full = (es.nombreCompleto || '').toLowerCase()
    return full.includes(q)
      || (es.cedula || '').toLowerCase().includes(q)
      || (es.email || '').toLowerCase().includes(q)
  }).slice(0, 20)
}

const initials = (n: string) => (n || '').split(' ').filter(Boolean).slice(0, 2).map(x => x[0]?.toUpperCase()).join('')

// ============ Helpers ============
const formatMoney = (n: any) => `$${(parseFloat(n) || 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const saldoFactura = (f: any) =>
  (parseFloat(f?.montoOriginal) || 0) - (parseFloat(f?.montoPagado) || 0)

const metodoIcon = (m: string) => ({
  EFECTIVO: 'pi pi-dollar',
  TRANSFERENCIA: 'pi pi-arrows-h',
  TARJETA: 'pi pi-credit-card'
}[m] || 'pi pi-wallet')

const fmtFecha = (d: any) => {
  if (!d) return null
  if (typeof d === 'string') return d.substring(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const DetailRow = defineComponent({
  props: ['label', 'value'],
  setup(props) {
    return () => h('div', null, [
      h('p', { class: 'text-[10px] uppercase tracking-wider text-ink-500 font-semibold mb-0.5' }, props.label),
      h('p', { class: 'text-xs font-medium text-ink-800' }, props.value || '—')
    ])
  }
})

// ============ Pestañas por situación de pago ============
const tabEst = ref<'sin_pagar' | 'con_saldo' | 'al_dia'>('sin_pagar')
const tabsEstudiantes = [
  { key: 'sin_pagar', label: 'Sin pagar',  icon: 'pi-exclamation-circle', activeBadge: 'bg-warning-100 text-warning-700' },
  { key: 'con_saldo', label: 'Con saldo',  icon: 'pi-clock',              activeBadge: 'bg-info-100 text-info-700' },
  { key: 'al_dia',    label: 'Al día',     icon: 'pi-check-circle',       activeBadge: 'bg-success-100 text-success-700' }
] as const

const estudiantesPorTab = (key: string) => {
  const sit = (e: any) => e.situacionPago
  return estudiantes.value.filter(e => {
    if (key === 'sin_pagar') return sit(e) === 'PENDIENTE_MATRICULA'
    if (key === 'con_saldo') return ['PAGO_PARCIAL', 'EN_MORA', 'AL_DIA'].includes(sit(e))
    if (key === 'al_dia')    return ['PAGADO_TOTAL', 'SIN_DEUDA'].includes(sit(e))
    return false
  })
}

const accionEstudiante = (est: any, tab: string) => {
  if (tab === 'sin_pagar') {
    // Abrir form factura precargado con este estudiante
    abrirFormFactura()
    selEstudiante.value = est
  } else if (tab === 'con_saldo') {
    // Abrir form de pago precargado con este estudiante
    abrirFormPago()
    selEstudiantePago.value = est
    onEstudiantePagoSelect({ value: est })
  } else {
    // Ver detalle (futuro: navegar a /estudiantes/:id)
    alert(`${est.nombreCompleto} está al día.`)
  }
}

// ============ Sincronización de situacion_pago ============
const sincronizando = ref(false)
const sincronizarSituacion = async () => {
  sincronizando.value = true
  try {
    const { data } = await api.post('/estudiantes/sync-situacion-pago')
    const msg = `Sincronización OK · ${data.actualizados} de ${data.total} estudiante(s) actualizado(s)`
                + (data.errores ? ` · ${data.errores} error(es)` : '')
    alert(msg)
    await cargar()
  } catch (e: any) {
    alert('No se pudo sincronizar: ' + (e.response?.data?.detail || e.message))
  } finally {
    sincronizando.value = false
  }
}

// ============ Carga inicial ============
const cargar = async () => {
  const [fRes, pRes, cRes, eRes] = await Promise.allSettled([
    api.get('/facturas', { params: { size: 200 } }),
    api.get('/pagos', { params: { size: 200 } }),
    api.get('/conceptos-facturacion', { params: { size: 100 } }),
    api.get('/estudiantes', { params: { size: 200 } })
  ])
  if (fRes.status === 'fulfilled') {
    facturas.value = fRes.value.data.content || []
    stats.facturas = facturas.value.length
    stats.totalFacturado = facturas.value.reduce((s, x) => s + parseFloat(x.montoOriginal || 0), 0)
    stats.totalPagado = facturas.value.reduce((s, x) => s + parseFloat(x.montoPagado || 0), 0)
    stats.saldoPendiente = stats.totalFacturado - stats.totalPagado
  }
  if (pRes.status === 'fulfilled') {
    pagos.value = pRes.value.data.content || []
    stats.pagos = pagos.value.length
  }
  if (cRes.status === 'fulfilled') {
    conceptos.value = (cRes.value.data.content || []).filter((c: any) => c.activo !== false)
  }
  if (eRes.status === 'fulfilled') {
    estudiantes.value = (eRes.value.data.content || []).map((e: any) => ({
      ...e,
      nombreCompleto: e.nombreCompleto || `${e.nombre ?? ''} ${e.apellido ?? ''}`.trim()
    }))
  }
}

// Resumen académico-financiero (curso + total + pagado + saldo) que muestra
// la mini-card y se usa para auto-completar el monto.
const resumenAcad = ref<any>(null)
const cargandoResumen = ref(false)

const progresoPct = computed(() => {
  const r = resumenAcad.value
  if (!r || !r.precioCurso || r.precioCurso <= 0) return 0
  const pagado = parseFloat(r.totalPagado || 0)
  const pct = (pagado / parseFloat(r.precioCurso)) * 100
  return Math.min(Math.max(Math.round(pct), 0), 100)
})

const precioRestantePorFacturar = computed(() => {
  const r = resumenAcad.value
  if (!r) return 0
  const dif = parseFloat(r.precioCurso || 0) - parseFloat(r.totalFacturado || 0)
  return Math.max(dif, 0)
})

const cargarResumenAcademico = async (estudianteId: number) => {
  cargandoResumen.value = true
  resumenAcad.value = null
  try {
    const { data } = await api.get(`/facturas/estudiante/${estudianteId}/resumen-academico`)
    resumenAcad.value = data
    // Auto-fill del monto si todavía está en 0 y hay saldo:
    if ((!formF.montoOriginal || formF.montoOriginal === 0) && data.saldoFacturado > 0) {
      formF.montoOriginal = parseFloat(data.saldoFacturado)
    } else if ((!formF.montoOriginal || formF.montoOriginal === 0) && data.saldoTotal > 0) {
      formF.montoOriginal = parseFloat(data.saldoTotal)
    }
  } catch (e) {
    console.warn('No se pudo cargar resumen académico', e)
  } finally { cargandoResumen.value = false }
}

watch(selEstudiante, (v) => {
  if (v) {
    formF.estudianteId = v.id
    cargarResumenAcademico(v.id)
  } else {
    resumenAcad.value = null
  }
})

onMounted(cargar)
</script>
