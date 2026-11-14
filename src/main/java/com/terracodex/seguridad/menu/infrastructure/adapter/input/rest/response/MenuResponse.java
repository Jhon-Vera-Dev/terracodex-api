package com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.response;

import java.util.Set;

import com.terracodex.seguridad.menu.application.dto.result.MenuResult;

public record MenuResponse(Long id, String descripcion, String path, String icon, int orden, Long padreId,
		boolean isPadre, boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {

	public static MenuResponse from(MenuResult m) {
		return new MenuResponse(m.id(), m.descripcion(), m.path(), m.icon(), m.orden(), m.padreId(), m.isPadre(),
				m.isWeb(), m.isMovil(), m.estado(), m.rolIds());
	}

}
