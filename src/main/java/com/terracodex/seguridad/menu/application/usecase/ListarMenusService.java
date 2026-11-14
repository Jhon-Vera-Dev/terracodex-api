package com.terracodex.seguridad.menu.application.usecase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.menu.application.dto.result.MenuResult;
import com.terracodex.seguridad.menu.application.port.input.ListarMenusUseCase;
import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.shared.application.UseCase;

@UseCase
class ListarMenusService implements ListarMenusUseCase {

	private final MenuRepositoryPort menus;

	ListarMenusService(MenuRepositoryPort menus) {
		this.menus = menus;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MenuResult> listar() {
		return menus.findAll().stream().map(MenuResult::from).toList();
	}

}
