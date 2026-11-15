package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.mapper;

import com.terracodex.seguridad.autenticacion.domain.model.CuentaGoogle;
import com.terracodex.seguridad.autenticacion.domain.model.InicioGoogle;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.CuentaGoogleEntity;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.InicioGoogleEntity;

public final class GooglePersistenceMapper {

	private GooglePersistenceMapper() {
	}

	public static InicioGoogleEntity toEntity(InicioGoogle i) {
		return new InicioGoogleEntity(i.id(), i.desafio(), i.canal(), i.creadoEn(), i.expiraEn(), i.usuarioId(),
				i.error(), i.consumidoEn());
	}

	public static InicioGoogle toDomain(InicioGoogleEntity e) {
		return InicioGoogle.reconstruir(e.getId(), e.getDesafio(), e.getCanal(), e.getCreadoEn(), e.getExpiraEn(),
				e.getUsuarioId(), e.getError(), e.getConsumidoEn());
	}

	public static CuentaGoogleEntity toEntity(CuentaGoogle c) {
		return new CuentaGoogleEntity(c.googleSub(), c.usuarioId(), c.email(), c.vinculadaEn());
	}

	public static CuentaGoogle toDomain(CuentaGoogleEntity e) {
		return new CuentaGoogle(e.getGoogleSub(), e.getUsuarioId(), e.getEmail(), e.getVinculadaEn());
	}

}
