package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CanjeGoogleRequest(@NotBlank @Size(max = 64) String id, @NotBlank @Size(max = 128) String verificador) {
}
