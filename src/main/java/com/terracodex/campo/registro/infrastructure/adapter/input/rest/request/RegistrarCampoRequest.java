package com.terracodex.campo.registro.infrastructure.adapter.input.rest.request;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.campo.registro.application.dto.command.RegistrarCampoCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * {@code POST /api/campo/registros}. La zona y el puntaje son los que devolvió
 * {@code POST /api/campo/recomendaciones}; si no se consultó, pueden ir vacíos.
 */
public record RegistrarCampoRequest(@NotNull Double latitud, @NotNull Double longitud, Double precisionMetros,
		@Valid ZonaRequest zona, @NotNull TipoRiego tipoRiego, @Size(max = 20) String cultivoCodigo,
		@NotBlank @Size(max = 100) String cultivoDescripcion, Double puntaje, Boolean sugerido,
		@Size(max = 500) String observacion) {

	public RegistrarCampoCommand toCommand(Long idUsuario) {
		var z = zona == null ? Zona.DESCONOCIDA : zona.toZona();
		return new RegistrarCampoCommand(idUsuario, latitud, longitud, precisionMetros, z, tipoRiego, cultivoCodigo,
				cultivoDescripcion, puntaje, Boolean.TRUE.equals(sugerido), observacion);
	}

	public record ZonaRequest(@Size(max = 200) String descripcion, @Size(max = 100) String distrito,
			@Size(max = 100) String provincia, @Size(max = 100) String departamento, @Size(max = 100) String pais) {

		Zona toZona() {
			return new Zona(descripcion, distrito, provincia, departamento, pais);
		}

	}

}
