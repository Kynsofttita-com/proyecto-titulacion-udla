package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.CreateDocumentoRequest;
import com.escuela.estudiantes.dto.DocumentoResponse;
import com.escuela.estudiantes.entity.Documento;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.exception.RecursoNotFoundException;
import com.escuela.estudiantes.repository.DocumentoRepository;
import com.escuela.estudiantes.repository.EstudianteRepository;
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
class DocumentoServiceTest {

    @Mock private DocumentoRepository documentoRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private FileStorageService fileStorage;

    @InjectMocks
    private DocumentoService service;

    @Test
    @DisplayName("listarPorEstudiante() valida que el estudiante exista")
    void listarFalla() {
        when(estudianteRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(EstudianteNotFoundException.class, () -> service.listarPorEstudiante(99L));
    }

    @Test
    @DisplayName("listarPorEstudiante() devuelve los documentos del estudiante")
    void listarOk() {
        Estudiante e = Estudiante.builder().id(1L).build();
        Documento doc = Documento.builder().id(10L).estudiante(e).tipo("CEDULA")
                .urlArchivo("minio://buckets/cedula.pdf").build();
        when(estudianteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(e));
        when(documentoRepository.findByEstudianteIdAndDeletedAtIsNullOrderByFechaSubidaDesc(1L))
                .thenReturn(List.of(doc));

        List<DocumentoResponse> r = service.listarPorEstudiante(1L);
        assertEquals(1, r.size());
        assertEquals("CEDULA", r.get(0).tipo());
    }

    @Test
    @DisplayName("subir() crea Documento asociado al estudiante")
    void subirOk() {
        Estudiante e = Estudiante.builder().id(1L).build();
        when(estudianteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(e));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(50L);
            return d;
        });

        DocumentoResponse r = service.subir(1L,
                new CreateDocumentoRequest("CEDULA", "minio://buckets/c.pdf", "application/pdf", 12345L));

        assertEquals(50L, r.id());
        assertEquals("CEDULA", r.tipo());
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    @DisplayName("eliminar() falla con RecursoNotFoundException si el doc no es del estudiante")
    void eliminarNotFound() {
        when(documentoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(99L, 1L))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNotFoundException.class, () -> service.eliminar(1L, 99L));
    }

    @Test
    @DisplayName("eliminar() hace soft delete")
    void eliminarOk() {
        Documento d = Documento.builder().id(10L).build();
        when(documentoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(d));

        service.eliminar(1L, 10L);

        assertNotNull(d.getDeletedAt());
        verify(documentoRepository).save(d);
    }
}
