package com.terracodex.seguridad.rol.application.port.input;

/** Borrado lógico: el registro nunca se elimina; se desactiva ({@code estado = false}) o se vuelve a activar. */
public interface CambiarEstadoRolUseCase {

	void cambiarEstado(Long id, boolean estado);

}
