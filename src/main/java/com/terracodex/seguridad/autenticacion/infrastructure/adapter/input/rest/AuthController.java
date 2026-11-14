package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.autenticacion.application.port.input.CerrarSesionUseCase;
import com.terracodex.seguridad.autenticacion.application.port.input.IniciarSesionUseCase;
import com.terracodex.seguridad.autenticacion.application.port.input.RenovarSesionUseCase;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request.LoginRequest;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request.RefreshTokenRequest;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response.SesionResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

import jakarta.validation.Valid;

/**
 * Endpoints públicos de sesión. Logout recibe el refresh token y no exige access token: así se puede cerrar sesión
 * aunque el access token ya haya expirado.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final IniciarSesionUseCase iniciar;

	private final RenovarSesionUseCase renovar;

	private final CerrarSesionUseCase cerrar;

	AuthController(IniciarSesionUseCase iniciar, RenovarSesionUseCase renovar, CerrarSesionUseCase cerrar) {
		this.iniciar = iniciar;
		this.renovar = renovar;
		this.cerrar = cerrar;
	}

	@PostMapping("/login")
	ApiResponse<SesionResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("Bienvenido", SesionResponse.from(iniciar.iniciar(request.toCommand())));
	}

	/** El refresh token enviado deja de servir: hay que guardar el nuevo. */
	@PostMapping("/refresh")
	ApiResponse<SesionResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ApiResponse.ok("Sesión renovada", SesionResponse.from(renovar.renovar(request.refreshToken())));
	}

	@PostMapping("/logout")
	ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		cerrar.cerrar(request.refreshToken());
		return ApiResponse.ok("Sesión cerrada");
	}

}
