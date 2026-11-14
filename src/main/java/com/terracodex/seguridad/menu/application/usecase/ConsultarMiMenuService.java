package com.terracodex.seguridad.menu.application.usecase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.menu.application.dto.query.ConsultarMiMenuQuery;
import com.terracodex.seguridad.menu.application.dto.result.NodoMenuResult;
import com.terracodex.seguridad.menu.application.port.input.ConsultarMiMenuUseCase;
import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.seguridad.menu.domain.model.Menu;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.NotFoundException;

/** Lee los roles de la base, no del token: un cambio de roles se ve sin volver a iniciar sesión. */
@UseCase
class ConsultarMiMenuService implements ConsultarMiMenuUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final MenuRepositoryPort menus;

	ConsultarMiMenuService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles, MenuRepositoryPort menus) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.menus = menus;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NodoMenuResult> consultar(ConsultarMiMenuQuery query) {
		var usuario = usuarios.findById(query.usuarioId())
			.orElseThrow(() -> new NotFoundException("Usuario", query.usuarioId()));
		if (!usuario.estado()) {
			throw new AccesoDenegadoException("El usuario está inactivo");
		}
		var rolesActivos = roles.findAllById(usuario.rolIds())
			.stream()
			.filter(Rol::estado)
			.map(Rol::id)
			.collect(Collectors.toSet());

		var visibles = menus.findAll().stream().filter(m -> m.visiblePara(query.canal(), rolesActivos)).toList();
		var hijosPorPadre = visibles.stream()
			.filter(m -> !m.isPadre() && m.datos().padreId() != null)
			.collect(Collectors.groupingBy(m -> m.datos().padreId(), LinkedHashMap::new, Collectors.toList()));

		return visibles.stream()
			.filter(m -> m.isPadre() || m.datos().padreId() == null)
			.map(m -> nodo(m, hijosPorPadre.getOrDefault(m.id(), List.of())))
			// un padre sin opciones visibles no se muestra
			.filter(n -> n.path() != null || !n.hijos().isEmpty())
			.toList();
	}

	private static NodoMenuResult nodo(Menu menu, List<Menu> hijos) {
		var d = menu.datos();
		return new NodoMenuResult(menu.id(), d.descripcion(), d.path(), d.icon(), d.isPadre(),
				hijos.stream().map(h -> nodo(h, List.of())).toList());
	}

}
