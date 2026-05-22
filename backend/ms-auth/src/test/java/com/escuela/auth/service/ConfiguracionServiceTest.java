package com.escuela.auth.service;

import com.escuela.auth.dto.ConfiguracionResponse;
import com.escuela.auth.dto.UpdateConfiguracionRequest;
import com.escuela.auth.entity.ConfiguracionEscuela;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.ConfiguracionMapper;
import com.escuela.auth.repository.ConfiguracionEscuelaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    @Mock private ConfiguracionEscuelaRepository repository;
    @Mock private ConfiguracionMapper mapper;

    @InjectMocks
    private ConfiguracionService service;

    @Test
    @DisplayName("obtener() devuelve la unica fila existente")
    void obtenerOk() {
        ConfiguracionEscuela conf = ConfiguracionEscuela.builder()
                .id(1L).nombre("Escuela Demo").ruc("1791251237001").build();
        when(repository.findAll()).thenReturn(List.of(conf));
        when(mapper.toResponse(conf)).thenReturn(new ConfiguracionResponse(
                1L, "Escuela Demo", "1791251237001", null, null, null, null, null, null,
                (short) 60, null, null, (short) 24, (short) 30));

        ConfiguracionResponse r = service.obtener();

        assertEquals("Escuela Demo", r.nombre());
        assertEquals("1791251237001", r.ruc());
    }

    @Test
    @DisplayName("obtener() falla si tabla vacia (caso defensivo)")
    void obtenerVacio() {
        when(repository.findAll()).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> service.obtener());
    }

    @Test
    @DisplayName("actualizar() falla con DuplicateResource si RUC cambia y choca")
    void actualizarRucDuplicado() {
        ConfiguracionEscuela conf = ConfiguracionEscuela.builder()
                .id(1L).nombre("X").ruc("1791251237001").build();
        ConfiguracionEscuela otro = ConfiguracionEscuela.builder().id(2L).build();

        when(repository.findAll()).thenReturn(List.of(conf));
        when(repository.findByRuc("0999999999001")).thenReturn(Optional.of(otro));

        UpdateConfiguracionRequest req = new UpdateConfiguracionRequest(
                "X", "0999999999001", null, null, null, null, null, null,
                null, null, null, null, null);

        assertThrows(DuplicateResourceException.class, () -> service.actualizar(req));
    }

    @Test
    @DisplayName("actualizar() aplica cambios y guarda")
    void actualizarOk() {
        ConfiguracionEscuela conf = ConfiguracionEscuela.builder()
                .id(1L).nombre("Nombre Viejo").ruc("1791251237001").build();
        when(repository.findAll()).thenReturn(List.of(conf));
        when(mapper.toResponse(any())).thenReturn(new ConfiguracionResponse(
                1L, "Nombre Nuevo", "1791251237001", null, null, null, null, null, null,
                (short) 60, null, null, (short) 24, (short) 30));

        UpdateConfiguracionRequest req = new UpdateConfiguracionRequest(
                "Nombre Nuevo", "1791251237001", null, null, null, null, null, null,
                null, null, null, null, null);

        ConfiguracionResponse r = service.actualizar(req);

        assertEquals("Nombre Nuevo", r.nombre());
        verify(repository).save(conf);
        verify(mapper).updateEntity(req, conf);
    }
}
