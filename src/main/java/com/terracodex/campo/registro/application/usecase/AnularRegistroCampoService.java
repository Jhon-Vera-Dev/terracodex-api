package com.terracodex.campo.registro.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.campo.registro.application.port.input.AnularRegistroCampoUseCase;
import com.terracodex.campo.registro.application.port.output.RegistroCampoRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class AnularRegistroCampoService implements AnularRegistroCampoUseCase {

	private final RegistroCampoRepositoryPort registros;

	AnularRegistroCampoService(RegistroCampoRepositoryPort registros) {
		this.registros = registros;
	}

	@Override
	@Transactional
	public void anular(Long id, Long solicitante, boolean supervisa) {
		var registro = registros.findById(id)
			.filter(r -> r.visiblePara(solicitante, supervisa))
			.orElseThrow(() -> new NotFoundException("El registro", id));
		registro.anular(solicitante, supervisa);
		registros.save(registro);
	}

}
