package com.terracodex.seguridad.autenticacion.application.port.input;

import java.util.Optional;

import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;

public interface CanjearLoginGoogleUseCase {

	/**
	 * @return la sesión, o vacío si el usuario todavía no termina en Google (la app vuelve a preguntar)
	 * @throws com.terracodex.shared.domain.NoAutenticadoException si no es válido, venció, falló o ya se canjeó
	 */
	Optional<SesionResult> canjear(String id, String verificador);

}
