package com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.request;

import java.util.Set;

import com.terracodex.seguridad.menu.application.dto.command.GuardarMenuCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Un padre solo necesita descripcion, icon, orden y canales; una opción además path, padreId y rolIds.
 *
 * @param isPadre opcional, {@code false} por defecto
 * @param estado opcional al crear (activo por defecto)
 */
public record GuardarMenuRequest(@NotBlank @Size(max = 80) String descripcion, @Size(max = 200) String path,
		@Size(max = 40) String icon, Integer orden, Long padreId, Boolean isPadre, @NotNull Boolean isWeb,
		@NotNull Boolean isMovil, Boolean estado, Set<Long> rolIds) {

	public GuardarMenuCommand toCommand() {
		return new GuardarMenuCommand(descripcion, path, icon, orden == null ? 0 : orden, padreId,
				Boolean.TRUE.equals(isPadre), isWeb, isMovil, estado == null || estado, rolIds);
	}

}
