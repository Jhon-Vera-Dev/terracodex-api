package com.terracodex.campo.recomendacion.application.dto.result;

import java.util.List;

import com.terracodex.campo.recomendacion.domain.model.CultivoRecomendado;
import com.terracodex.campo.recomendacion.domain.model.Recomendacion;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;

public record RecomendacionResult(double latitud, double longitud, TipoRiego tipoRiego, Zona zona,
		List<CultivoRecomendado> cultivos, String periodoReferencia, double errorMedio) {

	public static RecomendacionResult from(Recomendacion r) {
		return new RecomendacionResult(r.coordenada().latitud(), r.coordenada().longitud(), r.tipoRiego(), r.zona(),
				r.cultivos(), r.periodoReferencia(), r.errorMedio());
	}

}
