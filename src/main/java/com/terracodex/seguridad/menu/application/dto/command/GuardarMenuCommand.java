package com.terracodex.seguridad.menu.application.dto.command;

import java.util.Set;

import com.terracodex.seguridad.menu.domain.model.DatosMenu;

/** Mismos campos para registrar y actualizar un menú. */
public record GuardarMenuCommand(String descripcion, String path, String icon, int orden, Long padreId,
		boolean isPadre, boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {

	public DatosMenu toDatos() {
		return new DatosMenu(descripcion, path, icon, orden, padreId, isPadre, isWeb, isMovil, estado,
				rolIds == null ? Set.of() : rolIds);
	}

}
