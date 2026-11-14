package com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.request;

import com.terracodex.seguridad.rol.application.dto.command.ActualizarRolCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarRolRequest(@NotBlank @Size(max = 60) String descripcion, @NotNull Boolean estado) {

	public ActualizarRolCommand toCommand(Long id) {
		return new ActualizarRolCommand(id, descripcion, estado);
	}

}
