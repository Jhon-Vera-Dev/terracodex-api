package com.terracodex.seguridad.autenticacion.application.port.output;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.terracodex.seguridad.autenticacion.domain.model.Sesion;

public interface SesionRepositoryPort {

	void save(Sesion sesion);

	Optional<Sesion> findById(UUID id);

	Optional<Sesion> findByRefreshHash(String refreshHash);

	List<Sesion> findActivasDeUsuario(Long usuarioId, Instant ahora);

	/** Revoca todas las sesiones activas del usuario, salvo {@code excepto} (puede ser {@code null}). */
	default int revocarTodas(Long usuarioId, UUID excepto, Instant ahora) {
		var activas = findActivasDeUsuario(usuarioId, ahora).stream().filter(s -> !s.id().equals(excepto)).toList();
		activas.forEach(sesion -> {
			sesion.revocar(ahora);
			save(sesion);
		});
		return activas.size();
	}

}
