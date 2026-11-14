package com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.response;

import java.util.List;

import com.terracodex.seguridad.menu.application.dto.result.NodoMenuResult;

public record NodoMenuResponse(Long id, String descripcion, String path, String icon, boolean isPadre,
		List<NodoMenuResponse> hijos) {

	public static NodoMenuResponse from(NodoMenuResult n) {
		return new NodoMenuResponse(n.id(), n.descripcion(), n.path(), n.icon(), n.isPadre(),
				n.hijos().stream().map(NodoMenuResponse::from).toList());
	}

}
