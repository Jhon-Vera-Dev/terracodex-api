package com.terracodex.campo.recomendacion.domain.model;

import com.terracodex.shared.domain.DomainException;

/** Punto WGS84 en grados decimales, como lo entrega el GPS del celular. */
public record Coordenada(double latitud, double longitud) {

	public Coordenada {
		if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90) {
			throw new DomainException("La latitud debe estar entre -90 y 90");
		}
		if (!Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
			throw new DomainException("La longitud debe estar entre -180 y 180");
		}
	}

}
