package com.terracodex.campo.recomendacion.application.port.input;

import com.terracodex.campo.recomendacion.application.dto.query.RecomendarCultivoQuery;
import com.terracodex.campo.recomendacion.application.dto.result.RecomendacionResult;

public interface RecomendarCultivoUseCase {

	RecomendacionResult recomendar(RecomendarCultivoQuery query);

}
