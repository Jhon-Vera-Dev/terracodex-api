package com.terracodex.shared.infrastructure.adapter.input.rest;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de todas las respuestas del API, de éxito y de error:
 * <pre>
 * { "ok": true,  "status": 200, "message": "Usuario creado", "data": { ... }, "timestamp": "..." }
 * { "ok": false, "status": 400, "message": "Hay datos inválidos", "errors": [{ "field": "login", "message": "..." }] }
 * </pre>
 * En los listados, {@code data} es un {@link PageResponse}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean ok, int status, String message, T data, List<ApiError> errors,
		Instant timestamp) {

	public static <T> ApiResponse<T> ok(T data) {
		return ok("Operación exitosa", data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, HttpStatus.OK.value(), message, data, null, Instant.now());
	}

	public static ApiResponse<Void> ok(String message) {
		return ok(message, null);
	}

	/** Usar con {@code @ResponseStatus(HttpStatus.CREATED)} en el controlador. */
	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(true, HttpStatus.CREATED.value(), message, data, null, Instant.now());
	}

	public static ApiResponse<Void> error(HttpStatus status, String message) {
		return error(status, message, null);
	}

	public static ApiResponse<Void> error(HttpStatus status, String message, List<ApiError> errors) {
		return new ApiResponse<>(false, status.value(), message, null, errors, Instant.now());
	}

}
