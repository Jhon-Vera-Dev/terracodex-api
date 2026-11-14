package com.terracodex.seguridad.rol.application.port.input;

import java.util.List;

import com.terracodex.seguridad.rol.application.dto.query.BuscarRolesQuery;
import com.terracodex.seguridad.rol.application.dto.result.RolResult;
import com.terracodex.shared.application.pagination.PageResult;

public interface BuscarRolesUseCase {

	PageResult<RolResult> buscar(BuscarRolesQuery query);

	/** Roles activos, sin paginar: para listas desplegables. */
	List<RolResult> listarActivos();

}
