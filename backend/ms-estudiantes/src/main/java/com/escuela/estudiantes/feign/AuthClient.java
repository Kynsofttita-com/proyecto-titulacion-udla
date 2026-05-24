package com.escuela.estudiantes.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Cliente Feign hacia MS-Auth para crear cuentas de Usuario cuando se
 * matricula un estudiante. El enlace estudiante &lt;-&gt; usuario se completa
 * de forma asincrona via el evento {@code auth.usuario.creado} (ver
 * {@code EstudianteAuthEventListener}).
 *
 * <p>Usamos {@code Map<String, Object>} como payload para no acoplar este MS
 * al DTO interno de auth ({@code CreateUsuarioRequest}). Las claves esperadas
 * son: email, password, nombre, apellido, telefono, roles (List&lt;String&gt;),
 * cedula, fechaNacimiento, genero, direccion, passwordChangeRequired.</p>
 */
@FeignClient(name = "ms-auth", path = "/usuarios")
public interface AuthClient {

    /**
     * POST /usuarios — crea una cuenta de acceso al sistema.
     */
    @PostMapping
    Map<String, Object> crearUsuario(@RequestBody Map<String, Object> request);
}
