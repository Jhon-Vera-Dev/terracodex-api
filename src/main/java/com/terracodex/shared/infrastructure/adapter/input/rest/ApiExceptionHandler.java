package com.terracodex.shared.infrastructure.adapter.input.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NoAutenticadoException;
import com.terracodex.shared.domain.NotFoundException;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

/** Traduce cualquier excepción a {@link ApiResponse} con {@code ok: false}. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	// Negocio

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(DomainException ex) {
		return error(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(NotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(NoAutenticadoException ex) {
		return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(AccesoDenegadoException ex) {
		return error(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(ServicioNoDisponibleException ex) {
		log.warn("Servicio externo no disponible: {}", ex.getMessage(), ex.getCause());
		return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
	}

	/** {@code @PreAuthorize} sin el rol requerido. */
	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(AccessDeniedException ex) {
		return error(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
	}

	// Datos de entrada

	/** {@code @Valid} en body ({@code MethodArgumentNotValidException}) o en parámetros de consulta. */
	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(BindException ex) {
		var errores = ex.getFieldErrors()
			.stream()
			.map(campo -> new ApiError(campo.getField(), campo.getDefaultMessage()))
			.toList();
		return error(HttpStatus.BAD_REQUEST, "Hay datos inválidos", errores);
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(HandlerMethodValidationException ex) {
		var errores = ex.getParameterValidationResults()
			.stream()
			.flatMap(resultado -> resultado.getResolvableErrors()
				.stream()
				.map(e -> new ApiError(resultado.getMethodParameter().getParameterName(), e.getDefaultMessage())))
			.toList();
		return error(HttpStatus.BAD_REQUEST, "Hay datos inválidos", errores);
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(HttpMessageNotReadableException ex) {
		return error(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido o tiene un formato incorrecto");
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(MethodArgumentTypeMismatchException ex) {
		return error(HttpStatus.BAD_REQUEST, "El valor de '" + ex.getName() + "' no es válido",
				List.of(new ApiError(ex.getName(), "Valor no válido: " + ex.getValue())));
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(MissingServletRequestParameterException ex) {
		return error(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + ex.getParameterName() + "'");
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(IllegalArgumentException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	// HTTP y base de datos

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(NoResourceFoundException ex) {
		return error(HttpStatus.NOT_FOUND, "No existe el recurso " + ex.getResourcePath());
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(HttpRequestMethodNotSupportedException ex) {
		return error(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " no permitido en esta ruta");
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(DataIntegrityViolationException ex) {
		return error(HttpStatus.CONFLICT, "El registro ya existe o fue modificado por otra operación");
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(OptimisticLockingFailureException ex) {
		return error(HttpStatus.CONFLICT, "Otro usuario modificó el registro; vuelve a cargarlo e intenta de nuevo");
	}

	@ExceptionHandler
	ResponseEntity<ApiResponse<Void>> handle(Exception ex) {
		log.error("Error no controlado", ex);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
	}

	private static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
		return error(status, message, null);
	}

	private static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, List<ApiError> errors) {
		return ResponseEntity.status(status).body(ApiResponse.error(status, message, errors));
	}

}
