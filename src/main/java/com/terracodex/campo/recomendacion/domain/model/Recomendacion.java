package com.terracodex.campo.recomendacion.domain.model;

import java.util.List;

/**
 * Resultado del modelo para un punto y un tipo de riego.
 *
 * @param cultivos ordenados de mayor a menor aptitud
 * @param periodoReferencia años del clima histórico usado (1981-2010)
 * @param errorMedio error absoluto medio del modelo en este escenario, en puntos de 0 a 100
 */
public record Recomendacion(Coordenada coordenada, TipoRiego tipoRiego, Zona zona, List<CultivoRecomendado> cultivos,
		String periodoReferencia, double errorMedio) {

	public Recomendacion {
		zona = zona == null ? Zona.DESCONOCIDA : zona;
		cultivos = List.copyOf(cultivos);
	}

}
