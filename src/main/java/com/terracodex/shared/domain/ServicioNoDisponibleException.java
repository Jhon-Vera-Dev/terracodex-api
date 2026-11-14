package com.terracodex.shared.domain;

/** Un servicio externo del que depende la operación no responde. Se responde como 503: se puede reintentar. */
public class ServicioNoDisponibleException extends RuntimeException {

	public ServicioNoDisponibleException(String message) {
		super(message);
	}

	public ServicioNoDisponibleException(String message, Throwable cause) {
		super(message, cause);
	}

}
