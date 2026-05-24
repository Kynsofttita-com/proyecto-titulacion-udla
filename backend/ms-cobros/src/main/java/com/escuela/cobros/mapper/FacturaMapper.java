package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import com.escuela.cobros.entity.Factura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FacturaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroFactura", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "montoPagado", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "fechaEmision", ignore = true)
    @Mapping(target = "motivoAnulacion", ignore = true)
    @Mapping(target = "cuotasPagadas", ignore = true)
    @Mapping(target = "valorCuota", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Factura toEntity(FacturaRequest request);

    FacturaResponse toResponse(Factura factura);

    FacturaListResponse toListResponse(Factura factura);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroFactura", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "montoPagado", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "fechaEmision", ignore = true)
    @Mapping(target = "motivoAnulacion", ignore = true)
    @Mapping(target = "cuotasPagadas", ignore = true)
    @Mapping(target = "valorCuota", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(FacturaRequest request, @MappingTarget Factura factura);
}
