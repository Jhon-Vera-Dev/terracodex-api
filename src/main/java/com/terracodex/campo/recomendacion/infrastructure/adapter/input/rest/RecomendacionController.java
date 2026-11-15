package com.terracodex.campo.recomendacion.infrastructure.adapter.input.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.campo.recomendacion.application.port.input.RecomendarCultivoUseCase;
import com.terracodex.campo.recomendacion.infrastructure.adapter.input.rest.request.RecomendarCultivoRequest;
import com.terracodex.campo.recomendacion.infrastructure.adapter.input.rest.response.RecomendacionResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

import jakarta.validation.Valid;

/** Cualquier usuario con sesión. La primera consulta de un punto tarda (NASA POWER); luego queda en caché. */
@RestController
class RecomendacionController {

	private final RecomendarCultivoUseCase recomendar;

	RecomendacionController(RecomendarCultivoUseCase recomendar) {
		this.recomendar = recomendar;
	}

	/** {@code POST /api/campo/recomendaciones {"latitud": -14.06, "longitud": -75.73, "tipoRiego": "GOTEO"}} */
	@PostMapping("/api/campo/recomendaciones")
	ApiResponse<RecomendacionResponse> recomendar(@Valid @RequestBody RecomendarCultivoRequest request) {
		return ApiResponse.ok(RecomendacionResponse.from(recomendar.recomendar(request.toQuery())));
	}

}
