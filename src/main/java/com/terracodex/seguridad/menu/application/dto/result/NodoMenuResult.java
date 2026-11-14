package com.terracodex.seguridad.menu.application.dto.result;

import java.util.List;

/** Nodo del árbol de navegación: un padre con sus opciones, o una opción suelta. */
public record NodoMenuResult(Long id, String descripcion, String path, String icon, boolean isPadre,
		List<NodoMenuResult> hijos) {
}
