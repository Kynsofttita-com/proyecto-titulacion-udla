package com.escuela.instructores.service;

import com.escuela.instructores.dto.CreateInstructorRequest;
import com.escuela.instructores.dto.InstructorListResponse;
import com.escuela.instructores.dto.InstructorResponse;
import com.escuela.instructores.dto.UpdateInstructorRequest;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.exception.DuplicateResourceException;
import com.escuela.instructores.exception.InstructorNotFoundException;
import com.escuela.instructores.mapper.InstructorMapper;
import com.escuela.instructores.repository.InstructorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock private InstructorRepository repository;
    @Mock private InstructorMapper mapper;
    @Mock private InstructorEventDispatcher eventDispatcher;

    @InjectMocks
    private InstructorService service;

    private CreateInstructorRequest req() {
        return new CreateInstructorRequest(
                "1710034065", "Juan", "Perez", "j@p.com", "0991234567",
                "Av Quito", LocalDate.of(1990, 1, 1),
                "LIC-001", "B", LocalDate.now().minusYears(2),
                LocalDate.now().plusYears(3), LocalDate.now().minusYears(1),
                new BigDecimal("500.00"), null);
    }

    @Test
    @DisplayName("crear() falla con cedula invalida (digito verificador)")
    void crearCedulaInvalida() {
        CreateInstructorRequest bad = new CreateInstructorRequest(
                "1710034066", // dv incorrecto
                "X", "Y", "x@y.com", "0991234567", null, null,
                "LIC-2", "B", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2), null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.crear(bad));
    }

    @Test
    @DisplayName("crear() falla con cedula duplicada")
    void crearDuplicado() {
        when(repository.findByCedulaAndDeletedAtIsNull("1710034065"))
                .thenReturn(Optional.of(Instructor.builder().id(1L).build()));
        assertThrows(DuplicateResourceException.class, () -> service.crear(req()));
    }

    @Test
    @DisplayName("crear() OK con cedula valida y sin duplicados")
    void crearOk() {
        when(repository.findByCedulaAndDeletedAtIsNull("1710034065")).thenReturn(Optional.empty());
        when(repository.findByEmailAndDeletedAtIsNull("j@p.com")).thenReturn(Optional.empty());
        when(repository.findByLicenciaNumeroAndDeletedAtIsNull("LIC-001")).thenReturn(Optional.empty());
        when(mapper.toEntity(any())).thenReturn(Instructor.builder().cedula("1710034065").build());
        when(repository.save(any(Instructor.class))).thenAnswer(inv -> {
            Instructor i = inv.getArgument(0);
            i.setId(7L);
            return i;
        });
        when(mapper.toResponse(any())).thenReturn(new InstructorResponse(
                7L, "1710034065", "Juan", "Perez", "j@p.com", "0991234567",
                null, null, "LIC-001", "B", null, null, "ACTIVO",
                null, null, null, null));

        InstructorResponse r = service.crear(req());
        assertEquals(7L, r.id());
        assertEquals("ACTIVO", r.estado());
    }

    @Test
    @DisplayName("obtener() falla 404 si no existe")
    void obtenerNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(InstructorNotFoundException.class, () -> service.obtener(99L));
    }

    @Test
    @DisplayName("actualizar() solo cambia campos no-null")
    void actualizarParcial() {
        Instructor i = Instructor.builder().id(1L).nombre("OldName").apellido("Old")
                .email("a@b.com").licenciaNumero("LIC-1").build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(i));
        when(repository.save(any(Instructor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            Instructor x = inv.getArgument(0);
            return new InstructorResponse(x.getId(), x.getCedula(), x.getNombre(), x.getApellido(),
                    x.getEmail(), null, null, null, x.getLicenciaNumero(), null, null, null,
                    null, null, null, null, null);
        });

        UpdateInstructorRequest upd = new UpdateInstructorRequest(
                "NuevoNombre", null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        InstructorResponse r = service.actualizar(1L, upd);
        assertEquals("NuevoNombre", r.nombre());
        assertEquals("Old", r.apellido());
    }

    @Test
    @DisplayName("eliminar() hace soft delete + estado=INACTIVO")
    void eliminarSoftDelete() {
        Instructor i = Instructor.builder().id(1L).estado("ACTIVO").build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(i));

        service.eliminar(1L);

        assertEquals("INACTIVO", i.getEstado());
        assertNotNull(i.getDeletedAt());
        verify(repository).save(i);
    }
}
