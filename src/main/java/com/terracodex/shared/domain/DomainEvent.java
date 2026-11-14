package com.terracodex.shared.domain;

import java.time.Instant;

/**
 * Hecho de negocio ocurrido en un agregado. Los eventos de cada módulo viven en su paquete {@code events}
 * y son su contrato público: los escuchan otras features o módulos.
 */
public interface DomainEvent {

	/** Identificador del agregado que originó el evento. Sirve como clave si algún día se externalizan (Kafka). */
	String aggregateId();

	Instant occurredOn();

}
