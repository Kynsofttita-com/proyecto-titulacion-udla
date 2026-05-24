/**
 * Etiquetas humanas para valores enum que el backend devuelve en SCREAMING_CASE.
 * Centralizar acá evita repetir el mapeo en cada componente.
 */

export const LABELS_SITUACION_PAGO: Record<string, string> = {
  SIN_DEUDA:    'Sin deuda',
  PAGO_PARCIAL: 'Pago parcial',
  AL_DIA:       'Al día',
  EN_MORA:      'En mora',
  PAGADO_TOTAL: 'Pagado total'
}

export const LABELS_ESTADO_ESTUDIANTE: Record<string, string> = {
  PRE_MATRICULADO: 'Pre-matriculado',
  MATRICULADO:     'Matriculado',
  CURSANDO:        'Cursando',
  COMPLETADO:      'Completado',
  RETIRADO:        'Retirado',
  // Legado (por filas no migradas)
  ACTIVO:          'Matriculado',
  INACTIVO:        'Inactivo'
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
