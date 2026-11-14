package com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.entity.RolEntity;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long>, JpaSpecificationExecutor<RolEntity> {

	Optional<RolEntity> findByDescripcion(String descripcion);

	List<RolEntity> findByEstadoTrueOrderByDescripcion();

}
