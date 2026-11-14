package com.terracodex.seguridad.usuario.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.usuario.application.port.input.CambiarEstadoUsuarioUseCase;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.DomainEventPublisher;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class CambiarEstadoUsuarioService implements CambiarEstadoUsuarioUseCase {

	private final UsuarioRepositoryPort repositorio;

	private final DomainEventPublisher events;

	private final Clock clock;

	CambiarEstadoUsuarioService(UsuarioRepositoryPort repositorio, DomainEventPublisher events, Clock clock) {
		this.repositorio = repositorio;
		this.events = events;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void cambiarEstado(Long id, boolean estado, Long solicitanteId) {
		if (!estado && id.equals(solicitanteId)) {
			throw new DomainException("No puedes desactivar tu propio usuario");
		}
		var usuario = repositorio.findById(id).orElseThrow(() -> new NotFoundException("Usuario", id));
		usuario.cambiarEstado(estado, clock.instant());
		repositorio.save(usuario);
		events.publish(usuario.pullEvents());
	}

}
