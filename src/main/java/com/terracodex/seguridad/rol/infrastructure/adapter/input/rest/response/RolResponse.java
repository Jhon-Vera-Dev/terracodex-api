package com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.response;

import com.terracodex.seguridad.rol.application.dto.result.RolResult;

public record RolResponse(Long id, String descripcion, boolean estado) {

	public static RolResponse from(RolResult rol) {
		return new RolResponse(rol.id(), rol.descripcion(), rol.estado());
	}

}
