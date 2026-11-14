package com.terracodex.seguridad.autenticacion.application.port.input;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarSesionCommand;
import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;

public interface IniciarSesionUseCase {

	SesionResult iniciar(IniciarSesionCommand command);

}
