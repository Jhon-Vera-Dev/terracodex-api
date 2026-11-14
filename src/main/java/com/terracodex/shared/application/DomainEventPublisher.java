package com.terracodex.shared.application;

import java.util.Collection;

import com.terracodex.shared.domain.DomainEvent;

/** Puerto de salida para publicar eventos. Se llama dentro de la transacción del caso de uso. */
public interface DomainEventPublisher {

	void publish(Collection<? extends DomainEvent> events);

}
