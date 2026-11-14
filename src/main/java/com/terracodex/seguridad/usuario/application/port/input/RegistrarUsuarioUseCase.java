package com.terracodex.seguridad.usuario.application.port.input;

import com.terracodex.seguridad.usuario.application.dto.command.RegistrarUsuarioCommand;

public interface RegistrarUsuarioUseCase {

	/** @return id del usuario creado */
	Long registrar(RegistrarUsuarioCommand command);

}
