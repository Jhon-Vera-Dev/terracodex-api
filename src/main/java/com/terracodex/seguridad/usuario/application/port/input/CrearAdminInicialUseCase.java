package com.terracodex.seguridad.usuario.application.port.input;

public interface CrearAdminInicialUseCase {

	/** Crea el primer administrador solo si no hay usuarios. */
	void crearSiNoHayUsuarios();

}
