package com.terracodex.campo.registro.application.dto.command;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;

public record RegistrarCampoCommand(Long idUsuario, double latitud, double longitud, Double precisionMetros,
		Zona zona, TipoRiego tipoRiego, String cultivoCodigo, String cultivoDescripcion, Double puntaje,
		boolean sugerido, String observacion) {
}
