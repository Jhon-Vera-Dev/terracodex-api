package com.terracodex.campo.registro.application.port.input;

import com.terracodex.campo.registro.application.dto.command.RegistrarCampoCommand;

public interface RegistrarCampoUseCase {

	/** @return id del registro creado */
	Long registrar(RegistrarCampoCommand command);

}
