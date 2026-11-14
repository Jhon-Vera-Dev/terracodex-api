package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.security;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.shared.application.VerificadorSesion;

/**
 * Implementa el puerto que shared consulta al validar cada JWT. Una consulta por clave primaria en cada petición
 * autenticada: es el precio de poder revocar al instante.
 */
@Component
class VerificadorSesionAdapter implements VerificadorSesion {

	private final SesionRepositoryPort sesiones;

	private final Clock clock;

	VerificadorSesionAdapter(SesionRepositoryPort sesiones, Clock clock) {
		this.sesiones = sesiones;
		this.clock = clock;
	}

	@Override
	public boolean estaActiva(String sesionId) {
		UUID id;
		try {
			id = UUID.fromString(sesionId);
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			return false;
		}
		return sesiones.findById(id).map(sesion -> sesion.activaEn(clock.instant())).orElse(false);
	}

}
