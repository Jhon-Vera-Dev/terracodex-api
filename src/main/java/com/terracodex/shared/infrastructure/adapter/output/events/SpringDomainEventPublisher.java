package com.terracodex.shared.infrastructure.adapter.output.events;

import java.util.Collection;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.terracodex.shared.application.DomainEventPublisher;
import com.terracodex.shared.domain.DomainEvent;

/**
 * Publica en el bus de Spring. Spring Modulith guarda cada evento en la tabla {@code event_publication}
 * en la misma transacción (outbox), lo entrega a los listeners después del commit.
 */
@Component
class SpringDomainEventPublisher implements DomainEventPublisher {

	private final ApplicationEventPublisher publisher;

	SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publish(Collection<? extends DomainEvent> events) {
		events.forEach(publisher::publishEvent);
	}

}
