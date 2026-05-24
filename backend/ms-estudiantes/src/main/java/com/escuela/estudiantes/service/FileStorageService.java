package com.escuela.estudiantes.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Almacenamiento de archivos en filesystem local. Estructura:
 * {@code <base>/<estudianteId>/<uuid>_<nombreOriginal>}.
 *
 * <p>Implementacion simple para desarrollo. En produccion sustituir por
 * MinIO/S3 manteniendo la misma firma publica.</p>
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${storage.documentos.path:./storage/documentos}")
    private String basePath;

    private Path baseDir;

    @PostConstruct
    void init() throws IOException {
        baseDir = Paths.get(basePath).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);
        log.info("FileStorageService inicializado. baseDir={}", baseDir);
    }

    /**
     * Guarda el archivo bajo {@code <baseDir>/<estudianteId>/<uuid>_<filename>}.
     *
     * @return ruta relativa al baseDir, lista para persistir en BD.
     */
    public String guardar(Long estudianteId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo es requerido y no puede estar vacio");
        }

        String original = sanitizar(archivo.getOriginalFilename());
        String nombreFinal = UUID.randomUUID() + "_" + original;

        try {
            Path dirEstudiante = baseDir.resolve(estudianteId.toString());
            Files.createDirectories(dirEstudiante);

            Path destino = dirEstudiante.resolve(nombreFinal).normalize();
            if (!destino.startsWith(dirEstudiante)) {
                throw new IllegalArgumentException("Ruta de archivo invalida (path traversal detectado)");
            }

            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado estudianteId={} path={}", estudianteId, destino);
            return estudianteId + "/" + nombreFinal;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el archivo: " + ex.getMessage(), ex);
        }
    }

    /**
     * Carga un archivo previamente guardado como {@link Resource}.
     */
    public Resource cargar(String rutaRelativa) {
        try {
            Path archivo = baseDir.resolve(rutaRelativa).normalize();
            if (!archivo.startsWith(baseDir)) {
                throw new IllegalArgumentException("Ruta de archivo invalida");
            }
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("El archivo no existe o no se puede leer: " + rutaRelativa);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL invalida para el archivo: " + rutaRelativa, ex);
        }
    }

    /**
     * Elimina el archivo del filesystem. No falla si ya no existe.
     */
    public void eliminar(String rutaRelativa) {
        try {
            Path archivo = baseDir.resolve(rutaRelativa).normalize();
            if (!archivo.startsWith(baseDir)) {
                log.warn("Intento de eliminar fuera de baseDir: {}", rutaRelativa);
                return;
            }
            boolean eliminado = Files.deleteIfExists(archivo);
            if (eliminado) {
                log.info("Archivo eliminado path={}", archivo);
            }
        } catch (IOException ex) {
            log.warn("No se pudo eliminar archivo {}: {}", rutaRelativa, ex.getMessage());
        }
    }

    private String sanitizar(String filename) {
        if (filename == null || filename.isBlank()) return "archivo";
        String base = Paths.get(filename).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
