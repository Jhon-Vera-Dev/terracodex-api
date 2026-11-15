package com.terracodex.campo.recomendacion.infrastructure.adapter.input.rest.response;

import java.util.List;

import com.terracodex.campo.recomendacion.application.dto.result.RecomendacionResult;
import com.terracodex.campo.recomendacion.domain.model.CultivoRecomendado;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;

public record RecomendacionResponse(double latitud, double longitud, TipoRiego tipoRiego, Zona zona,
		List<CultivoRecomendado> cultivos, String periodoReferencia, double errorMedio) {

	public static RecomendacionResponse from(RecomendacionResult r) {
		return new RecomendacionResponse(r.latitud(), r.longitud(), r.tipoRiego(), r.zona(), r.cultivos(),
				r.periodoReferencia(), r.errorMedio());
	}

}
