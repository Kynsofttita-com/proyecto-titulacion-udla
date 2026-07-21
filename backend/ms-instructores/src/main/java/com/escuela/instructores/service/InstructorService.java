package com.escuela.instructores.service;

import com.escuela.common.events.instructores.InstructorCreadoEvent;
import com.escuela.common.events.instructores.InstructorEliminadoEvent;
import com.escuela.common.validation.core.CedulaEcuadorValidator;
import com.escuela.instructores.dto.CreateInstructorRequest;
import com.escuela.instructores.dto.InstructorListResponse;
import com.escuela.instructores.dto.InstructorResponse;
import com.escuela.instructores.dto.UpdateInstructorRequest;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.exception.DuplicateResourceException;
import com.escuela.instructores.exception.InstructorNotFoundException;
import com.escuela.instructores.mapper.InstructorMapper;
import com.escuela.instructores.mapper.InstructorMapperImpl;
import com.escuela.instructores.repository.InstructorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class InstructorService {

    private static final Logger log = LoggerFactory.getLogger(InstructorService.class);

    private final InstructorRepository repository;
    private final InstructorEventDispatcher eventDispatcher;
    private InstructorMapper mapper;

    public InstructorService(InstructorRepository repository,
                             InstructorEventDispatcher eventDispatcher) {
        this.repository = repository;
        this.eventDispatcher = eventDispatcher;
    }

    private InstructorMapper getMapper() {
        if (mapper == null) {
            this.mapper = new InstructorMapperImpl();
        }
        return mapper;
    }

    @Transactional(readOnly = true)
    public Page<InstructorListResponse> listar(Pageable pageable, String search, String estado) {
        return repository.buscar(search, estado, pageable).map(getMapper()::toListResponse);
    }

    @Transactional(readOnly = true)
    public InstructorResponse obtener(Long id) {
        return getMapper().toResponse(buscarOFallar(id));
    }

    /**
     * Devuelve el instructor cuyo usuario_id = usuarioId. Usado por el endpoint
     * GET /instructores/me para que un INSTRUCTOR logueado sepa cual de los
     * registros le pertenece (necesario para filtrar sus asignaciones y
     * estudiantes en el frontend).
     */
    @Transactional(readOnly = true)
    public InstructorResponse obtenerPorUsuarioId(Long usuarioId) {
        Instructor instructor = repository.findByUsuarioIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new InstructorNotFoundException(
                        "No existe instructor asociado al usuario " + usuarioId));
        return getMapper().toResponse(instructor);
    }

    public InstructorResponse crear(CreateInstructorRequest request) {
        if (!CedulaEcuadorValidator.isValid(request.cedula())) {
            throw new IllegalArgumentException("Cedula ecuatoriana invalida: " + request.cedula());
        }
        // Chequeamos contra la tabla completa (incluyendo soft-deleted): la UNIQUE
        // de la DB es global y sin este check el INSERT reventaria en 500 opaco.
        repository.findByCedula(request.cedula()).ifPresent(i -> {
            throw new DuplicateResourceException(mensajeDuplicado("cedula", request.cedula(), i.getDeletedAt()));
        });
        repository.findByEmail(request.email()).ifPresent(i -> {
            throw new DuplicateResourceException(mensajeDuplicado("email", request.email(), i.getDeletedAt()));
        });
        repository.findByLicenciaNumero(request.licenciaNumero()).ifPresent(i -> {
            throw new DuplicateResourceException(
                    mensajeDuplicado("licencia", request.licenciaNumero(), i.getDeletedAt()));
        });

        Instructor entity = getMapper().toEntity(request);
        entity.setEstado("ACTIVO");
        entity = repository.save(entity);
        log.info("Instructor creado id={} cedula={}", entity.getId(), entity.getCedula());

        // Publica evento; MS-Auth lo consume y crea usuario, devolviendo
        // UsuarioCreadoEvent que sera consumido por este MS para enlazar usuario_id.
        eventDispatcher.publishCreado(InstructorCreadoEvent.builder()
                .instructorId(entity.getId())
                .cedula(entity.getCedula())
                .email(entity.getEmail())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .build());

        return getMapper().toResponse(entity);
    }

    public InstructorResponse actualizar(Long id, UpdateInstructorRequest request) {
        Instructor i = buscarOFallar(id);

        if (request.email() != null && !request.email().equalsIgnoreCase(i.getEmail())) {
            repository.findByEmailAndDeletedAtIsNull(request.email()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Email ya existe: " + request.email());
                }
            });
            i.setEmail(request.email());
        }
        if (request.licenciaNumero() != null && !request.licenciaNumero().equals(i.getLicenciaNumero())) {
            // En Ecuador el numero de licencia debe coincidir con la cedula del instructor
            if (!request.licenciaNumero().equals(i.getCedula())) {
                throw new IllegalArgumentException(
                        "En Ecuador el numero de licencia debe coincidir con la cedula (" + i.getCedula() + ")");
            }
            repository.findByLicenciaNumeroAndDeletedAtIsNull(request.licenciaNumero()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Licencia ya existe: " + request.licenciaNumero());
                }
            });
            i.setLicenciaNumero(request.licenciaNumero());
        }

        if (request.nombre() != null) i.setNombre(request.nombre());
        if (request.apellido() != null) i.setApellido(request.apellido());
        if (request.telefono() != null && !request.telefono().isBlank()) i.setTelefono(request.telefono());
        if (request.direccion() != null) i.setDireccion(request.direccion());
        if (request.fechaNacimiento() != null) i.setFechaNacimiento(request.fechaNacimiento());
        if (request.licenciaCategoria() != null) i.setLicenciaCategoria(request.licenciaCategoria());
        if (request.licenciaEmision() != null) i.setLicenciaEmision(request.licenciaEmision());
        if (request.licenciaCaducidad() != null) i.setLicenciaCaducidad(request.licenciaCaducidad());
        if (request.estado() != null) i.setEstado(request.estado());
        if (request.fechaContratacion() != null) i.setFechaContratacion(request.fechaContratacion());
        if (request.salarioMensual() != null) i.setSalarioMensual(request.salarioMensual());
        if (request.tipoContrato() != null) i.setTipoContrato(request.tipoContrato());
        if (request.horasContratoSemanales() != null) i.setHorasContratoSemanales(request.horasContratoSemanales());
        if (request.tarifaHora() != null) i.setTarifaHora(request.tarifaHora());
        if (request.observaciones() != null) i.setObservaciones(request.observaciones());

        // Validacion de coherencia post-merge: POR_HORAS exige tarifaHora; TC/MT exigen salario
        if ("POR_HORAS".equals(i.getTipoContrato()) && i.getTarifaHora() == null) {
            throw new IllegalArgumentException(
                    "Tarifa por hora es obligatoria para contratos POR_HORAS");
        }
        if (("TIEMPO_COMPLETO".equals(i.getTipoContrato()) || "MEDIO_TIEMPO".equals(i.getTipoContrato()))
                && (i.getSalarioMensual() == null || i.getSalarioMensual().signum() <= 0)) {
            throw new IllegalArgumentException(
                    "Salario mensual es obligatorio para contratos TIEMPO_COMPLETO o MEDIO_TIEMPO");
        }

        repository.save(i);
        log.info("Instructor actualizado id={}", id);
        return getMapper().toResponse(i);
    }

    public void eliminar(Long id) {
        Instructor i = buscarOFallar(id);
        i.setEstado("INACTIVO");
        i.setDeletedAt(LocalDateTime.now());
        repository.save(i);
        log.info("Instructor soft-deleted id={}", id);

        eventDispatcher.publishEliminado(InstructorEliminadoEvent.builder()
                .instructorId(i.getId())
                .cedula(i.getCedula())
                .build());
    }

    /** Helper publico para los sub-services. */
    public Instructor buscarOFallar(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new InstructorNotFoundException(id));
    }

    private static String mensajeDuplicado(String campo, String valor, LocalDateTime deletedAt) {
        if (deletedAt == null) {
            return "Ya existe un instructor activo con " + campo + " " + valor;
        }
        return campo.substring(0, 1).toUpperCase() + campo.substring(1) + " " + valor
                + " pertenece a un instructor dado de baja el "
                + deletedAt.toLocalDate()
                + ". Contacte al administrador para liberarlo o reactivar el registro.";
    }
}
