package com.terracodex.seguridad.rol.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.dto.command.ActualizarRolCommand;
import com.terracodex.seguridad.rol.application.port.input.ActualizarRolUseCase;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NotFoundException;

@UseCase
class ActualizarRolService implements ActualizarRolUseCase {

	private final RolRepositoryPort roles;

	ActualizarRolService(RolRepositoryPort roles) {
		this.roles = roles;
	}

	@Override
	@Transactional
	public void actualizar(ActualizarRolCommand command) {
		var rol = roles.findById(command.id()).orElseThrow(() -> new NotFoundException("Rol", command.id()));
		var descripcion = Rol.normalizar(command.descripcion());
		roles.findByDescripcion(descripcion).filter(otro -> !otro.id().equals(rol.id())).ifPresent(otro -> {
			throw new DomainException("Ya existe el rol " + descripcion);
		});
		rol.actualizar(descripcion, command.estado());
		roles.save(rol);
	}

}
