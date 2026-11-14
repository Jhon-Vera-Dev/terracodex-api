package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request;

import java.util.UUID;

import com.terracodex.seguridad.autenticacion.application.dto.command.CambiarContrasenaCommand;

import jakarta.validation.constraints.NotBlank;

public record CambiarContrasenaRequest(@NotBlank String actual, @NotBlank String nueva) {

	public CambiarContrasenaCommand toCommand(Long usuarioId, UUID sesionActual) {
		return new CambiarContrasenaCommand(usuarioId, sesionActual, actual, nueva);
	}

}
