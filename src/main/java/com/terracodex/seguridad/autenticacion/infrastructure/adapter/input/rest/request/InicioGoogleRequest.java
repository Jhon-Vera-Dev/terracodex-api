package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarLoginGoogleCommand;
import com.terracodex.seguridad.usuario.domain.model.Canal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** @param desafio SHA-256 en hexadecimal (minúsculas) del verificador que genera la app */
public record InicioGoogleRequest(@NotNull Canal canal,
		@NotNull @Pattern(regexp = "[0-9a-f]{64}", message = "debe ser un SHA-256 en hexadecimal") String desafio) {

	public IniciarLoginGoogleCommand toCommand() {
		return new IniciarLoginGoogleCommand(canal, desafio);
	}

}
