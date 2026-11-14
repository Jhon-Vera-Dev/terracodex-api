package com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.mapper;

import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.entity.UsuarioEntity;

public final class UsuarioPersistenceMapper {

	private UsuarioPersistenceMapper() {
	}

	public static UsuarioEntity toEntity(Usuario u) {
		return new UsuarioEntity(u.id(), u.login(), u.descripcion(), u.contrasenaCifrada(), u.avatar(), u.isWeb(),
				u.isMovil(), u.estado(), u.rolIds());
	}

	public static Usuario toDomain(UsuarioEntity e) {
		return Usuario.reconstruir(e.getId(), e.getLogin(), e.getDescripcion(), e.getContrasena(), e.getAvatar(),
				e.isWeb(), e.isMovil(), e.isEstado(), e.getRolIds());
	}

}
