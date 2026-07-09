package com.escuela.notificaciones.service;

import com.escuela.notificaciones.dto.PreferenciaResponse;
import com.escuela.notificaciones.dto.UpdatePreferenciaRequest;
import com.escuela.notificaciones.entity.PreferenciaNotificacion;
import com.escuela.notificaciones.exception.PreferenciaNotFoundException;
import com.escuela.notificaciones.mapper.PreferenciaNotificacionMapper;
import com.escuela.notificaciones.repository.PreferenciaNotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenciaNotificacionServiceTest {

    @Mock
    private PreferenciaNotificacionRepository repository;

    @Mock
    private PreferenciaNotificacionMapper mapper;

    @InjectMocks
    private PreferenciaNotificacionService service;

    private PreferenciaNotificacion preferenciaEntity;
    private PreferenciaResponse preferenciaResponse;

    @BeforeEach
    void setUp() {
        preferenciaEntity = PreferenciaNotificacion.builder()
            .id(1L)
            .usuarioId(100L)
            .recibirEmail(true)
            .recibirInApp(true)
            .recibirRecordatorios(true)
            .recibirAlertasAdmin(false)
            .build();

        preferenciaResponse = new PreferenciaResponse(
            1L,
            100L,
            true,
            true,
            true,
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "admin",
            "admin"
        );
    }

    @Test
    void testObtenerPorUsuario() {
        when(repository.findByUsuarioId(100L)).thenReturn(Optional.of(preferenciaEntity));
        when(mapper.toResponse(preferenciaEntity)).thenReturn(preferenciaResponse);

        PreferenciaResponse response = service.obtenerPorUsuario(100L);

        assertNotNull(response);
        assertEquals(100L, response.usuarioId());
        assertTrue(response.recibirEmail());
        verify(repository).findByUsuarioId(100L);
    }

    @Test
    void testObtenerPorUsuarioNoEncontrada() {
        when(repository.findByUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(PreferenciaNotFoundException.class, () -> service.obtenerPorUsuario(999L));
    }

    @Test
    void testActualizar() {
        UpdatePreferenciaRequest request = new UpdatePreferenciaRequest(false, false, true, true);

        when(repository.findByUsuarioId(100L)).thenReturn(Optional.of(preferenciaEntity));
        doNothing().when(mapper).updateEntity(any(PreferenciaNotificacion.class), any(UpdatePreferenciaRequest.class));
        when(repository.save(any(PreferenciaNotificacion.class))).thenReturn(preferenciaEntity);
        when(mapper.toResponse(preferenciaEntity)).thenReturn(preferenciaResponse);

        PreferenciaResponse response = service.actualizar(100L, request);

        assertNotNull(response);
        verify(repository).findByUsuarioId(100L);
        verify(repository).save(any(PreferenciaNotificacion.class));
    }

    @Test
    void testCrearOActualizar_Crear() {
        UpdatePreferenciaRequest request = new UpdatePreferenciaRequest(true, true, true, false);

        when(repository.findByUsuarioId(200L)).thenReturn(Optional.empty());
        when(repository.save(any(PreferenciaNotificacion.class))).thenReturn(preferenciaEntity);
        when(mapper.toResponse(preferenciaEntity)).thenReturn(preferenciaResponse);

        PreferenciaResponse response = service.crearOActualizar(200L, request);

        assertNotNull(response);
        verify(repository).save(any(PreferenciaNotificacion.class));
    }

    @Test
    void testCrearOActualizar_Actualizar() {
        UpdatePreferenciaRequest request = new UpdatePreferenciaRequest(false, false, true, true);

        when(repository.findByUsuarioId(100L)).thenReturn(Optional.of(preferenciaEntity));
        doNothing().when(mapper).updateEntity(any(PreferenciaNotificacion.class), any(UpdatePreferenciaRequest.class));
        when(repository.save(any(PreferenciaNotificacion.class))).thenReturn(preferenciaEntity);
        when(mapper.toResponse(preferenciaEntity)).thenReturn(preferenciaResponse);

        PreferenciaResponse response = service.crearOActualizar(100L, request);

        assertNotNull(response);
        verify(repository).findByUsuarioId(100L);
        verify(repository).save(any(PreferenciaNotificacion.class));
    }
}
