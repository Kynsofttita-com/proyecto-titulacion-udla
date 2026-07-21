package com.escuela.cobros.service;

import com.escuela.cobros.dto.AnularMovimientoRequest;
import com.escuela.cobros.dto.MovimientoContableRequest;
import com.escuela.cobros.dto.MovimientoContableResponse;
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

    private final MovimientoContableRepository movimientosRepo;
    private final CuentaContableRepository cuentasRepo;
    private final CategoriaMovimientoRepository categoriasRepo;

    public MovimientoContableService(MovimientoContableRepository movimientosRepo,
                                     CuentaContableRepository cuentasRepo,
                                     CategoriaMovimientoRepository categoriasRepo) {
        this.movimientosRepo = movimientosRepo;
        this.cuentasRepo = cuentasRepo;
        this.categoriasRepo = categoriasRepo;
    }

    @Transactional(readOnly = true)
    public Page<MovimientoContableResponse> buscar(
            Long cuentaId, Long categoriaId, String tipo,
            LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        return movimientosRepo.buscarConFiltros(cuentaId, categoriaId, tipo, fechaInicio, fechaFin, pageable)
                .map(this::toResponse);
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
                .anulado(false)
                .build();
        m = movimientosRepo.save(m);
        log.info("Movimiento creado: id={} tipo={} monto={} cuenta={} categoria={}",
                m.getId(), m.getTipo(), m.getMonto(), cuenta.getNombre(), categoria.getCodigo());
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
                m.getAnulado(),
                m.getMotivoAnulacion(),
                m.getCreatedAt(),
                m.getCreatedBy()
        );
    }
}
