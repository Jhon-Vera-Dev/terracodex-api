package com.terracodex.shared.infrastructure.config.persistence;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Llena los campos de auditoría de {@code EntidadAuditada}. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "auditor", dateTimeProviderRef = "horaAuditoria")
class AuditoriaConfig {

	static final String SISTEMA = "SISTEMA";

	/** El login del usuario del token; {@code SISTEMA} si no hay usuario (eventos, procesos programados, arranque). */
	@Bean
	AuditorAware<String> auditor() {
		return () -> {
			var autenticacion = SecurityContextHolder.getContext().getAuthentication();
			if (autenticacion instanceof JwtAuthenticationToken token) {
				var login = token.getToken().getClaimAsString("login");
				if (login != null && !login.isBlank()) {
					return Optional.of(login);
				}
			}
			return Optional.of(SISTEMA);
		};
	}

	@Bean
	DateTimeProvider horaAuditoria(Clock clock) {
		return () -> Optional.of(Instant.now(clock));
	}

}
