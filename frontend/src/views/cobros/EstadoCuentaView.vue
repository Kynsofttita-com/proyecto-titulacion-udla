<template>
  <div class="space-y-6">
    <PageHeader
      title="Cobros"
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
              <StatusBadge :status="est.situacionPago || 'PENDIENTE_FACTURACION'" />
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
              <p v-if="estudiantesPorId[data.estudianteId]?.cedula" class="text-[11px] text-ink-500">
                {{ estudiantesPorId[data.estudianteId].cedula }}
              </p>
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
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos marcados con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
            Podés escribir para buscar el estudiante o hacer click en la flechita.
          </p>
        </div>

        <!-- ESTUDIANTE -->
        <div>
          <label for="field-fact-estudiante" class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-user text-brand-600" /> Estudiante <span class="text-danger-600 font-semibold">*</span>
            </span>
          </label>
          <AutoComplete
            v-model="selEstudiante"
            inputId="field-fact-estudiante"
            :suggestions="estudiantesFiltered"
            @complete="filterEstudiantes"
            @update:modelValue="clearErrF('estudiante')"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre, cédula o email..."
            :dropdown="true"
            dropdownMode="blank"
            forceSelection
            class="w-full"
            :pt="{ input: { class: errorsF.estudiante ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
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
          <p v-if="errorsF.estudiante" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.estudiante }}
          </p>

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
            <label for="field-fact-concepto" class="label mb-1.5 block">
              <span class="flex items-center gap-2">
                <i class="pi pi-tag text-brand-600" /> Concepto <span class="text-danger-600 font-semibold">*</span>
              </span>
            </label>
            <Dropdown
              v-model="formF.conceptoFacturacionId"
              inputId="field-fact-concepto"
              :options="conceptos"
              optionLabel="nombre"
              optionValue="id"
              placeholder="Selecciona un concepto"
              class="w-full"
              :class="errorsF.concepto ? '!border-danger-500 !bg-danger-50' : ''"
              @change="onConceptoChange"
              @update:modelValue="clearErrF('concepto')"
            >
              <template #option="{ option }">
                <div class="flex items-center justify-between gap-3 w-full">
                  <span class="text-sm font-medium">{{ option.nombre }}</span>
                  <span class="text-xs text-brand-700 font-semibold">{{ formatMoney(option.montoBase) }}</span>
                </div>
              </template>
            </Dropdown>
            <p v-if="errorsF.concepto" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.concepto }}
            </p>
          </div>
          <div>
            <label for="field-fact-monto" class="label mb-1.5 block">
              Monto (USD) <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputNumber
              v-model="formF.montoOriginal"
              inputId="field-fact-monto"
              mode="currency" currency="USD" locale="en-US"
              class="w-full" :min="0.01"
              :pt="{ input: { class: errorsF.monto ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
              @update:modelValue="clearErrF('monto')"
            />
            <p v-if="errorsF.monto" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.monto }}
            </p>
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
              <label for="field-fact-numeroCuotas" class="label mb-1.5 block">
                Nº de cuotas <span class="text-danger-600 font-semibold">*</span>
              </label>
              <InputNumber
                v-model="formF.numeroCuotas"
                inputId="field-fact-numeroCuotas"
                :min="2" :max="24"
                showButtons buttonLayout="horizontal"
                class="w-full"
                :pt="{ input: { class: errorsF.numeroCuotas ? 'w-full text-center !border-danger-500 !bg-danger-50' : 'w-full text-center' } }"
                @update:modelValue="clearErrF('numeroCuotas')"
              />
              <p v-if="errorsF.numeroCuotas" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.numeroCuotas }}
              </p>
            </div>
            <div>
              <label for="field-fact-frecuenciaCuota" class="label mb-1.5 block">
                Frecuencia <span class="text-danger-600 font-semibold">*</span>
              </label>
              <Dropdown
                v-model="formF.frecuenciaCuota"
                inputId="field-fact-frecuenciaCuota"
                :options="[
                  { label: 'Mensual', value: 'MENSUAL' },
                  { label: 'Quincenal', value: 'QUINCENAL' },
                  { label: 'Semanal', value: 'SEMANAL' }
                ]"
                optionLabel="label" optionValue="value"
                class="w-full"
                :class="errorsF.frecuenciaCuota ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrF('frecuenciaCuota')"
              />
              <p v-if="errorsF.frecuenciaCuota" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.frecuenciaCuota }}
              </p>
            </div>
            <div>
              <label for="field-fact-fechaPrimeraCuota" class="label mb-1.5 block">
                1ra cuota <span class="text-danger-600 font-semibold">*</span>
              </label>
              <Calendar
                v-model="formF.fechaPrimeraCuota"
                inputId="field-fact-fechaPrimeraCuota"
                dateFormat="yy-mm-dd"
                :showIcon="true"
                class="w-full"
                :inputClass="errorsF.fechaPrimeraCuota ? '!border-danger-500 !bg-danger-50' : ''"
                @update:modelValue="clearErrF('fechaPrimeraCuota')"
              />
              <p v-if="errorsF.fechaPrimeraCuota" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
                <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.fechaPrimeraCuota }}
              </p>
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
            <label for="field-fact-fechaVencimiento" class="label mb-1.5 block">
              Fecha vencimiento <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Calendar
              v-model="formF.fechaVencimiento"
              inputId="field-fact-fechaVencimiento"
              dateFormat="yy-mm-dd"
              :showIcon="true"
              class="w-full"
              :inputClass="errorsF.fechaVencimiento ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrF('fechaVencimiento')"
            />
            <p v-if="errorsF.fechaVencimiento" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsF.fechaVencimiento }}
            </p>
          </div>
          <div>
            <label class="label mb-1.5 block">Descripción <span class="text-xs text-ink-500">(opcional)</span></label>
            <InputText v-model="formF.descripcion" placeholder="Ej: Curso categoría B" class="w-full" maxlength="200" />
          </div>
        </div>

        <!-- Error de servidor (409, red, etc.) -->
        <div v-if="errF" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ errF }}</span>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormFactura = false" />
        <Button label="Crear factura" icon="pi pi-check" :loading="creandoF" @click="crearFactura" />
      </template>
    </Dialog>

    <!-- ============================ DIALOG: REGISTRAR PAGO ============================ -->
    <Dialog
      v-model:visible="mostrarFormPago"
      modal header="Registrar pago"
      :style="{ width: '560px' }"
    >
      <div class="space-y-5">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos marcados con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
            Podés escribir para buscar el estudiante o hacer click en la flechita.
          </p>
        </div>

        <!-- ESTUDIANTE -->
        <div>
          <label for="field-pago-estudiante" class="label mb-1.5 block">
            <span class="flex items-center gap-2"><i class="pi pi-user text-brand-600" /> Estudiante <span class="text-danger-600 font-semibold">*</span></span>
          </label>
          <AutoComplete
            v-model="selEstudiantePago"
            inputId="field-pago-estudiante"
            :suggestions="estudiantesFiltered"
            @complete="filterEstudiantes"
            @item-select="onEstudiantePagoSelect"
            @update:modelValue="clearErrP('estudiante')"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre, cédula o email..."
            :dropdown="true"
            dropdownMode="blank"
            forceSelection
            class="w-full"
            :pt="{ input: { class: errorsP.estudiante ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
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
          <p v-if="errorsP.estudiante" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsP.estudiante }}
          </p>
        </div>

        <!-- FACTURA DEL ESTUDIANTE -->
        <div v-if="selEstudiantePago">
          <label for="field-pago-factura" class="label mb-1.5 block">
            <span class="flex items-center gap-2"><i class="pi pi-receipt text-brand-600" /> Factura pendiente <span class="text-danger-600 font-semibold">*</span></span>
          </label>
          <Dropdown
            v-model="formP.facturaId"
            inputId="field-pago-factura"
            :options="facturasEstudiante"
            optionValue="id"
            optionLabel="numeroFactura"
            placeholder="Selecciona una factura con saldo"
            class="w-full"
            :class="errorsP.factura ? '!border-danger-500 !bg-danger-50' : ''"
            @change="onFacturaPagoChange"
            @update:modelValue="clearErrP('factura')"
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
          <p v-if="errorsP.factura" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsP.factura }}
          </p>

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
            <label for="field-pago-monto" class="label mb-1.5 block">
              Monto (USD) <span class="text-danger-600 font-semibold">*</span>
            </label>
            <InputNumber
              v-model="formP.monto"
              inputId="field-pago-monto"
              mode="currency" currency="USD" locale="en-US"
              :min="0.01" class="w-full"
              :pt="{ input: { class: errorsP.monto ? 'w-full !border-danger-500 !bg-danger-50' : 'w-full' } }"
              @update:modelValue="clearErrP('monto')"
            />
            <p v-if="errorsP.monto" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsP.monto }}
            </p>
          </div>
          <div>
            <label for="field-pago-metodoPago" class="label mb-1.5 block">
              Método de pago <span class="text-danger-600 font-semibold">*</span>
            </label>
            <Dropdown
              v-model="formP.metodoPago"
              inputId="field-pago-metodoPago"
              :options="[
                { label: 'Efectivo', value: 'EFECTIVO' },
                { label: 'Transferencia', value: 'TRANSFERENCIA' },
                { label: 'Tarjeta', value: 'TARJETA' }
              ]"
              optionLabel="label" optionValue="value" class="w-full"
              :class="errorsP.metodoPago ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearErrP('metodoPago')"
            />
            <p v-if="errorsP.metodoPago" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsP.metodoPago }}
            </p>
          </div>
        </div>

        <div>
          <label for="field-pago-cuentaId" class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-briefcase text-brand-600" />
              Cuenta destino
              <span v-if="!cuentaDefaultCobrosId" class="text-danger-600 font-semibold">*</span>
              <span v-else class="text-xs text-ink-500 font-normal">(usa la default si dejas vacío)</span>
            </span>
          </label>
          <Dropdown
            v-model="formP.cuentaId"
            inputId="field-pago-cuentaId"
            :options="cuentasActivasPago"
            optionLabel="nombre" optionValue="id"
            :placeholder="cuentaDefaultCobrosId ? 'Usando cuenta por defecto configurada' : '¿A qué cuenta entra este pago?'"
            class="w-full"
            :class="errorsP.cuentaId ? '!border-danger-500 !bg-danger-50' : ''"
            :showClear="!!cuentaDefaultCobrosId"
            @update:modelValue="clearErrP('cuentaId')"
          >
            <template #option="{ option }">
              <div class="flex items-center justify-between w-full gap-3">
                <div class="flex items-center gap-2 min-w-0">
                  <i :class="iconoCuentaPago(option.tipo)" class="text-brand-600 text-xs" />
                  <span class="text-sm truncate">{{ option.nombre }}</span>
                  <span
                    v-if="option.id === cuentaDefaultCobrosId"
                    class="text-[10px] px-1.5 py-0.5 rounded bg-brand-50 text-brand-700 border border-brand-200 font-medium"
                    v-tooltip.top="'Cuenta por defecto configurada'"
                  >default</span>
                </div>
                <span class="text-xs text-ink-500 font-mono">{{ formatMoney(option.saldoActual) }}</span>
              </div>
            </template>
          </Dropdown>
          <p v-if="errorsP.cuentaId" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsP.cuentaId }}
          </p>
          <p v-else-if="cuentasActivasPago.length === 0" class="text-xs text-warning-700 mt-1">
            <i class="pi pi-exclamation-triangle mr-1" />
            No hay cuentas activas.
            <router-link to="/finanzas/saldo" class="underline font-medium">Crea una primero</router-link>.
          </p>
          <p v-else-if="cuentaDefaultCobrosId && !formP.cuentaId" class="text-[11px] text-brand-700 mt-1 flex items-center gap-1">
            <i class="pi pi-info-circle text-[10px]" />
            Se registrará en <b>{{ nombreCuentaDefault }}</b> (configurable en
            <router-link to="/configuracion" class="underline">Configuración → Contabilidad</router-link>).
          </p>
        </div>

        <div>
          <label class="label mb-1.5 block">Referencia / Observaciones <span class="text-xs text-ink-500">(opcional)</span></label>
          <InputText v-model="formP.observaciones" placeholder="Comprobante, nº transacción..." class="w-full" maxlength="200" />
        </div>

        <!-- Error de servidor (409, red, etc.) -->
        <div v-if="errP" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ errP }}</span>
        </div>
      </div>

      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormPago = false" />
        <Button label="Registrar pago" icon="pi pi-check" :loading="creandoP" @click="crearPago" />
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

// ============ Helper factory de validación por campo ============
function useValidation() {
  const errors = reactive<Record<string, string>>({})
  const setError = (k: string, v: string) => { errors[k] = v }
  const clearError = (k: string) => { if (errors[k]) delete errors[k] }
  const clearAll = () => { Object.keys(errors).forEach(k => delete errors[k]) }
  const focusFirst = (orden: string[], prefijo: string, scroll = true) => {
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

// ============ Diálogo Nueva Factura ============
const mostrarFormFactura = ref(false)
const creandoF = ref(false)
const errF = ref('')

const valF = useValidation()
const errorsF = valF.errors
const setErrF = valF.setError
const clearErrF = valF.clearError
const clearAllF = valF.clearAll

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
  clearAllF()
  selEstudiante.value = null
  resumenAcad.value = null
  Object.assign(formF, {
    estudianteId: null, conceptoFacturacionId: null, montoOriginal: 0,
    fechaVencimiento: null, descripcion: '', tipoPago: 'CONTADO',
    numeroCuotas: 3, frecuenciaCuota: 'MENSUAL', fechaPrimeraCuota: null
  })
  // Precargar sugerencias para que el dropdown funcione al primer click
  estudiantesFiltered.value = estudiantes.value.slice(0, 20)
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

const validarFactura = (): boolean => {
  clearAllF()
  if (!selEstudiante.value || typeof selEstudiante.value !== 'object') {
    setErrF('estudiante', 'Selecciona un estudiante de la lista')
  }
  if (!formF.conceptoFacturacionId) {
    setErrF('concepto', 'Selecciona un concepto de facturación')
  }
  if (!formF.montoOriginal || formF.montoOriginal <= 0) {
    setErrF('monto', 'El monto debe ser mayor a $0')
  }
  if (!formF.fechaVencimiento) {
    setErrF('fechaVencimiento', 'La fecha de vencimiento es requerida')
  } else {
    const hoy = new Date(); hoy.setHours(0, 0, 0, 0)
    const fv = new Date(formF.fechaVencimiento); fv.setHours(0, 0, 0, 0)
    if (fv < hoy) setErrF('fechaVencimiento', 'La fecha de vencimiento no puede ser anterior a hoy')
  }
  if (formF.tipoPago === 'CREDITO') {
    if (!formF.numeroCuotas || formF.numeroCuotas < 2) {
      setErrF('numeroCuotas', 'Mínimo 2 cuotas')
    } else if (formF.numeroCuotas > 24) {
      setErrF('numeroCuotas', 'Máximo 24 cuotas')
    }
    if (!formF.frecuenciaCuota) setErrF('frecuenciaCuota', 'Selecciona la frecuencia')
    if (!formF.fechaPrimeraCuota) {
      setErrF('fechaPrimeraCuota', 'La fecha de la 1ra cuota es requerida')
    } else if (formF.fechaVencimiento) {
      const fpc = new Date(formF.fechaPrimeraCuota); fpc.setHours(0, 0, 0, 0)
      const fv = new Date(formF.fechaVencimiento); fv.setHours(0, 0, 0, 0)
      if (fpc > fv) setErrF('fechaPrimeraCuota', 'Debe ser anterior o igual al vencimiento')
    }
  }
  if (Object.keys(errorsF).length > 0) {
    const orden = ['estudiante', 'concepto', 'monto', 'numeroCuotas', 'frecuenciaCuota', 'fechaPrimeraCuota', 'fechaVencimiento']
    valF.focusFirst(orden, 'fact', true)
    return false
  }
  return true
}

const crearFactura = async () => {
  errF.value = ''
  if (!validarFactura()) return
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

const valP = useValidation()
const errorsP = valP.errors
const setErrP = valP.setError
const clearErrP = valP.clearError
const clearAllP = valP.clearAll

const selEstudiantePago = ref<any>(null)
const facturasEstudiante = ref<any[]>([])
const cuotasFactura = ref<any[]>([])

const formP = reactive<any>({ facturaId: null, monto: 0, metodoPago: 'EFECTIVO', cuentaId: null, observaciones: '' })

// ============ Cuentas contables (para pago) ============
const cuentasContables = ref<any[]>([])
const cuentasActivasPago = computed(() => cuentasContables.value.filter(c => c.activo))
const iconoCuentaPago = (t: string) => ({
  EFECTIVO: 'pi pi-money-bill',
  BANCO: 'pi pi-building',
  TARJETA: 'pi pi-credit-card'
}[t] || 'pi pi-briefcase')

// Cuenta default de cobros (configurada en Configuracion → Contabilidad)
const cuentaDefaultCobrosId = ref<number | null>(null)
const nombreCuentaDefault = computed(() =>
  cuentasContables.value.find(c => c.id === cuentaDefaultCobrosId.value)?.nombre || 'cuenta por defecto'
)

const cargarCuentasContables = async () => {
  try {
    const { data } = await api.get('/cuentas', { params: { soloActivas: false } })
    cuentasContables.value = data || []
  } catch (e) {
    console.warn('No se pudieron cargar las cuentas contables', e)
    cuentasContables.value = []
  }
}

const cargarCuentaDefault = async () => {
  try {
    const { data } = await api.get('/configuracion')
    cuentaDefaultCobrosId.value = data?.cuentaDefaultCobrosId ?? null
  } catch (e) {
    console.warn('No se pudo cargar la configuracion (cuenta default cobros)', e)
    cuentaDefaultCobrosId.value = null
  }
}

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

const abrirFormPago = async () => {
  errP.value = ''
  clearAllP()
  selEstudiantePago.value = null
  facturasEstudiante.value = []
  cuotasFactura.value = []
  Object.assign(formP, { facturaId: null, monto: 0, metodoPago: 'EFECTIVO', cuentaId: null, observaciones: '' })
  // Precargar sugerencias para que el dropdown funcione al primer click
  estudiantesFiltered.value = estudiantes.value.slice(0, 20)
  mostrarFormPago.value = true
  // Cargar cuentas + cuenta default (ambos disparados; pre-seleccionamos al terminar)
  await Promise.all([cargarCuentasContables(), cargarCuentaDefault()])
  if (cuentaDefaultCobrosId.value && cuentasActivasPago.value.some(c => c.id === cuentaDefaultCobrosId.value)) {
    formP.cuentaId = cuentaDefaultCobrosId.value
  }
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

const validarPago = (): boolean => {
  clearAllP()
  if (!selEstudiantePago.value || typeof selEstudiantePago.value !== 'object') {
    setErrP('estudiante', 'Selecciona un estudiante de la lista')
  }
  if (!formP.facturaId) {
    setErrP('factura', 'Selecciona una factura con saldo pendiente')
  }
  if (!formP.monto || formP.monto <= 0) {
    setErrP('monto', 'El monto debe ser mayor a $0')
  } else if (formP.facturaId && saldoFacturaPago.value > 0 && formP.monto > saldoFacturaPago.value) {
    setErrP('monto', `El monto no puede superar el saldo de la factura (${formatMoney(saldoFacturaPago.value)})`)
  }
  if (!formP.metodoPago) {
    setErrP('metodoPago', 'Selecciona el método de pago')
  }
  // Cuenta destino: obligatoria SOLO si no hay cuenta default configurada.
  // Si hay default, el backend la usa cuando cuentaId viene null.
  if (!formP.cuentaId && !cuentaDefaultCobrosId.value) {
    setErrP('cuentaId', 'Selecciona la cuenta a la que ingresa el pago (o configura una default en Configuración → Contabilidad)')
  }
  if (Object.keys(errorsP).length > 0) {
    const orden = ['estudiante', 'factura', 'monto', 'metodoPago', 'cuentaId']
    valP.focusFirst(orden, 'pago', true)
    return false
  }
  return true
}

const crearPago = async () => {
  errP.value = ''
  if (!validarPago()) return
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

// ============ Pestañas por situación de pago (modelo nuevo Sprint 9 ext) ============
// Las 4 pestañas reflejan los 4 valores definidos en backend:
// PENDIENTE_FACTURACION, PENDIENTE_PAGO, PAGO_PARCIAL, PAGADO_TOTAL.
// Los valores legacy (PENDIENTE_MATRICULA, AL_DIA, EN_MORA, SIN_DEUDA)
// también se mapean para no perder filas hasta que la migración termine.
const tabEst = ref<'sin_factura' | 'sin_pago' | 'parcial' | 'al_dia'>('sin_factura')
const tabsEstudiantes = [
  { key: 'sin_factura', label: 'Sin facturar', icon: 'pi-file',                 activeBadge: 'bg-warning-100 text-warning-700' },
  { key: 'sin_pago',    label: 'Por cobrar',   icon: 'pi-exclamation-circle',   activeBadge: 'bg-danger-100 text-danger-700' },
  { key: 'parcial',     label: 'Pago parcial', icon: 'pi-clock',                activeBadge: 'bg-warning-100 text-warning-700' },
  { key: 'al_dia',      label: 'Al día',       icon: 'pi-check-circle',         activeBadge: 'bg-success-100 text-success-700' }
] as const

const estudiantesPorTab = (key: string) => {
  const sit = (e: any) => e.situacionPago
  return estudiantes.value.filter(e => {
    if (key === 'sin_factura') return ['PENDIENTE_FACTURACION', 'PENDIENTE_MATRICULA'].includes(sit(e))
    if (key === 'sin_pago')    return sit(e) === 'PENDIENTE_PAGO'
    if (key === 'parcial')     return ['PAGO_PARCIAL', 'EN_MORA'].includes(sit(e))
    if (key === 'al_dia')      return ['PAGADO_TOTAL', 'SIN_DEUDA', 'AL_DIA'].includes(sit(e))
    return false
  })
}

const accionEstudiante = (est: any, tab: string) => {
  if (tab === 'sin_factura') {
    // Aún no le han emitido factura: abrir form de factura precargado.
    abrirFormFactura()
    selEstudiante.value = est
  } else if (tab === 'sin_pago' || tab === 'parcial') {
    // Tiene factura CONTADO pendiente o parcial: abrir form de pago.
    abrirFormPago()
    selEstudiantePago.value = est
    onEstudiantePagoSelect({ value: est })
  } else {
    // Al día (CONTADO pagado o CRÉDITO emitido).
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
