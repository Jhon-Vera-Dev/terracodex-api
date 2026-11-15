package com.terracodex.campo.registro.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.terracodex.campo.registro.infrastructure.adapter.output.persistence.entity.RegistroCampoEntity;

public interface RegistroCampoJpaRepository
		extends JpaRepository<RegistroCampoEntity, Long>, JpaSpecificationExecutor<RegistroCampoEntity> {

}
