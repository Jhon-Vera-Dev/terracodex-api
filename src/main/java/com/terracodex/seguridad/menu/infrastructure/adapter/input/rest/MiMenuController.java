package com.terracodex.seguridad.menu.infrastructure.adapter.input.rest;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.menu.application.dto.query.ConsultarMiMenuQuery;
import com.terracodex.seguridad.menu.application.port.input.ConsultarMiMenuUseCase;
import com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.response.NodoMenuResponse;
import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

/** Menú del usuario autenticado. Cualquier usuario con sesión puede pedir el suyo. */
@RestController
class MiMenuController {

	private final ConsultarMiMenuUseCase consultar;

	MiMenuController(ConsultarMiMenuUseCase consultar) {
		this.consultar = consultar;
	}

	/** @param canal si no se envía, se usa el canal con el que se inició sesión */
	@GetMapping("/api/seguridad/mi-menu")
	ApiResponse<List<NodoMenuResponse>> miMenu(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) Canal canal) {
		var canalEfectivo = canal != null ? canal : Canal.valueOf(jwt.getClaimAsString("canal"));
		var arbol = consultar.consultar(new ConsultarMiMenuQuery(Long.valueOf(jwt.getSubject()), canalEfectivo));
		return ApiResponse.ok(arbol.stream().map(NodoMenuResponse::from).toList());
	}

}
