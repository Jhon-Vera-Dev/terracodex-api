package com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.response;

import java.util.List;

import com.terracodex.seguridad.usuario.application.dto.result.UsuarioResult;

public record UsuarioResponse(Long id, String login, String descripcion, String avatar, boolean isWeb,
		boolean isMovil, boolean estado, List<RolAsignado> roles) {

	public static UsuarioResponse from(UsuarioResult u) {
		return new UsuarioResponse(u.id(), u.login(), u.descripcion(), u.avatar(), u.isWeb(), u.isMovil(), u.estado(),
				u.roles().stream().map(r -> new RolAsignado(r.id(), r.descripcion())).toList());
	}

	public record RolAsignado(Long id, String descripcion) {
	}

}
