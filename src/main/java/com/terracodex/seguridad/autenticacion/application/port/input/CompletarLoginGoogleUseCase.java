package com.terracodex.seguridad.autenticacion.application.port.input;

import com.terracodex.seguridad.autenticacion.application.dto.result.CallbackGoogleResult;

public interface CompletarLoginGoogleUseCase {

	/**
	 * Callback de Google. Si la cuenta no existe en terracodex, crea el usuario como OPERADOR con acceso web y móvil.
	 *
	 * @param errorGoogle {@code error} del callback (p. ej. {@code access_denied}), o {@code null}
	 * @return si quedó completado (si falló, el motivo lo recibe la app al canjear) y el canal del inicio
	 */
	CallbackGoogleResult completar(String state, String code, String errorGoogle);

}
