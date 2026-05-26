/**
 * Etiquetas humanas para valores enum que el backend devuelve en SCREAMING_CASE.
 * Centralizar acá evita repetir el mapeo en cada componente.
 */

export const LABELS_SITUACION_PAGO: Record<string, string> = {
  // Modelo nuevo (Sprint 9 ext): 4 valores claros.
  PENDIENTE_FACTURACION: 'Pendiente facturación',
  PENDIENTE_PAGO:        'Pendiente de pago',
  PAGO_PARCIAL:          'Pago parcial',
  PAGADO_TOTAL:          'Pagado total',
  // Valores legacy (la migración V4 los recodificó en BD; se dejan aquí
  // por si algún cache/respuesta vieja los devuelve).
  PENDIENTE_MATRICULA: 'Pendiente facturación',
  SIN_DEUDA:           'Pagado total',
  AL_DIA:              'Pagado total',
  EN_MORA:             'Pago parcial'
}

export const LABELS_ESTADO_ESTUDIANTE: Record<string, string> = {
  PRE_MATRICULADO: 'Pre-matriculado',
  MATRICULADO:     'Matriculado',
  CURSANDO:        'Cursando',
  COMPLETADO:      'Completado',
  RETIRADO:        'Retirado'
  // OJO: NO mapear 'ACTIVO' aquí porque colisiona con catálogos
  // (concepto/tipo curso). Si quedan estudiantes legacy con ACTIVO,
  // el LABELS_GENERICO lo resuelve a "Activo" abajo.
}

/**
 * Etiquetas genéricas que aplican a entidades de catálogo
 * (conceptos, tipos curso, usuarios, etc).
 */
export const LABELS_GENERICO: Record<string, string> = {
  ACTIVO:   'Activo',
  INACTIVO: 'Inactivo'
}

export const LABELS_ESTADO_FACTURA: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  PARCIAL:   'Parcial',
  PAGADA:    'Pagada',
  PAGADO:    'Pagada',
  VENCIDA:   'Vencida',
  ANULADA:   'Anulada'
}

export const LABELS_TIPO_PAGO: Record<string, string> = {
  CONTADO: 'Contado',
  CREDITO: 'Crédito'
}

export const LABELS_ESTADO_CUOTA: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  PARCIAL:   'Parcial',
  PAGADA:    'Pagada',
  VENCIDA:   'Vencida'
}

/** Lookup compuesto: si existe label específica devuelve esa, sino devuelve el valor crudo. */
const ALL: Record<string, string> = {
  ...LABELS_GENERICO,
  ...LABELS_SITUACION_PAGO,
  ...LABELS_ESTADO_ESTUDIANTE,
  ...LABELS_ESTADO_FACTURA,
  ...LABELS_TIPO_PAGO,
  ...LABELS_ESTADO_CUOTA
}

export function humanLabel(value: string | null | undefined, fallback = '—'): string {
  if (!value) return fallback
  return ALL[value] || value
}
