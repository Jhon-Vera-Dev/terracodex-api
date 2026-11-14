package com.terracodex.seguridad.menu.application.port.input;

/** Borrado lógico: el registro nunca se elimina; se desactiva ({@code estado = false}) o se vuelve a activar. */
public interface CambiarEstadoMenuUseCase {

	void cambiarEstado(Long id, boolean estado);

}
