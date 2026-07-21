package com.escuela.cobros.service;

import com.escuela.cobros.dto.CuentaRequest;
import com.escuela.cobros.dto.CuentaResponse;
import com.escuela.cobros.entity.CuentaContable;
import com.escuela.cobros.repository.CuentaContableRepository;
import com.escuela.cobros.repository.MovimientoContableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CuentaContableService {

    private static final Logger log = LoggerFactory.getLogger(CuentaContableService.class);

    private final CuentaContableRepository cuentasRepo;
    private final MovimientoContableRepository movimientosRepo;

    public CuentaContableService(CuentaContableRepository cuentasRepo,
                                 MovimientoContableRepository movimientosRepo) {
        this.cuentasRepo = cuentasRepo;
        this.movimientosRepo = movimientosRepo;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listarTodas() {
        return cuentasRepo.findAllByOrderByNombreAsc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listarActivas() {
        return cuentasRepo.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CuentaResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    public CuentaResponse crear(CuentaRequest request) {
        validarNumeroCuentaSegunTipo(request.tipo(), request.numeroCuenta());
        cuentasRepo.findByNombreIgnoreCase(request.nombre().trim()).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuenta con nombre " + request.nombre());
        });

        CuentaContable c = CuentaContable.builder()
                .nombre(request.nombre().trim())
                .tipo(request.tipo())
                .numeroCuenta(request.numeroCuenta() != null && !request.numeroCuenta().isBlank()
                        ? request.numeroCuenta().trim() : null)
                .saldoInicial(request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO)
                .activo(request.activo() != null ? request.activo() : true)
                .observaciones(request.observaciones())
                .build();
        c = cuentasRepo.save(c);
        log.info("Cuenta creada: id={} nombre={} tipo={}", c.getId(), c.getNombre(), c.getTipo());
        return toResponse(c);
    }

    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        validarNumeroCuentaSegunTipo(request.tipo(), request.numeroCuenta());
        CuentaContable c = findOrThrow(id);

        // Verificar conflicto de nombre solo si cambio
        if (!c.getNombre().equalsIgnoreCase(request.nombre().trim())) {
            cuentasRepo.findByNombreIgnoreCase(request.nombre().trim()).ifPresent(otra -> {
                if (!otra.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe otra cuenta con nombre " + request.nombre());
                }
            });
        }

        c.setNombre(request.nombre().trim());
        c.setTipo(request.tipo());
        c.setNumeroCuenta(request.numeroCuenta() != null && !request.numeroCuenta().isBlank()
                ? request.numeroCuenta().trim() : null);
        c.setSaldoInicial(request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO);
        if (request.activo() != null) c.setActivo(request.activo());
        c.setObservaciones(request.observaciones());
        c = cuentasRepo.save(c);
        log.info("Cuenta actualizada: id={} nombre={}", c.getId(), c.getNombre());
        return toResponse(c);
    }

    public void desactivar(Long id) {
        CuentaContable c = findOrThrow(id);
        c.setActivo(false);
        cuentasRepo.save(c);
        log.info("Cuenta desactivada: id={} nombre={}", c.getId(), c.getNombre());
    }

    /** Suma saldo_inicial + ingresos - gastos (excluye movimientos anulados). */
    private BigDecimal calcularSaldoActual(CuentaContable c) {
        BigDecimal ingresos = movimientosRepo.sumaIngresosPorCuenta(c.getId());
        BigDecimal gastos = movimientosRepo.sumaGastosPorCuenta(c.getId());
        return c.getSaldoInicial()
                .add(ingresos != null ? ingresos : BigDecimal.ZERO)
                .subtract(gastos != null ? gastos : BigDecimal.ZERO);
    }

    private void validarNumeroCuentaSegunTipo(String tipo, String numero) {
        if (("BANCO".equals(tipo) || "TARJETA".equals(tipo))
                && (numero == null || numero.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El numero de cuenta es requerido para tipo " + tipo);
        }
    }

    private CuentaContable findOrThrow(Long id) {
        return cuentasRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cuenta no encontrada: " + id));
    }

    private CuentaResponse toResponse(CuentaContable c) {
        return new CuentaResponse(
                c.getId(),
                c.getNombre(),
                c.getTipo(),
                c.getNumeroCuenta(),
                c.getSaldoInicial(),
                calcularSaldoActual(c),
                c.getActivo(),
                c.getObservaciones(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getCreatedBy(),
                c.getUpdatedBy()
        );
    }
}
