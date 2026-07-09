import api from './api'

export interface ReporteOperativoResponse {
  tipoReporte: string
  datos: Record<string, any>
  generadoEn: string
  duracionMs: number
  totalRegistros?: number
}

export interface ReporteFinancieroResponse {
  tipoReporte: string
  datos: Record<string, any>
  generadoEn: string
  duracionMs: number
}

export interface DashboardKPIResponse {
  kpis: Record<string, any>
  generadoEn: string
  duracionMs: number
}

export interface CreateReporteOperativoRequest {
  tipoReporte: string
  parametros?: Record<string, any>
}

export interface ExportarReporteRequest {
  tipoReporte: string
  formato: 'EXCEL' | 'CSV' | 'PDF'
}

class ReportesService {
  async obtenerKPIs(): Promise<DashboardKPIResponse> {
    try {
      const response = await api.get<DashboardKPIResponse>('/reportes/kpis')
      return response.data
    } catch (error) {
      console.error('Error obteniendo KPIs:', error)
      throw error
    }
  }

  // Reportes Operativos
  async generarReporteEstudiantesActivos(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteOperativoResponse> {
    try {
      const response = await api.post<ReporteOperativoResponse>(
        '/reportes/estudiantes-activos',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte estudiantes activos:', error)
      throw error
    }
  }

  async generarReporteInstructoresHoras(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteOperativoResponse> {
    try {
      const response = await api.post<ReporteOperativoResponse>(
        '/reportes/instructores-horas',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte instructores horas:', error)
      throw error
    }
  }

  async generarReporteVehiculosSoat(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteOperativoResponse> {
    try {
      const response = await api.post<ReporteOperativoResponse>(
        '/reportes/vehiculos-soat',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte vehículos SOAT:', error)
      throw error
    }
  }

  async generarReporteAsistencia(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteOperativoResponse> {
    try {
      const response = await api.post<ReporteOperativoResponse>(
        '/reportes/asistencia',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte asistencia:', error)
      throw error
    }
  }

  // Reportes Financieros
  async generarReporteIngresos(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteFinancieroResponse> {
    try {
      const response = await api.post<ReporteFinancieroResponse>(
        '/reportes/ingresos-periodo',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte ingresos:', error)
      throw error
    }
  }

  async generarReporteMorosidad(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteFinancieroResponse> {
    try {
      const response = await api.post<ReporteFinancieroResponse>(
        '/reportes/morosidad',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte morosidad:', error)
      throw error
    }
  }

  async generarReporteRecibos(
    request: CreateReporteOperativoRequest
  ): Promise<ReporteFinancieroResponse> {
    try {
      const response = await api.post<ReporteFinancieroResponse>(
        '/reportes/recibos',
        request
      )
      return response.data
    } catch (error) {
      console.error('Error generando reporte recibos:', error)
      throw error
    }
  }

  async exportarReporte(datos: ExportarReporteRequest): Promise<Blob> {
    try {
      const response = await api.post('/reportes/exportar', datos, {
        responseType: 'blob'
      })
      return response.data
    } catch (error) {
      console.error('Error exportando reporte:', error)
      throw error
    }
  }

  descargarArchivo(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = nombre
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }

  generarNombreArchivo(tipo: string, formato: string): string {
    const fecha = new Date().toISOString().split('T')[0]
    return `reporte_${tipo}_${fecha}.${formato.toLowerCase() === 'excel' ? 'xlsx' : 'csv'}`
  }
}

export default new ReportesService()
