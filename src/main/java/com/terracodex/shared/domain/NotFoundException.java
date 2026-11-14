package com.terracodex.shared.domain;

/** El recurso pedido no existe. Se responde como 404. */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String recurso, Object id) {
		super(recurso + " " + id + " no existe");
	}

}
