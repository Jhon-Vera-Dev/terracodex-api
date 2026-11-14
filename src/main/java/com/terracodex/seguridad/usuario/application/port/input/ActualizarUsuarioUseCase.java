package com.terracodex.seguridad.usuario.application.port.input;

import com.terracodex.seguridad.usuario.application.dto.command.ActualizarUsuarioCommand;

public interface ActualizarUsuarioUseCase {

	void actualizar(ActualizarUsuarioCommand command);

}
