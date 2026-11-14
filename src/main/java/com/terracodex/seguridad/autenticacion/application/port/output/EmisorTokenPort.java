package com.terracodex.seguridad.autenticacion.application.port.output;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.seguridad.usuario.domain.model.Usuario;

/** Emite el access token (JWT firmado en infraestructura). */
public interface EmisorTokenPort {

	TokenEmitido emitir(Usuario usuario, List<String> roles, Canal canal, UUID sesionId);

	record TokenEmitido(String valor, Instant expiraEn) {
	}

}
