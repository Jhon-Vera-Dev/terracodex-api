package com.terracodex.seguridad.menu.application.port.input;

import com.terracodex.seguridad.menu.application.dto.command.GuardarMenuCommand;

public interface RegistrarMenuUseCase {

	/** @return id del menú creado */
	Long registrar(GuardarMenuCommand command);

}
