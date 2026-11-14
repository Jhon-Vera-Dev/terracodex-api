package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response;

import java.time.Instant;
import java.util.List;

import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;

/** {@code data} de login y refresh. */
public record SesionResponse(String accessToken, String tokenType, Instant expiraEn, String refreshToken,
		Instant refreshExpiraEn, UsuarioSesion usuario) {

	public static SesionResponse from(SesionResult s) {
		var u = s.usuario();
		return new SesionResponse(s.accessToken(), "Bearer", s.expiraEn(), s.refreshToken(), s.refreshExpiraEn(),
				new UsuarioSesion(u.id(), u.login(), u.descripcion(), u.avatar(), u.isWeb(), u.isMovil(), u.roles()));
	}

	public record UsuarioSesion(Long id, String login, String descripcion, String avatar, boolean isWeb,
			boolean isMovil, List<String> roles) {
	}

}
