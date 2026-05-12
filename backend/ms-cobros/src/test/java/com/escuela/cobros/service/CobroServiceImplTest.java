package com.escuela.cobros.service;

import com.escuela.cobros.dto.CreateCobroRequest;
import com.escuela.cobros.dto.CobroResponse;
import com.escuela.cobros.entity.Cobro;
import com.escuela.cobros.exception.CobroNotFoundException;
import com.escuela.cobros.mapper.CobroMapper;
import com.escuela.cobros.repository.CobroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CobroServiceImplTest {

    @Mock
    private CobroRepository repository;

    @Mock
    private CobroMapper mapper;

    @InjectMocks
    private CobroServiceImpl service;

    @Test
    void testFindById_Success() {
        Cobro cobro = Cobro.builder()
                .id(1L)
                .estudianteId(1L)
                .monto(BigDecimal.valueOf(100))
                .tipoPago("INSCRIPCION")
                .estado("REGISTRADO")
                .fecha(LocalDate.now())
                .dateCreated(LocalDateTime.now())
                .build();

        CobroResponse response = new CobroResponse(
                1L, 1L, BigDecimal.valueOf(100), "INSCRIPCION", "REGISTRADO",
                null, LocalDate.now(), null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cobro));
        when(mapper.toResponse(cobro)).thenReturn(response);

        CobroResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, times(1)).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(CobroNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void testCreate_Success() {
        CreateCobroRequest request = new CreateCobroRequest(
                1L, BigDecimal.valueOf(100), "INSCRIPCION", null, null, null
        );

        Cobro cobro = Cobro.builder()
                .id(1L)
                .estudianteId(1L)
                .monto(BigDecimal.valueOf(100))
                .tipoPago("INSCRIPCION")
                .build();

        CobroResponse response = new CobroResponse(
                1L, 1L, BigDecimal.valueOf(100), "INSCRIPCION", "REGISTRADO",
                null, LocalDate.now(), null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(mapper.toEntity(request)).thenReturn(cobro);
        when(repository.save(any())).thenReturn(cobro);
        when(mapper.toResponse(cobro)).thenReturn(response);

        CobroResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testSoftDelete_Success() {
        Cobro cobro = Cobro.builder().id(1L).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cobro));

        service.softDelete(1L);

        verify(repository, times(1)).save(any());
    }
}
