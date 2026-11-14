package com.terracodex.shared.domain;

import java.util.ArrayList;
import java.util.List;

/** Raíz de agregado: acumula los eventos que produce hasta que el caso de uso los publica. */
public abstract class AggregateRoot {

	private final List<DomainEvent> events = new ArrayList<>();

	protected void raise(DomainEvent event) {
		events.add(event);
	}

	public List<DomainEvent> pullEvents() {
		var pending = List.copyOf(events);
		events.clear();
		return pending;
	}

}
