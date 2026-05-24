<template>
  <div class="space-y-6 max-w-6xl">
    <PageHeader
      title="Configuración de la escuela"
      description="Datos institucionales que aparecen en facturas, reportes y reglas operativas del sistema."
      icon="pi pi-building"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Configuración' }]"
    />

    <!-- Banner explicativo -->
    <div class="card animate-fade-up p-5 border-l-4 !border-l-brand-600">
      <div class="flex items-start gap-4">
        <div class="w-11 h-11 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0">
          <i class="pi pi-info-circle text-lg" />
        </div>
        <div class="flex-1">
          <h3 class="heading-3 mb-1">¿Qué se configura aquí?</h3>
          <p class="text-sm text-ink-600 leading-relaxed">
            Esta pantalla controla los datos de tu <span class="font-semibold text-ink-900">escuela como empresa</span>:
            los que aparecen impresos en las <span class="font-semibold text-ink-900">facturas</span> que reciben los estudiantes,
            los <span class="font-semibold text-ink-900">horarios</span> en que se pueden programar clases,
            la <span class="font-semibold text-ink-900">identidad visual</span> de los reportes y las
            <span class="font-semibold text-ink-900">alertas automáticas</span> del sistema.
          </p>
          <p class="text-xs text-ink-500 mt-2 flex items-start gap-1.5">
            <i class="pi pi-exclamation-circle mt-0.5" />
            <span><strong class="text-ink-700">Importante:</strong> estos datos son <strong>únicos para toda la escuela</strong> y afectan a todos los usuarios. Para cambiar tu información personal (nombre, contraseña, foto) ve a <router-link to="/perfil" class="text-brand-700 hover:underline font-medium">Mi perfil</router-link>.</span>
          </p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- Sidebar navegación interna -->
      <aside class="lg:col-span-1">
        <nav class="card p-2 space-y-1 sticky top-24">
          <button
            v-for="s in secciones"
            :key="s.key"
            @click="activa = s.key"
            :class="['w-full flex items-start gap-3 px-3 py-2.5 rounded-lg text-left transition',
              activa === s.key ? 'bg-brand-50 text-brand-700' : 'text-ink-600 hover:bg-ink-100']"
          >
            <i :class="['pi', s.icon, 'text-sm mt-0.5', activa === s.key ? 'text-brand-700' : 'text-ink-400']" />
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium">{{ s.label }}</p>
              <p :class="['text-xs mt-0.5 leading-tight', activa === s.key ? 'text-brand-600/80' : 'text-ink-400']">{{ s.hint }}</p>
            </div>
          </button>
        </nav>
      </aside>

      <!-- Contenido -->
      <div class="lg:col-span-3 space-y-6">
        <!-- =============== ESCUELA =============== -->
        <FormCard
          v-if="activa === 'escuela'"
          title="Información general de la escuela"
          description="Datos legales y de contacto. Aparecen en cada factura emitida y en los reportes oficiales."
          icon="pi pi-building"
        >
          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 mb-5 flex items-start gap-2 text-xs text-info-700">
            <i class="pi pi-info-circle mt-0.5" />
            <span>Estos datos identifican a tu escuela ante el SRI (Servicio de Rentas Internas) y aparecen impresos en cada factura que generes.</span>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Nombre de la escuela" required hint="Razón social. Ej: 'Escuela de Conducción ABC S.A.'">
              <InputText v-model="config.nombre" class="w-full" />
            </Field>
            <Field label="RUC" required hint="13 dígitos. Identificador tributario ante el SRI.">
              <InputText v-model="config.ruc" maxlength="13" class="w-full !font-mono" />
            </Field>
            <Field label="Email institucional" required hint="Dirección de contacto pública. Ej: info@escuela.com">
              <InputText v-model="config.email" type="email" class="w-full" />
            </Field>
            <Field label="Teléfono" required hint="Línea principal de atención al cliente.">
              <InputText v-model="config.telefono" class="w-full" />
            </Field>
            <Field label="Dirección física" class="md:col-span-2" hint="Calle, número, sector, ciudad. Aparece en facturas.">
              <Textarea v-model="config.direccion" rows="2" class="w-full" />
            </Field>
          </div>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <!-- =============== HORARIOS =============== -->
        <FormCard
          v-if="activa === 'horarios'"
          title="Horarios y reglas operativas"
          description="Define cuándo puede operar la escuela y la duración estándar de las clases."
          icon="pi pi-clock"
        >
          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 mb-5 flex items-start gap-2 text-xs text-info-700">
            <i class="pi pi-info-circle mt-0.5" />
            <span>El sistema impedirá programar asignaciones fuera del rango horario configurado. La duración default se aplica al crear una nueva clase si no especificas otra.</span>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Hora de apertura" required hint="A qué hora la escuela comienza a operar (Ej: 07:00)">
              <Calendar v-model="config.horarioApertura" timeOnly :showIcon="true" class="w-full" />
            </Field>
            <Field label="Hora de cierre" required hint="A qué hora la escuela deja de aceptar clases (Ej: 19:00)">
              <Calendar v-model="config.horarioCierre" timeOnly :showIcon="true" class="w-full" />
            </Field>
            <Field label="Duración default por clase (minutos)" required hint="Cuántos minutos dura una clase normal. Ej: 60">
              <InputNumber v-model="config.duracionClaseDefaultMin" :min="30" :max="240" class="w-full" />
            </Field>
            <Field label="Horas previas para recordatorio" required hint="Cuántas horas antes de la clase enviar email de recordatorio. Ej: 24">
              <InputNumber v-model="config.horasRecordatorioClase" :min="1" :max="72" class="w-full" />
            </Field>
          </div>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <!-- =============== MARCA =============== -->
        <FormCard
          v-if="activa === 'marca'"
          title="Identidad visual"
          description="Colores corporativos que se aplican a facturas exportadas, reportes PDF y logos del sistema."
          icon="pi pi-palette"
        >
          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 mb-5 flex items-start gap-2 text-xs text-info-700">
            <i class="pi pi-info-circle mt-0.5" />
            <span>Los colores definidos aquí se usan en los PDFs generados (facturas, reportes), en los emails enviados a estudiantes y eventualmente en el logo del sistema. <strong>No cambia los colores del panel administrativo</strong> que estás viendo ahora.</span>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Color primario" hint="Color principal de la marca. Aparece en encabezados de facturas y botones de reportes.">
              <div class="flex items-center gap-3">
                <input type="color" v-model="config.colorPrimario" class="w-14 h-11 rounded-lg border border-ink-200 cursor-pointer" />
                <InputText v-model="config.colorPrimario" class="flex-1 !font-mono" />
              </div>
            </Field>
            <Field label="Color secundario" hint="Color complementario. Aparece en líneas, separadores y acentos secundarios.">
              <div class="flex items-center gap-3">
                <input type="color" v-model="config.colorSecundario" class="w-14 h-11 rounded-lg border border-ink-200 cursor-pointer" />
                <InputText v-model="config.colorSecundario" class="flex-1 !font-mono" />
              </div>
            </Field>
          </div>

          <!-- Vista previa -->
          <div class="mt-6 pt-6 border-t border-ink-200">
            <p class="text-xs uppercase tracking-wider text-ink-500 font-semibold mb-3">Vista previa de la combinación</p>
            <div class="rounded-lg overflow-hidden border border-ink-200">
              <div :style="{ backgroundColor: config.colorPrimario }" class="px-4 py-3 text-white">
                <p class="font-bold text-sm">Encabezado factura — Escuela {{ config.nombre || 'XYZ' }}</p>
              </div>
              <div class="p-4 bg-white">
                <p class="text-sm text-ink-700">Cuerpo del documento con texto normal.</p>
                <div class="mt-3 h-1" :style="{ backgroundColor: config.colorSecundario }" />
                <p class="text-xs text-ink-500 mt-3">RUC: {{ config.ruc || '—' }} · {{ config.telefono || '—' }}</p>
              </div>
            </div>
          </div>

          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <!-- =============== ALERTAS =============== -->
        <FormCard
          v-if="activa === 'alertas'"
          title="Alertas automáticas del sistema"
          description="Umbrales para que el sistema genere avisos preventivos sobre mantenimiento, documentación y eventos críticos."
          icon="pi pi-bell"
        >
          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 mb-5 flex items-start gap-2 text-xs text-info-700">
            <i class="pi pi-info-circle mt-0.5" />
            <span>El sistema genera alertas visuales en el dashboard cuando un vehículo está próximo a vencer su SOAT (Seguro Obligatorio de Accidentes de Tránsito). El umbral define con cuántos días de anticipación quieres ser notificado.</span>
          </div>
          <Field
            label="Días previos para alerta de SOAT por vencer"
            hint="Con cuántos días de anticipación quieres ver la alerta. Recomendado: 30 días."
          >
            <InputNumber v-model="config.diasAlertaSoat" :min="1" :max="120" class="w-full md:w-1/2" suffix=" días" />
          </Field>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <!-- =============== CATÁLOGOS =============== -->
        <div v-if="activa === 'catalogos'" class="space-y-6">
          <!-- Card explicativa -->
          <div class="card animate-fade-up p-5 border-l-4 !border-l-accent-600">
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 rounded-lg bg-accent-50 text-accent-700 flex items-center justify-center flex-shrink-0">
                <i class="pi pi-book" />
              </div>
              <div class="flex-1">
                <h3 class="heading-3 mb-1">Catálogos maestros</h3>
                <p class="text-sm text-ink-600 leading-relaxed">
                  Define qué <span class="font-semibold text-ink-900">conceptos</span> puedes facturar
                  (matrícula, examen, clase extra, material…) y qué <span class="font-semibold text-ink-900">tipos de curso</span>
                  ofreces (por categoría de licencia), cada uno con su precio base. Estos valores aparecen
                  como opciones al crear una factura nueva en <router-link to="/cobros" class="text-brand-700 hover:underline font-medium">Cobros y Pagos</router-link>.
                </p>
              </div>
            </div>
          </div>

          <!-- Sub-tabs -->
          <div class="card overflow-hidden">
            <div class="flex border-b border-ink-200 bg-ink-50/40">
              <button
                v-for="t in catTabs"
                :key="t.key"
                @click="catTab = t.key"
                :class="['flex-1 px-4 py-3 text-sm font-medium transition border-b-2',
                  catTab === t.key
                    ? 'border-brand-600 text-brand-700 bg-white'
                    : 'border-transparent text-ink-600 hover:bg-ink-100/60']"
              >
                <i :class="['pi', t.icon, 'mr-2']" />
                {{ t.label }}
              </button>
            </div>

            <!-- CONCEPTOS DE FACTURACIÓN -->
            <div v-if="catTab === 'conceptos'" class="p-5">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h4 class="text-sm font-semibold text-ink-900">Conceptos facturables</h4>
                  <p class="text-xs text-ink-500 mt-0.5">{{ conceptos.length }} concepto(s) registrado(s).</p>
                </div>
                <Button label="Nuevo concepto" icon="pi pi-plus" size="small" @click="abrirFormConcepto()" />
              </div>

              <DataTable :value="conceptos" striped-rows :pt="{ table: { style: 'min-width: 100%' } }">
                <Column header="Nombre">
                  <template #body="{ data }">
                    <p class="text-sm font-medium">{{ data.nombre }}</p>
                    <p v-if="data.descripcion" class="text-[11px] text-ink-500">{{ data.descripcion }}</p>
                  </template>
                </Column>
                <Column header="Precio base" style="width: 140px">
                  <template #body="{ data }">
                    <span class="text-sm font-semibold text-brand-700">{{ formatMoney(data.montoBase) }}</span>
                  </template>
                </Column>
                <Column header="Estado" style="width: 120px">
                  <template #body="{ data }">
                    <StatusBadge :status="data.activo === false ? 'INACTIVO' : 'ACTIVO'" />
                  </template>
                </Column>
                <Column header="" style="width: 110px">
                  <template #body="{ data }">
                    <div class="flex items-center gap-1">
                      <Button v-tooltip.left="'Editar'" icon="pi pi-pencil" text rounded size="small" @click="abrirFormConcepto(data)" />
                      <Button v-tooltip.left="'Eliminar'" icon="pi pi-trash" text rounded size="small" severity="danger" @click="eliminarConcepto(data)" />
                    </div>
                  </template>
                </Column>
              </DataTable>

              <EmptyState
                v-if="conceptos.length === 0"
                icon="pi pi-tag"
                title="Sin conceptos creados"
                description="Crea el primer concepto facturable (curso básico, examen, etc.)."
              >
                <template #action>
                  <Button label="Nuevo concepto" icon="pi pi-plus" @click="abrirFormConcepto()" />
                </template>
              </EmptyState>
            </div>

            <!-- TIPOS DE CURSO -->
            <div v-if="catTab === 'cursos'" class="p-5">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h4 class="text-sm font-semibold text-ink-900">Tipos de curso</h4>
                  <p class="text-xs text-ink-500 mt-0.5">{{ tiposCurso.length }} tipo(s) de curso registrado(s).</p>
                </div>
                <Button label="Nuevo tipo de curso" icon="pi pi-plus" size="small" @click="abrirFormCurso()" />
              </div>

              <DataTable :value="tiposCurso" striped-rows :pt="{ table: { style: 'min-width: 100%' } }">
                <Column header="Nombre">
                  <template #body="{ data }">
                    <p class="text-sm font-medium">{{ data.nombre }}</p>
                    <p v-if="data.descripcion" class="text-[11px] text-ink-500">{{ data.descripcion }}</p>
                  </template>
                </Column>
                <Column header="Categoría" style="width: 100px">
                  <template #body="{ data }">
                    <span class="inline-flex items-center px-2 py-0.5 rounded-md bg-brand-50 text-brand-700 text-xs font-bold border border-brand-200">
                      {{ data.categoriaLicenciaCodigo || '—' }}
                    </span>
                  </template>
                </Column>
                <Column header="Duración" style="width: 120px">
                  <template #body="{ data }">
                    <span class="text-xs">{{ data.duracionTotalHoras }} h</span>
                  </template>
                </Column>
                <Column header="Precio base" style="width: 140px">
                  <template #body="{ data }">
                    <span class="text-sm font-semibold text-brand-700">{{ formatMoney(data.precioBase) }}</span>
                  </template>
                </Column>
                <Column header="Estado" style="width: 100px">
                  <template #body="{ data }">
                    <StatusBadge :status="data.activo === false ? 'INACTIVO' : 'ACTIVO'" />
                  </template>
                </Column>
                <Column header="" style="width: 110px">
                  <template #body="{ data }">
                    <div class="flex items-center gap-1">
                      <Button v-tooltip.left="'Editar'" icon="pi pi-pencil" text rounded size="small" @click="abrirFormCurso(data)" />
                      <Button v-tooltip.left="'Eliminar'" icon="pi pi-trash" text rounded size="small" severity="danger" @click="eliminarCurso(data)" />
                    </div>
                  </template>
                </Column>
              </DataTable>

              <EmptyState
                v-if="tiposCurso.length === 0"
                icon="pi pi-graduation-cap"
                title="Sin tipos de curso"
                description="Crea el primer tipo de curso (Básico, Profesional, etc.)."
              >
                <template #action>
                  <Button label="Nuevo tipo de curso" icon="pi pi-plus" @click="abrirFormCurso()" />
                </template>
              </EmptyState>
            </div>
          </div>

          <!-- DIALOG: Concepto -->
          <Dialog v-model:visible="dlgConcepto" modal :header="formConcepto.id ? 'Editar concepto' : 'Nuevo concepto'" :style="{ width: '480px' }">
            <div class="space-y-4">
              <div v-if="errConcepto" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errConcepto }}</div>
              <Field label="Nombre" required>
                <InputText v-model="formConcepto.nombre" placeholder="Ej: Curso Básico" class="w-full" />
              </Field>
              <Field label="Monto base (USD)" required>
                <InputNumber v-model="formConcepto.montoBase" mode="currency" currency="USD" locale="en-US" :min="0" class="w-full" :pt="{ input: { class: 'w-full' } }" />
              </Field>
              <Field label="Descripción">
                <Textarea v-model="formConcepto.descripcion" rows="2" class="w-full" />
              </Field>
              <div class="flex items-center gap-2">
                <Checkbox v-model="formConcepto.activo" :binary="true" inputId="cpt-activo" />
                <label for="cpt-activo" class="text-sm text-ink-700">Activo (aparece como opción al facturar)</label>
              </div>
            </div>
            <template #footer>
              <Button label="Cancelar" outlined @click="dlgConcepto = false" />
              <Button :label="formConcepto.id ? 'Guardar' : 'Crear'" icon="pi pi-check" :loading="guardandoConcepto" @click="guardarConcepto" />
            </template>
          </Dialog>

          <!-- DIALOG: Tipo de curso -->
          <Dialog v-model:visible="dlgCurso" modal :header="formCurso.id ? 'Editar tipo de curso' : 'Nuevo tipo de curso'" :style="{ width: '560px' }">
            <div class="space-y-4">
              <div v-if="errCurso" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errCurso }}</div>
              <Field label="Nombre" required>
                <InputText v-model="formCurso.nombre" placeholder="Ej: Curso Básico Auto" class="w-full" />
              </Field>
              <div class="grid grid-cols-2 gap-3">
                <Field label="Categoría de licencia" required>
                  <Dropdown
                    v-model="formCurso.categoriaLicenciaId"
                    :options="categorias"
                    optionValue="id" optionLabel="codigo"
                    placeholder="Categoría"
                    class="w-full"
                  >
                    <template #option="{ option }">
                      <div class="flex items-center gap-2">
                        <span class="inline-flex items-center px-1.5 py-0.5 rounded bg-brand-50 text-brand-700 text-xs font-bold">{{ option.codigo }}</span>
                        <span class="text-xs text-ink-600 truncate">{{ option.descripcion }}</span>
                      </div>
                    </template>
                  </Dropdown>
                </Field>
                <Field label="Duración (horas)" required>
                  <InputNumber v-model="formCurso.duracionTotalHoras" :min="1" :max="500" class="w-full" :pt="{ input: { class: 'w-full' } }" />
                </Field>
              </div>
              <Field label="Precio base (USD)" required>
                <InputNumber v-model="formCurso.precioBase" mode="currency" currency="USD" locale="en-US" :min="0" class="w-full" :pt="{ input: { class: 'w-full' } }" />
              </Field>
              <Field label="Descripción">
                <Textarea v-model="formCurso.descripcion" rows="2" class="w-full" />
              </Field>
              <div class="flex items-center gap-2">
                <Checkbox v-model="formCurso.activo" :binary="true" inputId="curso-activo" />
                <label for="curso-activo" class="text-sm text-ink-700">Activo</label>
              </div>
            </div>
            <template #footer>
              <Button label="Cancelar" outlined @click="dlgCurso = false" />
              <Button :label="formCurso.id ? 'Guardar' : 'Crear'" icon="pi pi-check" :loading="guardandoCurso" @click="guardarCurso" />
            </template>
          </Dialog>
        </div>

        <!-- =============== SEGURIDAD =============== -->
        <FormCard
          v-if="activa === 'seguridad'"
          title="Política de seguridad de cuentas"
          description="Controla el comportamiento del sistema ante intentos de acceso fallidos y la validez de los enlaces de recuperación."
          icon="pi pi-shield"
        >
          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 mb-5 flex items-start gap-2 text-xs text-info-700">
            <i class="pi pi-info-circle mt-0.5" />
            <span>
              Las cuentas se bloquean automáticamente tras superar el número de intentos fallidos definido.
              Después del tiempo de bloqueo se reactivan solas, pero un administrador siempre puede
              <router-link to="/usuarios" class="font-semibold underline">desbloquearlas manualmente</router-link>
              desde el módulo de Usuarios.
            </span>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field
              label="Máximo de intentos fallidos antes de bloquear"
              required
              hint="Después de cuántos intentos incorrectos se bloquea la cuenta. Recomendado: 3-5."
            >
              <InputNumber
                v-model="config.maxIntentosFallidos"
                :min="1"
                :max="10"
                showButtons
                buttonLayout="horizontal"
                incrementButtonIcon="pi pi-plus"
                decrementButtonIcon="pi pi-minus"
                suffix=" intentos"
                class="w-full"
              />
            </Field>
            <Field
              label="Duración del bloqueo (en minutos)"
              required
              hint="Cuánto tiempo permanece bloqueada la cuenta. Después se reactiva sola. Recomendado: 15-30 min."
            >
              <InputNumber
                v-model="config.duracionBloqueoMinutos"
                :min="1"
                :max="1440"
                showButtons
                buttonLayout="horizontal"
                incrementButtonIcon="pi pi-plus"
                decrementButtonIcon="pi pi-minus"
                suffix=" min"
                class="w-full"
              />
            </Field>
            <Field
              label="Vigencia del enlace de recuperación"
              required
              hint="Cuánto tiempo es válido el link enviado al hacer 'Olvidé mi contraseña'. Recomendado: 60 min."
              class="md:col-span-2"
            >
              <InputNumber
                v-model="config.expiracionTokenResetMinutos"
                :min="5"
                :max="1440"
                showButtons
                buttonLayout="horizontal"
                incrementButtonIcon="pi pi-plus"
                decrementButtonIcon="pi pi-minus"
                suffix=" min"
                class="w-full md:w-1/2"
              />
            </Field>
          </div>

          <!-- Vista previa de configuración actual -->
          <div class="mt-6 pt-6 border-t border-ink-200">
            <p class="text-xs uppercase tracking-wider text-ink-500 font-semibold mb-3">Resumen de la política actual</p>
            <div class="rounded-lg bg-ink-50 border border-ink-200 p-4 text-sm text-ink-700 leading-relaxed">
              <i class="pi pi-shield text-brand-700 mr-1.5" />
              Si un usuario falla <strong>{{ config.maxIntentosFallidos }} intentos</strong> de login,
              su cuenta queda bloqueada por <strong>{{ config.duracionBloqueoMinutos }} minutos</strong>.
              Los enlaces de "Olvidé mi contraseña" expiran a los <strong>{{ config.expiracionTokenResetMinutos }} minutos</strong> de ser emitidos.
            </div>
          </div>

          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <!-- =============== INFO FUTURO =============== -->
        <FormCard
          v-if="activa === 'futuro'"
          title="Próximas configuraciones"
          description="Funcionalidades planificadas para sprints posteriores."
          icon="pi pi-sparkles"
        >
          <div class="space-y-3">
            <FuturoItem
              icon="pi-envelope"
              titulo="Plantillas de email"
              descripcion="Editor visual para personalizar los textos de los correos automáticos (bienvenida, recuperación de contraseña, recordatorios de clase). Hoy las plantillas están hardcoded en el backend."
              sprint="Sprint 9"
            />
            <FuturoItem
              icon="pi-list"
              titulo="Catálogos editables"
              descripcion="CRUD visual para tipos de curso, categorías de licencia y conceptos de facturación. Hoy se gestionan via API directa."
              sprint="Sprint 9"
            />
            <FuturoItem
              icon="pi-image"
              titulo="Subir logo institucional"
              descripcion="Upload de imagen para el logo que aparece en facturas y reportes PDF. Hoy se usa un placeholder."
              sprint="Sprint 11"
            />
            <FuturoItem
              icon="pi-cloud-upload"
              titulo="Backups automáticos"
              descripcion="Configurar respaldos diarios de la base de datos con notificación de éxito/fallo."
              sprint="Sprint 12"
            />
          </div>
        </FormCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, defineComponent, h } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tooltip from 'primevue/tooltip'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import api from '@/services/api'

const vTooltip = Tooltip

const Field = defineComponent({
  props: ['label', 'required', 'hint'],
  setup(props, { slots, attrs }) {
    return () =>
      h('div', { ...attrs }, [
        h('label', { class: 'block text-sm font-medium text-ink-700 mb-1.5' }, [
          props.label,
          props.required && h('span', { class: 'text-danger-500 ml-0.5' }, '*')
        ]),
        slots.default?.(),
        props.hint && h('p', { class: 'text-xs text-ink-500 mt-1.5 leading-relaxed' }, props.hint)
      ])
  }
})

const FuturoItem = defineComponent({
  props: ['icon', 'titulo', 'descripcion', 'sprint'],
  setup(props) {
    return () =>
      h('div', { class: 'flex items-start gap-3 p-3 rounded-lg border border-ink-200 bg-ink-50/30' }, [
        h('div', { class: 'w-9 h-9 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0' }, [
          h('i', { class: `pi ${props.icon} text-sm` })
        ]),
        h('div', { class: 'flex-1' }, [
          h('div', { class: 'flex items-center gap-2 flex-wrap' }, [
            h('p', { class: 'text-sm font-semibold text-ink-900' }, props.titulo),
            h('span', { class: 'inline-flex items-center px-2 py-0.5 rounded-full bg-warning-50 text-warning-700 text-[10px] font-medium border border-warning-500/20' }, props.sprint)
          ]),
          h('p', { class: 'text-xs text-ink-600 mt-1 leading-relaxed' }, props.descripcion)
        ])
      ])
  }
})

const activa = ref<'escuela' | 'horarios' | 'marca' | 'alertas' | 'catalogos' | 'seguridad' | 'futuro'>('escuela')
const guardando = ref(false)

const secciones = [
  { key: 'escuela',   label: 'Información general', icon: 'pi-building', hint: 'Datos de la empresa' },
  { key: 'horarios',  label: 'Horarios',            icon: 'pi-clock',    hint: 'Reglas operativas' },
  { key: 'marca',     label: 'Identidad visual',    icon: 'pi-palette',  hint: 'Colores corporativos' },
  { key: 'alertas',   label: 'Alertas',             icon: 'pi-bell',     hint: 'Umbrales del sistema' },
  { key: 'catalogos', label: 'Catálogos',           icon: 'pi-book',     hint: 'Conceptos y tipos de curso' },
  { key: 'seguridad', label: 'Seguridad',           icon: 'pi-shield',   hint: 'Bloqueos y contraseñas' },
  { key: 'futuro',    label: 'Próximamente',        icon: 'pi-sparkles', hint: 'Funcionalidades futuras' }
]

const config = reactive<any>({
  nombre: '', ruc: '', email: '', telefono: '', direccion: '',
  horarioApertura: null, horarioCierre: null,
  duracionClaseDefaultMin: 60, horasRecordatorioClase: 24,
  colorPrimario: '#0f766e', colorSecundario: '#14b8a6',
  diasAlertaSoat: 30,
  maxIntentosFallidos: 3,
  duracionBloqueoMinutos: 15,
  expiracionTokenResetMinutos: 60
})

const cargar = async () => {
  try {
    const { data } = await api.get('/configuracion')
    Object.assign(config, data)
  } catch (e) { console.error(e) }
}

const guardar = async () => {
  guardando.value = true
  try {
    await api.put('/configuracion', config)
  } catch (e) { console.error(e) }
  finally { guardando.value = false }
}

// =========================================================================
// CATÁLOGOS: conceptos de facturación + tipos de curso
// =========================================================================
const catTabs = [
  { key: 'conceptos', label: 'Conceptos de facturación', icon: 'pi-tag' },
  { key: 'cursos',    label: 'Tipos de curso',           icon: 'pi-graduation-cap' }
]
const catTab = ref<'conceptos' | 'cursos'>('conceptos')

const conceptos = ref<any[]>([])
const tiposCurso = ref<any[]>([])
const categorias = ref<any[]>([])

const formatMoney = (n: any) =>
  `$${(parseFloat(n) || 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

// --- Conceptos ---
const dlgConcepto = ref(false)
const guardandoConcepto = ref(false)
const errConcepto = ref('')
const formConcepto = reactive<any>({ id: null, nombre: '', montoBase: 0, descripcion: '', activo: true })

const abrirFormConcepto = (c?: any) => {
  errConcepto.value = ''
  if (c) {
    Object.assign(formConcepto, {
      id: c.id, nombre: c.nombre, montoBase: Number(c.montoBase),
      descripcion: c.descripcion || '', activo: c.activo !== false
    })
  } else {
    Object.assign(formConcepto, { id: null, nombre: '', montoBase: 0, descripcion: '', activo: true })
  }
  dlgConcepto.value = true
}

const guardarConcepto = async () => {
  errConcepto.value = ''
  if (!formConcepto.nombre?.trim()) { errConcepto.value = 'El nombre es obligatorio'; return }
  if (formConcepto.montoBase == null || formConcepto.montoBase < 0) { errConcepto.value = 'Monto inválido'; return }
  guardandoConcepto.value = true
  try {
    const payload = {
      nombre: formConcepto.nombre.trim(),
      montoBase: formConcepto.montoBase,
      descripcion: formConcepto.descripcion?.trim() || null,
      activo: formConcepto.activo
    }
    if (formConcepto.id) await api.put(`/conceptos-facturacion/${formConcepto.id}`, payload)
    else                 await api.post('/conceptos-facturacion', payload)
    dlgConcepto.value = false
    await cargarCatalogos()
  } catch (e: any) {
    errConcepto.value = e.response?.data?.detail || e.response?.data?.message || 'No se pudo guardar'
  } finally { guardandoConcepto.value = false }
}

const eliminarConcepto = async (c: any) => {
  if (!confirm(`¿Eliminar el concepto "${c.nombre}"? Las facturas existentes lo conservan, solo deja de aparecer al crear nuevas.`)) return
  try {
    await api.delete(`/conceptos-facturacion/${c.id}`)
    await cargarCatalogos()
  } catch (e: any) {
    alert(e.response?.data?.detail || 'No se pudo eliminar')
  }
}

// --- Tipos de curso ---
const dlgCurso = ref(false)
const guardandoCurso = ref(false)
const errCurso = ref('')
const formCurso = reactive<any>({
  id: null, nombre: '', descripcion: '',
  duracionTotalHoras: 40, precioBase: 0,
  categoriaLicenciaId: null, activo: true
})

const abrirFormCurso = (c?: any) => {
  errCurso.value = ''
  if (c) {
    Object.assign(formCurso, {
      id: c.id, nombre: c.nombre, descripcion: c.descripcion || '',
      duracionTotalHoras: c.duracionTotalHoras, precioBase: Number(c.precioBase),
      categoriaLicenciaId: c.categoriaLicenciaId, activo: c.activo !== false
    })
  } else {
    Object.assign(formCurso, {
      id: null, nombre: '', descripcion: '',
      duracionTotalHoras: 40, precioBase: 0,
      categoriaLicenciaId: null, activo: true
    })
  }
  dlgCurso.value = true
}

const guardarCurso = async () => {
  errCurso.value = ''
  if (!formCurso.nombre?.trim()) { errCurso.value = 'El nombre es obligatorio'; return }
  if (!formCurso.duracionTotalHoras || formCurso.duracionTotalHoras < 1) { errCurso.value = 'Duración inválida'; return }
  if (formCurso.precioBase == null || formCurso.precioBase < 0) { errCurso.value = 'Precio inválido'; return }
  guardandoCurso.value = true
  try {
    const payload = {
      nombre: formCurso.nombre.trim(),
      descripcion: formCurso.descripcion?.trim() || null,
      duracionTotalHoras: formCurso.duracionTotalHoras,
      precioBase: formCurso.precioBase,
      categoriaLicenciaId: formCurso.categoriaLicenciaId,
      activo: formCurso.activo
    }
    if (formCurso.id) await api.put(`/tipos-curso/${formCurso.id}`, payload)
    else              await api.post('/tipos-curso', payload)
    dlgCurso.value = false
    await cargarCatalogos()
  } catch (e: any) {
    errCurso.value = e.response?.data?.detail || e.response?.data?.message || 'No se pudo guardar'
  } finally { guardandoCurso.value = false }
}

const eliminarCurso = async (c: any) => {
  if (!confirm(`¿Eliminar el tipo de curso "${c.nombre}"?`)) return
  try {
    await api.delete(`/tipos-curso/${c.id}`)
    await cargarCatalogos()
  } catch (e: any) {
    alert(e.response?.data?.detail || 'No se pudo eliminar')
  }
}

const cargarCatalogos = async () => {
  const [c, t, cat] = await Promise.allSettled([
    api.get('/conceptos-facturacion', { params: { size: 200 } }),
    api.get('/tipos-curso', { params: { size: 200 } }),
    api.get('/categorias-licencia', { params: { size: 200 } })
  ])
  if (c.status === 'fulfilled') conceptos.value = c.value.data.content || []
  if (t.status === 'fulfilled') tiposCurso.value = t.value.data.content || []
  if (cat.status === 'fulfilled') categorias.value = cat.value.data.content || []
}

// Carga perezosa al entrar a la sección
watch(activa, (v) => { if (v === 'catalogos') cargarCatalogos() })

onMounted(() => {
  cargar()
  // Si entras directo a /configuracion y eliges catálogos, también lo precargamos
  if (activa.value === 'catalogos') cargarCatalogos()
})
</script>
