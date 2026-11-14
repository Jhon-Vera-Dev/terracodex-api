package com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.usuario.application.dto.query.BuscarUsuariosQuery;
import com.terracodex.seguridad.usuario.application.port.input.ActualizarUsuarioUseCase;
import com.terracodex.seguridad.usuario.application.port.input.BuscarUsuariosUseCase;
import com.terracodex.seguridad.usuario.application.port.input.CambiarEstadoUsuarioUseCase;
import com.terracodex.seguridad.usuario.application.port.input.RegistrarUsuarioUseCase;
import com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.request.ActualizarUsuarioRequest;
import com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.request.RegistrarUsuarioRequest;
import com.terracodex.seguridad.usuario.infrastructure.adapter.input.rest.response.UsuarioResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.IdResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageRequestParams;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seguridad/usuarios")
@PreAuthorize("hasRole('ADMIN')")
class UsuarioController {

	private static final Set<String> ORDEN = Set.of("descripcion", "login", "estado");

	private final RegistrarUsuarioUseCase registrar;

	private final ActualizarUsuarioUseCase actualizar;

	private final BuscarUsuariosUseCase buscar;

	private final CambiarEstadoUsuarioUseCase cambiarEstado;

	UsuarioController(RegistrarUsuarioUseCase registrar, ActualizarUsuarioUseCase actualizar,
			BuscarUsuariosUseCase buscar, CambiarEstadoUsuarioUseCase cambiarEstado) {
		this.cambiarEstado = cambiarEstado;
		this.registrar = registrar;
		this.actualizar = actualizar;
		this.buscar = buscar;
	}

	/** {@code GET /api/seguridad/usuarios?page=0&size=20&search=juan&sort=login,asc&estado=true} */
	@GetMapping
	ApiResponse<PageResponse<UsuarioResponse>> listar(@Valid PageRequestParams pagina,
			@RequestParam(required = false) Boolean estado) {
		var resultado = buscar.buscar(new BuscarUsuariosQuery(estado, pagina.toQuery("descripcion", ORDEN)));
		return ApiResponse.ok(PageResponse.from(resultado, UsuarioResponse::from));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<IdResponse<Long>> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
		return ApiResponse.created("Usuario creado", new IdResponse<>(registrar.registrar(request.toCommand())));
	}

	/** El login no se edita: identifica al usuario en auditorías y registros. */
	@PutMapping("/{id}")
	ApiResponse<Void> actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		actualizar.actualizar(request.toCommand(id, Long.valueOf(jwt.getSubject())));
		return ApiResponse.ok("Usuario actualizado");
	}

	/** Borrado lógico: el usuario se conserva inactivo y sus sesiones se cierran. No puedes desactivarte a ti mismo. */
	@DeleteMapping("/{id}")
	ApiResponse<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		cambiarEstado.cambiarEstado(id, false, Long.valueOf(jwt.getSubject()));
		return ApiResponse.ok("Usuario desactivado: sus sesiones se cerraron");
	}

	@PostMapping("/{id}/activacion")
	ApiResponse<Void> activar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		cambiarEstado.cambiarEstado(id, true, Long.valueOf(jwt.getSubject()));
		return ApiResponse.ok("Usuario activado");
	}

}
