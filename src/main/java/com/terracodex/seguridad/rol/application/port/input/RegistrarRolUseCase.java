package com.terracodex.seguridad.rol.application.port.input;

import com.terracodex.seguridad.rol.application.dto.command.RegistrarRolCommand;

public interface RegistrarRolUseCase {

	/** @return id del rol creado */
	Long registrar(RegistrarRolCommand command);

}
