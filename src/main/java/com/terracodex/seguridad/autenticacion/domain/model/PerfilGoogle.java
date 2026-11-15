package com.terracodex.seguridad.autenticacion.domain.model;

import java.util.Locale;

/**
 * Datos de la cuenta que Google confirmó (id token ya verificado).
 *
 * @param sub id estable de la cuenta de Google
 * @param foto URL de la foto de perfil, o {@code null}
 */
public record PerfilGoogle(String sub, String email, String nombre, String foto) {

	private static final int MAX_LOGIN = 60;

	/** Login sugerido a partir del correo: {@code juan.perez@gmail.com} → {@code juan.perez}. */
	public String loginSugerido() {
		var base = email == null ? "" : email.split("@", 2)[0];
		var limpio = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", ".").replaceAll("\\.{2,}", ".");
		limpio = limpio.replaceAll("^[.]+|[.]+$", "");
		if (limpio.length() < 3) {
			limpio = "google." + (limpio.isEmpty() ? "usuario" : limpio);
		}
		return limpio.length() > MAX_LOGIN - 4 ? limpio.substring(0, MAX_LOGIN - 4) : limpio;
	}

	/** Nombre para mostrar: el de Google o, si no hay, el correo. */
	public String descripcion() {
		var valor = nombre != null && !nombre.isBlank() ? nombre.strip() : email;
		return valor.length() > 150 ? valor.substring(0, 150) : valor;
	}

	/** La columna del avatar admite 500 caracteres. */
	public String avatar() {
		return foto != null && foto.length() <= 500 ? foto : null;
	}

}
