package com.terracodex.shared.infrastructure.config.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.terracodex.shared.application.VerificadorSesion;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import tools.jackson.databind.json.JsonMapper;

/**
 * API sin estado protegida con JWT (HS256). El token lo emite el módulo seguridad; aquí solo se valida.
 * <ul>
 * <li>Público: {@code POST /api/auth/login | refresh | logout}, health e info.</li>
 * <li>Además de firma, emisor y expiración, cada token se rechaza si su sesión ({@code sid}) ya no está activa.</li>
 * <li>El resto exige token. Cada slice restringe por rol con {@code @PreAuthorize} en su controlador.</li>
 * <li>El claim {@code roles} se convierte en autoridades {@code ROLE_<rol>}.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class SecurityConfig {

	@Bean
	SecurityFilterChain api(HttpSecurity http, @Value("${spring.h2.console.enabled:false}") boolean consolaH2,
			JsonMapper json) throws Exception {
		var errores = new ApiSecurityErrorHandler(json);
		http.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
					.permitAll();
				auth.requestMatchers("/actuator/health/**", "/actuator/info", "/error").permitAll();
				if (consolaH2) {
					auth.requestMatchers("/h2-console/**").permitAll();
				}
				auth.requestMatchers("/actuator/**").hasRole("ADMIN");
				auth.anyRequest().authenticated();
			})
			.exceptionHandling(ex -> ex.authenticationEntryPoint(errores).accessDeniedHandler(errores))
			.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesDelToken()))
				.authenticationEntryPoint(errores)
				.accessDeniedHandler(errores));
		if (consolaH2) {
			http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
		}
		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${app.seguridad.jwt.secreto}") String secreto,
			@Value("${app.seguridad.jwt.emisor}") String emisor, VerificadorSesion verificador) {
		var decoder = NimbusJwtDecoder.withSecretKey(clave(secreto)).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(emisor),
				sesionActiva(verificador)));
		return decoder;
	}

	/** Un token firmado y vigente igual se rechaza si su sesión (claim sid) fue cerrada o revocada. */
	private static OAuth2TokenValidator<Jwt> sesionActiva(VerificadorSesion verificador) {
		var error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "La sesión fue cerrada o revocada", null);
		return jwt -> verificador.estaActiva(jwt.getClaimAsString("sid")) ? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(error);
	}

	@Bean
	JwtEncoder jwtEncoder(@Value("${app.seguridad.jwt.secreto}") String secreto) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(clave(secreto)));
	}

	private static JwtAuthenticationConverter rolesDelToken() {
		var autoridades = new JwtGrantedAuthoritiesConverter();
		autoridades.setAuthoritiesClaimName("roles");
		autoridades.setAuthorityPrefix("ROLE_");
		var converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(autoridades);
		return converter;
	}

	private static SecretKey clave(String secreto) {
		var bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException(
					"app.seguridad.jwt.secreto (variable JWT_SECRET) debe tener al menos 32 caracteres");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

}
