package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.EstadoCuentaResponse;
import com.escuela.cobros.dto.FacturaListResponse;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EstadoCuentaMapper {

    default EstadoCuentaResponse toResponse(
        Long estudianteId,
        BigDecimal saldoPendiente,
        BigDecimal saldoPagado,
        BigDecimal saldoVencido,
        Integer cantidadPendientes,
        Integer cantidadPagadas,
        Integer cantidadVencidas,
        LocalDateTime ultimaActividad,
        List<FacturaListResponse> facturasActivas) {

        return new EstadoCuentaResponse(
            estudianteId,
            saldoPendiente,
            saldoPagado,
            saldoVencido,
            cantidadPendientes,
            cantidadPagadas,
            cantidadVencidas,
            ultimaActividad,
            facturasActivas
        );
    }
}
