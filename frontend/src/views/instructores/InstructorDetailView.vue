<template>
  <div class="space-y-6">
    <PageHeader
      :title="`${instructor.nombre} ${instructor.apellido}`"
      :description="instructor.cedula"
      icon="pi pi-id-card"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Instructores', to: '/instructores' },
        { label: instructor.email || 'Detalle' }
      ]"
    >
      <template #actions>
        <Button label="Volver" icon="pi pi-arrow-left" outlined @click="router.back()" />
        <Button
          label="Eliminar"
          icon="pi pi-trash"
          severity="danger"
          outlined
          @click="confirmarEliminar"
        />
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12">
      <ProgressSpinner />
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Panel izquierdo: Perfil resumen -->
      <div class="lg:col-span-1 space-y-4">
        <div class="card p-5">
          <div class="flex flex-col items-center text-center mb-4">
            <Avatar :name="`${instructor.nombre} ${instructor.apellido}`" size="xlarge" class="mb-3" />
            <h2 class="text-lg font-bold text-ink-900">{{ instructor.nombre }} {{ instructor.apellido }}</h2>
            <p class="text-sm text-ink-500">{{ instructor.email }}</p>
          </div>

          <div class="space-y-2 border-t border-ink-200 pt-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Estado:</span>
              <div class="flex items-center gap-2">
                <StatusBadge :status="instructor.estado" />
                <Button
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
              <span class="text-sm text-ink-600">Licencia:</span>
              <span class="inline-flex items-center justify-center w-7 h-7 rounded-md bg-brand-100 text-brand-700 text-xs font-bold">
                {{ instructor.licenciaCategoria || '?' }}
              </span>
            </div>
            <div v-if="instructor.fechaContratacion" class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Contratado:</span>
              <span class="text-xs font-medium text-ink-700">
                {{ fmtFechaLocal(instructor.fechaContratacion) }}
              </span>
            </div>
            <div v-if="diasParaVencerLicencia !== null" class="flex items-center justify-between pt-2 border-t border-ink-100">
              <span class="text-sm text-ink-600">Vence licencia:</span>
              <span
                :class="[
                  'text-xs font-bold',
                  diasParaVencerLicencia < 0 ? 'text-danger-600' :
                  diasParaVencerLicencia < 60 ? 'text-warning-600' : 'text-success-600'
                ]"
              >
                {{
                  diasParaVencerLicencia < 0
                    ? `Vencida hace ${Math.abs(diasParaVencerLicencia)} d.`
                    : `En ${diasParaVencerLicencia} días`
                }}
              </span>
            </div>
          </div>

          <!-- Mini-widget de horario -->
          <button
            type="button"
            @click="activeTab = 1"
            class="w-full mt-4 p-3 rounded-lg bg-info-50 border border-info-200 hover:bg-info-100 transition text-left"
          >
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-medium text-info-700 flex items-center gap-1.5">
                <i class="pi pi-calendar-clock" />
                Horario configurado
              </span>
              <i class="pi pi-arrow-right text-info-600 text-xs" />
            </div>
            <p class="text-sm font-semibold text-ink-900">
              {{ resumenHorarioCorto }}
              <span v-if="disponibilidad.length > 0" class="font-normal text-ink-600">
                · {{ horasSemanalesConfiguradas }} h/sem
              </span>
            </p>
            <p v-if="excepcionesFuturas.length > 0" class="text-xs text-warning-700 mt-1">
              <i class="pi pi-exclamation-circle text-xs" />
              {{ excepcionesFuturas.length }} excepción{{ excepcionesFuturas.length === 1 ? '' : 'es' }} próxima{{ excepcionesFuturas.length === 1 ? '' : 's' }}
            </p>
          </button>
        </div>
      </div>

      <!-- Panel derecho: Información con tabs -->
      <div class="lg:col-span-2">
        <TabView v-model:activeIndex="activeTab" class="instructor-detail-tabs">
          <TabPanel header="Información">
            <div class="space-y-4">
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
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Cédula</label>
                <InputText v-model="formPersonal.cedula" maxlength="10" class="w-full" disabled />
              </div>
              <div>
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
            <DetailRow label="Nombre" :value="`${instructor.nombre} ${instructor.apellido}`" />
            <DetailRow label="Cédula" :value="instructor.cedula" />
            <DetailRow label="Fecha de nacimiento" :value="instructor.fechaNacimiento" type="date" />
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
                <InputText v-model="formContacto.email" type="email" placeholder="instructor@correo.com" class="w-full" />
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
              <Button label="Guardar" icon="pi pi-check" @click="guardarContacto()" :loading="guardando.contacto" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.contacto = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Email" :value="instructor.email" type="email" />
            <DetailRow label="Teléfono" :value="instructor.telefono" type="phone" />
            <DetailRow label="Dirección" :value="instructor.direccion" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarContacto()" size="small" />
          </div>
        </div>

        <!-- Información laboral -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-briefcase text-brand-600" />
            Información laboral
          </h3>

          <div v-if="editando.laboral" class="space-y-4">
            <div class="grid grid-cols-1 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de contratación</label>
                <Calendar v-model="formLaboral.fechaContratacion" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarLaboral()" :loading="guardando.laboral" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.laboral = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Fecha de contratación" :value="instructor.fechaContratacion" type="date" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarLaboral()" size="small" />
          </div>
        </div>

        <!-- Contrato -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-file-edit text-brand-600" />
            Contrato
          </h3>

          <div v-if="editando.contrato" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-2">Tipo de contrato</label>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
                <button
                  v-for="(meta, key) in TIPOS_CONTRATO"
                  :key="key"
                  type="button"
                  @click="seleccionarTipoContratoEnDetail(key as TipoContrato)"
                  :class="['p-3 rounded-lg border-2 text-left transition-all',
                    formContrato.tipoContrato === key
                      ? 'border-brand-600 bg-brand-50'
                      : 'border-ink-200 bg-white hover:border-brand-300']"
                >
                  <div class="flex items-center gap-2 mb-1">
                    <i :class="[meta.icon, formContrato.tipoContrato === key ? 'text-brand-700' : 'text-ink-500']" />
                    <span :class="['font-semibold text-xs', formContrato.tipoContrato === key ? 'text-brand-800' : 'text-ink-900']">
                      {{ meta.label }}
                    </span>
                  </div>
                  <p class="text-xs text-ink-600">{{ meta.desc }}</p>
                </button>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Horas semanales</label>
                <InputNumber v-model="formContrato.horasContratoSemanales" :min="1" :max="60" suffix=" h" class="w-full" />
              </div>
              <div v-if="formContrato.tipoContrato !== 'POR_HORAS'">
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Salario mensual (USD) *</label>
                <InputNumber v-model="formContrato.salarioMensual" mode="currency" currency="USD" locale="en-US" :minFractionDigits="2" class="w-full" />
              </div>
              <div v-if="formContrato.tipoContrato === 'POR_HORAS'">
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Tarifa por hora (USD) *</label>
                <InputNumber v-model="formContrato.tarifaHora" mode="currency" currency="USD" locale="en-US" :minFractionDigits="2" class="w-full" />
              </div>
              <div v-if="formContrato.tipoContrato !== 'POR_HORAS'" class="col-span-2">
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Tarifa hora extra (USD)
                  <span class="text-xs text-ink-500">(opcional)</span>
                </label>
                <InputNumber v-model="formContrato.tarifaHora" mode="currency" currency="USD" locale="en-US" :minFractionDigits="2" class="w-full" />
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarContrato()" :loading="guardando.contrato" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.contrato = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <div class="flex items-center gap-3 p-3 rounded-lg bg-brand-50 border border-brand-200">
              <i :class="[TIPOS_CONTRATO[instructor.tipoContrato]?.icon || 'pi pi-file', 'text-brand-700 text-xl']" />
              <div>
                <p class="text-sm font-semibold text-brand-800">{{ TIPOS_CONTRATO[instructor.tipoContrato]?.label || instructor.tipoContrato }}</p>
                <p class="text-xs text-ink-600">{{ TIPOS_CONTRATO[instructor.tipoContrato]?.desc }}</p>
              </div>
            </div>
            <DetailRow label="Horas contratadas por semana" :value="`${instructor.horasContratoSemanales} h`" />
            <DetailRow
              v-if="instructor.tipoContrato !== 'POR_HORAS'"
              label="Salario mensual"
              :value="instructor.salarioMensual ? `$${Number(instructor.salarioMensual).toFixed(2)}` : null"
            />
            <DetailRow
              v-if="instructor.tipoContrato === 'POR_HORAS'"
              label="Tarifa por hora"
              :value="instructor.tarifaHora ? `$${Number(instructor.tarifaHora).toFixed(2)} / h` : null"
            />
            <DetailRow
              v-if="instructor.tipoContrato !== 'POR_HORAS' && instructor.tarifaHora"
              label="Tarifa hora extra"
              :value="`$${Number(instructor.tarifaHora).toFixed(2)} / h`"
            />
            <Button label="Editar contrato" icon="pi pi-pencil" text @click="editarContrato()" size="small" />
          </div>
        </div>

        <!-- Resumen de horas / nómina estimada -->
        <div class="card p-5">
          <div class="flex items-center justify-between mb-4">
            <h3 class="heading-3 flex items-center gap-2">
              <i class="pi pi-chart-bar text-brand-600" />
              Resumen de horas
            </h3>
            <Button icon="pi pi-refresh" text rounded size="small" @click="cargarResumenHoras" :loading="cargandoResumen" v-tooltip="'Recargar'" />
          </div>

          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label class="block text-xs font-medium text-ink-600 mb-1">Desde</label>
              <input v-model="rangoResumen.desde" type="date" class="w-full text-sm px-3 py-1.5 border border-ink-300 rounded-lg bg-ink-50" @change="cargarResumenHoras" />
            </div>
            <div>
              <label class="block text-xs font-medium text-ink-600 mb-1">Hasta</label>
              <input v-model="rangoResumen.hasta" type="date" class="w-full text-sm px-3 py-1.5 border border-ink-300 rounded-lg bg-ink-50" @change="cargarResumenHoras" />
            </div>
          </div>

          <div v-if="cargandoResumen" class="text-center py-6">
            <ProgressSpinner style="width: 36px; height: 36px" />
          </div>

          <div v-else-if="resumenHoras" class="space-y-4">
            <div class="grid grid-cols-3 gap-3">
              <div class="p-3 bg-brand-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">Horas contratadas</p>
                <p class="text-2xl font-bold text-brand-700">{{ Number(resumenHoras.horasContratadas).toFixed(1) }}</p>
              </div>
              <div class="p-3 bg-success-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">Horas cumplidas</p>
                <p class="text-2xl font-bold text-success-700">{{ Number(resumenHoras.horasCumplidas).toFixed(1) }}</p>
              </div>
              <div class="p-3 bg-warning-50 rounded-lg">
                <p class="text-xs text-ink-600 mb-1">Restantes</p>
                <p class="text-2xl font-bold text-warning-700">{{ Number(resumenHoras.horasRestantes).toFixed(1) }}</p>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between text-xs text-ink-600 mb-1">
                <span>Cumplimiento</span>
                <span class="font-semibold">{{ Number(resumenHoras.porcentajeCumplimiento).toFixed(1) }}%</span>
              </div>
              <div class="w-full bg-ink-100 rounded-full h-2 overflow-hidden">
                <div
                  class="h-full bg-success-500 transition-all"
                  :style="{ width: `${Math.min(100, Number(resumenHoras.porcentajeCumplimiento))}%` }"
                />
              </div>
            </div>

            <div class="p-4 rounded-lg bg-ink-50 border border-ink-200 flex items-center justify-between">
              <div>
                <p class="text-xs text-ink-600 mb-1">Sueldo estimado en este rango</p>
                <p class="text-2xl font-bold text-ink-900">${{ Number(resumenHoras.sueldoEstimado).toFixed(2) }}</p>
              </div>
              <i class="pi pi-dollar text-3xl text-success-500" />
            </div>

            <p v-if="resumenHoras.observacion" class="text-xs text-ink-500 italic">{{ resumenHoras.observacion }}</p>
          </div>

          <p v-else class="text-sm text-ink-500 italic">No se pudo cargar el resumen.</p>
        </div>

        <!-- Licencia de conducir -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-id-card text-brand-600" />
            Licencia de conducir
          </h3>

          <div v-if="editando.licencia" class="space-y-4">
            <div class="rounded-lg bg-info-50 border border-info-200 p-2 text-xs text-ink-700 flex items-start gap-2">
              <i class="pi pi-info-circle text-info-600 mt-0.5" />
              <p>El número de licencia coincide con la cédula del instructor y no se puede modificar.</p>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Número de licencia</label>
                <InputText :value="instructor.cedula" class="w-full" disabled />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Categoría *</label>
                <div class="grid grid-cols-6 gap-1">
                  <button
                    v-for="cat in ['A', 'B', 'C', 'D', 'E', 'F']"
                    :key="cat"
                    type="button"
                    @click="formLicencia.licenciaCategoria = cat"
                    :class="['h-10 rounded-lg border-2 font-bold text-sm transition-all',
                      formLicencia.licenciaCategoria === cat
                        ? 'border-brand-600 bg-brand-50 text-brand-700'
                        : 'border-ink-200 bg-white text-ink-600 hover:border-brand-300']"
                  >{{ cat }}</button>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de emisión *</label>
                <Calendar v-model="formLicencia.licenciaEmision" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
              </div>
              <div>
                <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de caducidad *</label>
                <Calendar v-model="formLicencia.licenciaCaducidad" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
              </div>
            </div>
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarLicencia()" :loading="guardando.licencia" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.licencia = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <DetailRow label="Número" :value="instructor.licenciaNumero" />
            <DetailRow label="Categoría" :value="instructor.licenciaCategoria" />
            <DetailRow label="Fecha de emisión" :value="instructor.licenciaEmision" type="date" />
            <DetailRow label="Fecha de caducidad" :value="instructor.licenciaCaducidad" type="date" />
            <Button label="Editar información" icon="pi pi-pencil" text @click="editarLicencia()" size="small" />
          </div>
        </div>

        <!-- Observaciones -->
        <div class="card p-5">
          <h3 class="heading-3 mb-4 flex items-center gap-2">
            <i class="pi pi-comment text-brand-600" />
            Observaciones
          </h3>

          <div v-if="editando.observaciones" class="space-y-4">
            <Textarea
              v-model="formObservaciones.observaciones"
              rows="4"
              placeholder="Notas internas sobre el instructor..."
              class="w-full"
            />
            <div class="flex gap-2">
              <Button label="Guardar" icon="pi pi-check" @click="guardarObservaciones()" :loading="guardando.observaciones" />
              <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined @click="editando.observaciones = false" />
            </div>
          </div>

          <div v-else class="space-y-3">
            <p v-if="instructor.observaciones" class="text-sm text-ink-700 whitespace-pre-wrap">
              {{ instructor.observaciones }}
            </p>
            <p v-else class="text-sm text-ink-400 italic">Sin observaciones</p>
            <Button label="Editar observaciones" icon="pi pi-pencil" text @click="editarObservaciones()" size="small" />
          </div>
        </div>
            </div>
          </TabPanel>

          <!-- Tab: Horario y disponibilidad -->
          <TabPanel header="Horario y disponibilidad">
            <div class="space-y-6">
              <!-- Sección: Disponibilidad semanal recurrente -->
              <div class="card p-5">
                <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
                  <h3 class="heading-3 flex items-center gap-2">
                    <i class="pi pi-calendar text-brand-600" />
                    Disponibilidad semanal recurrente
                  </h3>
                  <Button label="Agregar franja" icon="pi pi-plus" size="small" @click="abrirDialogFranja()" />
                </div>

                <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700 mb-4 flex items-start gap-2">
                  <i class="pi pi-info-circle text-info-600 mt-0.5" />
                  <p>
                    Este es el horario <strong>base recurrente</strong> del instructor. Aplica todas las semanas.
                    Para faltas puntuales o turnos extra usa "Excepciones" abajo.
                    <span v-if="horasSemanalesConfiguradas > 0" class="ml-1">
                      Total: <strong>{{ horasSemanalesConfiguradas }} h/sem</strong>
                      <span v-if="instructor.horasContratoSemanales && horasSemanalesConfiguradas > instructor.horasContratoSemanales" class="text-warning-700">
                        (excede las {{ instructor.horasContratoSemanales }} h del contrato)
                      </span>
                    </span>
                  </p>
                </div>

                <div v-if="cargandoDisponibilidad" class="text-center py-6">
                  <ProgressSpinner style="width: 36px; height: 36px" />
                </div>

                <div v-else class="grid grid-cols-1 md:grid-cols-7 gap-3">
                  <div
                    v-for="dia in DIAS_SEMANA"
                    :key="dia.value"
                    class="rounded-lg border border-ink-200 bg-ink-50 p-3 min-h-[140px] flex flex-col"
                  >
                    <div class="flex items-center justify-between mb-2 pb-2 border-b border-ink-200">
                      <span class="text-sm font-bold text-ink-900">{{ dia.short }}</span>
                      <Button
                        icon="pi pi-plus"
                        text
                        rounded
                        size="small"
                        severity="success"
                        v-tooltip="`Agregar franja ${dia.label.toLowerCase()}`"
                        @click="abrirDialogFranja(dia.value)"
                      />
                    </div>
                    <div class="flex-1 space-y-1.5">
                      <div
                        v-for="f in franjasPorDia[dia.value]"
                        :key="f.id"
                        class="group p-2 rounded-md bg-brand-50 border border-brand-200 text-xs hover:bg-brand-100 transition"
                      >
                        <div class="flex items-center justify-between gap-1">
                          <span class="font-semibold text-brand-800">
                            {{ f.horaInicio.substring(0, 5) }} - {{ f.horaFin.substring(0, 5) }}
                          </span>
                          <div class="flex opacity-0 group-hover:opacity-100 transition">
                            <Button
                              icon="pi pi-pencil"
                              text
                              rounded
                              size="small"
                              v-tooltip="'Editar'"
                              @click="abrirDialogEditarFranja(f)"
                            />
                            <Button
                              icon="pi pi-trash"
                              text
                              rounded
                              size="small"
                              severity="danger"
                              v-tooltip="'Eliminar'"
                              @click="confirmarEliminarFranja(f)"
                            />
                          </div>
                        </div>
                      </div>
                      <p v-if="franjasPorDia[dia.value].length === 0" class="text-xs text-ink-400 italic">No trabaja</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Sección: Excepciones (ausencias / horas extra) -->
              <div class="card p-5">
                <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
                  <h3 class="heading-3 flex items-center gap-2">
                    <i class="pi pi-calendar-times text-brand-600" />
                    Excepciones puntuales
                  </h3>
                  <Button label="Agregar excepción" icon="pi pi-plus" size="small" @click="abrirDialogExcepcion()" />
                </div>

                <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700 mb-4 flex items-start gap-2">
                  <i class="pi pi-info-circle text-info-600 mt-0.5" />
                  <p>
                    Modifican el horario recurrente en un día específico.
                    <span class="px-1.5 py-0.5 mx-0.5 rounded bg-danger-100 text-danger-700 text-[10px] font-semibold">AUSENCIA</span> bloquea todo el día,
                    <span class="px-1.5 py-0.5 mx-0.5 rounded bg-success-100 text-success-700 text-[10px] font-semibold">EXTRA</span> agrega una franja adicional.
                  </p>
                </div>

                <div v-if="cargandoExcepciones" class="text-center py-6">
                  <ProgressSpinner style="width: 36px; height: 36px" />
                </div>

                <div v-else-if="excepciones.length === 0" class="text-center py-8">
                  <i class="pi pi-calendar text-4xl text-ink-300 mb-2" />
                  <p class="text-sm text-ink-500">Sin excepciones registradas</p>
                  <p class="text-xs text-ink-400 mt-1">Cuando programes una ausencia o turno extra aparecerá aquí.</p>
                </div>

                <div v-else class="space-y-4">
                  <!-- Próximas -->
                  <div v-if="excepcionesFuturas.length > 0">
                    <p class="text-xs font-semibold text-ink-600 uppercase mb-2">Próximas ({{ excepcionesFuturas.length }})</p>
                    <div class="space-y-2">
                      <div
                        v-for="e in excepcionesFuturas"
                        :key="e.id"
                        class="p-3 rounded-lg border-l-4 flex items-start justify-between gap-3"
                        :class="e.tipo === 'AUSENCIA' ? 'bg-danger-50 border-l-danger-500' : 'bg-success-50 border-l-success-500'"
                      >
                        <div class="flex-1 min-w-0">
                          <div class="flex items-center gap-2 mb-1 flex-wrap">
                            <span
                              :class="['inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold',
                                e.tipo === 'AUSENCIA' ? 'bg-danger-100 text-danger-700' : 'bg-success-100 text-success-700']"
                            >
                              <i :class="e.tipo === 'AUSENCIA' ? 'pi pi-times-circle mr-1' : 'pi pi-plus-circle mr-1'" />
                              {{ e.tipo }}
                            </span>
                            <span class="text-sm font-semibold text-ink-900">
                              {{ fmtFechaLocal(e.fecha, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) }}
                            </span>
                            <span v-if="e.horaInicio && e.horaFin" class="text-xs font-medium text-ink-600">
                              · {{ e.horaInicio.substring(0, 5) }} - {{ e.horaFin.substring(0, 5) }}
                            </span>
                          </div>
                          <p v-if="e.motivo" class="text-xs text-ink-600 italic">"{{ e.motivo }}"</p>
                        </div>
                        <Button
                          icon="pi pi-trash"
                          text
                          rounded
                          size="small"
                          severity="danger"
                          v-tooltip="'Eliminar'"
                          @click="confirmarEliminarExcepcion(e)"
                        />
                      </div>
                    </div>
                  </div>

                  <!-- Pasadas -->
                  <div v-if="excepcionesPasadas.length > 0">
                    <p class="text-xs font-semibold text-ink-600 uppercase mb-2">Historial ({{ excepcionesPasadas.length }})</p>
                    <div class="space-y-2 opacity-60">
                      <div
                        v-for="e in excepcionesPasadas"
                        :key="e.id"
                        class="p-2 rounded-lg bg-ink-50 border border-ink-200 flex items-center justify-between gap-3"
                      >
                        <div class="flex items-center gap-2 text-xs text-ink-700 flex-wrap">
                          <span
                            :class="['inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold',
                              e.tipo === 'AUSENCIA' ? 'bg-danger-100 text-danger-700' : 'bg-success-100 text-success-700']"
                          >
                            {{ e.tipo }}
                          </span>
                          <span>{{ fmtFechaLocal(e.fecha) }}</span>
                          <span v-if="e.horaInicio && e.horaFin">
                            {{ e.horaInicio.substring(0, 5) }} - {{ e.horaFin.substring(0, 5) }}
                          </span>
                          <span v-if="e.motivo" class="italic text-ink-500">— {{ e.motivo }}</span>
                        </div>
                        <Button
                          icon="pi pi-trash"
                          text
                          rounded
                          size="small"
                          severity="danger"
                          @click="confirmarEliminarExcepcion(e)"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </TabPanel>

          <!-- Tab: Sueldos pagados -->
          <TabPanel header="Sueldos">
            <div class="card p-5 space-y-4">
              <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-start gap-2 text-xs text-ink-700">
                <i class="pi pi-info-circle text-info-600 mt-0.5" />
                <span>
                  Historial de sueldos pagados a este instructor. Los registros se crean desde
                  <router-link to="/finanzas/gastos" class="underline font-medium">Finanzas → Gastos</router-link>
                  con categoría "Sueldos instructores".
                </span>
              </div>

              <div v-if="cargandoSueldos" class="text-center py-8"><ProgressSpinner style="width:32px;height:32px" /></div>
              <div v-else-if="sueldosPagados.length === 0" class="text-center py-10">
                <i class="pi pi-money-bill text-4xl text-ink-300 mb-2" />
                <p class="text-sm text-ink-500">Sin sueldos registrados.</p>
                <router-link to="/finanzas/gastos" class="text-xs text-brand-700 hover:underline mt-1 inline-block">
                  Registrar el primero <i class="pi pi-external-link text-[10px]" />
                </router-link>
              </div>
              <div v-else>
                <div class="p-4 rounded-lg bg-gradient-to-br from-brand-600 to-brand-700 text-white mb-4">
                  <p class="text-xs text-white/80 uppercase tracking-wider">Total pagado</p>
                  <p class="text-3xl font-bold mt-1">${{ totalSueldos.toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</p>
                  <p class="text-xs text-white/70 mt-1">{{ sueldosPagados.length }} pago{{ sueldosPagados.length === 1 ? '' : 's' }} registrado{{ sueldosPagados.length === 1 ? '' : 's' }}</p>
                </div>

                <table class="w-full text-sm">
                  <thead>
                    <tr class="border-b border-ink-200 text-left text-xs text-ink-600 uppercase">
                      <th class="py-2 pr-2 font-medium">Fecha</th>
                      <th class="py-2 px-2 font-medium">Cuenta</th>
                      <th class="py-2 px-2 font-medium">Descripción</th>
                      <th class="py-2 pl-2 font-medium text-right">Monto</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in sueldosPagados" :key="s.id" class="border-b border-ink-100 hover:bg-ink-50">
                      <td class="py-2 pr-2 text-ink-900 whitespace-nowrap">{{ s.fecha }}</td>
                      <td class="py-2 px-2 text-ink-700 text-xs">{{ s.cuentaNombre }}</td>
                      <td class="py-2 px-2 text-ink-700 text-xs">{{ s.descripcion || '—' }}</td>
                      <td class="py-2 pl-2 text-right font-semibold text-danger-700">− ${{ Number(s.monto).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </TabPanel>
        </TabView>
      </div>
    </div>

    <Toast />
    <ConfirmDialog />

    <!-- Dialog: cambiar estado -->
    <Dialog
      v-model:visible="dialogEstadoVisible"
      modal
      header="Cambiar estado del instructor"
      :style="{ width: '440px' }"
      :closable="!cambiandoEstado"
    >
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700">
          <p>Estado actual: <strong>{{ instructor.estado }}</strong></p>
          <p class="mt-1 text-ink-600">
            Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>
        <div>
          <label for="field-inst-estado" class="block text-sm font-medium text-ink-700 mb-1.5">
            Nuevo estado <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Dropdown
            v-model="nuevoEstado"
            inputId="field-inst-estado"
            :options="estadosOpciones"
            option-label="label"
            option-value="value"
            placeholder="Selecciona estado"
            class="w-full border border-ink-300 bg-ink-50"
            :class="errorsInstEst.estado ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrInstEst('estado')"
          />
          <p v-if="errorsInstEst.estado" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsInstEst.estado }}
          </p>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" outlined @click="dialogEstadoVisible = false" :disabled="cambiandoEstado" />
        <Button label="Cambiar estado" icon="pi pi-check" @click="cambiarEstado" :loading="cambiandoEstado" />
      </template>
    </Dialog>

    <!-- Dialog: agregar/editar franja semanal -->
    <Dialog
      v-model:visible="dialogFranjaVisible"
      modal
      :header="editandoFranjaId ? 'Editar franja' : 'Nueva franja semanal'"
      :style="{ width: '460px' }"
      :closable="!guardandoFranja"
    >
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>
        <div>
          <label for="field-franja-diaSemana" class="block text-sm font-medium text-ink-700 mb-1.5">
            Día de la semana <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Dropdown
            v-model="formFranja.diaSemana"
            inputId="field-franja-diaSemana"
            :options="DIAS_SEMANA"
            option-label="label"
            option-value="value"
            placeholder="Selecciona el día"
            class="w-full border border-ink-300 bg-ink-50"
            :class="errorsFranja.diaSemana ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrFranja('diaSemana')"
          />
          <p v-if="errorsFranja.diaSemana" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsFranja.diaSemana }}
          </p>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label for="field-franja-horaInicio" class="block text-sm font-medium text-ink-700 mb-1.5">
              Hora inicio <span class="text-danger-600 font-semibold">*</span>
            </label>
            <input
              id="field-franja-horaInicio"
              v-model="formFranja.horaInicio"
              type="time"
              :class="['w-full px-3 py-2 border rounded-lg text-sm',
                errorsFranja.horaInicio ? 'border-danger-500 bg-danger-50' : 'border-ink-300 bg-ink-50']"
              @input="clearErrFranja('horaInicio')"
            />
            <p v-if="errorsFranja.horaInicio" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsFranja.horaInicio }}
            </p>
          </div>
          <div>
            <label for="field-franja-horaFin" class="block text-sm font-medium text-ink-700 mb-1.5">
              Hora fin <span class="text-danger-600 font-semibold">*</span>
            </label>
            <input
              id="field-franja-horaFin"
              v-model="formFranja.horaFin"
              type="time"
              :class="['w-full px-3 py-2 border rounded-lg text-sm',
                errorsFranja.horaFin ? 'border-danger-500 bg-danger-50' : 'border-ink-300 bg-ink-50']"
              @input="clearErrFranja('horaFin')"
            />
            <p v-if="errorsFranja.horaFin" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsFranja.horaFin }}
            </p>
          </div>
        </div>
        <div class="rounded-lg bg-info-50 border border-info-200 p-2 text-xs text-ink-700 flex items-start gap-2">
          <i class="pi pi-info-circle text-info-600 mt-0.5" />
          <p>Esta franja se repetirá cada semana. Para una ausencia puntual o turno extra, usa "Excepciones".</p>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" outlined @click="dialogFranjaVisible = false" :disabled="guardandoFranja" />
        <Button :label="editandoFranjaId ? 'Guardar cambios' : 'Agregar franja'" icon="pi pi-check" @click="guardarFranja" :loading="guardandoFranja" />
      </template>
    </Dialog>

    <!-- Dialog: agregar excepción -->
    <Dialog
      v-model:visible="dialogExcepcionVisible"
      modal
      header="Nueva excepción"
      :style="{ width: '500px' }"
      :closable="!guardandoExcepcion"
    >
      <div class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
          <i class="pi pi-info-circle text-info-600" />
          <p class="text-sm text-ink-700">
            Los campos con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-2">
            Tipo de excepción <span class="text-danger-600 font-semibold">*</span>
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              @click="formExcepcion.tipo = 'AUSENCIA'"
              :class="['p-3 rounded-lg border-2 text-left transition',
                formExcepcion.tipo === 'AUSENCIA'
                  ? 'border-danger-500 bg-danger-50'
                  : 'border-ink-200 bg-white hover:border-danger-300']"
            >
              <div class="flex items-center gap-2 mb-1">
                <i :class="['pi pi-times-circle', formExcepcion.tipo === 'AUSENCIA' ? 'text-danger-600' : 'text-ink-500']" />
                <span :class="['font-semibold text-sm', formExcepcion.tipo === 'AUSENCIA' ? 'text-danger-800' : 'text-ink-900']">
                  Ausencia
                </span>
              </div>
              <p class="text-xs text-ink-600">El instructor no trabaja ese día (bloquea todo)</p>
            </button>
            <button
              type="button"
              @click="formExcepcion.tipo = 'EXTRA'"
              :class="['p-3 rounded-lg border-2 text-left transition',
                formExcepcion.tipo === 'EXTRA'
                  ? 'border-success-500 bg-success-50'
                  : 'border-ink-200 bg-white hover:border-success-300']"
            >
              <div class="flex items-center gap-2 mb-1">
                <i :class="['pi pi-plus-circle', formExcepcion.tipo === 'EXTRA' ? 'text-success-600' : 'text-ink-500']" />
                <span :class="['font-semibold text-sm', formExcepcion.tipo === 'EXTRA' ? 'text-success-800' : 'text-ink-900']">
                  Hora extra
                </span>
              </div>
              <p class="text-xs text-ink-600">Agrega una franja adicional ese día</p>
            </button>
          </div>
        </div>

        <div>
          <label for="field-exc-fecha" class="block text-sm font-medium text-ink-700 mb-1.5">
            Fecha <span class="text-danger-600 font-semibold">*</span>
          </label>
          <Calendar
            v-model="formExcepcion.fecha"
            inputId="field-exc-fecha"
            dateFormat="yy-mm-dd"
            :showIcon="true"
            class="w-full"
            :minDate="new Date()"
            :inputClass="errorsExc.fecha ? '!border-danger-500 !bg-danger-50' : ''"
            @update:modelValue="clearErrExc('fecha')"
          />
          <p v-if="errorsExc.fecha" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
            <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsExc.fecha }}
          </p>
        </div>

        <div v-if="formExcepcion.tipo === 'EXTRA'" class="grid grid-cols-2 gap-3 animate-fade-up">
          <div>
            <label for="field-exc-horaInicio" class="block text-sm font-medium text-ink-700 mb-1.5">
              Hora inicio <span class="text-danger-600 font-semibold">*</span>
            </label>
            <input
              id="field-exc-horaInicio"
              v-model="formExcepcion.horaInicio"
              type="time"
              :class="['w-full px-3 py-2 border rounded-lg text-sm',
                errorsExc.horaInicio ? 'border-danger-500 bg-danger-50' : 'border-ink-300 bg-ink-50']"
              @input="clearErrExc('horaInicio')"
            />
            <p v-if="errorsExc.horaInicio" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsExc.horaInicio }}
            </p>
          </div>
          <div>
            <label for="field-exc-horaFin" class="block text-sm font-medium text-ink-700 mb-1.5">
              Hora fin <span class="text-danger-600 font-semibold">*</span>
            </label>
            <input
              id="field-exc-horaFin"
              v-model="formExcepcion.horaFin"
              type="time"
              :class="['w-full px-3 py-2 border rounded-lg text-sm',
                errorsExc.horaFin ? 'border-danger-500 bg-danger-50' : 'border-ink-300 bg-ink-50']"
              @input="clearErrExc('horaFin')"
            />
            <p v-if="errorsExc.horaFin" class="text-xs text-danger-600 mt-1 flex items-center gap-1">
              <i class="pi pi-exclamation-circle text-[10px]" />{{ errorsExc.horaFin }}
            </p>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">
            Motivo <span class="text-xs text-ink-500">(opcional)</span>
          </label>
          <Textarea
            v-model="formExcepcion.motivo"
            rows="2"
            placeholder="Ej: Capacitación ANT, enfermedad, evento especial..."
            class="w-full"
            maxlength="500"
          />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" outlined @click="dialogExcepcionVisible = false" :disabled="guardandoExcepcion" />
        <Button label="Agregar excepción" icon="pi pi-check" @click="guardarExcepcion" :loading="guardandoExcepcion" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import ProgressSpinner from 'primevue/progressspinner'
import Tooltip from 'primevue/tooltip'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import Avatar from '@/components/ui/Avatar.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import DetailRow from '@/components/ui/DetailRow.vue'
import instructoresService, {
  type InstructorResponse,
  type ResumenHorasResponse,
  type TipoContrato,
  type DisponibilidadResponse,
  type HorarioTrabajoResponse,
  type TipoExcepcion
} from '@/services/instructores'
import api from '@/services/api'
import { fmtFechaLocal } from '@/utils/fechas'

const vTooltip = Tooltip
const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const instructorId = computed(() => parseInt(route.params.id as string))

const loading = ref(false)
const instructor = reactive<InstructorResponse>({
  id: 0,
  cedula: '',
  nombre: '',
  apellido: '',
  email: '',
  telefono: '',
  direccion: '',
  fechaNacimiento: '',
  licenciaNumero: '',
  licenciaCategoria: '',
  licenciaEmision: '',
  licenciaCaducidad: '',
  estado: 'ACTIVO',
  fechaContratacion: '',
  salarioMensual: 0,
  tipoContrato: 'TIEMPO_COMPLETO',
  horasContratoSemanales: 40,
  tarifaHora: 0,
  observaciones: ''
})

const TIPOS_CONTRATO: Record<TipoContrato, { label: string; icon: string; desc: string }> = {
  TIEMPO_COMPLETO: { label: 'Tiempo completo', icon: 'pi pi-briefcase', desc: '~40 h/semana, salario fijo' },
  MEDIO_TIEMPO:    { label: 'Medio tiempo',    icon: 'pi pi-clock',     desc: '~20 h/semana, salario fijo' },
  POR_HORAS:       { label: 'Por horas',       icon: 'pi pi-calculator', desc: 'Pago variable por hora' }
}

// ----- Estados de edición inline -----
const editando = reactive({
  personal: false,
  contacto: false,
  laboral: false,
  contrato: false,
  licencia: false,
  observaciones: false
})
const guardando = reactive({
  personal: false,
  contacto: false,
  laboral: false,
  contrato: false,
  licencia: false,
  observaciones: false
})

const formPersonal = reactive<any>({ nombre: '', apellido: '', cedula: '', fechaNacimiento: null })
const formContacto = reactive<any>({ email: '', telefono: '', direccion: '' })
const formLaboral = reactive<any>({ fechaContratacion: null })
const formContrato = reactive<any>({
  tipoContrato: 'TIEMPO_COMPLETO' as TipoContrato,
  horasContratoSemanales: 40,
  salarioMensual: 0,
  tarifaHora: null
})
const formLicencia = reactive<any>({
  licenciaNumero: '',
  licenciaCategoria: '',
  licenciaEmision: null,
  licenciaCaducidad: null
})
const formObservaciones = reactive<any>({ observaciones: '' })

// ----- Resumen de horas (mes actual por default) -----
const resumenHoras = ref<ResumenHorasResponse | null>(null)
const cargandoResumen = ref(false)
const rangoResumen = reactive({
  desde: '',
  hasta: ''
})

// ----- Tabs -----
const activeTab = ref(0) // 0 = Información, 1 = Horario

// ----- Horario semanal recurrente -----
const disponibilidad = ref<DisponibilidadResponse[]>([])
const cargandoDisponibilidad = ref(false)
const dialogFranjaVisible = ref(false)
const guardandoFranja = ref(false)
const editandoFranjaId = ref<number | null>(null)
const formFranja = reactive<{ diaSemana: number; horaInicio: string; horaFin: string }>({
  diaSemana: 1,
  horaInicio: '08:00',
  horaFin: '12:00'
})

const DIAS_SEMANA = [
  { value: 1, label: 'Lunes',     short: 'Lun' },
  { value: 2, label: 'Martes',    short: 'Mar' },
  { value: 3, label: 'Miércoles', short: 'Mié' },
  { value: 4, label: 'Jueves',    short: 'Jue' },
  { value: 5, label: 'Viernes',   short: 'Vie' },
  { value: 6, label: 'Sábado',    short: 'Sáb' },
  { value: 7, label: 'Domingo',   short: 'Dom' }
]

const franjasPorDia = computed(() => {
  const map: Record<number, DisponibilidadResponse[]> = {}
  for (let i = 1; i <= 7; i++) map[i] = []
  for (const f of disponibilidad.value) {
    if (!map[f.diaSemana]) map[f.diaSemana] = []
    map[f.diaSemana].push(f)
  }
  for (const k of Object.keys(map)) {
    map[+k].sort((a, b) => a.horaInicio.localeCompare(b.horaInicio))
  }
  return map
})

const horasSemanalesConfiguradas = computed(() => {
  const minutosToHHMM = (hhmm: string): number => {
    const [h, m] = hhmm.split(':').map(Number)
    return h * 60 + (m || 0)
  }
  let total = 0
  for (const f of disponibilidad.value) {
    total += (minutosToHHMM(f.horaFin) - minutosToHHMM(f.horaInicio))
  }
  return Math.round((total / 60) * 10) / 10
})

const resumenHorarioCorto = computed(() => {
  if (disponibilidad.value.length === 0) return 'Sin horario configurado'
  // Calcular qué días tienen al menos 1 franja
  const dias = new Set(disponibilidad.value.map(f => f.diaSemana))
  const diasOrdenados = Array.from(dias).sort()
  if (diasOrdenados.length === 5 && diasOrdenados.join(',') === '1,2,3,4,5') return 'L-V'
  if (diasOrdenados.length === 6 && diasOrdenados.join(',') === '1,2,3,4,5,6') return 'L-S'
  if (diasOrdenados.length === 7) return 'Todos los días'
  return diasOrdenados.map(d => DIAS_SEMANA[d - 1]?.short).join(', ')
})

// ----- Excepciones (ausencias y horas extra) -----
const excepciones = ref<HorarioTrabajoResponse[]>([])
const cargandoExcepciones = ref(false)
const dialogExcepcionVisible = ref(false)
const guardandoExcepcion = ref(false)
const formExcepcion = reactive<{
  fecha: Date | null
  tipo: TipoExcepcion
  horaInicio: string
  horaFin: string
  motivo: string
}>({
  fecha: null,
  tipo: 'AUSENCIA',
  horaInicio: '',
  horaFin: '',
  motivo: ''
})

const excepcionesOrdenadas = computed(() =>
  [...excepciones.value].sort((a, b) => a.fecha.localeCompare(b.fecha))
)

const excepcionesFuturas = computed(() => {
  const hoy = new Date().toISOString().substring(0, 10)
  return excepcionesOrdenadas.value.filter(e => e.fecha >= hoy)
})

const excepcionesPasadas = computed(() => {
  const hoy = new Date().toISOString().substring(0, 10)
  return excepcionesOrdenadas.value.filter(e => e.fecha < hoy).reverse() // más recientes primero
})

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

const valInstEst = useValidation()
const errorsInstEst = valInstEst.errors
const setErrInstEst = valInstEst.setError
const clearErrInstEst = valInstEst.clearError
const clearAllInstEst = valInstEst.clearAll

const valFranja = useValidation()
const errorsFranja = valFranja.errors
const setErrFranja = valFranja.setError
const clearErrFranja = valFranja.clearError
const clearAllFranja = valFranja.clearAll

const valExc = useValidation()
const errorsExc = valExc.errors
const setErrExc = valExc.setError
const clearErrExc = valExc.clearError
const clearAllExc = valExc.clearAll

// ----- Dialog cambiar estado -----
const dialogEstadoVisible = ref(false)
const cambiandoEstado = ref(false)
const nuevoEstado = ref<string | null>(null)
const estadosOpciones = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'Inactivo', value: 'INACTIVO' },
  { label: 'Suspendido', value: 'SUSPENDIDO' }
]

// ----- Helpers -----
const fmtFecha = (v: any): string | undefined => {
  if (!v) return undefined
  if (typeof v === 'string') return v.substring(0, 10)
  if (v instanceof Date) {
    return `${v.getFullYear()}-${String(v.getMonth() + 1).padStart(2, '0')}-${String(v.getDate()).padStart(2, '0')}`
  }
  return undefined
}

const parseFecha = (v?: string): Date | null => {
  if (!v) return null
  const parts = v.substring(0, 10).split('-')
  if (parts.length !== 3) return null
  return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
}

const diasParaVencerLicencia = computed(() => {
  if (!instructor.licenciaCaducidad) return null
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  const venc = parseFecha(instructor.licenciaCaducidad)
  if (!venc) return null
  return Math.floor((venc.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24))
})

// ----- Carga -----
const cargar = async () => {
  loading.value = true
  try {
    const data = await instructoresService.obtenerInstructor(instructorId.value)
    Object.assign(instructor, data)
    cargarResumenHoras()
    cargarDisponibilidad()
    cargarExcepciones()
    cargarSueldos()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo cargar el instructor',
      life: 4000
    })
    setTimeout(() => router.back(), 1200)
  } finally {
    loading.value = false
  }
}

// ============ HORARIO SEMANAL ============
const cargarDisponibilidad = async () => {
  cargandoDisponibilidad.value = true
  try {
    disponibilidad.value = await instructoresService.listarDisponibilidadSemanal(instructorId.value)
  } catch (e) {
    console.warn('No se pudo cargar disponibilidad', e)
    disponibilidad.value = []
  } finally {
    cargandoDisponibilidad.value = false
  }
}

const abrirDialogFranja = (diaPrefill?: number) => {
  editandoFranjaId.value = null
  formFranja.diaSemana = diaPrefill ?? 1
  formFranja.horaInicio = '08:00'
  formFranja.horaFin = '12:00'
  clearAllFranja()
  dialogFranjaVisible.value = true
}

const abrirDialogEditarFranja = (f: DisponibilidadResponse) => {
  editandoFranjaId.value = f.id
  formFranja.diaSemana = f.diaSemana
  formFranja.horaInicio = f.horaInicio.substring(0, 5)
  formFranja.horaFin = f.horaFin.substring(0, 5)
  clearAllFranja()
  dialogFranjaVisible.value = true
}

const validarFranja = (): boolean => {
  clearAllFranja()
  if (formFranja.diaSemana == null) setErrFranja('diaSemana', 'Selecciona el día')
  if (!formFranja.horaInicio) setErrFranja('horaInicio', 'La hora de inicio es requerida')
  if (!formFranja.horaFin) setErrFranja('horaFin', 'La hora de fin es requerida')
  if (formFranja.horaInicio && formFranja.horaFin) {
    if (formFranja.horaFin <= formFranja.horaInicio) {
      setErrFranja('horaFin', 'La hora de fin debe ser posterior al inicio')
    } else {
      const [hi, mi] = formFranja.horaInicio.split(':').map(Number)
      const [hf, mf] = formFranja.horaFin.split(':').map(Number)
      const diff = (hf * 60 + mf) - (hi * 60 + mi)
      if (diff < 30) setErrFranja('horaFin', 'La franja debe durar al menos 30 minutos')
    }
  }
  if (Object.keys(errorsFranja).length > 0) {
    valFranja.focusFirst(['diaSemana', 'horaInicio', 'horaFin'], 'franja', false)
    return false
  }
  return true
}

const guardarFranja = async () => {
  if (!validarFranja()) return
  guardandoFranja.value = true
  try {
    const payload = {
      diaSemana: formFranja.diaSemana,
      horaInicio: formFranja.horaInicio + ':00',
      horaFin: formFranja.horaFin + ':00'
    }
    if (editandoFranjaId.value) {
      await instructoresService.actualizarDisponibilidadSemanal(instructorId.value, editandoFranjaId.value, payload)
      toast.add({ severity: 'success', summary: 'Franja actualizada', detail: 'Cambios guardados', life: 2500 })
    } else {
      await instructoresService.agregarDisponibilidadSemanal(instructorId.value, payload)
      toast.add({ severity: 'success', summary: 'Franja agregada', detail: 'Horario actualizado', life: 2500 })
    }
    dialogFranjaVisible.value = false
    await cargarDisponibilidad()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar la franja',
      life: 4000
    })
  } finally {
    guardandoFranja.value = false
  }
}

const confirmarEliminarFranja = (f: DisponibilidadResponse) => {
  confirm.require({
    message: `¿Eliminar la franja ${DIAS_SEMANA[f.diaSemana - 1]?.label} ${f.horaInicio.substring(0, 5)} - ${f.horaFin.substring(0, 5)}?`,
    header: 'Eliminar franja',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await instructoresService.eliminarDisponibilidadSemanal(instructorId.value, f.id)
        toast.add({ severity: 'success', summary: 'Eliminada', detail: 'Franja eliminada', life: 2500 })
        await cargarDisponibilidad()
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

// ============ EXCEPCIONES ============
const cargarExcepciones = async () => {
  cargandoExcepciones.value = true
  try {
    excepciones.value = await instructoresService.listarHorariosTrabajo(instructorId.value)
  } catch (e) {
    console.warn('No se pudieron cargar excepciones', e)
    excepciones.value = []
  } finally {
    cargandoExcepciones.value = false
  }
}

const abrirDialogExcepcion = () => {
  formExcepcion.fecha = null
  formExcepcion.tipo = 'AUSENCIA'
  formExcepcion.horaInicio = ''
  formExcepcion.horaFin = ''
  formExcepcion.motivo = ''
  clearAllExc()
  dialogExcepcionVisible.value = true
}

const validarExcepcion = (): boolean => {
  clearAllExc()
  if (!formExcepcion.fecha) setErrExc('fecha', 'La fecha es requerida')
  if (formExcepcion.tipo === 'EXTRA') {
    if (!formExcepcion.horaInicio) setErrExc('horaInicio', 'La hora de inicio es requerida')
    if (!formExcepcion.horaFin) setErrExc('horaFin', 'La hora de fin es requerida')
    if (formExcepcion.horaInicio && formExcepcion.horaFin && formExcepcion.horaFin <= formExcepcion.horaInicio) {
      setErrExc('horaFin', 'La hora de fin debe ser posterior al inicio')
    }
  }
  if (Object.keys(errorsExc).length > 0) {
    valExc.focusFirst(['fecha', 'horaInicio', 'horaFin'], 'exc', false)
    return false
  }
  return true
}

const guardarExcepcion = async () => {
  if (!validarExcepcion()) return
  guardandoExcepcion.value = true
  try {
    await instructoresService.agregarHorarioTrabajo(instructorId.value, {
      fecha: fmtFecha(formExcepcion.fecha)!,
      tipo: formExcepcion.tipo,
      horaInicio: formExcepcion.tipo === 'EXTRA' ? formExcepcion.horaInicio + ':00' : undefined,
      horaFin: formExcepcion.tipo === 'EXTRA' ? formExcepcion.horaFin + ':00' : undefined,
      motivo: formExcepcion.motivo?.trim() || undefined
    })
    toast.add({ severity: 'success', summary: 'Excepción agregada', detail: 'Registro guardado', life: 2500 })
    dialogExcepcionVisible.value = false
    await cargarExcepciones()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo agregar la excepción',
      life: 4000
    })
  } finally {
    guardandoExcepcion.value = false
  }
}

const confirmarEliminarExcepcion = (e: HorarioTrabajoResponse) => {
  confirm.require({
    message: `¿Eliminar la excepción del ${fmtFechaLocal(e.fecha)} (${e.tipo})?`,
    header: 'Eliminar excepción',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await instructoresService.eliminarHorarioTrabajo(instructorId.value, e.id)
        toast.add({ severity: 'success', summary: 'Eliminada', detail: 'Excepción eliminada', life: 2500 })
        await cargarExcepciones()
      } catch (err: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: err.response?.data?.detail || 'No se pudo eliminar',
          life: 4000
        })
      }
    }
  })
}

const fmtDateISO = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const inicializarRango = () => {
  if (rangoResumen.desde && rangoResumen.hasta) return
  const hoy = new Date()
  const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), 1)
  const fin = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0)
  rangoResumen.desde = fmtDateISO(inicio)
  rangoResumen.hasta = fmtDateISO(fin)
}

const cargarResumenHoras = async () => {
  inicializarRango()
  cargandoResumen.value = true
  try {
    resumenHoras.value = await instructoresService.obtenerResumenHoras(
      instructorId.value,
      rangoResumen.desde,
      rangoResumen.hasta
    )
  } catch (e: any) {
    console.warn('No se pudo cargar resumen de horas', e)
    resumenHoras.value = null
  } finally {
    cargandoResumen.value = false
  }
}

// ----- Sueldos pagados (movimientos contables categoria SUELDO_INSTRUCTOR vinculados) -----
const sueldosPagados = ref<any[]>([])
const cargandoSueldos = ref(false)
const totalSueldos = computed(() => sueldosPagados.value.reduce((s, m) => s + Number(m.monto || 0), 0))

const cargarSueldos = async () => {
  cargandoSueldos.value = true
  try {
    const { data } = await api.get('/movimientos', {
      params: { pagadoAId: instructorId.value, tipo: 'GASTO', size: 200 }
    })
    // Filtro adicional para asegurar que solo veamos sueldos de instructor
    sueldosPagados.value = (data?.content || []).filter(
      (m: any) => m.categoriaCodigo === 'SUELDO_INSTRUCTOR'
    )
  } catch (e) {
    console.warn('No se pudieron cargar los sueldos', e)
    sueldosPagados.value = []
  } finally { cargandoSueldos.value = false }
}

// ----- Editar Personal -----
const editarPersonal = () => {
  Object.assign(formPersonal, {
    nombre: instructor.nombre,
    apellido: instructor.apellido,
    cedula: instructor.cedula,
    fechaNacimiento: parseFecha(instructor.fechaNacimiento)
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
    await instructoresService.actualizarInstructor(instructorId.value, {
      nombre: formPersonal.nombre.trim(),
      apellido: formPersonal.apellido.trim(),
      fechaNacimiento: fmtFecha(formPersonal.fechaNacimiento)
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Información personal guardada', life: 3000 })
    editando.personal = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.personal = false
  }
}

// ----- Editar Contacto -----
const editarContacto = () => {
  Object.assign(formContacto, {
    email: instructor.email,
    telefono: instructor.telefono,
    direccion: instructor.direccion || ''
  })
  editando.contacto = true
}

const guardarContacto = async () => {
  if (!formContacto.email?.trim() || !formContacto.telefono?.trim()) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Email y teléfono son requeridos', life: 3000 })
    return
  }
  guardando.contacto = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      email: formContacto.email.trim(),
      telefono: formContacto.telefono.trim(),
      direccion: formContacto.direccion?.trim() || undefined
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Información de contacto guardada', life: 3000 })
    editando.contacto = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.contacto = false
  }
}

// ----- Editar Laboral -----
const editarLaboral = () => {
  Object.assign(formLaboral, {
    fechaContratacion: parseFecha(instructor.fechaContratacion)
  })
  editando.laboral = true
}

const guardarLaboral = async () => {
  guardando.laboral = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      fechaContratacion: fmtFecha(formLaboral.fechaContratacion)
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Información laboral guardada', life: 3000 })
    editando.laboral = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.laboral = false
  }
}

// ----- Editar Contrato -----
const editarContrato = () => {
  Object.assign(formContrato, {
    tipoContrato: instructor.tipoContrato || 'TIEMPO_COMPLETO',
    horasContratoSemanales: instructor.horasContratoSemanales || 40,
    salarioMensual: instructor.salarioMensual ? Number(instructor.salarioMensual) : 0,
    tarifaHora: instructor.tarifaHora ? Number(instructor.tarifaHora) : null
  })
  editando.contrato = true
}

const seleccionarTipoContratoEnDetail = (tipo: TipoContrato) => {
  formContrato.tipoContrato = tipo
  if (tipo === 'POR_HORAS') {
    formContrato.salarioMensual = 0
    formContrato.horasContratoSemanales = formContrato.horasContratoSemanales || 10
  } else if (tipo === 'MEDIO_TIEMPO') {
    formContrato.horasContratoSemanales = 20
  } else {
    formContrato.horasContratoSemanales = 40
  }
}

const guardarContrato = async () => {
  if (formContrato.tipoContrato === 'POR_HORAS' && (!formContrato.tarifaHora || formContrato.tarifaHora <= 0)) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'La tarifa por hora es obligatoria para POR_HORAS', life: 4000 })
    return
  }
  if (formContrato.tipoContrato !== 'POR_HORAS' && (!formContrato.salarioMensual || formContrato.salarioMensual <= 0)) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'El salario mensual es obligatorio para este tipo de contrato', life: 4000 })
    return
  }
  guardando.contrato = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      tipoContrato: formContrato.tipoContrato,
      horasContratoSemanales: Number(formContrato.horasContratoSemanales),
      salarioMensual: formContrato.tipoContrato !== 'POR_HORAS' && formContrato.salarioMensual
        ? Number(formContrato.salarioMensual)
        : undefined,
      tarifaHora: formContrato.tarifaHora ? Number(formContrato.tarifaHora) : undefined
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Contrato guardado', life: 3000 })
    editando.contrato = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.contrato = false
  }
}

// ----- Editar Licencia -----
const editarLicencia = () => {
  Object.assign(formLicencia, {
    licenciaNumero: instructor.licenciaNumero,
    licenciaCategoria: instructor.licenciaCategoria,
    licenciaEmision: parseFecha(instructor.licenciaEmision),
    licenciaCaducidad: parseFecha(instructor.licenciaCaducidad)
  })
  editando.licencia = true
}

const guardarLicencia = async () => {
  if (!formLicencia.licenciaCategoria) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'La categoría de licencia es requerida', life: 3000 })
    return
  }
  if (!formLicencia.licenciaEmision || !formLicencia.licenciaCaducidad) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Fechas de emisión y caducidad son requeridas', life: 3000 })
    return
  }
  guardando.licencia = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      // Numero de licencia = cedula, no se envia (el backend lo valida)
      licenciaCategoria: formLicencia.licenciaCategoria,
      licenciaEmision: fmtFecha(formLicencia.licenciaEmision),
      licenciaCaducidad: fmtFecha(formLicencia.licenciaCaducidad)
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Licencia guardada', life: 3000 })
    editando.licencia = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.licencia = false
  }
}

// ----- Editar Observaciones -----
const editarObservaciones = () => {
  formObservaciones.observaciones = instructor.observaciones || ''
  editando.observaciones = true
}

const guardarObservaciones = async () => {
  guardando.observaciones = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      observaciones: formObservaciones.observaciones?.trim() || undefined
    })
    toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Observaciones guardadas', life: 3000 })
    editando.observaciones = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 4000
    })
  } finally {
    guardando.observaciones = false
  }
}

// ----- Cambiar estado -----
const abrirDialogEstado = () => {
  nuevoEstado.value = null
  clearAllInstEst()
  dialogEstadoVisible.value = true
}

const validarInstEstado = (): boolean => {
  clearAllInstEst()
  if (!nuevoEstado.value) setErrInstEst('estado', 'Selecciona el nuevo estado')
  if (Object.keys(errorsInstEst).length > 0) {
    valInstEst.focusFirst(['estado'], 'inst', false)
    return false
  }
  return true
}

const cambiarEstado = async () => {
  if (!validarInstEstado()) return
  cambiandoEstado.value = true
  try {
    await instructoresService.actualizarInstructor(instructorId.value, {
      estado: nuevoEstado.value as 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO'
    })
    toast.add({
      severity: 'success',
      summary: 'Estado actualizado',
      detail: `Instructor ahora está en ${nuevoEstado.value}`,
      life: 3000
    })
    dialogEstadoVisible.value = false
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

// ----- Eliminar instructor -----
const confirmarEliminar = () => {
  confirm.require({
    message: `¿Eliminar al instructor "${instructor.nombre} ${instructor.apellido}"? Esta acción no se puede deshacer.`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await instructoresService.eliminarInstructor(instructorId.value)
        toast.add({ severity: 'success', summary: 'Eliminado', detail: 'Instructor eliminado correctamente', life: 3000 })
        setTimeout(() => router.push('/instructores'), 800)
      } catch (e: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: e.response?.data?.detail || 'No se pudo eliminar el instructor',
          life: 4000
        })
      }
    }
  })
}

onMounted(cargar)
</script>
