package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity.InicioGoogleEntity;

public interface InicioGoogleJpaRepository extends JpaRepository<InicioGoogleEntity, String> {

}
