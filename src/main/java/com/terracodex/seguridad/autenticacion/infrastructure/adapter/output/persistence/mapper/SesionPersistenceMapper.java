package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.mapper;

import com.terracodex.seguridad.autenticacion.domain.model.Sesion;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.SesionEntity;

public final class SesionPersistenceMapper {

	private SesionPersistenceMapper() {
	}

	public static SesionEntity toEntity(Sesion s) {
		return new SesionEntity(s.id(), s.usuarioId(), s.canal(), s.refreshHash(), s.creadaEn(), s.expiraEn(),
				s.revocadaEn());
	}

	public static Sesion toDomain(SesionEntity e) {
		return Sesion.reconstruir(e.getId(), e.getUsuarioId(), e.getCanal(), e.getRefreshHash(), e.getCreadaEn(),
				e.getExpiraEn(), e.getRevocadaEn());
	}

}
