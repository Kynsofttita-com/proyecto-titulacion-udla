package com.escuela.auth.service;

import com.escuela.auth.dto.CategoriaLicenciaResponse;
import com.escuela.auth.dto.CreateCategoriaLicenciaRequest;
import com.escuela.auth.dto.UpdateCategoriaLicenciaRequest;
import com.escuela.auth.entity.CategoriaLicencia;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.CategoriaLicenciaMapper;
import com.escuela.auth.repository.CategoriaLicenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaLicenciaServiceTest {

    @Mock private CategoriaLicenciaRepository repository;
    @Mock private CategoriaLicenciaMapper mapper;

    @InjectMocks
    private CategoriaLicenciaService service;

    @Test
    @DisplayName("listar() devuelve Page mapeado a Response")
    void listar() {
        Pageable pageable = PageRequest.of(0, 10);
        CategoriaLicencia cat = CategoriaLicencia.builder().id(1L).codigo("B").descripcion("Auto").activa(true).build();
        when(repository.findByDeletedAtIsNull(pageable)).thenReturn(new PageImpl<>(List.of(cat)));
        when(mapper.toResponse(cat)).thenReturn(new CategoriaLicenciaResponse(1L, "B", "Auto", true));

        Page<CategoriaLicenciaResponse> page = service.listar(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("B", page.getContent().get(0).codigo());
    }

    @Test
    @DisplayName("obtener() falla 404 si no existe")
    void obtenerNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.obtener(99L));
    }

    @Test
    @DisplayName("crear() falla con DuplicateResource si codigo ya existe")
    void crearDuplicado() {
        when(repository.findByCodigoAndDeletedAtIsNull("B"))
                .thenReturn(Optional.of(CategoriaLicencia.builder().id(1L).codigo("B").build()));

        assertThrows(DuplicateResourceException.class,
                () -> service.crear(new CreateCategoriaLicenciaRequest("B", "Auto", null)));
    }

    @Test
    @DisplayName("crear() establece activa=true por defecto si no se especifica")
    void crearActivaPorDefecto() {
        when(repository.findByCodigoAndDeletedAtIsNull("D")).thenReturn(Optional.empty());

        CategoriaLicencia entityNueva = CategoriaLicencia.builder().codigo("D").descripcion("Bus").activa(null).build();
        when(mapper.toEntity(any())).thenReturn(entityNueva);

        when(repository.save(any(CategoriaLicencia.class))).thenAnswer(inv -> {
            CategoriaLicencia c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            CategoriaLicencia c = inv.getArgument(0);
            return new CategoriaLicenciaResponse(c.getId(), c.getCodigo(), c.getDescripcion(), c.getActiva());
        });

        CategoriaLicenciaResponse r = service.crear(new CreateCategoriaLicenciaRequest("D", "Bus", null));

        assertTrue(r.activa());
    }

    @Test
    @DisplayName("actualizar() valida codigo duplicado solo si cambio")
    void actualizarCodigoDuplicado() {
        CategoriaLicencia existing = CategoriaLicencia.builder().id(1L).codigo("A").descripcion("Moto").activa(true).build();
        CategoriaLicencia otro = CategoriaLicencia.builder().id(2L).codigo("B").build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(repository.findByCodigoAndDeletedAtIsNull("B")).thenReturn(Optional.of(otro));

        assertThrows(DuplicateResourceException.class,
                () -> service.actualizar(1L, new UpdateCategoriaLicenciaRequest("B", null, null)));
    }

    @Test
    @DisplayName("eliminar() hace soft-delete (set deletedAt)")
    void eliminarSoftDelete() {
        CategoriaLicencia cat = CategoriaLicencia.builder().id(1L).codigo("B").build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(cat));

        service.eliminar(1L);

        assertNotNull(cat.getDeletedAt());
        verify(repository).save(cat);
    }
}
