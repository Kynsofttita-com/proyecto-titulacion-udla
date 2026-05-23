package com.escuela.cobros.service;

import com.escuela.cobros.dto.EstadoCuentaResponse;

public interface EstadoCuentaService {

    EstadoCuentaResponse obtenerEstadoCuenta(Long estudianteId);
}
