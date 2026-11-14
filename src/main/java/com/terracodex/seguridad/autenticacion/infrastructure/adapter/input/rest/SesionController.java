package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.autenticacion.application.port.input.CambiarContrasenaUseCase;
import com.terracodex.seguridad.autenticacion.application.port.input.RevocarSesionesUseCase;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request.CambiarContrasenaRequest;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response.RevocacionResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

import jakarta.validation.Valid;

/** Operaciones sobre las sesiones de un usuario que ya está autenticado. */
@RestController
class SesionController {

	private final RevocarSesionesUseCase revocar;

	private final CambiarContrasenaUseCase cambiarContrasena;

	SesionController(RevocarSesionesUseCase revocar, CambiarContrasenaUseCase cambiarContrasena) {
		this.revocar = revocar;
		this.cambiarContrasena = cambiarContrasena;
	}

	/** Cierra todas las sesiones de un usuario (por ejemplo, si perdió el celular). Efecto inmediato. */
	@PostMapping("/api/seguridad/usuarios/{id}/revocacion")
	@PreAuthorize("hasRole('ADMIN')")
	ApiResponse<RevocacionResponse> revocar(@PathVariable Long id) {
		var cerradas = revocar.revocarTodas(id);
		return ApiResponse.ok(cerradas + " sesión(es) cerrada(s)", new RevocacionResponse(cerradas));
	}

	/** Cada usuario cambia la suya. Para restablecer la de otro, el administrador usa PUT /usuarios/{id}. */
	@PutMapping("/api/seguridad/mi-contrasena")
	ApiResponse<Void> cambiarContrasena(@Valid @RequestBody CambiarContrasenaRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		cambiarContrasena.cambiar(
				request.toCommand(Long.valueOf(jwt.getSubject()), UUID.fromString(jwt.getClaimAsString("sid"))));
		return ApiResponse.ok("Contraseña actualizada; tus otras sesiones se cerraron");
	}

}
