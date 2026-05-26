package com.escuela.asignaciones.config;

import com.escuela.asignaciones.controller.AsignacionController;
import com.escuela.asignaciones.exception.AsignacionNotFoundException;
import com.escuela.asignaciones.exception.DisponibilidadException;
import com.escuela.asignaciones.exception.EstudianteInactivoException;
import com.escuela.asignaciones.exception.EstudianteNoEncontradoException;
import com.escuela.asignaciones.exception.InstructorInactivoException;
import com.escuela.asignaciones.exception.InstructorNoEncontradoException;
import com.escuela.asignaciones.exception.VehiculoEliminadoException;
import com.escuela.asignaciones.exception.VehiculoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EstudianteNoEncontradoException.class, InstructorNoEncontradoException.class, VehiculoNoEncontradoException.class})
    public ResponseEntity<ProblemDetail> handleEntidadNoEncontrada(RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Entidad referenciada no encontrada");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler({EstudianteInactivoException.class, InstructorInactivoException.class, VehiculoEliminadoException.class})
    public ResponseEntity<ProblemDetail> handleEntidadInactiva(RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Entidad referenciada inactiva");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(AsignacionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AsignacionNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Asignación no encontrada");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DisponibilidadException.class)
    public ResponseEntity<ProblemDetail> handleDisponibilidad(DisponibilidadException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("No disponible");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(AsignacionController.NoAutenticadoException.class)
    public ResponseEntity<ProblemDetail> handleNoAutenticado(AsignacionController.NoAutenticadoException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("No autenticado");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AsignacionController.SinPermisoException.class)
    public ResponseEntity<ProblemDetail> handleSinPermiso(AsignacionController.SinPermisoException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Acceso denegado");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validacion fallida");
        problem.setDetail("Verifique los campos requeridos");
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ProblemDetail> handleNoHandler(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Ruta no encontrada");
        problem.setDetail(e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception e) {
        log.error("Unhandled exception in MS-Asignaciones: {} - {}", e.getClass().getName(), e.getMessage(), e);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error interno");
        problem.setDetail("Ocurrió un error inesperado: " + e.getMessage());
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
