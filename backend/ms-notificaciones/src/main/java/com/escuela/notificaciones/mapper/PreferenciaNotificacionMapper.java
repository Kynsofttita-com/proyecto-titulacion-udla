package com.escuela.notificaciones.mapper;

import com.escuela.notificaciones.dto.PreferenciaResponse;
import com.escuela.notificaciones.dto.UpdatePreferenciaRequest;
import com.escuela.notificaciones.entity.PreferenciaNotificacion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PreferenciaNotificacionMapper {

    PreferenciaResponse toResponse(PreferenciaNotificacion entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(@MappingTarget PreferenciaNotificacion entity, UpdatePreferenciaRequest request);
}
