package com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.mapper;

import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.entity.RolEntity;

public final class RolPersistenceMapper {

	private RolPersistenceMapper() {
	}

	public static RolEntity toEntity(Rol rol) {
		return new RolEntity(rol.id(), rol.descripcion(), rol.estado());
	}

	public static Rol toDomain(RolEntity entity) {
		return Rol.reconstruir(entity.getId(), entity.getDescripcion(), entity.isEstado());
	}

}
