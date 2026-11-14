package com.terracodex.seguridad.usuario.application.port.input;

import com.terracodex.seguridad.usuario.application.dto.query.BuscarUsuariosQuery;
import com.terracodex.seguridad.usuario.application.dto.result.UsuarioResult;
import com.terracodex.shared.application.pagination.PageResult;

public interface BuscarUsuariosUseCase {

	PageResult<UsuarioResult> buscar(BuscarUsuariosQuery query);

}
