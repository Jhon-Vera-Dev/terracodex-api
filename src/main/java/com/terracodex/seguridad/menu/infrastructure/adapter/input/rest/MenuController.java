package com.terracodex.seguridad.menu.infrastructure.adapter.input.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.menu.application.port.input.ActualizarMenuUseCase;
import com.terracodex.seguridad.menu.application.port.input.CambiarEstadoMenuUseCase;
import com.terracodex.seguridad.menu.application.port.input.ListarMenusUseCase;
import com.terracodex.seguridad.menu.application.port.input.RegistrarMenuUseCase;
import com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.request.GuardarMenuRequest;
import com.terracodex.seguridad.menu.infrastructure.adapter.input.rest.response.MenuResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.IdResponse;

import jakarta.validation.Valid;

/** Mantenimiento de menús. El menú para navegar es GET /api/seguridad/mi-menu ({@link MiMenuController}). */
@RestController
@RequestMapping("/api/seguridad/menus")
@PreAuthorize("hasRole('ADMIN')")
class MenuController {

	private final RegistrarMenuUseCase registrar;

	private final ActualizarMenuUseCase actualizar;

	private final ListarMenusUseCase listar;

	private final CambiarEstadoMenuUseCase cambiarEstado;

	MenuController(RegistrarMenuUseCase registrar, ActualizarMenuUseCase actualizar, ListarMenusUseCase listar, CambiarEstadoMenuUseCase cambiarEstado) {
		this.cambiarEstado = cambiarEstado;
		this.registrar = registrar;
		this.actualizar = actualizar;
		this.listar = listar;
	}

	@GetMapping
	ApiResponse<List<MenuResponse>> listar() {
		return ApiResponse.ok(listar.listar().stream().map(MenuResponse::from).toList());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<IdResponse<Long>> registrar(@Valid @RequestBody GuardarMenuRequest request) {
		return ApiResponse.created("Menú creado", new IdResponse<>(registrar.registrar(request.toCommand())));
	}

	@PutMapping("/{id}")
	ApiResponse<Void> actualizar(@PathVariable Long id, @Valid @RequestBody GuardarMenuRequest request) {
		actualizar.actualizar(id, request.toCommand());
		return ApiResponse.ok("Menú actualizado");
	}

	/** Borrado lógico: el registro se conserva con estado inactivo y deja de salir en las listas desplegables. */
	@DeleteMapping("/{id}")
	ApiResponse<Void> desactivar(@PathVariable Long id) {
		cambiarEstado.cambiarEstado(id, false);
		return ApiResponse.ok("Menú desactivado");
	}

	@PostMapping("/{id}/activacion")
	ApiResponse<Void> activar(@PathVariable Long id) {
		cambiarEstado.cambiarEstado(id, true);
		return ApiResponse.ok("Menú activado");
	}

}
