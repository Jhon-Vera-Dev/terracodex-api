package com.terracodex.shared.infrastructure.adapter.input.rest;

/** Error de un campo concreto del request, para que el frontend lo muestre junto al input. */
public record ApiError(String field, String message) {
}
