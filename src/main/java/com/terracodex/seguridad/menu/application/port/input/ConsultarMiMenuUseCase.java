package com.terracodex.seguridad.menu.application.port.input;

import java.util.List;

import com.terracodex.seguridad.menu.application.dto.query.ConsultarMiMenuQuery;
import com.terracodex.seguridad.menu.application.dto.result.NodoMenuResult;

public interface ConsultarMiMenuUseCase {

	/** Árbol de menú visible para los roles activos del usuario en el canal indicado. */
	List<NodoMenuResult> consultar(ConsultarMiMenuQuery query);

}
