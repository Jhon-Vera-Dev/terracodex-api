package com.terracodex.seguridad.usuario.application.dto.result;

import java.util.List;

/** Nunca incluye la contraseña, ni siquiera cifrada. */
public record UsuarioResult(Long id, String login, String descripcion, String avatar, boolean isWeb, boolean isMovil,
		boolean estado, List<RolAsignado> roles) {

	public record RolAsignado(Long id, String descripcion) {
	}

}
