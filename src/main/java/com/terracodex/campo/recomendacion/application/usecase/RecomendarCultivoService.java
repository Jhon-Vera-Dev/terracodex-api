package com.terracodex.campo.recomendacion.application.usecase;

import com.terracodex.campo.recomendacion.application.dto.query.RecomendarCultivoQuery;
import com.terracodex.campo.recomendacion.application.dto.result.RecomendacionResult;
import com.terracodex.campo.recomendacion.application.port.input.RecomendarCultivoUseCase;
import com.terracodex.campo.recomendacion.application.port.output.RecomendadorCultivoPort;
import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.shared.application.UseCase;

/** Sin transacción: no toca la base de datos, solo consulta el modelo. */
@UseCase
class RecomendarCultivoService implements RecomendarCultivoUseCase {

	private final RecomendadorCultivoPort recomendador;

	RecomendarCultivoService(RecomendadorCultivoPort recomendador) {
		this.recomendador = recomendador;
	}

	@Override
	public RecomendacionResult recomendar(RecomendarCultivoQuery query) {
		var coordenada = new Coordenada(query.latitud(), query.longitud());
		return RecomendacionResult.from(recomendador.recomendar(coordenada, query.tipoRiego()));
	}

}
