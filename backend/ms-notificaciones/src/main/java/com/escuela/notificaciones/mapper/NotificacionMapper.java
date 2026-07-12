package com.escuela.notificaciones.mapper;

import com.escuela.notificaciones.dto.NotificacionResponse;
import com.escuela.notificaciones.entity.Notificacion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    NotificacionResponse toResponse(Notificacion entity);
}
