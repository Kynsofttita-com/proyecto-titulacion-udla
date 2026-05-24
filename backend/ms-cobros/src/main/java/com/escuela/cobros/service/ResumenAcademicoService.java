package com.escuela.cobros.service;

import com.escuela.cobros.client.AuthClient;
import com.escuela.cobros.client.EstudianteClient;
import com.escuela.cobros.dto.EstudianteDetailDTO;
import com.escuela.cobros.dto.ResumenAcademicoResponse;
import com.escuela.cobros.dto.TipoCursoDTO;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.cobros.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Construye el {@link ResumenAcademicoResponse} de un estudiante combinando:
 *  - Datos del estudiante via Feign a MS-Estudiantes (incluye tipo_curso_id)
 *  - Precio del curso via Feign a MS-Auth (catalogo /tipos-curso/{id})
 *  - Historia de facturas/pagos locales en MS-Cobros
 *
 * <p>Endpoint expuesto: GET /facturas/estudiante/{id}/resumen-academico</p>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ResumenAcademicoService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final EstudianteClient estudianteClient;
    private final ObjectProvider<AuthClient> authClientProvider;

    public ResumenAcademicoResponse calcular(Long estudianteId) {
        EstudianteDetailDTO est;
        try {
            est = estudianteClient.obtenerEstudiante(estudianteId);
        } catch (Exception e) {
            log.warn("No se pudo obtener estudiante {}: {}", estudianteId, e.getMessage());
            return responseSinDatos(estudianteId);
        }

        // Datos del curso contratado (si existe)
        TipoCursoDTO curso = null;
        if (est.tipoCursoId() != null) {
            AuthClient authClient = authClientProvider.getIfAvailable();
            if (authClient != null) {
                try {
                    curso = authClient.obtenerTipoCurso(est.tipoCursoId());
                } catch (Exception e) {
                    log.warn("No se pudo obtener tipo_curso {}: {}", est.tipoCursoId(), e.getMessage());
                }
            }
        }

        // Sumas de facturas/pagos
        List<Factura> facturas = facturaRepository
            .findByEstudianteIdAndDeletedAtIsNull(estudianteId, PageRequest.of(0, 1000))
            .getContent()
            .stream()
            .filter(f -> !"ANULADA".equalsIgnoreCase(f.getEstado())
                      && !"CANCELADA".equalsIgnoreCase(f.getEstado()))
            .toList();

        BigDecimal totalFacturado = facturas.stream()
            .map(Factura::getMontoOriginal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPagado = facturas.stream()
            .map(Factura::getMontoPagado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal precioCurso = (curso != null && curso.precioBase() != null)
            ? curso.precioBase() : BigDecimal.ZERO;
        // saldoTotal: lo que aun debe sobre el curso completo (independiente de si lo factu-
        // raron entero). Si todavia no hay curso asignado, no podemos calcularlo asi que 0.
        BigDecimal saldoTotal = (curso != null && curso.precioBase() != null)
            ? precioCurso.subtract(totalPagado).max(BigDecimal.ZERO)
            : BigDecimal.ZERO;
        BigDecimal saldoFacturado = totalFacturado.subtract(totalPagado).max(BigDecimal.ZERO);

        int cantPagos = facturas.stream()
            .mapToInt(f -> pagoRepository.findByFacturaId(f.getId(), PageRequest.of(0, 1000))
                .getNumberOfElements())
            .sum();

        return new ResumenAcademicoResponse(
            estudianteId,
            est.nombreCompleto(),
            est.estado(),
            est.situacionPago(),
            est.tipoCursoId(),
            curso != null ? curso.nombre() : null,
            curso != null ? curso.categoriaLicenciaCodigo() : null,
            curso != null ? curso.duracionTotalHoras() : null,
            precioCurso,
            totalFacturado,
            totalPagado,
            saldoTotal,
            saldoFacturado,
            curso != null && curso.precioBase() != null,
            facturas.size(),
            cantPagos
        );
    }

    private ResumenAcademicoResponse responseSinDatos(Long estudianteId) {
        return new ResumenAcademicoResponse(
            estudianteId, null, null, null,
            null, null, null, null,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO,
            false, 0, 0
        );
    }
}
