package com.terracodex.shared.domain;

/** Una regla de negocio no se cumple. Se responde como 422. */
public class DomainException extends RuntimeException {

	public DomainException(String message) {
		super(message);
	}

}
