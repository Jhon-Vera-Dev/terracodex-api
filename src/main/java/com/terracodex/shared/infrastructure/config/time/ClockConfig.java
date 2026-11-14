package com.terracodex.shared.infrastructure.config.time;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El dominio nunca llama a {@code now()}: los casos de uso reciben la hora de este reloj (fácil de fijar en tests). */
@Configuration(proxyBeanMethods = false)
class ClockConfig {

	@Bean
	Clock clock(@Value("${app.zona-horaria:America/Lima}") String zona) {
		return Clock.system(ZoneId.of(zona));
	}

}
