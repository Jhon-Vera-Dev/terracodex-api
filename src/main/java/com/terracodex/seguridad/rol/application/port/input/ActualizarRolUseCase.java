package com.terracodex.seguridad.rol.application.port.input;

import com.terracodex.seguridad.rol.application.dto.command.ActualizarRolCommand;

public interface ActualizarRolUseCase {

	void actualizar(ActualizarRolCommand command);

}
