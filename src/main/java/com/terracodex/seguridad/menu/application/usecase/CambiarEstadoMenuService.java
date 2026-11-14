package com.terracodex.seguridad.menu.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.menu.application.port.input.CambiarEstadoMenuUseCase;
import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class CambiarEstadoMenuService implements CambiarEstadoMenuUseCase {

	private final MenuRepositoryPort repositorio;

	CambiarEstadoMenuService(MenuRepositoryPort repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public void cambiarEstado(Long id, boolean estado) {
		var registro = repositorio.findById(id).orElseThrow(() -> new NotFoundException("Menú", id));
		registro.cambiarEstado(estado);
		repositorio.save(registro);
	}

}
