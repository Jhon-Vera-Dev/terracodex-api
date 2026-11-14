package com.terracodex.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.Optional;
import java.util.Set;

import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainEvent;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Reglas de arquitectura. Todos los módulos siguen el esquema por feature:
 * <pre>
 * modulo/
 *   package-info.java                 &#64;ApplicationModule
 *   events/                           eventos públicos del módulo (&#64;NamedInterface "events"; los ven otros módulos y Kafka)
 *   feature/                          slice vertical (usuario, rol, entrega, cliente, partediario…)
 *     domain/                         Java puro: model/, events/
 *     application/
 *       port/input/                   casos de uso (interfaces)
 *       port/output/                  repositorios y servicios externos (interfaces)
 *       usecase/                      implementación de los casos de uso
 *       dto/                          command/, query/, result/
 *     infrastructure/adapter/
 *       input/                        rest/ (controller, request/, response/), event/, startup/
 *       output/                       persistence/ (entity/, repository/, mapper/, adapter), security/…
 * </pre>
 */
@AnalyzeClasses(packages = "com.terracodex", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTests {

	private static final String BASE = "com.terracodex.";

	private static final Set<String> CAPAS = Set.of("domain", "application", "infrastructure");

	// Reglas comunes a los dos esquemas

	@ArchTest
	static final ArchRule el_dominio_no_conoce_frameworks = noClasses().that()
		.resideInAnyPackage("com.terracodex..domain..", "com.terracodex.*.events..")
		// package-info solo lleva la anotación @NamedInterface de Spring Modulith
		.and()
		.doNotHaveSimpleName("package-info")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("org.springframework..", "jakarta.persistence..", "jakarta.servlet..",
				"org.apache.kafka..", "tools.jackson..", "com.fasterxml.jackson..")
		.because("el dominio debe poder probarse y reutilizarse sin infraestructura");

	@ArchTest
	static final ArchRule el_dominio_solo_depende_de_dominio = classes().that()
		.resideInAPackage("com.terracodex..domain..")
		.should()
		.onlyDependOnClassesThat()
		.resideInAnyPackage("com.terracodex..domain..", "com.terracodex.*.events..", "java..")
		.because("las dependencias apuntan hacia el dominio, nunca desde él");

	@ArchTest
	static final ArchRule solo_la_infraestructura_usa_infraestructura = noClasses().that()
		.resideOutsideOfPackages("com.terracodex..infrastructure..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("com.terracodex..infrastructure..")
		.because("los casos de uso hablan con puertos; los adaptadores se inyectan");

	@ArchTest
	static final ArchRule los_eventos_son_records_inmutables = classes().that()
		.implement(DomainEvent.class)
		.should()
		.beRecords()
		.andShould()
		.resideInAPackage("..events..");

	// Esquema por feature

	@ArchTest
	static final ArchRule la_aplicacion_no_conoce_web_ni_persistencia = noClasses().that()
		.resideInAPackage("com.terracodex..application..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("org.springframework.web..", "org.springframework.data..", "org.springframework.security..",
				"jakarta.persistence..", "jakarta.servlet..", "org.apache.kafka..", "tools.jackson..",
				"com.fasterxml.jackson..")
		.because("application orquesta el dominio a través de puertos; solo usa Spring para @Transactional y @Value");

	@ArchTest
	static final ArchRule los_adaptadores_de_entrada_solo_usan_puertos_de_entrada = noClasses().that()
		.resideInAPackage("..infrastructure.adapter.input..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..application.usecase..", "..application.port.output..",
				"..infrastructure.adapter.output..")
		.because("un controlador o listener llama a un caso de uso por su interfaz, nunca a un repositorio");

	@ArchTest
	static final ArchRule los_casos_de_uso_implementan_un_puerto_de_entrada = classes().that()
		.resideInAPackage("..application.usecase..")
		.and()
		.areNotInterfaces()
		.should()
		.beAnnotatedWith(UseCase.class)
		.andShould()
		.implement(JavaClass.Predicates.resideInAPackage("..application.port.input.."))
		.because("cada caso de uso se expone solo por su puerto de entrada");

	@ArchTest
	static final ArchRule una_feature_solo_usa_dominio_y_puertos_de_otra = classes().that()
		.resideInAPackage("com.terracodex..")
		.should(new SoloDominioYPuertosDeOtraFeature())
		.because("las features de un módulo se comunican por su dominio, sus puertos o eventos; "
				+ "su implementación es privada");

	@ArchTest
	static final ArchRule los_casos_de_uso_viven_en_application_usecase = classes().that()
		.areAnnotatedWith(UseCase.class)
		.should()
		.resideInAPackage("..application.usecase..");

	@ArchTest
	static final ArchRule todos_los_modulos_usan_el_esquema_por_feature = classes().that()
		.resideInAPackage("com.terracodex.*..")
		.and()
		.resideOutsideOfPackage("com.terracodex.shared..")
		.should(new SiguenElEsquemaPorFeature())
		.because("fuera de shared, cada clase vive en modulo/feature/{domain,application,infrastructure}; "
				+ "en la raíz del módulo solo van package-info y events/");

	/** {@code modulo.feature} si la clase sigue el esquema por feature. */
	static Optional<String> feature(JavaClass javaClass) {
		var paquete = javaClass.getPackageName();
		if (!paquete.startsWith(BASE)) {
			return Optional.empty();
		}
		var partes = paquete.substring(BASE.length()).split("\\.");
		if (partes.length < 3 || partes[0].equals("shared") || !CAPAS.contains(partes[2])) {
			return Optional.empty();
		}
		return Optional.of(partes[0] + "." + partes[1]);
	}

	static class SoloDominioYPuertosDeOtraFeature extends ArchCondition<JavaClass> {

		SoloDominioYPuertosDeOtraFeature() {
			super("usar de otra feature solo su domain y application.port");
		}

		@Override
		public void check(JavaClass origen, ConditionEvents events) {
			var featureOrigen = feature(origen);
			if (featureOrigen.isEmpty()) {
				return;
			}
			for (var dependencia : origen.getDirectDependenciesFromSelf()) {
				var destino = dependencia.getTargetClass();
				var featureDestino = feature(destino);
				if (featureDestino.isEmpty() || featureDestino.equals(featureOrigen)) {
					continue;
				}
				var paquete = destino.getPackageName();
				var permitido = paquete.contains(".domain") || paquete.contains(".application.port.");
				if (!permitido) {
					events.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
				}
			}
		}

	}

	static class SiguenElEsquemaPorFeature extends ArchCondition<JavaClass> {

		SiguenElEsquemaPorFeature() {
			super("seguir el esquema modulo/feature/{domain,application,infrastructure}");
		}

		@Override
		public void check(JavaClass clase, ConditionEvents events) {
			if (feature(clase).isPresent() || clase.getSimpleName().equals("package-info")) {
				return;
			}
			// Los eventos públicos del módulo viven en modulo/events (contrato con otros módulos y Kafka)
			var partes = clase.getPackageName().substring(BASE.length()).split("\\.");
			if (partes.length == 2 && partes[1].equals("events")) {
				return;
			}
			events.add(SimpleConditionEvent.violated(clase,
					clase.getName() + " no está en modulo/feature/{domain,application,infrastructure}"));
		}

	}

}
