package com.escuela.auth.mapper;

import com.escuela.auth.dto.ConceptoFacturacionResponse;
import com.escuela.auth.dto.CreateConceptoFacturacionRequest;
import com.escuela.auth.dto.UpdateConceptoFacturacionRequest;
import com.escuela.auth.entity.ConceptoFacturacion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConceptoFacturacionMapper {

    ConceptoFacturacionResponse toResponse(ConceptoFacturacion entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ConceptoFacturacion toEntity(CreateConceptoFacturacionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateConceptoFacturacionRequest request, @MappingTarget ConceptoFacturacion entity);
}
