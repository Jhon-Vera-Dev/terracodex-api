package com.terracodex.seguridad.autenticacion.application.port.output;

import java.util.Optional;

import com.terracodex.seguridad.autenticacion.domain.model.CuentaGoogle;

public interface CuentaGoogleRepositoryPort {

	void save(CuentaGoogle cuenta);

	Optional<CuentaGoogle> findByGoogleSub(String googleSub);

}
