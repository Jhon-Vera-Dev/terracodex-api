package com.terracodex.campo.registro.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.registro.application.dto.command.RegistrarCampoCommand;
import com.terracodex.campo.registro.application.port.input.RegistrarCampoUseCase;
import com.terracodex.campo.registro.application.port.output.RegistroCampoRepositoryPort;
import com.terracodex.campo.registro.domain.model.Cultivo;
import com.terracodex.campo.registro.domain.model.RegistroCampo;
import com.terracodex.shared.application.UseCase;

@UseCase
class RegistrarCampoService implements RegistrarCampoUseCase {

	private final RegistroCampoRepositoryPort registros;

	private final Clock clock;

	RegistrarCampoService(RegistroCampoRepositoryPort registros, Clock clock) {
		this.registros = registros;
		this.clock = clock;
	}

	@Override
	@Transactional
	public Long registrar(RegistrarCampoCommand c) {
		var cultivo = new Cultivo(c.cultivoCodigo(), c.cultivoDescripcion(), c.puntaje(), c.sugerido());
		var registro = RegistroCampo.registrar(c.idUsuario(), new Coordenada(c.latitud(), c.longitud()),
				c.precisionMetros(), c.zona(), c.tipoRiego(), cultivo, c.observacion(), clock.instant());
		return registros.save(registro);
	}

}
