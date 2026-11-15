package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.CuentaGoogleEntity;

public interface CuentaGoogleJpaRepository extends JpaRepository<CuentaGoogleEntity, String> {

}
