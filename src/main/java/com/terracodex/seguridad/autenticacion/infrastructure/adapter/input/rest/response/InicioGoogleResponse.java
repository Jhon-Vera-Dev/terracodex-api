package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response;

import java.time.Instant;

import com.terracodex.seguridad.autenticacion.application.dto.result.InicioGoogleResult;

public record InicioGoogleResponse(String id, String url, Instant expiraEn) {

	public static InicioGoogleResponse from(InicioGoogleResult r) {
		return new InicioGoogleResponse(r.id(), r.url(), r.expiraEn());
	}

}
