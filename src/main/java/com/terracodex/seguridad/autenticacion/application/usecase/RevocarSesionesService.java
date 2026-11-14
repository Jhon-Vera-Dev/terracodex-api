package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.port.input.RevocarSesionesUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class RevocarSesionesService implements RevocarSesionesUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final SesionRepositoryPort sesiones;

	private final Clock clock;

	RevocarSesionesService(UsuarioRepositoryPort usuarios, SesionRepositoryPort sesiones, Clock clock) {
		this.usuarios = usuarios;
		this.sesiones = sesiones;
		this.clock = clock;
	}

	@Override
	@Transactional
	public int revocarTodas(Long usuarioId) {
		if (usuarios.findById(usuarioId).isEmpty()) {
			throw new NotFoundException("Usuario", usuarioId);
		}
		return sesiones.revocarTodas(usuarioId, null, clock.instant());
	}

}
