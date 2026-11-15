package com.terracodex.campo.registro.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.campo.registro.application.dto.query.BuscarRegistrosCampoQuery;
import com.terracodex.campo.registro.application.dto.result.RegistroCampoResult;
import com.terracodex.campo.registro.application.port.input.BuscarRegistrosCampoUseCase;
import com.terracodex.campo.registro.application.port.output.RegistroCampoRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.application.pagination.PageResult;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class BuscarRegistrosCampoService implements BuscarRegistrosCampoUseCase {

	private final RegistroCampoRepositoryPort registros;

	BuscarRegistrosCampoService(RegistroCampoRepositoryPort registros) {
		this.registros = registros;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<RegistroCampoResult> buscar(BuscarRegistrosCampoQuery query) {
		if (query.todos() && !query.supervisa()) {
			throw new AccesoDenegadoException("Solo un supervisor puede ver los registros de todos los usuarios");
		}
		var idUsuario = query.todos() ? null : query.solicitante();
		return registros.buscar(idUsuario, query.estado(), query.page()).map(RegistroCampoResult::from);
	}

	@Override
	@Transactional(readOnly = true)
	public RegistroCampoResult obtener(Long id, Long solicitante, boolean supervisa) {
		return registros.findById(id)
			.filter(r -> r.visiblePara(solicitante, supervisa))
			.map(RegistroCampoResult::from)
			.orElseThrow(() -> new NotFoundException("El registro", id));
	}

}
