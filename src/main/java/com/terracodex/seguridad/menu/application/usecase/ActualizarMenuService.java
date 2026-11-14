package com.terracodex.seguridad.menu.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.menu.application.dto.command.GuardarMenuCommand;
import com.terracodex.seguridad.menu.application.port.input.ActualizarMenuUseCase;
import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class ActualizarMenuService implements ActualizarMenuUseCase {

	private final MenuRepositoryPort menus;

	private final RolRepositoryPort roles;

	ActualizarMenuService(MenuRepositoryPort menus, RolRepositoryPort roles) {
		this.menus = menus;
		this.roles = roles;
	}

	@Override
	@Transactional
	public void actualizar(Long id, GuardarMenuCommand command) {
		var menu = menus.findById(id).orElseThrow(() -> new NotFoundException("Menú", id));
		var datos = command.toDatos();
		menus.verificarPadre(datos.padreId());
		roles.verificarExistentes(datos.rolIds());
		menu.actualizar(datos);
		menus.save(menu);
	}

}
