package com.terracodex.seguridad.menu.domain.model;

import java.util.Set;

/** Campos editables de un menú. Evita constructores con diez parámetros sueltos. */
public record DatosMenu(String descripcion, String path, String icon, int orden, Long padreId, boolean isPadre,
		boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {
}
