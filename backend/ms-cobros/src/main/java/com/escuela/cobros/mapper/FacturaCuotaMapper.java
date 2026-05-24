package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.FacturaCuotaResponse;
import com.escuela.cobros.entity.FacturaCuota;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FacturaCuotaMapper {

    FacturaCuotaResponse toResponse(FacturaCuota cuota);
}
