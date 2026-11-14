package com.terracodex.shared.domain;

/** No se pudo identificar al usuario (credenciales incorrectas). Se traduce a 401. */
public class NoAutenticadoException extends RuntimeException {

	public NoAutenticadoException(String message) {
		super(message);
	}

}
