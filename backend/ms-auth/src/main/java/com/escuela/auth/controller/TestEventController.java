package com.escuela.auth.controller;

import com.escuela.auth.service.UsuarioEventPublisher;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller TEMPORAL solo para validar el flujo de eventos en T3.3.
 *
 * <p>Endpoint: {@code POST /test/publicar-usuario-creado}
 * Body: {@code { "usuarioId": 1, "email": "...", "nombre": "...", "apellido": "..." }}</p>
 *
 * <p>Una vez que MS-Auth implemente el flujo real de creacion de usuarios
 * (Sprint 4), este endpoint debe eliminarse.</p>
 */
@RestController
@RequestMapping("/test")
@Profile("!test")
public class TestEventController {

    private final UsuarioEventPublisher publisher;

    public TestEventController(UsuarioEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publicar-usuario-creado")
    public ResponseEntity<Map<String, Object>> publicarUsuarioCreado(
            @RequestBody UsuarioCreadoEvent body) {
        publisher.publicarUsuarioCreado(body);
        return ResponseEntity.accepted().body(Map.of(
                "status", "publicado",
                "eventId", body.getEventId(),
                "routingKey", UsuarioCreadoEvent.ROUTING_KEY
        ));
    }
}
