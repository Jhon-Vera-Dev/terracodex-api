package com.terracodex.campo.recomendacion.application.port.output;

import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.recomendacion.domain.model.Recomendacion;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;

/** Modelo de aptitud de cultivos (servicio-ml). */
public interface RecomendadorCultivoPort {

	/** @throws com.terracodex.shared.domain.ServicioNoDisponibleException si el servicio no responde */
	Recomendacion recomendar(Coordenada coordenada, TipoRiego tipoRiego);

}
