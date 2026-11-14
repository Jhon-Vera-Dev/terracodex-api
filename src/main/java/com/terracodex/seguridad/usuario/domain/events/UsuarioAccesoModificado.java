package com.terracodex.seguridad.usuario.domain.events;

import java.time.Instant;

import com.terracodex.shared.domain.DomainEvent;

/**
 * Cambió el estado, los canales, los roles o la contraseña de un usuario: sus sesiones abiertas tienen el acceso
 * anterior. La feature autenticacion lo escucha y las revoca.
 */
public record UsuarioAccesoModificado(Long usuarioId, Instant occurredOn) implements DomainEvent {

	@Override
	public String aggregateId() {
		return usuarioId.toString();
	}

}
