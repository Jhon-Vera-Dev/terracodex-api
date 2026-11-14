package com.terracodex.shared.infrastructure.config.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

/**
 * Los 401 y 403 que genera Spring Security en sus filtros (antes de llegar a un controlador) no pasan por
 * {@code ApiExceptionHandler}; este manejador les da el mismo formato {@link ApiResponse}.
 */
class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final JsonMapper json;

	ApiSecurityErrorHandler(JsonMapper json) {
		this.json = json;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
			throws IOException {
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		escribir(response, HttpStatus.UNAUTHORIZED, "Tu sesión no es válida o expiró, vuelve a ingresar");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
			throws IOException {
		escribir(response, HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
	}

	private void escribir(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		json.writeValue(response.getOutputStream(), ApiResponse.error(status, message));
	}

}
