package com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.request;

import com.terracodex.seguridad.rol.application.dto.command.RegistrarRolCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarRolRequest(@NotBlank @Size(max = 60) String descripcion) {

	public RegistrarRolCommand toCommand() {
		return new RegistrarRolCommand(descripcion);
	}

}
