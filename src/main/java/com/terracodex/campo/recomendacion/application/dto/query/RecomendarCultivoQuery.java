package com.terracodex.campo.recomendacion.application.dto.query;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;

public record RecomendarCultivoQuery(double latitud, double longitud, TipoRiego tipoRiego) {
}
