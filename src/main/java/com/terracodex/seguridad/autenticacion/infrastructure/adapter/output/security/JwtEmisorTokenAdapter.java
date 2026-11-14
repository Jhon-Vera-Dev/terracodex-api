package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.security;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.autenticacion.application.port.output.EmisorTokenPort;
import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.seguridad.usuario.domain.model.Usuario;

/**
 * Firma el JWT con la misma clave que usa el servidor de recursos para validarlo (shared.infrastructure.config).
 * Claims: sub = id del usuario, login, nombre, roles, canal y sid (sesión que se puede revocar).
 */
@Component
class JwtEmisorTokenAdapter implements EmisorTokenPort {

	private final JwtEncoder encoder;

	private final Clock clock;

	private final String emisor;

	private final Duration expiracion;

	JwtEmisorTokenAdapter(JwtEncoder encoder, Clock clock, @Value("${app.seguridad.jwt.emisor}") String emisor,
			@Value("${app.seguridad.jwt.expiracion}") Duration expiracion) {
		this.encoder = encoder;
		this.clock = clock;
		this.emisor = emisor;
		this.expiracion = expiracion;
	}

	@Override
	public TokenEmitido emitir(Usuario usuario, List<String> roles, Canal canal, UUID sesionId) {
		var ahora = clock.instant();
		var expira = ahora.plus(expiracion);
		var claims = JwtClaimsSet.builder()
			.issuer(emisor)
			.subject(usuario.id().toString())
			.issuedAt(ahora)
			.expiresAt(expira)
			.claim("login", usuario.login())
			.claim("nombre", usuario.descripcion())
			.claim("roles", roles)
			.claim("canal", canal.name())
			.claim("sid", sesionId.toString())
			.build();
		var header = JwsHeader.with(MacAlgorithm.HS256).build();
		var token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new TokenEmitido(token, expira);
	}

}
