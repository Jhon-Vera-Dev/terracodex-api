package com.terracodex.seguridad.autenticacion.application.port.input;

import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;

public interface RenovarSesionUseCase {

	/** Cambia el refresh token por un access token nuevo y rota el refresh token. */
	SesionResult renovar(String refreshToken);

}
