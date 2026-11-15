package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.terracodex.seguridad.autenticacion.application.port.output.CuentaGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.application.port.output.InicioGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.CuentaGoogle;
import com.terracodex.seguridad.autenticacion.domain.model.InicioGoogle;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.mapper.GooglePersistenceMapper;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository.CuentaGoogleJpaRepository;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository.InicioGoogleJpaRepository;

@Component
class GooglePersistenceAdapter implements InicioGoogleRepositoryPort, CuentaGoogleRepositoryPort {

	private final InicioGoogleJpaRepository inicios;

	private final CuentaGoogleJpaRepository cuentas;

	GooglePersistenceAdapter(InicioGoogleJpaRepository inicios, CuentaGoogleJpaRepository cuentas) {
		this.inicios = inicios;
		this.cuentas = cuentas;
	}

	@Override
	public void save(InicioGoogle inicio) {
		inicios.save(GooglePersistenceMapper.toEntity(inicio));
	}

	@Override
	public Optional<InicioGoogle> findById(String id) {
		return inicios.findById(id).map(GooglePersistenceMapper::toDomain);
	}

	@Override
	public void save(CuentaGoogle cuenta) {
		cuentas.save(GooglePersistenceMapper.toEntity(cuenta));
	}

	@Override
	public Optional<CuentaGoogle> findByGoogleSub(String googleSub) {
		return cuentas.findById(googleSub).map(GooglePersistenceMapper::toDomain);
	}

}
