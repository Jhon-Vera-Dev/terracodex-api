package com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.request;

import java.util.Set;

import com.terracodex.seguridad.usuario.application.dto.command.ActualizarUsuarioCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** @param nuevaContrasena opcional: vacía para no cambiarla */
public record ActualizarUsuarioRequest(@NotBlank @Size(max = 150) String descripcion,
		@Size(max = 500) String avatar, @NotNull Boolean isWeb, @NotNull Boolean isMovil, @NotNull Boolean estado,
		@NotEmpty Set<Long> rolIds,
		String nuevaContrasena) {

	public ActualizarUsuarioCommand toCommand(Long id, Long solicitanteId) {
		return new ActualizarUsuarioCommand(id, descripcion, avatar, isWeb, isMovil, estado, rolIds, nuevaContrasena,
				solicitanteId);
	}

}
