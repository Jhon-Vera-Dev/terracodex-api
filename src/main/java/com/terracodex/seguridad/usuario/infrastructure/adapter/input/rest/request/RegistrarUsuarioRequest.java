package com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.request;

import java.util.Set;

import com.terracodex.seguridad.usuario.application.dto.command.RegistrarUsuarioCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioRequest(@NotBlank @Size(max = 60) String login,
		@NotBlank @Size(max = 150) String descripcion, @NotBlank String contrasena, @Size(max = 500) String avatar,
		@NotNull Boolean isWeb, @NotNull Boolean isMovil, @NotEmpty Set<Long> rolIds) {

	public RegistrarUsuarioCommand toCommand() {
		return new RegistrarUsuarioCommand(login, descripcion, contrasena, avatar, isWeb, isMovil, rolIds);
	}

}
