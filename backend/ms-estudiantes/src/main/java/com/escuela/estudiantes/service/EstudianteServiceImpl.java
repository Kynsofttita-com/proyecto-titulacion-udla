package com.escuela.estudiantes.service;

import com.escuela.common.validation.core.CedulaEcuadorValidator;
import com.escuela.common.events.estudiantes.EstudianteActualizadoEvent;
import com.escuela.common.events.estudiantes.EstudianteCreadoEvent;
import com.escuela.common.events.estudiantes.EstudianteEliminadoEvent;
import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteListResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import com.escuela.estudiantes.entity.ContactoEmergencia;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.CedulaDuplicadaException;
import com.escuela.estudiantes.feign.AuthClient;
import com.escuela.estudiantes.exception.CedulaInvalidaException;
import com.escuela.estudiantes.exception.EmailDuplicadoException;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.exception.TransicionEstadoInvalidaException;
import com.escuela.estudiantes.mapper.EstudianteMapper;
import com.escuela.estudiantes.mapper.EstudianteMapperImpl;
import com.escuela.estudiantes.repository.EstudianteRepository;
import com.escuela.estudiantes.specification.EstudianteSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implementacion del servicio de Estudiantes.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Validacion de cedula ecuatoriana (digito verificador via
 *       {@link CedulaEcuadorValidator}).</li>
 *   <li>Validacion de unicidad de cedula y email (entre los no eliminados).</li>
 *   <li>Mapeo Entity &lt;-&gt; DTO via {@link EstudianteMapper}.</li>
 *   <li>Soft delete (no borra fisicamente).</li>
 *   <li>Publicacion de eventos a RabbitMQ via {@link EstudianteEventDispatcher}.</li>
 * </ul>
 *
 * <p>Eventos publicados:</p>
 * <ul>
 *   <li>{@code estudiantes.creado} - tras crear un estudiante</li>
 *   <li>{@code estudiantes.actualizado} - tras actualizar</li>
 *   <li>{@code estudiantes.eliminado} - tras soft-delete</li>
 * </ul>
 *
 * <p>El evento {@code estudiantes.matriculado} se publica desde
 * {@link EstudianteEstadoService} cuando la auto-transicion
 * PRE_MATRICULADO &rarr; MATRICULADO ocurre por evento de pago o factura.</p>
 */
@Service
public class EstudianteServiceImpl implements EstudianteService {

    private static final Logger log = LoggerFactory.getLogger(EstudianteServiceImpl.class);

    private final EstudianteRepository repository;
    private final EstudianteEventDispatcher eventDispatcher;
    private final ObjectProvider<AuthClient> authClientProvider;
    private final ObjectProvider<com.escuela.estudiantes.feign.TipoCursoClient> tipoCursoClientProvider;
    private EstudianteMapper mapper;

    public EstudianteServiceImpl(EstudianteRepository repository,
                                 EstudianteEventDispatcher eventDispatcher,
                                 ObjectProvider<AuthClient> authClientProvider,
                                 ObjectProvider<com.escuela.estudiantes.feign.TipoCursoClient> tipoCursoClientProvider) {
        this.repository = repository;
        this.eventDispatcher = eventDispatcher;
        this.authClientProvider = authClientProvider;
        this.tipoCursoClientProvider = tipoCursoClientProvider;
    }

    private EstudianteMapper getMapper() {
        if (mapper == null) {
            this.mapper = new EstudianteMapperImpl();
        }
        return mapper;
    }

    @Override
    @Transactional
    public EstudianteResponse create(CreateEstudianteRequest request) {
        validarCedula(request.cedula());

        // Chequeamos contra la tabla completa (incluye soft-deleted) porque el
        // UNIQUE de la DB es global. Politica de reactivacion:
        //   1. Cedula matchea activo         -> 409 (conflicto real)
        //   2. Email matchea activo (distinto reg.) -> 409
        //   3. Cedula matchea soft-deleted   -> REACTIVAR ese registro
        //   4. Solo email matchea soft-del.  -> REACTIVAR ese registro
        //   5. Nada matchea                  -> crear nuevo
        Optional<Estudiante> porCedula = repository.findByCedula(request.cedula());
        Optional<Estudiante> porEmail  = repository.findByEmail(request.email());

        if (porCedula.isPresent() && porCedula.get().getDeletedAt() == null) {
            throw new CedulaDuplicadaException(request.cedula(), null);
        }
        if (porEmail.isPresent() && porEmail.get().getDeletedAt() == null
                && (porCedula.isEmpty() || !porCedula.get().getId().equals(porEmail.get().getId()))) {
            throw new EmailDuplicadoException(request.email(), null);
        }

        if (porCedula.isPresent()) {
            // Ambiguo: cedula X (soft-del reg A) + email Y (soft-del reg B distinto).
            // Reactivar A pisando su email por Y colisionaria con la UNIQUE global.
            // Se lo rechaza con mensaje claro para que el admin resuelva a mano.
            if (porEmail.isPresent() && !porEmail.get().getId().equals(porCedula.get().getId())) {
                throw new EmailDuplicadoException(request.email(), porEmail.get().getDeletedAt());
            }
            return reactivar(porCedula.get(), request);
        }
        if (porEmail.isPresent()) {
            return reactivar(porEmail.get(), request);
        }

        Estudiante estudiante = getMapper().toEntity(request);
        // Re-asignar el padre en los contactos hijos (MapStruct no lo hace solo
        // en relaciones bidireccionales)
        if (estudiante.getContactosEmergencia() != null) {
            for (ContactoEmergencia ce : estudiante.getContactosEmergencia()) {
                ce.setEstudiante(estudiante);
            }
        }

        Estudiante guardado = repository.save(estudiante);
        log.info("Estudiante creado id={} cedula={}", guardado.getId(), guardado.getCedula());

        // Auto-crear cuenta de Usuario en MS-Auth (rol ESTUDIANTE) para que
        // pueda loguearse. El enlace usuario_id se completa via el evento
        // auth.usuario.creado que escucha EstudianteAuthEventListener.
        crearCuentaUsuarioAsociada(guardado);

        eventDispatcher.publishCreado(EstudianteCreadoEvent.builder()
                .estudianteId(guardado.getId())
                .cedula(guardado.getCedula())
                .email(guardado.getEmail())
                .nombreCompleto(nombreCompleto(guardado))
                .estado(guardado.getEstado())
                .build());

        return getMapper().toResponse(guardado);
    }

    /**
     * Reactiva un estudiante soft-deleted con los datos del request nuevo.
     * En Ecuador la cedula = persona, asi que reactivar es semanticamente correcto:
     * es la misma persona volviendo. Actualizamos todos los campos y limpiamos
     * deleted_at. Tambien disparamos el evento y la creacion de cuenta de usuario:
     * el ms-auth AutoUsuarioCreator reactivara el usuario soft-deleted correspondiente.
     */
    private EstudianteResponse reactivar(Estudiante viejo, CreateEstudianteRequest request) {
        viejo.setDeletedAt(null);
        viejo.setEstado("PRE_MATRICULADO");
        viejo.setCedula(request.cedula());
        viejo.setNombre(request.nombre());
        viejo.setApellido(request.apellido());
        viejo.setEmail(request.email());
        viejo.setTelefono(request.telefono());
        viejo.setDireccion(request.direccion());
        viejo.setFechaNacimiento(request.fechaNacimiento());
        viejo.setGenero(request.genero());
        viejo.setTipoCursoId(request.tipoCursoId());
        viejo.setCategoriaLicenciaId(request.categoriaLicenciaId());
        viejo.setObservaciones(request.observaciones());
        viejo.setMinutosCompletados(0);
        viejo.setFechaMatricula(null);

        // Contactos de emergencia: reemplazar los que hubiera (los viejos van a
        // orphanRemoval) por los del request nuevo.
        if (viejo.getContactosEmergencia() != null) {
            viejo.getContactosEmergencia().clear();
        }
        if (request.contactosEmergencia() != null) {
            for (var ceReq : request.contactosEmergencia()) {
                ContactoEmergencia ce = new ContactoEmergencia();
                ce.setNombre(ceReq.nombre());
                ce.setTelefono(ceReq.telefono());
                ce.setParentesco(ceReq.parentesco());
                ce.setEsPrincipal(ceReq.esPrincipal() != null && ceReq.esPrincipal());
                ce.setEstudiante(viejo);
                viejo.getContactosEmergencia().add(ce);
            }
        }

        Estudiante reactivado = repository.save(viejo);
        log.info("Estudiante REACTIVADO id={} cedula={} email={}",
                reactivado.getId(), reactivado.getCedula(), reactivado.getEmail());

        crearCuentaUsuarioAsociada(reactivado);
        eventDispatcher.publishCreado(EstudianteCreadoEvent.builder()
                .estudianteId(reactivado.getId())
                .cedula(reactivado.getCedula())
                .email(reactivado.getEmail())
                .nombreCompleto(nombreCompleto(reactivado))
                .estado(reactivado.getEstado())
                .build());
        return getMapper().toResponse(reactivado);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDetailResponse findById(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        return getMapper().toDetailResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDetailResponse findByUsuarioId(Long usuarioId) {
        Estudiante estudiante = repository.findByUsuarioIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new EstudianteNotFoundException(
                        "No existe estudiante asociado al usuario " + usuarioId));
        return getMapper().toDetailResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstudianteListResponse> findAll(Pageable pageable, String search, String estado) {
        Specification<Estudiante> spec = Specification.where(EstudianteSpecifications.notDeleted())
                .and(EstudianteSpecifications.estado(estado))
                .and(EstudianteSpecifications.searchTerm(search));
        return repository.findAll(spec, pageable).map(e -> toListResponseWithHoras(e));
    }

    private EstudianteListResponse toListResponseWithHoras(Estudiante entity) {
        String nombreCompleto = entity.getNombre() + " " + entity.getApellido();

        // Convertir minutos a horas (redondeado hacia arriba)
        Integer horasCompletadas = entity.getMinutosCompletados() != null
            ? (int) Math.ceil(entity.getMinutosCompletados() / 60.0)
            : 0;

        // Horas requeridas: consulta al catalogo tipos-curso via MS-Auth.
        // Fallback a 0 si el estudiante no tiene tipoCursoId asignado o si
        // MS-Auth esta caido.
        Integer horasRequeridas = obtenerHorasRequeridasSafe(entity.getTipoCursoId());
        if (horasRequeridas == null) horasRequeridas = 0;

        return new EstudianteListResponse(
                entity.getId(),
                entity.getCedula(),
                nombreCompleto,
                entity.getEmail(),
                entity.getTelefono(),
                entity.getEstado(),
                entity.getSituacionPago(),
                entity.getFechaMatricula(),
                horasCompletadas,
                horasRequeridas
        );
    }

    @Override
    @Transactional
    public EstudianteResponse update(Long id, UpdateEstudianteRequest request) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));

        // Si cambian el email, validar unicidad (excepto si es el mismo email)
        if (request.email() != null && !request.email().equalsIgnoreCase(estudiante.getEmail())
                && repository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        getMapper().updateEntity(estudiante, request);
        Estudiante actualizado = repository.save(estudiante);
        log.info("Estudiante actualizado id={}", actualizado.getId());

        eventDispatcher.publishActualizado(EstudianteActualizadoEvent.builder()
                .estudianteId(actualizado.getId())
                .cedula(actualizado.getCedula())
                .email(actualizado.getEmail())
                .nombreCompleto(nombreCompleto(actualizado))
                .estado(actualizado.getEstado())
                .build());

        // Nota: el evento estudiantes.matriculado se publica desde
        // EstudianteEstadoService cuando la transicion PRE_MATRICULADO ->
        // MATRICULADO ocurre por evento (pago saldado o factura CREDITO
        // emitida). El cambio manual de estado via PATCH /estado tampoco
        // publica matriculado, ya que ese flujo es para correcciones
        // administrativas, no para la matricula real.

        return getMapper().toResponse(actualizado);
    }

    /**
     * Matriz de transiciones manuales permitidas. Las transiciones
     * {@code PRE_MATRICULADO -> MATRICULADO} y {@code MATRICULADO -> CURSANDO}
     * NO estan aqui porque solo deben ocurrir via eventos
     * (pago.registrado / asignacion.creada) para mantener consistencia con
     * MS-Cobros y MS-Asignaciones.
     */
    private static final Map<String, Set<String>> TRANSICIONES_PERMITIDAS = Map.of(
            "MATRICULADO", Set.of("COMPLETADO", "RETIRADO"),
            "CURSANDO",    Set.of("COMPLETADO", "RETIRADO"),
            "COMPLETADO",  Set.of("CURSANDO"),
            "RETIRADO",    Set.of("MATRICULADO", "CURSANDO")
    );

    private static final DateTimeFormatter OBS_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public EstudianteResponse cambiarEstado(Long id, String nuevoEstado, String motivo) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));

        String estadoActual = estudiante.getEstado();
        if (Objects.equals(estadoActual, nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    "El estudiante ya esta en estado " + nuevoEstado);
        }

        Set<String> permitidos = TRANSICIONES_PERMITIDAS.get(estadoActual);
        if (permitidos == null || !permitidos.contains(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(estadoActual, nuevoEstado);
        }

        if ("RETIRADO".equals(nuevoEstado) && (motivo == null || motivo.isBlank())) {
            throw new TransicionEstadoInvalidaException(
                    "El motivo es obligatorio al marcar el estudiante como RETIRADO");
        }

        estudiante.setEstado(nuevoEstado);

        if (motivo != null && !motivo.isBlank()) {
            String entrada = "[" + LocalDateTime.now().format(OBS_TIMESTAMP) + "] "
                    + estadoActual + " -> " + nuevoEstado + ": " + motivo.trim();
            String obsPrevia = estudiante.getObservaciones();
            estudiante.setObservaciones(
                    obsPrevia == null || obsPrevia.isBlank() ? entrada : obsPrevia + "\n" + entrada);
        }

        Estudiante actualizado = repository.save(estudiante);
        log.info("Estudiante id={} cambio de estado {} -> {} (motivo presente: {})",
                id, estadoActual, nuevoEstado, motivo != null && !motivo.isBlank());

        eventDispatcher.publishActualizado(EstudianteActualizadoEvent.builder()
                .estudianteId(actualizado.getId())
                .cedula(actualizado.getCedula())
                .email(actualizado.getEmail())
                .nombreCompleto(nombreCompleto(actualizado))
                .estado(actualizado.getEstado())
                .build());

        return getMapper().toResponse(actualizado);
    }

    @Override
    @Transactional
    public com.escuela.estudiantes.dto.IncrementarHorasResponse incrementarMinutosCompletados(
            Long id, com.escuela.estudiantes.dto.IncrementarHorasRequest request) {
        Estudiante e = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));

        Integer anterior = e.getMinutosCompletados() == null ? 0 : e.getMinutosCompletados();
        Integer nuevo = anterior + request.minutos();
        e.setMinutosCompletados(nuevo);

        // Si esta MATRICULADO y aun no esta cursando, lo movemos a CURSANDO
        boolean transicionado = false;
        String estadoAnterior = e.getEstado();
        if ("MATRICULADO".equals(estadoAnterior)) {
            e.setEstado("CURSANDO");
            transicionado = true;
            log.info("Estudiante id={} transicionado MATRICULADO -> CURSANDO al sumar horas", id);
        }

        // Consulta tipo_curso para saber si ya completo el 100% de horas requeridas.
        // Si es asi, transiciona a COMPLETADO y publica CursoCompletadoEvent.
        Integer horasRequeridas = obtenerHorasRequeridasSafe(e.getTipoCursoId());
        boolean cursoCompletadoAhora = false;
        if (horasRequeridas != null && horasRequeridas > 0
                && !"COMPLETADO".equals(e.getEstado())
                && nuevo >= horasRequeridas * 60) {
            e.setEstado("COMPLETADO");
            cursoCompletadoAhora = true;
            log.info("Estudiante id={} transicionado {} -> COMPLETADO al alcanzar {} min (={}h de {}h)",
                    id, estadoAnterior, nuevo, nuevo / 60, horasRequeridas);
        }

        repository.save(e);

        if (cursoCompletadoAhora) {
            eventDispatcher.publishCursoCompletado(
                    com.escuela.common.events.estudiantes.CursoCompletadoEvent.builder()
                            .estudianteId(e.getId())
                            .usuarioIdEstudiante(e.getUsuarioId())
                            .cedula(e.getCedula())
                            .nombreCompleto(nombreCompleto(e))
                            .email(e.getEmail())
                            .tipoCursoNombre(obtenerNombreCursoSafe(e.getTipoCursoId()))
                            .horasCompletadas(nuevo / 60)
                            .horasRequeridas(horasRequeridas)
                            .fechaCompletado(java.time.LocalDate.now())
                            .build());
        }

        log.info("Estudiante id={} minutos {} -> {} fuente={}", id, anterior, nuevo, request.fuente());
        String obs = transicionado
                ? "Sumados " + request.minutos() + " min. Estado: " + estadoAnterior + " -> " + e.getEstado()
                : "Sumados " + request.minutos() + " min (acumulado: " + nuevo + " min = " + (nuevo / 60) + "h " + (nuevo % 60) + "m)";

        return new com.escuela.estudiantes.dto.IncrementarHorasResponse(
                id, anterior, nuevo, e.getEstado(), transicionado, obs);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        estudiante.markAsDeleted();
        repository.save(estudiante);
        log.info("Estudiante soft-deleted id={}", id);

        eventDispatcher.publishEliminado(EstudianteEliminadoEvent.builder()
                .estudianteId(estudiante.getId())
                .cedula(estudiante.getCedula())
                .build());
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void validarCedula(String cedula) {
        if (!CedulaEcuadorValidator.isValid(cedula)) {
            throw new CedulaInvalidaException(cedula);
        }
    }

    private String nombreCompleto(Estudiante e) {
        return e.getNombre() + " " + e.getApellido();
    }

    /**
     * Consulta MS-Auth por el tipo de curso y devuelve {@code duracionTotalHoras}.
     * Devuelve {@code null} si el estudiante no tiene tipo_curso_id asignado, si
     * MS-Auth esta caido, o si la respuesta no incluye el campo.
     */
    private Integer obtenerHorasRequeridasSafe(Long tipoCursoId) {
        if (tipoCursoId == null) return null;
        com.escuela.estudiantes.feign.TipoCursoClient client = tipoCursoClientProvider.getIfAvailable();
        if (client == null) {
            log.debug("TipoCursoClient no disponible; no se puede calcular horas requeridas");
            return null;
        }
        try {
            java.util.Map<String, Object> tc = client.obtener(tipoCursoId);
            Object horas = tc == null ? null : tc.get("duracionTotalHoras");
            if (horas instanceof Number n) return n.intValue();
            if (horas != null) return Integer.parseInt(horas.toString());
            return null;
        } catch (Exception ex) {
            log.warn("Fallo consultar tipoCurso id={} para horas requeridas: {}", tipoCursoId, ex.getMessage());
            return null;
        }
    }

    private String obtenerNombreCursoSafe(Long tipoCursoId) {
        if (tipoCursoId == null) return null;
        com.escuela.estudiantes.feign.TipoCursoClient client = tipoCursoClientProvider.getIfAvailable();
        if (client == null) return null;
        try {
            java.util.Map<String, Object> tc = client.obtener(tipoCursoId);
            Object nombre = tc == null ? null : tc.get("nombre");
            return nombre == null ? null : nombre.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Crea (best-effort) una cuenta de Usuario asociada al estudiante en
     * MS-Auth. Si MS-Auth esta caido o el email ya existe alli, se logguea y
     * se sigue: el estudiante ya quedo persistido y el enlace
     * {@code usuario_id} puede completarse despues por evento RabbitMQ.
     */
    private void crearCuentaUsuarioAsociada(Estudiante e) {
        AuthClient client = authClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("AuthClient no disponible (perfil test o ms-auth caido); " +
                    "estudiante id={} sin cuenta de Usuario auto-creada", e.getId());
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", e.getEmail());
        payload.put("password", PasswordGenerator.generarTemporal());
        payload.put("nombre", e.getNombre());
        payload.put("apellido", e.getApellido());
        payload.put("telefono", e.getTelefono());
        payload.put("roles", java.util.List.of("ESTUDIANTE"));
        payload.put("cedula", e.getCedula());
        payload.put("fechaNacimiento", e.getFechaNacimiento() != null
                ? e.getFechaNacimiento().toString() : null);
        payload.put("genero", e.getGenero());
        payload.put("direccion", e.getDireccion());
        payload.put("passwordChangeRequired", Boolean.TRUE);

        try {
            client.crearUsuario(payload);
            log.info("Usuario auto-creado en MS-Auth para estudiante id={} email={} (password temporal generada, cambio obligatorio en primer login)",
                    e.getId(), e.getEmail());
        } catch (Exception ex) {
            log.warn("No se pudo auto-crear Usuario en MS-Auth para estudiante id={} email={}: {}. " +
                    "El estudiante queda sin cuenta de acceso; puede crearse manualmente en /usuarios.",
                    e.getId(), e.getEmail(), ex.getMessage());
        }
    }
}
