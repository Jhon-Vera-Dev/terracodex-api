package com.terracodex.campo.registro.infrastructure.adapter.input.rest.response;

import java.time.Instant;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.campo.registro.application.dto.result.RegistroCampoResult;

public record RegistroCampoResponse(Long id, Long idUsuario, double latitud, double longitud, Double precisionMetros,
		Zona zona, TipoRiego tipoRiego, String cultivoCodigo, String cultivoDescripcion, Double puntaje,
		boolean sugerido, String observacion, Instant fechaRegistro, boolean estado) {

	public static RegistroCampoResponse from(RegistroCampoResult r) {
		return new RegistroCampoResponse(r.id(), r.idUsuario(), r.latitud(), r.longitud(), r.precisionMetros(),
				r.zona(), r.tipoRiego(), r.cultivoCodigo(), r.cultivoDescripcion(), r.puntaje(), r.sugerido(),
				r.observacion(), r.fechaRegistro(), r.estado());
	}

}
