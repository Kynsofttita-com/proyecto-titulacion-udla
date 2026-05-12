package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoResponse;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.PlacaDuplicadaException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.mapper.VehiculoMapper;
import com.escuela.vehiculos.repository.VehiculoRepository;
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
class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository repository;

    @Mock
    private VehiculoMapper mapper;

    @InjectMocks
    private VehiculoServiceImpl service;

    @Test
    void testFindById_Success() {
        Vehiculo vehiculo = Vehiculo.builder()
                .id(1L)
                .placa("ABC-1234")
                .marca("Toyota")
                .modelo("Corolla")
                .anio((short)2023)
                .vin("12345678")
                .color("Blanco")
                .kilometraje(5000)
                .createdAt(LocalDateTime.now())
                .build();

        VehiculoResponse response = new VehiculoResponse(
                1L, "ABC-1234", "Toyota", "Corolla", (short)2023, "12345678", "Blanco",
                5000L, null, null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(vehiculo));
        when(mapper.toResponse(vehiculo)).thenReturn(response);

        VehiculoResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals("ABC-1234", result.placa());
        verify(repository, times(1)).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(VehiculoNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void testCreate_Success() {
        CreateVehiculoRequest request = new CreateVehiculoRequest(
                "ABC-1234", "Toyota", "Corolla", (short)2023, "12345678", "Blanco",
                5000L, null, null, null
        );

        Vehiculo vehiculo = Vehiculo.builder()
                .id(1L)
                .placa("ABC-1234")
                .marca("Toyota")
                .modelo("Corolla")
                .anio((short)2023)
                .build();

        VehiculoResponse response = new VehiculoResponse(
                1L, "ABC-1234", "Toyota", "Corolla", (short)2023, "12345678", "Blanco",
                5000L, null, null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.existsByPlacaAndDeletedAtIsNull("ABC-1234")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(vehiculo);
        when(repository.save(any())).thenReturn(vehiculo);
        when(mapper.toResponse(vehiculo)).thenReturn(response);

        VehiculoResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("ABC-1234", result.placa());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testCreate_PlacaDuplicada() {
        CreateVehiculoRequest request = new CreateVehiculoRequest(
                "ABC-1234", "Toyota", "Corolla", (short)2023, "12345678", "Blanco",
                5000L, null, null, null
        );

        when(repository.existsByPlacaAndDeletedAtIsNull("ABC-1234")).thenReturn(true);
        assertThrows(PlacaDuplicadaException.class, () -> service.create(request));
    }

    @Test
    void testSoftDelete_Success() {
        Vehiculo vehiculo = Vehiculo.builder().id(1L).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(vehiculo));

        service.softDelete(1L);

        verify(repository, times(1)).save(any());
    }
}
