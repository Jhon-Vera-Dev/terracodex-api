package com.terracodex.seguridad.menu.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.menu.application.dto.command.GuardarMenuCommand;
import com.terracodex.seguridad.menu.application.port.input.RegistrarMenuUseCase;
import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.seguridad.menu.domain.model.Menu;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.shared.application.UseCase;

@UseCase
class RegistrarMenuService implements RegistrarMenuUseCase {

	private final MenuRepositoryPort menus;

	private final RolRepositoryPort roles;

	RegistrarMenuService(MenuRepositoryPort menus, RolRepositoryPort roles) {
		this.menus = menus;
		this.roles = roles;
	}

	@Override
	@Transactional
	public Long registrar(GuardarMenuCommand command) {
		var datos = command.toDatos();
		menus.verificarPadre(datos.padreId());
		roles.verificarExistentes(datos.rolIds());
		return menus.save(Menu.crear(datos));
	}

}
