package com.terracodex.seguridad.autenticacion.application.port.input;

import com.terracodex.seguridad.autenticacion.application.dto.command.CambiarContrasenaCommand;

public interface CambiarContrasenaUseCase {

	/** El usuario cambia la suya; se cierran sus demás sesiones y la actual sigue abierta. */
	void cambiar(CambiarContrasenaCommand command);

}
