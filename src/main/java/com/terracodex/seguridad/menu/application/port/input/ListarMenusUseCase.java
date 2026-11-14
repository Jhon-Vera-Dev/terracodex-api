package com.terracodex.seguridad.menu.application.port.input;

import java.util.List;

import com.terracodex.seguridad.menu.application.dto.result.MenuResult;

/** Lista completa para el mantenimiento. No se pagina: el frontend la muestra como árbol (padres e hijos). */
public interface ListarMenusUseCase {

	List<MenuResult> listar();

}
