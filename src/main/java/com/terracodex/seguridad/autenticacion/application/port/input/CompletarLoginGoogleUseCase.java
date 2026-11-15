package com.terracodex.seguridad.autenticacion.application.port.input;

public interface CompletarLoginGoogleUseCase {

	/**
	 * Callback de Google. Si la cuenta no existe en terracodex, crea el usuario como OPERADOR con acceso móvil.
	 *
	 * @param errorGoogle {@code error} del callback (p. ej. {@code access_denied}), o {@code null}
	 * @return {@code true} si quedó completado; {@code false} si falló (el motivo lo recibe la app al canjear)
	 */
	boolean completar(String state, String code, String errorGoogle);

}
