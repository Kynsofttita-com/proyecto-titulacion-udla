package com.escuela.cobros.exception;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal montoPago, BigDecimal saldo) {
        super("Monto a pagar (" + montoPago + ") excede el saldo disponible (" + saldo + ")");
    }

    public SaldoInsuficienteException(String message) {
        super(message);
    }
}
