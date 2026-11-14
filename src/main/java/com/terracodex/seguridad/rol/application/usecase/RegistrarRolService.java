package com.terracodex.seguridad.rol.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.dto.command.RegistrarRolCommand;
import com.terracodex.seguridad.rol.application.port.input.RegistrarRolUseCase;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;

@UseCase
class RegistrarRolService implements RegistrarRolUseCase {

	private final RolRepositoryPort roles;

	RegistrarRolService(RolRepositoryPort roles) {
		this.roles = roles;
	}

	@Override
	@Transactional
	public Long registrar(RegistrarRolCommand command) {
		var rol = Rol.crear(command.descripcion());
		if (roles.findByDescripcion(rol.descripcion()).isPresent()) {
			throw new DomainException("Ya existe el rol " + rol.descripcion());
		}
		return roles.save(rol);
	}

}
