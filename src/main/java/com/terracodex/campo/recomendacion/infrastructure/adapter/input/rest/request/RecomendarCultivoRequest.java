package com.terracodex.campo.recomendacion.infrastructure.adapter.input.rest.request;

import com.terracodex.campo.recomendacion.application.dto.query.RecomendarCultivoQuery;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;

import jakarta.validation.constraints.NotNull;

public record RecomendarCultivoRequest(@NotNull Double latitud, @NotNull Double longitud, TipoRiego tipoRiego) {

	public RecomendarCultivoQuery toQuery() {
		return new RecomendarCultivoQuery(latitud, longitud, tipoRiego == null ? TipoRiego.SECANO : tipoRiego);
	}

}
