package com.terracodex.seguridad.autenticacion.application.port.input;

public interface RevocarSesionesUseCase {

	/** @return cuántas sesiones se cerraron */
	int revocarTodas(Long usuarioId);

}
