package com.terracodex.shared.domain;

/** El usuario está identificado pero no puede realizar la operación. Se traduce a 403. */
public class AccesoDenegadoException extends RuntimeException {

	public AccesoDenegadoException(String message) {
		super(message);
	}

}
