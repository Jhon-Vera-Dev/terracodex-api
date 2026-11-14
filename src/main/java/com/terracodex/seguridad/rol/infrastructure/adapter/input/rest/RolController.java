package com.terracodex.seguridad.rol.infrastructure.adapter.input.rest;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.terracodex.seguridad.rol.application.dto.query.BuscarRolesQuery;
import com.terracodex.seguridad.rol.application.port.input.ActualizarRolUseCase;
import com.terracodex.seguridad.rol.application.port.input.BuscarRolesUseCase;
import com.terracodex.seguridad.rol.application.port.input.CambiarEstadoRolUseCase;
import com.terracodex.seguridad.rol.application.port.input.RegistrarRolUseCase;
import com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.request.ActualizarRolRequest;
import com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.request.RegistrarRolRequest;
import com.terracodex.seguridad.rol.infrastructure.adapter.input.rest.response.RolResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.IdResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageRequestParams;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageResponse;

import jakarta.validation.Valid;

/** Adaptador de entrada HTTP: traduce request → command, llama al puerto de entrada y result → response. */
@RestController
@RequestMapping("/api/seguridad/roles")
@PreAuthorize("hasRole('ADMIN')")
class RolController {

	private static final Set<String> ORDEN = Set.of("descripcion", "estado");

	private final RegistrarRolUseCase registrar;

	private final ActualizarRolUseCase actualizar;

	private final BuscarRolesUseCase buscar;

	private final CambiarEstadoRolUseCase cambiarEstado;

	RolController(RegistrarRolUseCase registrar, ActualizarRolUseCase actualizar, BuscarRolesUseCase buscar, CambiarEstadoRolUseCase cambiarEstado) {
		this.cambiarEstado = cambiarEstado;
		this.registrar = registrar;
		this.actualizar = actualizar;
		this.buscar = buscar;
	}

	/** {@code GET /api/seguridad/roles?page=0&size=20&search=super&sort=descripcion,asc&estado=true} */
	@GetMapping
	ApiResponse<PageResponse<RolResponse>> listar(@Valid PageRequestParams pagina,
			@RequestParam(required = false) Boolean estado) {
		var resultado = buscar.buscar(new BuscarRolesQuery(estado, pagina.toQuery("descripcion", ORDEN)));
		return ApiResponse.ok(PageResponse.from(resultado, RolResponse::from));
	}

	/** Roles activos sin paginar, para listas desplegables y checkboxes. */
	@GetMapping("/opciones")
	ApiResponse<List<RolResponse>> opciones() {
		return ApiResponse.ok(buscar.listarActivos().stream().map(RolResponse::from).toList());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<IdResponse<Long>> registrar(@Valid @RequestBody RegistrarRolRequest request) {
		return ApiResponse.created("Rol creado", new IdResponse<>(registrar.registrar(request.toCommand())));
	}

	@PutMapping("/{id}")
	ApiResponse<Void> actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarRolRequest request) {
		actualizar.actualizar(request.toCommand(id));
		return ApiResponse.ok("Rol actualizado");
	}

	/** Borrado lógico: el registro se conserva con estado inactivo y deja de salir en las listas desplegables. */
	@DeleteMapping("/{id}")
	ApiResponse<Void> desactivar(@PathVariable Long id) {
		cambiarEstado.cambiarEstado(id, false);
		return ApiResponse.ok("Rol desactivado");
	}

	@PostMapping("/{id}/activacion")
	ApiResponse<Void> activar(@PathVariable Long id) {
		cambiarEstado.cambiarEstado(id, true);
		return ApiResponse.ok("Rol activado");
	}

}
