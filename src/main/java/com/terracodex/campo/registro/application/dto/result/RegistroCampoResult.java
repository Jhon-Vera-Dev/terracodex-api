package com.terracodex.campo.registro.application.dto.result;

import java.time.Instant;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.campo.registro.domain.model.RegistroCampo;

public record RegistroCampoResult(Long id, Long idUsuario, double latitud, double longitud, Double precisionMetros,
		Zona zona, TipoRiego tipoRiego, String cultivoCodigo, String cultivoDescripcion, Double puntaje,
		boolean sugerido, String observacion, Instant fechaRegistro, boolean estado) {

	public static RegistroCampoResult from(RegistroCampo r) {
		return new RegistroCampoResult(r.id(), r.idUsuario(), r.coordenada().latitud(), r.coordenada().longitud(),
				r.precisionMetros(), r.zona(), r.tipoRiego(), r.cultivo().codigo(), r.cultivo().descripcion(),
				r.cultivo().puntaje(), r.cultivo().sugerido(), r.observacion(), r.fechaRegistro(), r.estado());
	}

}
