package com.terracodex.seguridad.menu.application.port.input;

import com.terracodex.seguridad.menu.application.dto.command.GuardarMenuCommand;

public interface ActualizarMenuUseCase {

	void actualizar(Long id, GuardarMenuCommand command);

}
