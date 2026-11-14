package com.terracodex.seguridad.autenticacion.application.port.input;

public interface CerrarSesionUseCase {

	/** Idempotente: un token desconocido o ya revocado no es un error. */
	void cerrar(String refreshToken);

}
