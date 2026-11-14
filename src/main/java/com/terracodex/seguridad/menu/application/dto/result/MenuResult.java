package com.terracodex.seguridad.menu.application.dto.result;

import java.util.Set;

import com.terracodex.seguridad.menu.domain.model.Menu;

public record MenuResult(Long id, String descripcion, String path, String icon, int orden, Long padreId,
		boolean isPadre, boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {

	public static MenuResult from(Menu menu) {
		var d = menu.datos();
		return new MenuResult(menu.id(), d.descripcion(), d.path(), d.icon(), d.orden(), d.padreId(), d.isPadre(),
				d.isWeb(), d.isMovil(), d.estado(), d.rolIds());
	}

}
