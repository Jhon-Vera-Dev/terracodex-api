package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarSesionCommand;
import com.terracodex.seguridad.usuario.domain.model.Canal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotBlank String login, @NotBlank String contrasena, @NotNull Canal canal) {

	public IniciarSesionCommand toCommand() {
		return new IniciarSesionCommand(login, contrasena, canal);
	}

}
