package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.Sesion;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.mapper.SesionPersistenceMapper;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository.SesionJpaRepository;

@Component
class SesionPersistenceAdapter implements SesionRepositoryPort {

	private final SesionJpaRepository jpa;

	SesionPersistenceAdapter(SesionJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void save(Sesion sesion) {
		jpa.save(SesionPersistenceMapper.toEntity(sesion));
	}

	@Override
	public Optional<Sesion> findById(UUID id) {
		return jpa.findById(id).map(SesionPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Sesion> findByRefreshHash(String refreshHash) {
		return jpa.findByRefreshHash(refreshHash).map(SesionPersistenceMapper::toDomain);
	}

	@Override
	public List<Sesion> findActivasDeUsuario(Long usuarioId, Instant ahora) {
		return jpa.findByUsuarioIdAndRevocadaEnIsNullAndExpiraEnAfter(usuarioId, ahora)
			.stream()
			.map(SesionPersistenceMapper::toDomain)
			.toList();
	}

}
