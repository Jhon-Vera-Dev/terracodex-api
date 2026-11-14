package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.port.input.CerrarSesionUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.RefreshToken;
import com.terracodex.shared.application.UseCase;

@UseCase
class CerrarSesionService implements CerrarSesionUseCase {

	private final SesionRepositoryPort sesiones;

	private final Clock clock;

	CerrarSesionService(SesionRepositoryPort sesiones, Clock clock) {
		this.sesiones = sesiones;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void cerrar(String refreshToken) {
		sesiones.findByRefreshHash(RefreshToken.hashDe(refreshToken)).ifPresent(sesion -> {
			sesion.revocar(clock.instant());
			sesiones.save(sesion);
		});
	}

}
