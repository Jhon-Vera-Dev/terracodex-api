package com.terracodex.seguridad.usuario.application.port.input;

/** Borrado lógico: el registro nunca se elimina; se desactiva ({@code estado = false}) o se vuelve a activar. */
public interface CambiarEstadoUsuarioUseCase {

	void cambiarEstado(Long id, boolean estado, Long solicitanteId);

}
