<template>
  <div class="space-y-6">
    <PageHeader
      :title="`${vehiculo.marca} ${vehiculo.modelo}`"
      :description="`Placa ${vehiculo.placa} · ${vehiculo.anio}`"
      icon="pi pi-car"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Vehículos', to: '/vehiculos' },
        { label: vehiculo.placa || 'Detalle' }
      ]"
    >
      <template #actions>
        <Button label="Volver" icon="pi pi-arrow-left" outlined @click="router.back()" />
        <Button label="Editar" icon="pi pi-pencil" outlined @click="router.push(`/vehiculos/${vehiculoId}/editar`)" />
        <Button label="Eliminar" icon="pi pi-trash" severity="danger" outlined @click="confirmarEliminar" />
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12">
      <ProgressSpinner />
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Panel izquierdo: Patente + estado + alertas -->
      <div class="lg:col-span-1 space-y-4">
        <div class="card p-5">
          <div class="flex flex-col items-center text-center mb-4">
            <div class="w-20 h-20 rounded-2xl bg-brand-50 text-brand-700 flex items-center justify-center mb-3">
              <i class="pi pi-car text-4xl" />
            </div>
            <!-- Placa estilo "patente" -->
            <div class="inline-flex items-center px-4 py-2 rounded-md bg-ink-900 text-white font-mono font-bold text-lg shadow-inner border-4 border-ink-300">
              {{ vehiculo.placa }}
            </div>
            <h2 class="text-base font-bold text-ink-900 mt-3">{{ vehiculo.marca }} {{ vehiculo.modelo }}</h2>
            <p class="text-xs text-ink-500">{{ vehiculo.anio }}{{ vehiculo.color ? ` · ${vehiculo.color}` : '' }}</p>
          </div>

          <div class="space-y-2 border-t border-ink-200 pt-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Estado:</span>
              <div class="flex items-center gap-2">
                <StatusBadge :status="vehiculo.estado" />
                <Button icon="pi pi-pencil" text rounded size="small" severity="info" v-tooltip="'Cambiar estado'" @click="abrirDialogEstado" />
              </div>
            </div>

            <div v-if="categoriaActual" class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Categoría:</span>
              <span class="inline-flex items-center justify-center w-7 h-7 rounded-md bg-brand-100 text-brand-700 text-xs font-bold" :title="categoriaActual.descripcion">
                {{ categoriaActual.codigo }}
              </span>
            </div>

            <div v-if="combustibleActual" class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Combustible:</span>
              <span class="text-xs font-medium text-ink-700">
                <i :class="iconoCombustible(combustibleActual.codigo)" class="mr-1" />
                {{ combustibleActual.codigo }}
                <span class="text-ink-500 ml-1">${{ Number(combustibleActual.precioActual).toFixed(2) }}/{{ combustibleActual.unidad.toLowerCase() }}</span>
              </span>
            </div>

            <div class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Kilometraje:</span>
              <span class="text-xs font-medium text-ink-700">{{ (vehiculo.kilometraje || 0).toLocaleString('es-EC') }} km</span>
            </div>

            <div v-if="vehiculo.capacidadPasajeros" class="flex items-center justify-between">
              <span class="text-sm text-ink-600">Capacidad:</span>
              <span class="text-xs font-medium text-ink-700">{{ vehiculo.capacidadPasajeros }} pasajeros</span>
            </div>
          </div>

          <!-- Alertas SOAT/RTV -->
          <div v-if="vehiculo.soatVencimiento || vehiculo.revisionVencimiento" class="mt-4 space-y-2 border-t border-ink-200 pt-4">
            <div v-if="vehiculo.soatVencimiento" class="p-2 rounded-lg" :class="alertaBg(vehiculo.soatVencimiento)">
              <div class="flex items-center justify-between">
                <span class="text-xs font-medium text-ink-700"><i class="pi pi-shield mr-1" /> SOAT</span>
                <span :class="['text-xs font-bold', alertaTexto(vehiculo.soatVencimiento)]">
                  {{ etiquetaTiempo(vehiculo.soatVencimiento) }}
                </span>
              </div>
            </div>
            <div v-if="vehiculo.revisionVencimiento" class="p-2 rounded-lg" :class="alertaBg(vehiculo.revisionVencimiento)">
              <div class="flex items-center justify-between">
                <span class="text-xs font-medium text-ink-700"><i class="pi pi-verified mr-1" /> RTV</span>
                <span :class="['text-xs font-bold', alertaTexto(vehiculo.revisionVencimiento)]">
                  {{ etiquetaTiempo(vehiculo.revisionVencimiento) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Mini-widget de mantenimientos y consumo -->
        <div class="card p-4 space-y-3">
          <button type="button" @click="activeTab = 1" class="w-full text-left p-3 rounded-lg bg-info-50 border border-info-200 hover:bg-info-100 transition">
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-info-700"><i class="pi pi-wrench mr-1" /> Mantenimientos</span>
              <span class="text-sm font-bold text-ink-900">{{ mantenimientos.length }}</span>
            </div>
          </button>
          <button type="button" @click="activeTab = 3" class="w-full text-left p-3 rounded-lg bg-warning-50 border border-warning-200 hover:bg-warning-100 transition">
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-warning-700"><i class="pi pi-bolt mr-1" /> Combustible</span>
              <span class="text-sm font-bold text-ink-900">{{ combustibleRegistros.length }} cargas</span>
            </div>
          </button>
        </div>
      </div>

      <!-- Panel derecho: TabView -->
      <div class="lg:col-span-2">
        <TabView v-model:activeIndex="activeTab">
          <!-- Tab 1: Información -->
          <TabPanel header="Información">
            <div class="space-y-4">
              <div class="card p-5">
                <h3 class="heading-3 mb-4 flex items-center gap-2">
                  <i class="pi pi-info-circle text-brand-600" />
                  Datos del vehículo
                </h3>
                <div class="space-y-3">
                  <DetailRow label="Placa" :value="vehiculo.placa" />
                  <DetailRow label="Marca y modelo" :value="`${vehiculo.marca} ${vehiculo.modelo}`" />
                  <DetailRow label="Año" :value="vehiculo.anio" />
                  <DetailRow label="Color" :value="vehiculo.color" />
                  <DetailRow label="VIN" :value="vehiculo.vin" />
                  <DetailRow label="Número de motor" :value="vehiculo.numeroMotor" />
                  <DetailRow label="Número de chasis" :value="vehiculo.numeroChasis" />
                  <DetailRow label="Capacidad de pasajeros" :value="vehiculo.capacidadPasajeros" />
                  <DetailRow label="Kilometraje actual" :value="`${(vehiculo.kilometraje || 0).toLocaleString('es-EC')} km`" />
                  <DetailRow label="Categoría requerida" :value="categoriaActual ? `${categoriaActual.codigo} - ${categoriaActual.descripcion}` : null" />
                  <DetailRow label="Tipo de combustible" :value="combustibleActual ? combustibleActual.nombre : null" />
                </div>
              </div>

              <div class="card p-5">
                <h3 class="heading-3 mb-4 flex items-center gap-2">
                  <i class="pi pi-dollar text-brand-600" />
                  Compra y valor
                </h3>
                <div class="space-y-3">
                  <DetailRow label="Fecha de compra" :value="vehiculo.fechaCompra" type="date" />
                  <DetailRow label="Valor de compra" :value="vehiculo.valorCompra ? `$${Number(vehiculo.valorCompra).toFixed(2)}` : null" />
                </div>
              </div>

              <div class="card p-5">
                <h3 class="heading-3 mb-4 flex items-center gap-2">
                  <i class="pi pi-shield text-brand-600" />
                  Documentos y vencimientos
                </h3>
                <div class="space-y-3">
                  <DetailRow label="SOAT vence" :value="vehiculo.soatVencimiento" type="date" />
                  <DetailRow label="Revisión técnica vence" :value="vehiculo.revisionVencimiento" type="date" />
                </div>
              </div>

              <div v-if="vehiculo.observaciones" class="card p-5">
                <h3 class="heading-3 mb-3 flex items-center gap-2">
                  <i class="pi pi-comment text-brand-600" />
                  Observaciones
                </h3>
                <p class="text-sm text-ink-700 whitespace-pre-wrap">{{ vehiculo.observaciones }}</p>
              </div>
            </div>
          </TabPanel>

          <!-- Tab 2: Mantenimientos -->
          <TabPanel header="Mantenimientos">
            <div class="card p-5">
              <div class="flex items-center justify-between mb-4">
                <h3 class="heading-3 flex items-center gap-2">
                  <i class="pi pi-wrench text-brand-600" />
                  Historial de mantenimientos
                  <span v-if="mantenimientos.length" class="text-xs text-ink-500 font-normal">({{ mantenimientos.length }})</span>
                </h3>
                <Button label="Agregar" icon="pi pi-plus" size="small" @click="abrirDialogMantenimiento" />
              </div>

              <div v-if="cargandoMant" class="text-center py-6"><ProgressSpinner style="width:32px;height:32px" /></div>
              <div v-else-if="mantenimientos.length === 0" class="text-center py-8">
                <i class="pi pi-wrench text-4xl text-ink-300 mb-2" />
                <p class="text-sm text-ink-500">Sin mantenimientos registrados</p>
              </div>
              <div v-else class="space-y-2">
                <div v-for="m in mantenimientosOrdenados" :key="m.id" class="p-3 rounded-lg border-l-4 flex items-start justify-between gap-3"
                  :class="m.tipo === 'PREVENTIVO' ? 'bg-info-50 border-l-info-500' : 'bg-warning-50 border-l-warning-500'">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 flex-wrap mb-1">
                      <span :class="['inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold',
                        m.tipo === 'PREVENTIVO' ? 'bg-info-100 text-info-700' : 'bg-warning-100 text-warning-700']">
                        {{ m.tipo }}
                      </span>
                      <span class="text-sm font-semibold text-ink-900">{{ fmtFechaLocal(m.fecha) }}</span>
                      <span class="text-sm font-bold text-success-700">${{ Number(m.costo).toFixed(2) }}</span>
                      <span v-if="m.kilometrajeServicio" class="text-xs text-ink-500">@ {{ m.kilometrajeServicio.toLocaleString('es-EC') }} km</span>
                    </div>
                    <p class="text-sm text-ink-700">{{ m.descripcion }}</p>
                    <p v-if="m.taller" class="text-xs text-ink-500 mt-1"><i class="pi pi-building mr-1" />{{ m.taller }}</p>
                    <p v-if="m.proximaFecha" class="text-xs text-info-600 mt-1"><i class="pi pi-calendar mr-1" />Próximo: {{ fmtFechaLocal(m.proximaFecha) }}</p>
                  </div>
                  <Button icon="pi pi-trash" text rounded size="small" severity="danger" @click="confirmarEliminarMantenimiento(m)" />
                </div>
              </div>
            </div>
          </TabPanel>

          <!-- Tab 3: Inspecciones -->
          <TabPanel header="Inspecciones">
            <div class="card p-5">
              <div class="flex items-center justify-between mb-4">
                <h3 class="heading-3 flex items-center gap-2">
                  <i class="pi pi-verified text-brand-600" />
                  Inspecciones técnicas
                  <span v-if="inspecciones.length" class="text-xs text-ink-500 font-normal">({{ inspecciones.length }})</span>
                </h3>
                <Button label="Registrar" icon="pi pi-plus" size="small" @click="abrirDialogInspeccion" />
              </div>

              <div v-if="cargandoInsp" class="text-center py-6"><ProgressSpinner style="width:32px;height:32px" /></div>
              <div v-else-if="inspecciones.length === 0" class="text-center py-8">
                <i class="pi pi-verified text-4xl text-ink-300 mb-2" />
                <p class="text-sm text-ink-500">Sin inspecciones registradas</p>
              </div>
              <div v-else class="space-y-2">
                <div v-for="i in inspeccionesOrdenadas" :key="i.id" class="p-3 rounded-lg border-l-4 flex items-start justify-between gap-3"
                  :class="i.resultado === 'APROBADA' ? 'bg-success-50 border-l-success-500' :
                          i.resultado === 'CONDICIONADA' ? 'bg-warning-50 border-l-warning-500' :
                          'bg-danger-50 border-l-danger-500'">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 flex-wrap mb-1">
                      <span class="inline-flex items-center px-2 py-0.5 rounded bg-ink-100 text-[10px] font-bold text-ink-700">{{ i.tipo }}</span>
                      <span :class="['inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold',
                        i.resultado === 'APROBADA' ? 'bg-success-100 text-success-700' :
                        i.resultado === 'CONDICIONADA' ? 'bg-warning-100 text-warning-700' :
                        'bg-danger-100 text-danger-700']">
                        {{ i.resultado }}
                      </span>
                      <span class="text-sm font-semibold text-ink-900">{{ fmtFechaLocal(i.fecha) }}</span>
                    </div>
                    <p v-if="i.observaciones" class="text-xs text-ink-600 italic">{{ i.observaciones }}</p>
                    <p v-if="i.proximaInspeccion" class="text-xs text-info-600 mt-1"><i class="pi pi-calendar mr-1" />Próxima: {{ fmtFechaLocal(i.proximaInspeccion) }}</p>
                  </div>
                  <div class="flex items-center gap-1 flex-shrink-0">
                    <Button icon="pi pi-pencil" text rounded size="small" severity="info" @click="abrirDialogInspeccion(i)" />
                    <Button icon="pi pi-trash" text rounded size="small" severity="danger" @click="confirmarEliminarInspeccion(i)" />
                  </div>
                </div>
              </div>
            </div>
          </TabPanel>

          <!-- Tab 4: Combustible -->
          <TabPanel header="Combustible">
            <div class="card p-5">
              <div class="flex items-center justify-between mb-4">
                <h3 class="heading-3 flex items-center gap-2">
                  <i class="pi pi-bolt text-brand-600" />
                  Registros de carga
                  <span v-if="combustibleRegistros.length" class="text-xs text-ink-500 font-normal">({{ combustibleRegistros.length }})</span>
                </h3>
                <Button label="Registrar carga" icon="pi pi-plus" size="small" @click="abrirDialogCombustible" />
              </div>

              <div v-if="!combustibleActual" class="mb-4 p-3 rounded-lg bg-warning-50 border border-warning-200 text-xs text-ink-700">
                <i class="pi pi-exclamation-triangle text-warning-600 mr-1" />
                Este vehículo no tiene <strong>tipo de combustible</strong> asignado. Editalo primero para poder registrar cargas con auto-cálculo de costo.
              </div>

              <div v-if="cargandoComb" class="text-center py-6"><ProgressSpinner style="width:32px;height:32px" /></div>
              <div v-else-if="combustibleRegistros.length === 0" class="text-center py-8">
                <i class="pi pi-bolt text-4xl text-ink-300 mb-2" />
                <p class="text-sm text-ink-500">Sin cargas registradas</p>
              </div>
              <div v-else>
                <!-- ============ Card EFICIENCIA Y CONSUMO ============ -->
                <div v-if="eficiencia" class="mb-5 rounded-xl border-2 border-brand-200 bg-gradient-to-br from-brand-50 to-white p-5">
                  <div class="flex items-center justify-between mb-4">
                    <h4 class="text-sm font-bold text-brand-800 flex items-center gap-2">
                      <i class="pi pi-chart-line text-brand-600" />
                      Eficiencia y consumo
                    </h4>
                    <span class="text-xs text-ink-500">Basado en {{ combustibleRegistros.length }} cargas registradas</span>
                  </div>

                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <!-- Rendimiento -->
                    <div class="p-3 bg-white rounded-lg border border-brand-100">
                      <p class="text-xs text-ink-600 mb-1 flex items-center gap-1"><i class="pi pi-bolt text-brand-500 text-xs" />Rendimiento</p>
                      <p class="text-xl font-bold text-brand-700">
                        {{ eficiencia.kmPorUnidad != null ? eficiencia.kmPorUnidad.toFixed(1) : '—' }}
                        <span class="text-xs font-normal text-ink-500">km/{{ combustibleActual?.unidad?.toLowerCase() || 'unidad' }}</span>
                      </p>
                      <p v-if="eficiencia.kmPorUnidad == null" class="text-xs text-ink-400 italic mt-0.5">Se calcula con ≥ 2 cargas</p>
                    </div>

                    <!-- Total recorrido entre cargas -->
                    <div class="p-3 bg-white rounded-lg border border-brand-100">
                      <p class="text-xs text-ink-600 mb-1 flex items-center gap-1"><i class="pi pi-map text-brand-500 text-xs" />Recorrido tracked</p>
                      <p class="text-xl font-bold text-brand-700">
                        {{ eficiencia.kmRecorridoTotal.toLocaleString('es-EC') }}
                        <span class="text-xs font-normal text-ink-500">km</span>
                      </p>
                      <p class="text-xs text-ink-500 mt-0.5">entre cargas registradas</p>
                    </div>

                    <!-- Última carga -->
                    <div class="p-3 bg-white rounded-lg border border-brand-100">
                      <p class="text-xs text-ink-600 mb-1 flex items-center gap-1"><i class="pi pi-history text-brand-500 text-xs" />Última carga</p>
                      <p class="text-xl font-bold text-brand-700">
                        hace {{ eficiencia.kmDesdeUltimaCarga.toLocaleString('es-EC') }}
                        <span class="text-xs font-normal text-ink-500">km</span>
                      </p>
                      <p class="text-xs text-ink-500 mt-0.5">
                        {{ fmtFechaLocal(eficiencia.fechaUltimaCarga, {}, '') }}
                      </p>
                    </div>

                    <!-- Próxima carga estimada -->
                    <div :class="['p-3 rounded-lg border-2',
                      eficiencia.alertaProxima ? 'bg-warning-50 border-warning-300' : 'bg-white border-brand-100']">
                      <p class="text-xs text-ink-600 mb-1 flex items-center gap-1">
                        <i :class="['text-xs', eficiencia.alertaProxima ? 'pi pi-exclamation-triangle text-warning-600' : 'pi pi-forward text-brand-500']" />
                        Próxima carga estim.
                      </p>
                      <p v-if="eficiencia.kmProximaCarga != null" :class="['text-xl font-bold', eficiencia.alertaProxima ? 'text-warning-700' : 'text-brand-700']">
                        {{ eficiencia.kmHastaProxima > 0 ? `en ${eficiencia.kmHastaProxima.toLocaleString('es-EC')} km` : '¡Cargar ya!' }}
                      </p>
                      <p v-else class="text-sm text-ink-400 italic mt-1">Sin datos suficientes</p>
                      <p v-if="eficiencia.kmProximaCarga != null" class="text-xs text-ink-500 mt-0.5">
                        a los {{ eficiencia.kmProximaCarga.toLocaleString('es-EC') }} km
                      </p>
                    </div>
                  </div>

                  <!-- Línea adicional con detalles del cálculo -->
                  <div class="mt-4 pt-3 border-t border-brand-200 grid grid-cols-1 md:grid-cols-3 gap-3 text-xs text-ink-600">
                    <div class="flex items-center gap-2">
                      <i class="pi pi-info-circle text-info-600" />
                      <span>Total {{ combustibleActual?.unidad?.toLowerCase() || 'unidades' }} consumidas: <strong>{{ eficiencia.unidadesConsumidas.toFixed(2) }}</strong></span>
                    </div>
                    <div class="flex items-center gap-2">
                      <i class="pi pi-dollar text-success-600" />
                      <span>Costo por km: <strong>${{ eficiencia.costoPorKm != null ? eficiencia.costoPorKm.toFixed(3) : '—' }}</strong></span>
                    </div>
                    <div class="flex items-center gap-2">
                      <i class="pi pi-calendar text-warning-600" />
                      <span>Recorrido este mes: <strong>{{ eficiencia.kmEsteMes.toLocaleString('es-EC') }} km</strong></span>
                    </div>
                  </div>
                </div>

                <!-- Resumen agregado -->
                <div class="grid grid-cols-3 gap-3 mb-4">
                  <div class="p-3 bg-brand-50 rounded-lg">
                    <p class="text-xs text-ink-600 mb-1">Total litros</p>
                    <p class="text-xl font-bold text-brand-700">{{ totalLitros.toFixed(2) }}</p>
                  </div>
                  <div class="p-3 bg-success-50 rounded-lg">
                    <p class="text-xs text-ink-600 mb-1">Total gastado</p>
                    <p class="text-xl font-bold text-success-700">${{ totalGastado.toFixed(2) }}</p>
                  </div>
                  <div class="p-3 bg-info-50 rounded-lg">
                    <p class="text-xs text-ink-600 mb-1">Precio/unidad prom.</p>
                    <p class="text-xl font-bold text-info-700">${{ precioPromedio.toFixed(4) }}</p>
                  </div>
                </div>
                <table class="w-full text-sm">
                  <thead>
                    <tr class="border-b border-ink-200 text-left text-xs text-ink-600 uppercase">
                      <th class="py-2 pr-2 font-medium">Fecha</th>
                      <th class="py-2 px-2 font-medium text-right">Litros</th>
                      <th class="py-2 px-2 font-medium text-right">Costo</th>
                      <th class="py-2 px-2 font-medium text-right">$/u</th>
                      <th class="py-2 px-2 font-medium text-right">Km</th>
                      <th class="py-2 pl-2 font-medium">Estación</th>
                      <th class="py-2 pl-2 font-medium"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in combustibleRegistros" :key="r.id" class="border-b border-ink-100 hover:bg-ink-50">
                      <td class="py-2 pr-2 text-ink-900 whitespace-nowrap">{{ fmtFechaHoraLocal(r.fecha, { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) }}</td>
                      <td class="py-2 px-2 text-right text-ink-700">{{ Number(r.litros).toFixed(2) }}</td>
                      <td class="py-2 px-2 text-right font-semibold text-ink-900">${{ Number(r.costoTotal).toFixed(2) }}</td>
                      <td class="py-2 px-2 text-right text-ink-500 text-xs">${{ Number(r.costoPorLitro).toFixed(4) }}</td>
                      <td class="py-2 px-2 text-right text-ink-700">{{ r.kilometrajeActual.toLocaleString('es-EC') }}</td>
                      <td class="py-2 pl-2 text-ink-500 text-xs">{{ r.estacion || '—' }}</td>
                      <td class="py-2 pl-2 text-right">
                        <Button icon="pi pi-trash" text rounded size="small" severity="danger" @click="confirmarEliminarCombustible(r)" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </TabPanel>

          <!-- Tab 5: Documentos -->
          <TabPanel header="Documentos">
            <div class="card p-5">
              <div class="flex items-center justify-between mb-4">
                <h3 class="heading-3 flex items-center gap-2">
                  <i class="pi pi-file text-brand-600" />
                  Documentos archivados
                  <span v-if="documentos.length" class="text-xs text-ink-500 font-normal">({{ documentos.length }})</span>
                </h3>
              </div>
              <div v-if="documentos.length === 0" class="text-center py-8">
                <i class="pi pi-file text-4xl text-ink-300 mb-2" />
                <p class="text-sm text-ink-500">Sin documentos registrados</p>
                <p class="text-xs text-ink-400 mt-1">Próximamente: upload directo desde aquí.</p>
              </div>
              <div v-else class="space-y-2">
                <div v-for="d in documentos" :key="d.id" class="p-3 rounded-lg bg-ink-50 border border-ink-200 flex items-center justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-ink-900">{{ d.tipo }} <span v-if="d.numero" class="text-ink-500 font-normal">· {{ d.numero }}</span></p>
                    <p class="text-xs text-ink-500">Vence: {{ d.fechaVencimiento || '—' }}</p>
                  </div>
                  <a :href="d.urlArchivo" target="_blank" rel="noopener" class="text-brand-600 text-xs"><i class="pi pi-external-link" /> Ver</a>
                </div>
              </div>
            </div>
          </TabPanel>
        </TabView>
      </div>
    </div>

    <Toast />
    <ConfirmDialog />

    <!-- Dialog: cambiar estado -->
    <Dialog v-model:visible="dialogEstadoVisible" modal header="Cambiar estado del vehículo" :style="{ width:'440px' }">
      <div class="space-y-4">
        <p class="text-sm text-ink-600">Estado actual: <strong>{{ vehiculo.estado }}</strong></p>
        <Dropdown v-model="nuevoEstado" :options="opcionesEstado" option-label="label" option-value="value" placeholder="Selecciona" class="w-full" />
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogEstadoVisible = false" />
        <Button label="Cambiar" icon="pi pi-check" :loading="cambiandoEstado" :disabled="!nuevoEstado" @click="cambiarEstado" />
      </template>
    </Dialog>

    <!-- Dialog: agregar mantenimiento -->
    <Dialog v-model:visible="dialogMantVisible" modal header="Registrar mantenimiento" :style="{ width:'520px' }">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Tipo *</label>
            <Dropdown v-model="formMant.tipo" :options="[{label:'Preventivo',value:'PREVENTIVO'},{label:'Correctivo',value:'CORRECTIVO'}]" option-label="label" option-value="value" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Fecha *</label>
            <Calendar v-model="formMant.fecha" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :maxDate="new Date()" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Costo (USD) *</label>
            <InputNumber v-model="formMant.costo" mode="currency" currency="USD" locale="en-US" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Km al servicio</label>
            <InputNumber v-model="formMant.kilometrajeServicio" :min="0" suffix=" km" class="w-full" />
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-medium text-ink-700 mb-1">Descripción *</label>
            <Textarea v-model="formMant.descripcion" rows="2" placeholder="Cambio de aceite, frenos, etc." class="w-full" />
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-medium text-ink-700 mb-1">Taller</label>
            <InputText v-model="formMant.taller" class="w-full" />
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-medium text-ink-700 mb-1">Próxima fecha (recordatorio)</label>
            <Calendar v-model="formMant.proximaFecha" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </div>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogMantVisible = false" />
        <Button label="Guardar" icon="pi pi-check" :loading="guardandoMant" @click="guardarMantenimiento" />
      </template>
    </Dialog>

    <!-- Dialog: agregar / editar inspección -->
    <Dialog v-model:visible="dialogInspVisible" modal :header="inspeccionEnEdicion ? 'Editar inspección' : 'Registrar inspección'" :style="{ width:'520px' }">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Tipo *</label>
            <Dropdown v-model="formInsp.tipo" :options="[{label:'Técnica (RTV)',value:'TECNICA'},{label:'SOAT',value:'SOAT'},{label:'Interna',value:'INTERNA'}]" option-label="label" option-value="value" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Resultado *</label>
            <Dropdown v-model="formInsp.resultado" :options="[{label:'Aprobada',value:'APROBADA'},{label:'Condicionada',value:'CONDICIONADA'},{label:'Reprobada',value:'REPROBADA'}]" option-label="label" option-value="value" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Fecha *</label>
            <Calendar v-model="formInsp.fecha" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :maxDate="new Date()" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Próxima inspección</label>
            <Calendar v-model="formInsp.proximaInspeccion" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-medium text-ink-700 mb-1">Observaciones</label>
            <Textarea v-model="formInsp.observaciones" rows="2" class="w-full" />
          </div>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogInspVisible = false; inspeccionEnEdicion = null" />
        <Button :label="inspeccionEnEdicion ? 'Guardar cambios' : 'Guardar'" icon="pi pi-check" :loading="guardandoInsp" @click="guardarInspeccion" />
      </template>
    </Dialog>

    <!-- Dialog: registrar combustible (con auto-cálculo) -->
    <Dialog v-model:visible="dialogCombVisible" modal header="Registrar carga de combustible" :style="{ width:'560px' }">
      <div class="space-y-4">
        <div v-if="combustibleActual" class="p-3 rounded-lg bg-info-50 border border-info-200 text-xs text-ink-700">
          <i class="pi pi-info-circle text-info-600 mr-1" />
          Combustible del vehículo: <strong>{{ combustibleActual.nombre }}</strong> a
          <strong>${{ Number(combustibleActual.precioActual).toFixed(2) }}/{{ combustibleActual.unidad.toLowerCase() }}</strong>.
          Si dejas "Costo total" vacío, se calcula automáticamente.
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Fecha *</label>
            <Calendar v-model="formComb.fecha" dateFormat="yy-mm-dd" :showIcon="true" showTime hourFormat="24" class="w-full" :maxDate="new Date()" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">Estación</label>
            <InputText v-model="formComb.estacion" placeholder="Ej: Primax 6 de Diciembre" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">{{ combustibleActual?.unidad === 'KWH' ? 'kWh' : 'Litros' }} *</label>
            <InputNumber v-model="formComb.litros" :min="0.01" :minFractionDigits="2" class="w-full" />
          </div>
          <div>
            <label class="block text-xs font-medium text-ink-700 mb-1">
              Costo total <span class="text-ink-400">(auto)</span>
            </label>
            <InputNumber v-model="formComb.costoTotal" :min="0.01" mode="currency" currency="USD" locale="en-US" :minFractionDigits="2" :placeholder="costoEstimado ? `$${costoEstimado.toFixed(2)}` : '0.00'" class="w-full" />
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-medium text-ink-700 mb-1">Kilometraje actual *</label>
            <InputNumber v-model="formComb.kilometrajeActual" :min="vehiculo.kilometraje || 0" suffix=" km" class="w-full" />
            <p class="text-xs text-ink-500 mt-1">Debe ser ≥ {{ (vehiculo.kilometraje || 0).toLocaleString('es-EC') }} km (actual del vehículo).</p>
          </div>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogCombVisible = false" />
        <Button label="Registrar" icon="pi pi-check" :loading="guardandoComb" @click="guardarCombustible" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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
import StatusBadge from '@/components/ui/StatusBadge.vue'
import DetailRow from '@/components/ui/DetailRow.vue'
import vehiculosService, {
  type VehiculoResponse, type EstadoVehiculo,
  type MantenimientoResponse, type MantenimientoRequest,
  type InspeccionResponse, type InspeccionRequest,
  type CombustibleResponse,
  type DocumentoVehiculoResponse,
  type CategoriaLicenciaResponse, type TipoCombustibleResponse
} from '@/services/vehiculos'
import { fmtFechaLocal, fmtFechaHoraLocal } from '@/utils/fechas'

const vTooltip = Tooltip
const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const vehiculoId = computed(() => parseInt(route.params.id as string))

const loading = ref(false)
const activeTab = ref(0)

const vehiculo = reactive<VehiculoResponse>({
  id: 0, placa: '', marca: '', modelo: '', anio: 0, kilometraje: 0, estado: 'ACTIVO'
})
const categorias = ref<CategoriaLicenciaResponse[]>([])
const tiposCombustible = ref<TipoCombustibleResponse[]>([])

const categoriaActual = computed(() =>
  categorias.value.find(c => c.id === vehiculo.categoriaLicenciaId) || null)
const combustibleActual = computed(() =>
  tiposCombustible.value.find(t => t.id === vehiculo.tipoCombustibleId) || null)

const opcionesEstado = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'En mantenimiento', value: 'MANTENIMIENTO' },
  { label: 'Fuera de servicio', value: 'FUERA_SERVICIO' }
]

const iconoCombustible = (codigo: string): string => {
  if (codigo === 'ELECTRICO') return 'pi pi-bolt'
  if (codigo === 'DIESEL') return 'pi pi-truck'
  if (codigo === 'SUPER') return 'pi pi-star'
  return 'pi pi-cog'
}

const fmtFecha = (v: any): string | undefined => {
  if (!v) return undefined
  if (typeof v === 'string') return v.substring(0, 10)
  if (v instanceof Date) {
    return `${v.getFullYear()}-${String(v.getMonth() + 1).padStart(2, '0')}-${String(v.getDate()).padStart(2, '0')}`
  }
  return undefined
}

const fmtFechaISO = (v: any): string | undefined => {
  if (!v) return undefined
  if (typeof v === 'string') return v
  if (v instanceof Date) return v.toISOString().substring(0, 19) // YYYY-MM-DDTHH:mm:ss
  return undefined
}

const diasParaVencer = (f?: string): number | null => {
  if (!f) return null
  const hoy = new Date(); hoy.setHours(0, 0, 0, 0)
  return Math.floor((new Date(f).getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24))
}
const alertaBg = (f?: string) => {
  const d = diasParaVencer(f)
  if (d === null) return ''
  if (d < 0) return 'bg-danger-50 border border-danger-200'
  if (d < 30) return 'bg-warning-50 border border-warning-200'
  return 'bg-success-50 border border-success-200'
}
const alertaTexto = (f?: string) => {
  const d = diasParaVencer(f)
  if (d === null) return 'text-ink-500'
  if (d < 0) return 'text-danger-700'
  if (d < 30) return 'text-warning-700'
  return 'text-success-700'
}
const etiquetaTiempo = (f?: string) => {
  const d = diasParaVencer(f)
  if (d === null) return ''
  if (d < 0) return `Vencido hace ${Math.abs(d)}d`
  if (d === 0) return 'Vence hoy'
  return `En ${d} días`
}

// ----- Estado: Cambiar estado del vehículo -----
const dialogEstadoVisible = ref(false)
const cambiandoEstado = ref(false)
const nuevoEstado = ref<string | null>(null)

const abrirDialogEstado = () => { nuevoEstado.value = null; dialogEstadoVisible.value = true }
const cambiarEstado = async () => {
  if (!nuevoEstado.value) return
  cambiandoEstado.value = true
  try {
    await vehiculosService.actualizarVehiculo(vehiculoId.value, { estado: nuevoEstado.value as EstadoVehiculo })
    toast.add({ severity:'success', summary:'Actualizado', detail:`Ahora ${nuevoEstado.value}`, life:2500 })
    dialogEstadoVisible.value = false
    await cargar()
  } catch (e: any) {
    toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo cambiar', life:4000 })
  } finally { cambiandoEstado.value = false }
}

// ----- Mantenimientos -----
const mantenimientos = ref<MantenimientoResponse[]>([])
const cargandoMant = ref(false)
const mantenimientosOrdenados = computed(() =>
  [...mantenimientos.value].sort((a,b) => b.fecha.localeCompare(a.fecha)))

const dialogMantVisible = ref(false)
const guardandoMant = ref(false)
const formMant = reactive<any>({
  tipo: 'PREVENTIVO', fecha: new Date(), costo: 0, descripcion: '', taller: '',
  kilometrajeServicio: null, proximaFecha: null
})
const abrirDialogMantenimiento = () => {
  Object.assign(formMant, { tipo:'PREVENTIVO', fecha:new Date(), costo:0, descripcion:'', taller:'', kilometrajeServicio:vehiculo.kilometraje, proximaFecha:null })
  dialogMantVisible.value = true
}
const guardarMantenimiento = async () => {
  if (!formMant.descripcion?.trim()) { toast.add({severity:'error',summary:'Error',detail:'Descripción requerida',life:3000}); return }
  guardandoMant.value = true
  try {
    const payload: MantenimientoRequest = {
      tipo: formMant.tipo,
      fecha: fmtFecha(formMant.fecha)!,
      costo: Number(formMant.costo),
      descripcion: formMant.descripcion.trim(),
      taller: formMant.taller?.trim() || undefined,
      kilometrajeServicio: formMant.kilometrajeServicio || undefined,
      proximaFecha: fmtFecha(formMant.proximaFecha)
    }
    await vehiculosService.registrarMantenimiento(vehiculoId.value, payload)
    toast.add({ severity:'success', summary:'Registrado', detail:'Mantenimiento agregado', life:2500 })
    dialogMantVisible.value = false
    await cargarMantenimientos()
  } catch (e: any) {
    toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo registrar', life:4000 })
  } finally { guardandoMant.value = false }
}
const confirmarEliminarMantenimiento = (m: MantenimientoResponse) => {
  confirm.require({
    message: `¿Eliminar mantenimiento del ${fmtFechaLocal(m.fecha)}?`,
    header: 'Eliminar', icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar', acceptLabel: 'Eliminar', acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.eliminarMantenimiento(vehiculoId.value, m.id)
        toast.add({ severity:'success', summary:'Eliminado', detail:'Mantenimiento eliminado', life:2500 })
        await cargarMantenimientos()
      } catch (e: any) {
        toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo eliminar', life:4000 })
      }
    }
  })
}

// ----- Inspecciones -----
const inspecciones = ref<InspeccionResponse[]>([])
const cargandoInsp = ref(false)
const inspeccionesOrdenadas = computed(() =>
  [...inspecciones.value].sort((a,b) => b.fecha.localeCompare(a.fecha)))

const dialogInspVisible = ref(false)
const guardandoInsp = ref(false)
// null = modo alta; con valor = modo edicion (habilita PUT en vez de POST)
const inspeccionEnEdicion = ref<InspeccionResponse | null>(null)
const formInsp = reactive<any>({
  tipo: 'TECNICA', resultado: 'APROBADA', fecha: new Date(),
  proximaInspeccion: null, observaciones: ''
})

// Parser local para "YYYY-MM-DD" del backend -> Date sin corrimiento de TZ.
const strToDate = (s?: string | null): Date | null => {
  if (!s) return null
  const [y, m, d] = String(s).substring(0, 10).split('-').map(Number)
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}

const abrirDialogInspeccion = (i?: InspeccionResponse) => {
  if (i) {
    inspeccionEnEdicion.value = i
    Object.assign(formInsp, {
      tipo: i.tipo,
      resultado: i.resultado,
      fecha: strToDate(i.fecha) || new Date(),
      proximaInspeccion: strToDate(i.proximaInspeccion),
      observaciones: i.observaciones || ''
    })
  } else {
    inspeccionEnEdicion.value = null
    Object.assign(formInsp, {
      tipo: 'TECNICA', resultado: 'APROBADA', fecha: new Date(),
      proximaInspeccion: null, observaciones: ''
    })
  }
  dialogInspVisible.value = true
}
const guardarInspeccion = async () => {
  guardandoInsp.value = true
  try {
    const payload: InspeccionRequest = {
      tipo: formInsp.tipo, resultado: formInsp.resultado,
      fecha: fmtFecha(formInsp.fecha)!,
      proximaInspeccion: fmtFecha(formInsp.proximaInspeccion),
      observaciones: formInsp.observaciones?.trim() || undefined
    }
    if (inspeccionEnEdicion.value) {
      await vehiculosService.actualizarInspeccion(vehiculoId.value, inspeccionEnEdicion.value.id, payload)
      toast.add({ severity:'success', summary:'Actualizada', detail:'Inspección actualizada', life:2500 })
    } else {
      await vehiculosService.registrarInspeccion(vehiculoId.value, payload)
      toast.add({ severity:'success', summary:'Registrada', detail:'Inspección agregada', life:2500 })
    }
    dialogInspVisible.value = false
    inspeccionEnEdicion.value = null
    // Recargamos vehiculo (para reflejar propagacion de fechas SOAT/RTV que hace
    // el backend al guardar) y la lista de inspecciones.
    await cargarInspecciones()
    try {
      const vRefrescado = await vehiculosService.obtenerVehiculo(vehiculoId.value)
      Object.assign(vehiculo, vRefrescado)
    } catch { /* no rompemos el flujo si falla la recarga */ }
  } catch (e: any) {
    toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo guardar', life:4000 })
  } finally { guardandoInsp.value = false }
}
const confirmarEliminarInspeccion = (i: InspeccionResponse) => {
  confirm.require({
    message: `¿Eliminar inspección ${i.tipo} del ${fmtFechaLocal(i.fecha)}?`,
    header: 'Eliminar', icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar', acceptLabel: 'Eliminar', acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.eliminarInspeccion(vehiculoId.value, i.id)
        toast.add({ severity:'success', summary:'Eliminada', life:2500 })
        await cargarInspecciones()
      } catch (e: any) {
        toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo eliminar', life:4000 })
      }
    }
  })
}

// ----- Combustible -----
const combustibleRegistros = ref<CombustibleResponse[]>([])
const cargandoComb = ref(false)
const totalLitros = computed(() => combustibleRegistros.value.reduce((s,r) => s + Number(r.litros), 0))
const totalGastado = computed(() => combustibleRegistros.value.reduce((s,r) => s + Number(r.costoTotal), 0))
const precioPromedio = computed(() => totalLitros.value > 0 ? totalGastado.value / totalLitros.value : 0)

/**
 * Calcula métricas de eficiencia derivadas del historial de cargas:
 *  - kmPorUnidad: rendimiento promedio (km/galón o km/kWh)
 *  - kmRecorridoTotal: km entre la primera y última carga (lo que se midió con tanque)
 *  - unidadesConsumidas: total consumidas entre cargas (excluye la última, aún no se usó)
 *  - kmDesdeUltimaCarga: km actual del vehículo - km al momento de la última carga
 *  - kmProximaCarga: cuándo (en km del odómetro) se espera próxima carga
 *  - kmEsteMes: total recorrido en los últimos 30 días
 */
const eficiencia = computed(() => {
  const cargas = [...combustibleRegistros.value]
    .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())
  if (cargas.length === 0) return null

  const primera = cargas[0]
  const ultima = cargas[cargas.length - 1]

  // Km totales entre primera y última carga (lo recorrido con esos tanques)
  const kmRecorridoTotal = Math.max(0, ultima.kilometrajeActual - primera.kilometrajeActual)

  // Litros consumidos (excluyendo los de la última carga porque están "en el tanque")
  const unidadesConsumidas = cargas.slice(0, -1).reduce((s, r) => s + Number(r.litros), 0)

  // Rendimiento (km por unidad)
  const kmPorUnidad = (cargas.length >= 2 && unidadesConsumidas > 0)
    ? kmRecorridoTotal / unidadesConsumidas
    : null

  // Costo total consumido
  const costoConsumido = cargas.slice(0, -1).reduce((s, r) => s + Number(r.costoTotal), 0)
  const costoPorKm = (kmRecorridoTotal > 0) ? costoConsumido / kmRecorridoTotal : null

  // Km desde la última carga
  const kmActualVehiculo = vehiculo.kilometraje || 0
  const kmDesdeUltimaCarga = Math.max(0, kmActualVehiculo - ultima.kilometrajeActual)

  // Predicción próxima carga: ultima_carga.km + (litros_ultima_carga * rendimiento)
  let kmProximaCarga: number | null = null
  let kmHastaProxima = 0
  let alertaProxima = false
  if (kmPorUnidad != null && kmPorUnidad > 0) {
    const autonomiaUltimoTanque = Number(ultima.litros) * kmPorUnidad
    kmProximaCarga = Math.round(ultima.kilometrajeActual + autonomiaUltimoTanque)
    kmHastaProxima = Math.max(0, kmProximaCarga - kmActualVehiculo)
    // Alerta si quedan < 20% del rango
    alertaProxima = kmHastaProxima < (autonomiaUltimoTanque * 0.2)
  }

  // Recorrido del mes (últimos 30 días)
  const hace30d = Date.now() - 30 * 24 * 60 * 60 * 1000
  const cargasMes = cargas.filter(c => new Date(c.fecha).getTime() >= hace30d)
  let kmEsteMes = 0
  if (cargasMes.length >= 1) {
    // Desde la primera carga del mes hasta el km actual del vehículo
    kmEsteMes = Math.max(0, kmActualVehiculo - cargasMes[0].kilometrajeActual)
  }

  return {
    kmPorUnidad,
    kmRecorridoTotal,
    unidadesConsumidas,
    kmDesdeUltimaCarga,
    fechaUltimaCarga: ultima.fecha,
    kmProximaCarga,
    kmHastaProxima,
    alertaProxima,
    costoPorKm,
    kmEsteMes
  }
})

const dialogCombVisible = ref(false)
const guardandoComb = ref(false)
const formComb = reactive<any>({
  fecha: new Date(), litros: 0, costoTotal: null, kilometrajeActual: 0, estacion: ''
})
const costoEstimado = computed(() => {
  if (!combustibleActual.value || !formComb.litros) return null
  return Number(formComb.litros) * Number(combustibleActual.value.precioActual)
})
const abrirDialogCombustible = () => {
  Object.assign(formComb, {
    fecha: new Date(), litros: 0, costoTotal: null,
    kilometrajeActual: vehiculo.kilometraje || 0, estacion: ''
  })
  dialogCombVisible.value = true
}
const guardarCombustible = async () => {
  if (!formComb.litros || formComb.litros <= 0) {
    toast.add({severity:'error',summary:'Error',detail:'Litros debe ser > 0',life:3000}); return
  }
  if (formComb.kilometrajeActual < (vehiculo.kilometraje || 0)) {
    toast.add({severity:'error',summary:'Error',detail:`Kilometraje debe ser >= ${vehiculo.kilometraje}`,life:3000}); return
  }
  guardandoComb.value = true
  try {
    await vehiculosService.registrarCombustible(vehiculoId.value, {
      fecha: fmtFechaISO(formComb.fecha)!,
      litros: Number(formComb.litros),
      costoTotal: formComb.costoTotal ? Number(formComb.costoTotal) : undefined,
      kilometrajeActual: Number(formComb.kilometrajeActual),
      estacion: formComb.estacion?.trim() || undefined
    })
    toast.add({ severity:'success', summary:'Carga registrada', life:2500 })
    dialogCombVisible.value = false
    await Promise.all([cargarCombustible(), cargar()])
  } catch (e: any) {
    toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo registrar', life:4000 })
  } finally { guardandoComb.value = false }
}
const confirmarEliminarCombustible = (r: CombustibleResponse) => {
  confirm.require({
    message: `¿Eliminar carga del ${fmtFechaHoraLocal(r.fecha, { day: '2-digit', month: 'short', year: 'numeric' })}?`,
    header: 'Eliminar', icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar', acceptLabel: 'Eliminar', acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.eliminarCombustible(vehiculoId.value, r.id)
        toast.add({ severity:'success', summary:'Eliminada', life:2500 })
        await cargarCombustible()
      } catch (e: any) {
        toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo eliminar', life:4000 })
      }
    }
  })
}

// ----- Documentos -----
const documentos = ref<DocumentoVehiculoResponse[]>([])

// ----- Carga inicial -----
const cargar = async () => {
  loading.value = true
  try {
    const [v, cats, tipos] = await Promise.all([
      vehiculosService.obtenerVehiculo(vehiculoId.value),
      vehiculosService.listarCategoriasLicencia(false).catch(() => []),
      vehiculosService.listarTiposCombustible(false).catch(() => [])
    ])
    Object.assign(vehiculo, v)
    categorias.value = cats
    tiposCombustible.value = tipos
    cargarMantenimientos()
    cargarInspecciones()
    cargarCombustible()
    cargarDocumentos()
  } catch (e: any) {
    toast.add({ severity:'error', summary:'Error', detail:'No se pudo cargar el vehículo', life:4000 })
    setTimeout(() => router.back(), 1200)
  } finally { loading.value = false }
}

const cargarMantenimientos = async () => {
  cargandoMant.value = true
  try { mantenimientos.value = await vehiculosService.listarMantenimientos(vehiculoId.value) }
  catch { mantenimientos.value = [] }
  finally { cargandoMant.value = false }
}
const cargarInspecciones = async () => {
  cargandoInsp.value = true
  try { inspecciones.value = await vehiculosService.listarInspecciones(vehiculoId.value) }
  catch { inspecciones.value = [] }
  finally { cargandoInsp.value = false }
}
const cargarCombustible = async () => {
  cargandoComb.value = true
  try {
    const r = await vehiculosService.listarCombustible(vehiculoId.value, 0, 100)
    combustibleRegistros.value = r.content
  } catch { combustibleRegistros.value = [] }
  finally { cargandoComb.value = false }
}
const cargarDocumentos = async () => {
  try { documentos.value = await vehiculosService.listarDocumentos(vehiculoId.value) }
  catch { documentos.value = [] }
}

// ----- Eliminar vehículo -----
const confirmarEliminar = () => {
  confirm.require({
    message: `¿Eliminar el vehículo "${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.placa})"?`,
    header: 'Confirmar eliminación', icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar', acceptLabel: 'Eliminar', acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.eliminarVehiculo(vehiculoId.value)
        toast.add({ severity:'success', summary:'Eliminado', life:2500 })
        setTimeout(() => router.push('/vehiculos'), 800)
      } catch (e: any) {
        toast.add({ severity:'error', summary:'Error', detail: e.response?.data?.detail || 'No se pudo eliminar', life:4000 })
      }
    }
  })
}

onMounted(cargar)
</script>
