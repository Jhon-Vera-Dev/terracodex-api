package com.terracodex.seguridad.autenticacion.application.port.output;

import java.util.Optional;

import com.terracodex.seguridad.autenticacion.domain.model.InicioGoogle;

public interface InicioGoogleRepositoryPort {

	void save(InicioGoogle inicio);

	Optional<InicioGoogle> findById(String id);

}
