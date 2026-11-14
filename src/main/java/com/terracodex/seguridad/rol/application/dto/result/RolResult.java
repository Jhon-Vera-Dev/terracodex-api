package com.terracodex.seguridad.rol.application.dto.result;

import com.terracodex.seguridad.rol.domain.model.Rol;

/** Lo que devuelven los casos de uso: el adaptador de entrada no ve el modelo de dominio. */
public record RolResult(Long id, String descripcion, boolean estado) {

	public static RolResult from(Rol rol) {
		return new RolResult(rol.id(), rol.descripcion(), rol.estado());
	}

}
