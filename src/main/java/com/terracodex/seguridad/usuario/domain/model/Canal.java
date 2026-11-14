package com.terracodex.seguridad.usuario.domain.model;

/** Desde dónde se conecta el usuario. Usuarios y menús indican en cuáles están disponibles. */
public enum Canal {

	WEB, MOVIL;

	public boolean permitidoPor(boolean isWeb, boolean isMovil) {
		return this == WEB ? isWeb : isMovil;
	}

}
