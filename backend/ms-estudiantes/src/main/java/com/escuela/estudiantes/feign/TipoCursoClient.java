package com.escuela.estudiantes.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente Feign hacia MS-Auth para consultar tipos de curso.
 * Se usa desde EstudianteServiceImpl al sumar minutos para saber si el estudiante
 * completo el 100% del curso y disparar CursoCompletadoEvent.
 *
 * <p>Usamos Map en lugar de un DTO acoplado para minimizar dependencias.
 * Claves esperadas: id, nombre, descripcion, duracionTotalHoras, precioBase,
 * categoriaLicenciaId, categoriaLicenciaCodigo, activo.</p>
 */
@FeignClient(name = "ms-auth", contextId = "authTiposCursoClient", path = "/tipos-curso")
public interface TipoCursoClient {

    @GetMapping("/{id}")
    Map<String, Object> obtener(@PathVariable("id") Long id);
}
