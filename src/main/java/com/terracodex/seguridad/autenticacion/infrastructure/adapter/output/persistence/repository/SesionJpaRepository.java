package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.SesionEntity;

public interface SesionJpaRepository extends JpaRepository<SesionEntity, UUID> {

	Optional<SesionEntity> findByRefreshHash(String refreshHash);

	List<SesionEntity> findByUsuarioIdAndRevocadaEnIsNullAndExpiraEnAfter(Long usuarioId, Instant ahora);

}
