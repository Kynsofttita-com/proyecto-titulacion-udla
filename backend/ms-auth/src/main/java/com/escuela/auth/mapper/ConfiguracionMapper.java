package com.escuela.auth.mapper;

import com.escuela.auth.dto.ConfiguracionResponse;
import com.escuela.auth.dto.UpdateConfiguracionRequest;
import com.escuela.auth.entity.ConfiguracionEscuela;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConfiguracionMapper {

    ConfiguracionResponse toResponse(ConfiguracionEscuela entity);

    /** Mezcla los campos no-null del request sobre la entidad existente. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateConfiguracionRequest request, @MappingTarget ConfiguracionEscuela entity);
}
