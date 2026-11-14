package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
