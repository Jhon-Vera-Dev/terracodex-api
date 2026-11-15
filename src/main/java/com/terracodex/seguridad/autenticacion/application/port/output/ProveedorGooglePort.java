package com.terracodex.seguridad.autenticacion.application.port.output;

import com.terracodex.seguridad.autenticacion.domain.model.PerfilGoogle;

/** Google OAuth 2.0 / OpenID Connect (flujo de código de autorización en el servidor). */
public interface ProveedorGooglePort {

	/** {@code false} si faltan GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET. */
	boolean configurado();

	/** URL de la pantalla de Google a la que se envía al usuario. */
	String urlAutorizacion(String state);

	/**
	 * Canjea el {@code code} del callback y verifica el id token (firma, emisor, audiencia y correo verificado).
	 *
	 * @throws com.terracodex.shared.domain.NoAutenticadoException si el código o el token no son válidos
	 * @throws com.terracodex.shared.domain.ServicioNoDisponibleException si Google no responde
	 */
	PerfilGoogle perfil(String code);

}
