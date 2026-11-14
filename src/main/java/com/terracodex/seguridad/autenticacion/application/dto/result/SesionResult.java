package com.terracodex.seguridad.autenticacion.application.dto.result;

import java.time.Instant;
import java.util.List;

import com.terracodex.seguridad.autenticacion.application.port.output.EmisorTokenPort.TokenEmitido;
import com.terracodex.seguridad.autenticacion.domain.model.Sesion;
import com.terracodex.seguridad.usuario.domain.model.Usuario;

/** Resultado de iniciar o renovar una sesión. */
public record SesionResult(String accessToken, Instant expiraEn, String refreshToken, Instant refreshExpiraEn,
		UsuarioSesion usuario) {

	public static SesionResult of(TokenEmitido acceso, String refreshToken, Sesion sesion, Usuario usuario,
			List<String> roles) {
		return new SesionResult(acceso.valor(), acceso.expiraEn(), refreshToken, sesion.expiraEn(),
				new UsuarioSesion(usuario.id(), usuario.login(), usuario.descripcion(), usuario.avatar(),
						usuario.isWeb(), usuario.isMovil(), roles));
	}

	public record UsuarioSesion(Long id, String login, String descripcion, String avatar, boolean isWeb,
			boolean isMovil, List<String> roles) {
	}

}
