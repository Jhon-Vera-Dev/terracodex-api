package com.terracodex.seguridad.usuario.domain.model;

import com.terracodex.shared.domain.DomainException;

/** Reglas mínimas para una contraseña en texto plano, antes de cifrarla. BCrypt solo usa los primeros 72 bytes. */
public final class PoliticaContrasena {

	private PoliticaContrasena() {
	}

	public static void validar(String contrasena) {
		if (contrasena == null || contrasena.length() < 8 || contrasena.length() > 72
				|| contrasena.chars().noneMatch(Character::isLetter)
				|| contrasena.chars().noneMatch(Character::isDigit)) {
			throw new DomainException("La contraseña debe tener entre 8 y 72 caracteres, con letras y números");
		}
	}

}
