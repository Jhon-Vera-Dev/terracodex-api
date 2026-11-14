package com.terracodex.seguridad.autenticacion.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh token opaco: 256 bits aleatorios. Al cliente se le da el valor; en la base solo se guarda su SHA-256, así
 * una copia de la base no sirve para abrir sesiones.
 */
public record RefreshToken(String valor, String hash) {

	private static final SecureRandom RANDOM = new SecureRandom();

	public static RefreshToken generar() {
		var bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		var valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return new RefreshToken(valor, hashDe(valor));
	}

	public static String hashDe(String valor) {
		try {
			var digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 no disponible", ex);
		}
	}

}
