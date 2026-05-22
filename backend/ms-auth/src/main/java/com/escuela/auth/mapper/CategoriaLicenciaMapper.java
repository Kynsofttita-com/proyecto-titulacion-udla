package com.escuela.auth.mapper;

import com.escuela.auth.dto.CategoriaLicenciaResponse;
import com.escuela.auth.dto.CreateCategoriaLicenciaRequest;
import com.escuela.auth.dto.UpdateCategoriaLicenciaRequest;
import com.escuela.auth.entity.CategoriaLicencia;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoriaLicenciaMapper {

    CategoriaLicenciaResponse toResponse(CategoriaLicencia entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    CategoriaLicencia toEntity(CreateCategoriaLicenciaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateCategoriaLicenciaRequest request, @MappingTarget CategoriaLicencia entity);
}
