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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);

    private final DocumentoRepository documentoRepository;
    private final EstudianteRepository estudianteRepository;
    private final FileStorageService fileStorage;

    public DocumentoService(DocumentoRepository documentoRepository,
                            EstudianteRepository estudianteRepository,
                            FileStorageService fileStorage) {
        this.documentoRepository = documentoRepository;
        this.estudianteRepository = estudianteRepository;
        this.fileStorage = fileStorage;
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

    /**
     * Recibe un archivo multipart, lo persiste en el filesystem y crea el
     * registro {@link Documento} con la ruta local como {@code urlArchivo}.
     */
    public DocumentoResponse subirArchivo(Long estudianteId, String tipo, MultipartFile archivo) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es requerido");
        }

        String rutaRelativa = fileStorage.guardar(estudianteId, archivo);

        Documento doc = Documento.builder()
                .estudiante(estudiante)
                .tipo(tipo.trim())
                .urlArchivo(rutaRelativa)
                .mimeType(archivo.getContentType())
                .tamanoBytes(archivo.getSize())
                .fechaSubida(LocalDateTime.now())
                .build();

        doc = documentoRepository.save(doc);
        log.info("Documento subido (upload) id={} estudianteId={} tipo={} bytes={}",
                doc.getId(), estudianteId, doc.getTipo(), doc.getTamanoBytes());
        return toResponse(doc);
    }

    /**
     * Devuelve el archivo binario asociado al documento + metadata para los
     * headers HTTP (Content-Type, filename).
     */
    @Transactional(readOnly = true)
    public ArchivoDescarga descargar(Long estudianteId, Long documentoId) {
        Documento doc = documentoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(documentoId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("Documento", documentoId));
        Resource resource = fileStorage.cargar(doc.getUrlArchivo());
        String nombre = extraerNombreOriginal(doc.getUrlArchivo());
        return new ArchivoDescarga(resource, doc.getMimeType(), nombre);
    }

    private String extraerNombreOriginal(String rutaRelativa) {
        if (rutaRelativa == null) return "archivo";
        String filename = rutaRelativa.contains("/")
                ? rutaRelativa.substring(rutaRelativa.lastIndexOf('/') + 1)
                : rutaRelativa;
        int underscore = filename.indexOf('_');
        return underscore >= 0 && underscore < filename.length() - 1
                ? filename.substring(underscore + 1)
                : filename;
    }

    /**
     * Tupla de respuesta para descargas: contenido + metadata HTTP.
     */
    public record ArchivoDescarga(Resource resource, String mimeType, String nombreOriginal) {}

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
