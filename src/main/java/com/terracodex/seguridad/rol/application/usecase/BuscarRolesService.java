package com.terracodex.seguridad.rol.application.usecase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.dto.query.BuscarRolesQuery;
import com.terracodex.seguridad.rol.application.dto.result.RolResult;
import com.terracodex.seguridad.rol.application.port.input.BuscarRolesUseCase;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.application.pagination.PageResult;

@UseCase
class BuscarRolesService implements BuscarRolesUseCase {

	private final RolRepositoryPort roles;

	BuscarRolesService(RolRepositoryPort roles) {
		this.roles = roles;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<RolResult> buscar(BuscarRolesQuery query) {
		return roles.buscar(query.estado(), query.page()).map(RolResult::from);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RolResult> listarActivos() {
		return roles.findActivos().stream().map(RolResult::from).toList();
	}

}
