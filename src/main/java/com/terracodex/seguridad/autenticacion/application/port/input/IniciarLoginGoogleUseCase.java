package com.terracodex.seguridad.autenticacion.application.port.input;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarLoginGoogleCommand;
import com.terracodex.seguridad.autenticacion.application.dto.result.InicioGoogleResult;

public interface IniciarLoginGoogleUseCase {

	InicioGoogleResult iniciar(IniciarLoginGoogleCommand command);

}
