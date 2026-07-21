package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import com.escuela.cobros.entity.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "factura", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "numeroCuota", ignore = true)
    @Mapping(target = "facturaCuotaId", ignore = true)
    @Mapping(target = "cuentaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Pago toEntity(PagoRequest request);

    @Mapping(source = "factura.id", target = "facturaId")
    PagoResponse toResponse(Pago pago);

    @Mapping(source = "factura.id", target = "facturaId")
    PagoListResponse toListResponse(Pago pago);
}
