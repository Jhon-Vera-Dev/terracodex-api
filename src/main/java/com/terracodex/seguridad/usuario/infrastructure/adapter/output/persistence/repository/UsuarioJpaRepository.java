package com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository
		extends JpaRepository<UsuarioEntity, Long>, JpaSpecificationExecutor<UsuarioEntity> {

	Optional<UsuarioEntity> findByLogin(String login);

	boolean existsByLogin(String login);

	List<UsuarioEntity> findByEstadoTrueOrderByDescripcion();

}
