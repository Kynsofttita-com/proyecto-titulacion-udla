package com.escuela.vehiculos.service;

import com.escuela.vehiculos.client.CobrosClient;
import com.escuela.vehiculos.dto.CombustibleRequest;
import com.escuela.vehiculos.dto.CombustibleResponse;
import com.escuela.vehiculos.dto.MovimientoVehiculoRequest;
import com.escuela.vehiculos.entity.RegistroCombustible;
import com.escuela.vehiculos.entity.TipoCombustible;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.RecursoNotFoundException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.repository.RegistroCombustibleRepository;
import com.escuela.vehiculos.repository.TipoCombustibleRepository;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class CombustibleService {

    private static final Logger log = LoggerFactory.getLogger(CombustibleService.class);

    private final RegistroCombustibleRepository repository;
    private final VehiculoRepository vehiculoRepository;
    private final TipoCombustibleRepository tipoCombustibleRepository;
    private final CobrosClient cobrosClient;

    public CombustibleService(RegistroCombustibleRepository repository,
                              VehiculoRepository vehiculoRepository,
                              TipoCombustibleRepository tipoCombustibleRepository,
                              CobrosClient cobrosClient) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
        this.tipoCombustibleRepository = tipoCombustibleRepository;
        this.cobrosClient = cobrosClient;
    }

    @Transactional(readOnly = true)
    public Page<CombustibleResponse> listar(Long vehiculoId, Pageable pageable) {
        vehiculoOFallar(vehiculoId);
        return repository.findByVehiculoIdOrderByFechaDesc(vehiculoId, pageable).map(this::toResponse);
    }

    public CombustibleResponse registrar(Long vehiculoId, CombustibleRequest request) {
        Vehiculo v = vehiculoOFallar(vehiculoId);
        if (request.kilometrajeActual() < (v.getKilometraje() == null ? 0 : v.getKilometraje())) {
            throw new IllegalArgumentException(
                    "Kilometraje actual (" + request.kilometrajeActual() +
                            ") no puede ser menor al actual del vehiculo (" + v.getKilometraje() + ")");
        }

        BigDecimal costoTotal = resolverCostoTotal(v, request);

        RegistroCombustible r = RegistroCombustible.builder()
                .vehiculo(v)
                .fecha(request.fecha())
                .litros(request.litros())
                .costoTotal(costoTotal)
                .kilometrajeActual(request.kilometrajeActual())
                .estacion(request.estacion())
                .build();
        r = repository.save(r);

        // Actualiza el kilometraje del vehiculo (defensivo)
        v.setKilometraje(request.kilometrajeActual());
        vehiculoRepository.save(v);

        log.info("Combustible registrado id={} vehiculoId={} litros={} costoTotal={}",
                r.getId(), vehiculoId, r.getLitros(), costoTotal);

        // Sincroniza el movimiento GASTO en contabilidad (best-effort).
        sincronizarConCobrosCrear(r, v);

        return toResponse(r);
    }

    /**
     * Elimina el registro Y anula el movimiento contable asociado (best-effort).
     */
    public void eliminar(Long vehiculoId, Long registroId) {
        RegistroCombustible r = repository.findByIdAndVehiculoId(registroId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("RegistroCombustible", registroId));
        // Hard delete (no extiende BaseEntity)
        repository.delete(r);
        log.info("Combustible eliminado id={}", registroId);

        try {
            cobrosClient.anularMovimientoCombustible(registroId,
                    "Registro de combustible #" + registroId + " eliminado en Vehiculos");
        } catch (Exception ex) {
            log.warn("No se pudo anular el movimiento contable del combustible {}: {}",
                    registroId, ex.getMessage());
        }
    }

    private void sincronizarConCobrosCrear(RegistroCombustible r, Vehiculo v) {
        try {
            MovimientoVehiculoRequest req = new MovimientoVehiculoRequest(
                    r.getFecha().toLocalDate(),
                    r.getCostoTotal(),
                    v.getId(),
                    v.getPlaca(),
                    r.getKilometrajeActual(),
                    descripcionCombustible(r),
                    v.getPlaca()
            );
            cobrosClient.crearMovimientoCombustible(r.getId(), req);
        } catch (Exception ex) {
            log.warn("No se pudo sincronizar el gasto contable del combustible {}: {}",
                    r.getId(), ex.getMessage());
        }
    }

    private String descripcionCombustible(RegistroCombustible r) {
        String base = "Combustible: " + r.getLitros() + " L";
        if (r.getEstacion() != null && !r.getEstacion().isBlank()) {
            base += " en " + r.getEstacion();
        }
        return base;
    }

    /**
     * Resuelve el costoTotal final del registro:
     *  1) Si viene explicito en el request, se respeta.
     *  2) Si no, se autocalcula con litros × precio_actual del tipo de combustible.
     *     - Override: si el request manda tipoCombustibleIdOverride, se usa ese tipo.
     *     - Default: se usa el tipo_combustible_id configurado en el vehiculo.
     *  3) Si no hay forma de calcularlo, falla con mensaje claro.
     */
    private BigDecimal resolverCostoTotal(Vehiculo v, CombustibleRequest request) {
        if (request.costoTotal() != null) {
            return request.costoTotal();
        }

        Long tipoId = request.tipoCombustibleIdOverride() != null
                ? request.tipoCombustibleIdOverride()
                : v.getTipoCombustibleId();

        if (tipoId == null) {
            throw new IllegalArgumentException(
                    "No se puede autocalcular el costo: el vehiculo no tiene tipo de combustible asignado " +
                            "y no se envio costoTotal ni tipoCombustibleIdOverride");
        }
        TipoCombustible tipo = tipoCombustibleRepository.findById(tipoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "TipoCombustible no encontrado: " + tipoId));

        return request.litros()
                .multiply(tipo.getPrecioActual())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Vehiculo vehiculoOFallar(Long id) {
        return vehiculoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    private CombustibleResponse toResponse(RegistroCombustible r) {
        return new CombustibleResponse(r.getId(), r.getVehiculo().getId(),
                r.getFecha(), r.getLitros(), r.getCostoTotal(),
                r.getCostoPorLitro(), r.getKilometrajeActual(), r.getEstacion());
    }
}
