package com.terracodex.campo.registro.application.port.input;

import com.terracodex.campo.registro.application.dto.query.BuscarRegistrosCampoQuery;
import com.terracodex.campo.registro.application.dto.result.RegistroCampoResult;
import com.terracodex.shared.application.pagination.PageResult;

public interface BuscarRegistrosCampoUseCase {

	PageResult<RegistroCampoResult> buscar(BuscarRegistrosCampoQuery query);

	/** @throws com.terracodex.shared.domain.NotFoundException si no existe o no es visible para el solicitante */
	RegistroCampoResult obtener(Long id, Long solicitante, boolean supervisa);

}
