/**
 * Helpers para formatear fechas del backend sin corrimientos de zona horaria.
 *
 * Problema: el backend serializa `LocalDate` como string "YYYY-MM-DD" (sin hora).
 * `new Date("2026-07-19")` en JS lo interpreta como UTC medianoche → en TZ Ecuador
 * (UTC-5) `toLocaleDateString` devuelve el DIA ANTERIOR.
 *
 * Estos helpers parsean los componentes Y-M-D en local para preservar el dia
 * tal como lo guarda el backend.
 */

const DEFAULT_LOCALE = 'es-EC'

/**
 * Parsea una fecha en formato "YYYY-MM-DD" (LocalDate del backend) como fecha
 * local. Devuelve null si el string es invalido o vacio.
 *
 * Acepta tambien strings ISO completos ("YYYY-MM-DDTHH:MM:SS"); en ese caso
 * usa `new Date(str)` que ya interpreta la hora correctamente.
 */
export function parseLocalDate(fecha: string | null | undefined): Date | null {
  if (!fecha) return null
  const iso = String(fecha).trim()
  // Si trae hora (T o espacio), delegar en Date (respeta TZ del string).
  if (iso.length > 10 && (iso.includes('T') || iso.includes(' '))) {
    const d = new Date(iso)
    return isNaN(d.getTime()) ? null : d
  }
  const [y, m, d] = iso.substring(0, 10).split('-').map(Number)
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}

/**
 * Formatea "YYYY-MM-DD" como fecha corta local (ej: "19/7/2026") sin desfase.
 * Devuelve el fallback (por defecto '--') si el string es invalido.
 */
export function fmtFechaLocal(
  fecha: string | null | undefined,
  opts: Intl.DateTimeFormatOptions = {},
  fallback = '--',
  locale: string = DEFAULT_LOCALE
): string {
  const d = parseLocalDate(fecha)
  if (!d) return fallback
  return d.toLocaleDateString(locale, opts)
}

/**
 * Igual que fmtFechaLocal pero para timestamps ISO completos (LocalDateTime).
 * Aca sí conviene usar new Date(str) porque hay hora explicita.
 */
export function fmtFechaHoraLocal(
  fecha: string | null | undefined,
  opts: Intl.DateTimeFormatOptions = {},
  fallback = '--',
  locale: string = DEFAULT_LOCALE
): string {
  if (!fecha) return fallback
  const d = new Date(fecha)
  if (isNaN(d.getTime())) return fallback
  return d.toLocaleString(locale, opts)
}
