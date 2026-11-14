package com.terracodex.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.terracodex.TerracodexApiApplication;

/**
 * Screaming architecture verificada: cada paquete de primer nivel es un módulo de negocio.
 * Falla si un módulo usa clases internas de otro, si hay ciclos o si usa una dependencia no declarada
 * en su {@code package-info.java}.
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(TerracodexApiApplication.class);

	@Test
	void respetaLosLimitesDeLosModulos() {
		modules.verify();
	}

	/** Genera diagramas C4 y el canvas de cada módulo en target/spring-modulith-docs. */
	@Test
	void documentaLosModulos() {
		new Documenter(modules).writeDocumentation();
	}

}
