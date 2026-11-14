package com.terracodex.seguridad.usuario.application.usecase;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.usuario.application.dto.query.BuscarUsuariosQuery;
import com.terracodex.seguridad.usuario.application.dto.result.UsuarioResult;
import com.terracodex.seguridad.usuario.application.dto.result.UsuarioResult.RolAsignado;
import com.terracodex.seguridad.usuario.application.port.input.BuscarUsuariosUseCase;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.application.pagination.PageResult;

@UseCase
class BuscarUsuariosService implements BuscarUsuariosUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	BuscarUsuariosService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles) {
		this.usuarios = usuarios;
		this.roles = roles;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<UsuarioResult> buscar(BuscarUsuariosQuery query) {
		var pagina = usuarios.buscar(query.estado(), query.page());
		// Una sola consulta de roles para toda la página
		var idsDeRoles = pagina.content().stream().flatMap(u -> u.rolIds().stream()).collect(Collectors.toSet());
		var rolesPorId = roles.findAllById(idsDeRoles)
			.stream()
			.collect(Collectors.toMap(Rol::id, Function.identity()));
		return pagina.map(u -> new UsuarioResult(u.id(), u.login(), u.descripcion(), u.avatar(), u.isWeb(),
				u.isMovil(), u.estado(),
				u.rolIds()
					.stream()
					.map(rolesPorId::get)
					.map(r -> new RolAsignado(r.id(), r.descripcion()))
					.sorted(Comparator.comparing(RolAsignado::descripcion))
					.toList()));
	}

}
