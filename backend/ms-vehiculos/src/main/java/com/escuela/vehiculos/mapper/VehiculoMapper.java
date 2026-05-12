package com.escuela.vehiculos.mapper;

import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import com.escuela.vehiculos.entity.Vehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehiculoMapper {

    @Mapping(target = "anio", source = "año")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "soatVencimiento", ignore = true)
    @Mapping(target = "revisionVencimiento", ignore = true)
    @Mapping(target = "fechaCompra", ignore = true)
    @Mapping(target = "valorCompra", ignore = true)
    @Mapping(target = "categoriaLicenciaId", ignore = true)
    Vehiculo toEntity(CreateVehiculoRequest request);

    @Mapping(target = "anio", source = "año")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "placa", ignore = true)
    @Mapping(target = "vin", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "soatVencimiento", ignore = true)
    @Mapping(target = "revisionVencimiento", ignore = true)
    @Mapping(target = "fechaCompra", ignore = true)
    @Mapping(target = "valorCompra", ignore = true)
    @Mapping(target = "categoriaLicenciaId", ignore = true)
    void updateEntity(UpdateVehiculoRequest request, @MappingTarget Vehiculo entity);

    @Mapping(target = "año", source = "anio")
    @Mapping(target = "fechaMantenimiento", ignore = true)
    @Mapping(target = "fechaInspeccion", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    VehiculoResponse toResponse(Vehiculo entity);

    @Mapping(target = "año", source = "anio")
    @Mapping(target = "fechaMantenimiento", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    VehiculoListResponse toListResponse(Vehiculo entity);
}
