package com.terracodex.seguridad.rol.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.port.input.CambiarEstadoRolUseCase;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class CambiarEstadoRolService implements CambiarEstadoRolUseCase {

	private final RolRepositoryPort repositorio;

	CambiarEstadoRolService(RolRepositoryPort repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public void cambiarEstado(Long id, boolean estado) {
		var registro = repositorio.findById(id).orElseThrow(() -> new NotFoundException("Rol", id));
		registro.cambiarEstado(estado);
		repositorio.save(registro);
	}

}
