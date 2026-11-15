package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.google;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.terracodex.seguridad.autenticacion.application.port.output.ProveedorGooglePort;
import com.terracodex.seguridad.autenticacion.domain.model.PerfilGoogle;
import com.terracodex.shared.domain.NoAutenticadoException;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

/**
 * OpenID Connect con Google, flujo de código de autorización. El secreto nunca sale del servidor. El id token se
 * verifica con las claves públicas de Google (JWKS): firma, emisor, audiencia (nuestro client id) y vigencia.
 */
@Component
class GoogleOAuthAdapter implements ProveedorGooglePort {

	private static final String AUTORIZACION = "https://accounts.google.com/o/oauth2/v2/auth";

	private static final String TOKEN = "https://oauth2.googleapis.com/token";

	private static final String JWKS = "https://www.googleapis.com/oauth2/v3/certs";

	private static final List<String> EMISORES = List.of("https://accounts.google.com", "accounts.google.com");

	private final String clientId;

	private final String clientSecret;

	private final String redirectUri;

	private final RestClient http;

	private volatile JwtDecoder decoder;

	GoogleOAuthAdapter(@Value("${app.seguridad.google.client-id:}") String clientId,
			@Value("${app.seguridad.google.client-secret:}") String clientSecret,
			@Value("${app.seguridad.google.redirect-uri:}") String redirectUri) {
		this.clientId = clientId.strip();
		this.clientSecret = clientSecret.strip();
		this.redirectUri = redirectUri.strip();
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setReadTimeout(Duration.ofSeconds(15));
		this.http = RestClient.builder().requestFactory(factory).build();
	}

	@Override
	public boolean configurado() {
		return !clientId.isEmpty() && !clientSecret.isEmpty() && !redirectUri.isEmpty();
	}

	@Override
	public String urlAutorizacion(String state) {
		return UriComponentsBuilder.fromUriString(AUTORIZACION)
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.queryParam("response_type", "code")
			.queryParam("scope", "openid email profile")
			.queryParam("state", state)
			.queryParam("prompt", "select_account")
			.encode()
			.toUriString();
	}

	@Override
	public PerfilGoogle perfil(String code) {
		var form = new LinkedMultiValueMap<String, String>();
		form.add("code", code);
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);
		form.add("redirect_uri", redirectUri);
		form.add("grant_type", "authorization_code");
		RespuestaToken respuesta;
		try {
			respuesta = http.post()
				.uri(TOKEN)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(RespuestaToken.class);
		}
		catch (HttpClientErrorException ex) {
			throw new NoAutenticadoException("Google rechazó el inicio de sesión. Vuelve a intentarlo");
		}
		catch (RestClientException ex) {
			throw new ServicioNoDisponibleException("Google no respondió. Intenta nuevamente en unos minutos", ex);
		}
		if (respuesta == null || respuesta.idToken() == null) {
			throw new NoAutenticadoException("Google no devolvió la identidad de la cuenta");
		}

		Jwt jwt;
		try {
			jwt = decoder().decode(respuesta.idToken());
		}
		catch (JwtException ex) {
			throw new NoAutenticadoException("La identidad de Google no es válida");
		}
		var email = jwt.getClaimAsString("email");
		if (email == null || !Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
			throw new NoAutenticadoException("La cuenta de Google no tiene un correo verificado");
		}
		return new PerfilGoogle(jwt.getSubject(), email, jwt.getClaimAsString("name"), jwt.getClaimAsString("picture"));
	}

	private JwtDecoder decoder() {
		if (decoder == null) {
			var nimbus = NimbusJwtDecoder.withJwkSetUri(JWKS).build();
			OAuth2TokenValidator<Jwt> emisor = jwt -> EMISORES.contains(jwt.getClaimAsString("iss"))
					? OAuth2TokenValidatorResult.success()
					: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Emisor no válido", null));
			OAuth2TokenValidator<Jwt> audiencia = jwt -> jwt.getAudience() != null && jwt.getAudience().contains(clientId)
					? OAuth2TokenValidatorResult.success()
					: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audiencia no válida", null));
			nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), emisor, audiencia));
			decoder = nimbus;
		}
		return decoder;
	}

	record RespuestaToken(@JsonProperty("id_token") String idToken) {
	}

}
