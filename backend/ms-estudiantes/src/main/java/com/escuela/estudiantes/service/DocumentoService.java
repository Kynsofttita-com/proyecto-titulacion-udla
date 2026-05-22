package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.CreateDocumentoRequest;
import com.escuela.estudiantes.dto.DocumentoResponse;
import com.escuela.estudiantes.entity.Documento;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.exception.RecursoNotFoundException;
import com.escuela.estudiantes.repository.DocumentoRepository;
import com.escuela.estudiantes.repository.EstudianteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);

    private final DocumentoRepository documentoRepository;
    private final EstudianteRepository estudianteRepository;

    public DocumentoService(DocumentoRepository documentoRepository,
                            EstudianteRepository estudianteRepository) {
        this.documentoRepository = documentoRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarPorEstudiante(Long estudianteId) {
        verificarEstudianteExiste(estudianteId);
        return documentoRepository.findByEstudianteIdAndDeletedAtIsNullOrderByFechaSubidaDesc(estudianteId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponse obtener(Long estudianteId, Long documentoId) {
        Documento doc = documentoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(documentoId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("Documento", documentoId));
        return toResponse(doc);
    }

    public DocumentoResponse subir(Long estudianteId, CreateDocumentoRequest request) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        Documento doc = Documento.builder()
                .estudiante(estudiante)
                .tipo(request.tipo())
                .urlArchivo(request.urlArchivo())
                .mimeType(request.mimeType())
                .tamanoBytes(request.tamanoBytes())
                .fechaSubida(LocalDateTime.now())
                .build();

        doc = documentoRepository.save(doc);
        log.info("Documento subido id={} estudianteId={} tipo={}", doc.getId(), estudianteId, doc.getTipo());
        return toResponse(doc);
    }

    public void eliminar(Long estudianteId, Long documentoId) {
        Documento doc = documentoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(documentoId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("Documento", documentoId));
        doc.setDeletedAt(LocalDateTime.now());
        documentoRepository.save(doc);
        log.info("Documento soft-deleted id={} estudianteId={}", documentoId, estudianteId);
    }

    private void verificarEstudianteExiste(Long estudianteId) {
        if (!estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId).isPresent()) {
            throw new EstudianteNotFoundException(estudianteId);
        }
    }

    private DocumentoResponse toResponse(Documento d) {
        return new DocumentoResponse(d.getId(), d.getTipo(), d.getUrlArchivo(),
                d.getFechaSubida(), d.getMimeType(), d.getTamanoBytes());
    }
}
