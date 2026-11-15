package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;
import java.time.Duration;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarLoginGoogleCommand;
import com.terracodex.seguridad.autenticacion.application.dto.result.InicioGoogleResult;
import com.terracodex.seguridad.autenticacion.application.port.input.IniciarLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.InicioGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.application.port.output.ProveedorGooglePort;
import com.terracodex.seguridad.autenticacion.domain.model.InicioGoogle;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

@UseCase
class IniciarLoginGoogleService implements IniciarLoginGoogleUseCase {

	/** Tiempo para elegir la cuenta en Google y volver. */
	static final Duration VIGENCIA = Duration.ofMinutes(10);

	private final ProveedorGooglePort google;

	private final InicioGoogleRepositoryPort inicios;

	private final Clock clock;

	IniciarLoginGoogleService(ProveedorGooglePort google, InicioGoogleRepositoryPort inicios, Clock clock) {
		this.google = google;
		this.inicios = inicios;
		this.clock = clock;
	}

	@Override
	@Transactional
	public InicioGoogleResult iniciar(IniciarLoginGoogleCommand command) {
		if (!google.configurado()) {
			throw new ServicioNoDisponibleException("El inicio de sesión con Google no está configurado");
		}
		var inicio = InicioGoogle.crear(command.desafio(), command.canal(), clock.instant(), VIGENCIA);
		inicios.save(inicio);
		return new InicioGoogleResult(inicio.id(), google.urlAutorizacion(inicio.id()), inicio.expiraEn());
	}

}
