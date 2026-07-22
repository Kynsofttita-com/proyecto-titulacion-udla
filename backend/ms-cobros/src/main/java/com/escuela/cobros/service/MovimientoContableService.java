package com.escuela.cobros.service;

import com.escuela.cobros.client.AuthClient;
import com.escuela.cobros.dto.AnularMovimientoRequest;
import com.escuela.cobros.dto.ConfiguracionEscuelaDTO;
import com.escuela.cobros.dto.MovimientoContableRequest;
import com.escuela.cobros.dto.MovimientoContableResponse;
import com.escuela.cobros.dto.MovimientoVehiculoRequest;
import com.escuela.cobros.entity.CategoriaMovimiento;
import com.escuela.cobros.entity.CuentaContable;
import com.escuela.cobros.entity.MovimientoContable;
import com.escuela.cobros.entity.Pago;
import com.escuela.cobros.repository.CategoriaMovimientoRepository;
import com.escuela.cobros.repository.CuentaContableRepository;
import com.escuela.cobros.repository.MovimientoContableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@Transactional
public class MovimientoContableService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoContableService.class);

    private static final String CATEGORIA_COBRO_ESTUDIANTE = "COBRO_ESTUDIANTE";
    private static final String CATEGORIA_COMBUSTIBLE = "COMBUSTIBLE";
    private static final String CATEGORIA_MANTENIMIENTO = "MANTENIMIENTO_VEHICULO";

    private final MovimientoContableRepository movimientosRepo;
    private final CuentaContableRepository cuentasRepo;
    private final CategoriaMovimientoRepository categoriasRepo;
    private final AuthClient authClient;

    public MovimientoContableService(MovimientoContableRepository movimientosRepo,
                                     CuentaContableRepository cuentasRepo,
                                     CategoriaMovimientoRepository categoriasRepo,
                                     AuthClient authClient) {
        this.movimientosRepo = movimientosRepo;
        this.cuentasRepo = cuentasRepo;
        this.categoriasRepo = categoriasRepo;
        this.authClient = authClient;
    }

    @Transactional(readOnly = true)
    public Page<MovimientoContableResponse> buscar(
            Long cuentaId, Long categoriaId, String tipo, Long vehiculoId, Long pagadoAId,
            LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        return movimientosRepo.buscarConFiltros(cuentaId, categoriaId, tipo, vehiculoId, pagadoAId, fechaInicio, fechaFin, pageable)
                .map(this::toResponse);
    }

    /** Resumen de gastos de un vehiculo (combustible + mantenimiento). */
    @Transactional(readOnly = true)
    public java.util.Map<String, java.math.BigDecimal> resumenGastosVehiculo(Long vehiculoId) {
        java.math.BigDecimal combustible = movimientosRepo
                .sumaGastosPorVehiculoYCategoria(vehiculoId, CATEGORIA_COMBUSTIBLE);
        java.math.BigDecimal mantenimiento = movimientosRepo
                .sumaGastosPorVehiculoYCategoria(vehiculoId, CATEGORIA_MANTENIMIENTO);
        java.util.Map<String, java.math.BigDecimal> r = new java.util.LinkedHashMap<>();
        r.put("combustible", combustible);
        r.put("mantenimiento", mantenimiento);
        r.put("total", combustible.add(mantenimiento));
        return r;
    }

    @Transactional(readOnly = true)
    public MovimientoContableResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    public MovimientoContableResponse crear(MovimientoContableRequest request) {
        CuentaContable cuenta = cuentasRepo.findById(request.cuentaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + request.cuentaId()));
        if (Boolean.FALSE.equals(cuenta.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta '" + cuenta.getNombre() + "' esta desactivada");
        }
        CategoriaMovimiento categoria = categoriasRepo.findById(request.categoriaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada: " + request.categoriaId()));
        if (!categoria.getTipo().equals(request.tipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoria '" + categoria.getNombre() + "' es de tipo "
                            + categoria.getTipo() + ", no " + request.tipo());
        }
        if (Boolean.FALSE.equals(categoria.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoria '" + categoria.getNombre() + "' esta desactivada");
        }
        if (CATEGORIA_COBRO_ESTUDIANTE.equals(categoria.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoria 'Cobro de estudiante' es exclusiva del flujo automatico de pagos. "
                            + "Registra el pago desde Cobros.");
        }

        MovimientoContable m = MovimientoContable.builder()
                .fecha(request.fecha())
                .tipo(request.tipo())
                .monto(request.monto())
                .cuenta(cuenta)
                .categoria(categoria)
                .descripcion(trimOrNull(request.descripcion()))
                .referencia(trimOrNull(request.referencia()))
                .vehiculoId(request.vehiculoId())
                .placaVehiculo(trimOrNull(request.placaVehiculo()))
                .kilometraje(request.kilometraje())
                .pagadoAId(request.pagadoAId())
                .nombrePagadoA(trimOrNull(request.nombrePagadoA()))
                .anulado(false)
                .build();
        m = movimientosRepo.save(m);
        log.info("Movimiento creado: id={} tipo={} monto={} cuenta={} categoria={} vehiculo={} pagadoA={}",
                m.getId(), m.getTipo(), m.getMonto(), cuenta.getNombre(), categoria.getCodigo(),
                m.getPlacaVehiculo() != null ? m.getPlacaVehiculo() : "—",
                m.getNombrePagadoA() != null ? m.getNombrePagadoA() : "—");
        return toResponse(m);
    }

    public MovimientoContableResponse actualizar(Long id, MovimientoContableRequest request) {
        MovimientoContable m = findOrThrow(id);
        if (Boolean.TRUE.equals(m.getAnulado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede editar un movimiento anulado");
        }
        if (m.getPagoId() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede editar un movimiento generado automaticamente por un pago. "
                            + "Anula y recrea si es necesario.");
        }
        if (m.getRegistroCombustibleId() != null || m.getMantenimientoId() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede editar un movimiento generado desde Vehiculos. "
                            + "Modifica el registro origen desde el modulo Vehiculos.");
        }
        CuentaContable cuenta = cuentasRepo.findById(request.cuentaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + request.cuentaId()));
        CategoriaMovimiento categoria = categoriasRepo.findById(request.categoriaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada: " + request.categoriaId()));
        if (!categoria.getTipo().equals(request.tipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoria es de tipo " + categoria.getTipo() + ", no " + request.tipo());
        }
        if (CATEGORIA_COBRO_ESTUDIANTE.equals(categoria.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede asignar la categoria 'Cobro de estudiante' manualmente.");
        }
        m.setFecha(request.fecha());
        m.setTipo(request.tipo());
        m.setMonto(request.monto());
        m.setCuenta(cuenta);
        m.setCategoria(categoria);
        m.setDescripcion(trimOrNull(request.descripcion()));
        m.setReferencia(trimOrNull(request.referencia()));
        m.setVehiculoId(request.vehiculoId());
        m.setPlacaVehiculo(trimOrNull(request.placaVehiculo()));
        m.setKilometraje(request.kilometraje());
        m.setPagadoAId(request.pagadoAId());
        m.setNombrePagadoA(trimOrNull(request.nombrePagadoA()));
        m = movimientosRepo.save(m);
        return toResponse(m);
    }

    public void anular(Long id, AnularMovimientoRequest request) {
        MovimientoContable m = findOrThrow(id);
        if (Boolean.TRUE.equals(m.getAnulado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El movimiento ya esta anulado");
        }
        if (m.getPagoId() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede anular un movimiento generado por un pago. "
                            + "Anula el pago desde Cobros.");
        }
        if (m.getRegistroCombustibleId() != null || m.getMantenimientoId() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede anular un movimiento generado desde Vehiculos. "
                            + "Elimina el registro origen desde el modulo Vehiculos.");
        }
        m.setAnulado(true);
        m.setMotivoAnulacion(request.motivo());
        movimientosRepo.save(m);
        log.info("Movimiento anulado: id={} motivo={}", id, request.motivo());
    }

    /**
     * Crea el movimiento contable INGRESO asociado a un pago. Se invoca desde
     * PagoServiceImpl al crear el pago dentro de la misma transaccion.
     * <p>Devuelve null si el pago no trae cuenta (pagos historicos).
     */
    public MovimientoContable crearDesdePago(Pago pago, CuentaContable cuenta) {
        CategoriaMovimiento cat = categoriasRepo.findByCodigo(CATEGORIA_COBRO_ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException(
                        "Categoria de sistema COBRO_ESTUDIANTE no existe. Verifica seed V3."));
        String descripcion = "Pago factura #" + pago.getFactura().getNumeroFactura()
                + " (" + pago.getMetodoPago() + ")";
        String referencia = pago.getFactura().getNumeroFactura();
        MovimientoContable m = MovimientoContable.builder()
                .fecha(pago.getFechaPago().toLocalDate())
                .tipo("INGRESO")
                .monto(pago.getMonto())
                .cuenta(cuenta)
                .categoria(cat)
                .descripcion(descripcion)
                .referencia(referencia)
                .pagoId(pago.getId())
                .anulado(false)
                .build();
        m = movimientosRepo.save(m);
        log.info("Movimiento auto-generado desde pago: id={} pagoId={} cuenta={} monto={}",
                m.getId(), pago.getId(), cuenta.getNombre(), pago.getMonto());
        return m;
    }

    // ========================================================================
    // Sincronizacion desde ms-vehiculos: combustible + mantenimiento
    // ========================================================================

    /**
     * Crea (o reemplaza si ya existia) el movimiento GASTO asociado a un
     * registro de combustible. Devuelve null si no hay cuenta default
     * configurada (no bloquea la operacion en Vehiculos; el llamador lo
     * loguea como warning).
     */
    public MovimientoContable crearDesdeCombustible(Long registroCombustibleId,
                                                    MovimientoVehiculoRequest req) {
        return crearDesdeVehiculo(
                registroCombustibleId, req,
                CATEGORIA_COMBUSTIBLE,
                obtenerConfigOrEmpty().cuentaDefaultCombustibleId(),
                (mov, id) -> mov.setRegistroCombustibleId(id));
    }

    public MovimientoContable crearDesdeMantenimiento(Long mantenimientoId,
                                                      MovimientoVehiculoRequest req) {
        return crearDesdeVehiculo(
                mantenimientoId, req,
                CATEGORIA_MANTENIMIENTO,
                obtenerConfigOrEmpty().cuentaDefaultMantenimientoId(),
                (mov, id) -> mov.setMantenimientoId(id));
    }

    public MovimientoContable actualizarDesdeCombustible(Long registroCombustibleId,
                                                         MovimientoVehiculoRequest req) {
        return movimientosRepo.findByRegistroCombustibleIdAndAnuladoFalse(registroCombustibleId)
                .map(m -> aplicarUpdateVehiculo(m, req))
                .orElseGet(() -> crearDesdeCombustible(registroCombustibleId, req));
    }

    public MovimientoContable actualizarDesdeMantenimiento(Long mantenimientoId,
                                                           MovimientoVehiculoRequest req) {
        return movimientosRepo.findByMantenimientoIdAndAnuladoFalse(mantenimientoId)
                .map(m -> aplicarUpdateVehiculo(m, req))
                .orElseGet(() -> crearDesdeMantenimiento(mantenimientoId, req));
    }

    public void anularDesdeCombustible(Long registroCombustibleId, String motivo) {
        movimientosRepo.findByRegistroCombustibleIdAndAnuladoFalse(registroCombustibleId)
                .ifPresent(m -> anularVehiculo(m, motivo));
    }

    public void anularDesdeMantenimiento(Long mantenimientoId, String motivo) {
        movimientosRepo.findByMantenimientoIdAndAnuladoFalse(mantenimientoId)
                .ifPresent(m -> anularVehiculo(m, motivo));
    }

    // -------- helpers privados --------

    @FunctionalInterface
    private interface OrigenSetter {
        void set(MovimientoContable m, Long id);
    }

    private MovimientoContable crearDesdeVehiculo(Long origenId,
                                                  MovimientoVehiculoRequest req,
                                                  String codigoCategoria,
                                                  Long cuentaDefaultId,
                                                  OrigenSetter origenSetter) {
        if (cuentaDefaultId == null) {
            log.warn("Sin cuenta default para {} — movimiento no se crea (origenId={}, monto={}). "
                    + "Configura una en Configuracion → Contabilidad.",
                    codigoCategoria, origenId, req.monto());
            return null;
        }
        CuentaContable cuenta = cuentasRepo.findById(cuentaDefaultId).orElse(null);
        if (cuenta == null || Boolean.FALSE.equals(cuenta.getActivo())) {
            log.warn("Cuenta default {} de {} no existe o esta inactiva — movimiento no se crea (origenId={})",
                    cuentaDefaultId, codigoCategoria, origenId);
            return null;
        }
        CategoriaMovimiento categoria = categoriasRepo.findByCodigo(codigoCategoria)
                .orElseThrow(() -> new IllegalStateException(
                        "Categoria de sistema " + codigoCategoria + " no existe. Verifica seed V3."));

        MovimientoContable m = MovimientoContable.builder()
                .fecha(req.fecha())
                .tipo("GASTO")
                .monto(req.monto())
                .cuenta(cuenta)
                .categoria(categoria)
                .descripcion(trimOrNull(req.descripcion()))
                .referencia(trimOrNull(req.referencia()))
                .vehiculoId(req.vehiculoId())
                .placaVehiculo(trimOrNull(req.placaVehiculo()))
                .kilometraje(req.kilometraje())
                .anulado(false)
                .build();
        origenSetter.set(m, origenId);
        m = movimientosRepo.save(m);
        log.info("Movimiento GASTO {} auto-generado desde vehiculo: id={} origenId={} placa={} monto={}",
                codigoCategoria, m.getId(), origenId, m.getPlacaVehiculo(), m.getMonto());
        return m;
    }

    private MovimientoContable aplicarUpdateVehiculo(MovimientoContable m, MovimientoVehiculoRequest req) {
        m.setFecha(req.fecha());
        m.setMonto(req.monto());
        m.setDescripcion(trimOrNull(req.descripcion()));
        m.setReferencia(trimOrNull(req.referencia()));
        m.setPlacaVehiculo(trimOrNull(req.placaVehiculo()));
        m.setKilometraje(req.kilometraje());
        m.setVehiculoId(req.vehiculoId());
        return movimientosRepo.save(m);
    }

    private void anularVehiculo(MovimientoContable m, String motivo) {
        m.setAnulado(true);
        m.setMotivoAnulacion(motivo != null && !motivo.isBlank() ? motivo
                : "Registro origen eliminado en Vehiculos");
        movimientosRepo.save(m);
        log.info("Movimiento GASTO anulado desde vehiculo: id={}", m.getId());
    }

    private ConfiguracionEscuelaDTO obtenerConfigOrEmpty() {
        try {
            ConfiguracionEscuelaDTO c = authClient.obtenerConfiguracion();
            return c != null ? c : new ConfiguracionEscuelaDTO(null, null, null);
        } catch (Exception ex) {
            log.warn("No se pudo leer la configuracion de la escuela: {}", ex.getMessage());
            return new ConfiguracionEscuelaDTO(null, null, null);
        }
    }

    private MovimientoContable findOrThrow(Long id) {
        return movimientosRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado: " + id));
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private MovimientoContableResponse toResponse(MovimientoContable m) {
        return new MovimientoContableResponse(
                m.getId(),
                m.getFecha(),
                m.getTipo(),
                m.getMonto(),
                m.getCuenta().getId(),
                m.getCuenta().getNombre(),
                m.getCategoria().getId(),
                m.getCategoria().getCodigo(),
                m.getCategoria().getNombre(),
                m.getDescripcion(),
                m.getReferencia(),
                m.getPagoId(),
                m.getRegistroCombustibleId(),
                m.getMantenimientoId(),
                m.getVehiculoId(),
                m.getPlacaVehiculo(),
                m.getKilometraje(),
                m.getPagadoAId(),
                m.getNombrePagadoA(),
                m.getAnulado(),
                m.getMotivoAnulacion(),
                m.getCreatedAt(),
                m.getCreatedBy()
        );
    }
}
