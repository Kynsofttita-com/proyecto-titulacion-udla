package com.escuela.notificaciones.service;

import com.escuela.notificaciones.dto.NotificacionResponse;
import com.escuela.notificaciones.entity.Notificacion;
import com.escuela.notificaciones.exception.NotificacionNotFoundException;
import com.escuela.notificaciones.mapper.NotificacionMapper;
import com.escuela.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @Mock
    private NotificacionMapper mapper;

    @InjectMocks
    private NotificacionService service;

    private Notificacion notificacionEntity;
    private NotificacionResponse notificacionResponse;

    @BeforeEach
    void setUp() {
        notificacionEntity = Notificacion.builder()
            .id(1L)
            .usuarioId(100L)
            .tipo("RECORDATORIO")
            .titulo("Recordatorio de clase")
            .mensaje("Tu clase comienza en 30 minutos")
            .leida(false)
            .fechaCreacion(LocalDateTime.now())
            .prioridad("ALTO")
            .build();

        notificacionResponse = new NotificacionResponse(
            1L,
            100L,
            "RECORDATORIO",
            "Recordatorio de clase",
            "Tu clase comienza en 30 minutos",
            false,
            LocalDateTime.now(),
            null,
            null,
            "ALTO",
            LocalDateTime.now(),
            "admin",
            null
        );
    }

    @Test
    void testObtenerPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(notificacionEntity));
        when(mapper.toResponse(notificacionEntity)).thenReturn(notificacionResponse);

        NotificacionResponse response = service.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("RECORDATORIO", response.tipo());
        verify(repository).findById(1L);
    }

    @Test
    void testObtenerPorIdNoEncontrada() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotificacionNotFoundException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    void testListarPorUsuario() {
        Page<Notificacion> page = new PageImpl<>(List.of(notificacionEntity));
        when(repository.findByUsuarioId(100L, PageRequest.of(0, 20)))
            .thenReturn(page);
        when(mapper.toResponse(notificacionEntity)).thenReturn(notificacionResponse);

        Page<NotificacionResponse> result = service.listarPorUsuario(100L, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testMarcarComoLeida() {
        when(repository.findById(1L)).thenReturn(Optional.of(notificacionEntity));
        when(repository.save(any(Notificacion.class))).thenReturn(notificacionEntity);
        when(mapper.toResponse(notificacionEntity)).thenReturn(notificacionResponse);

        NotificacionResponse response = service.marcarComoLeida(1L);

        assertNotNull(response);
        assertTrue(notificacionEntity.getLeida());
        assertNotNull(notificacionEntity.getFechaLectura());
        verify(repository).save(any(Notificacion.class));
    }

    @Test
    void testEliminar() {
        when(repository.findById(1L)).thenReturn(Optional.of(notificacionEntity));
        when(repository.save(any(Notificacion.class))).thenReturn(notificacionEntity);

        service.eliminar(1L);

        assertNotNull(notificacionEntity.getDeletedAt());
        verify(repository).save(any(Notificacion.class));
    }
}
