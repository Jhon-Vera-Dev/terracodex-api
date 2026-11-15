package com.terracodex.campo.registro.infrastructure.adapter.input.rest;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.campo.registro.application.dto.query.BuscarRegistrosCampoQuery;
import com.terracodex.campo.registro.application.port.input.AnularRegistroCampoUseCase;
import com.terracodex.campo.registro.application.port.input.BuscarRegistrosCampoUseCase;
import com.terracodex.campo.registro.application.port.input.RegistrarCampoUseCase;
import com.terracodex.campo.registro.infrastructure.adapter.input.rest.request.RegistrarCampoRequest;
import com.terracodex.campo.registro.infrastructure.adapter.input.rest.response.RegistroCampoResponse;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.IdResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageRequestParams;
import com.terracodex.shared.infrastructure.adapter.input.rest.PageResponse;

import jakarta.validation.Valid;

/**
 * Registros de campo. Cada usuario ve y anula los suyos; ADMIN y SUPERVISOR ven los de todos con {@code todos=true}
 * y pueden anular cualquiera.
 */
@RestController
@RequestMapping("/api/campo/registros")
class RegistroCampoController {

	private static final Set<String> ORDEN = Set.of("fechaRegistro", "cultivoDescripcion", "zona", "estado");

	private static final Set<String> SUPERVISAN = Set.of("ADMIN", "SUPERVISOR");

	private final RegistrarCampoUseCase registrar;

	private final BuscarRegistrosCampoUseCase buscar;

	private final AnularRegistroCampoUseCase anular;

	RegistroCampoController(RegistrarCampoUseCase registrar, BuscarRegistrosCampoUseCase buscar,
			AnularRegistroCampoUseCase anular) {
		this.registrar = registrar;
		this.buscar = buscar;
		this.anular = anular;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<IdResponse<Long>> registrar(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody RegistrarCampoRequest request) {
		var id = registrar.registrar(request.toCommand(usuario(jwt)));
		return ApiResponse.created("Registro guardado", new IdResponse<>(id));
	}

	/** {@code GET /api/campo/registros?page=0&size=20&search=papa&sort=fechaRegistro,desc&estado=true&todos=false} */
	@GetMapping
	ApiResponse<PageResponse<RegistroCampoResponse>> listar(@AuthenticationPrincipal Jwt jwt,
			@Valid PageRequestParams pagina, @RequestParam(required = false) Boolean estado,
			@RequestParam(defaultValue = "false") boolean todos) {
		var pageQuery = pagina.toQuery("fechaRegistro", ORDEN);
		if (pagina.sort() == null || pagina.sort().isBlank()) {
			// Por defecto, lo más reciente primero
			pageQuery = new PageQuery(pageQuery.page(), pageQuery.size(), pageQuery.search(), "fechaRegistro", false);
		}
		var resultado = buscar
			.buscar(new BuscarRegistrosCampoQuery(usuario(jwt), todos, supervisa(jwt), estado, pageQuery));
		return ApiResponse.ok(PageResponse.from(resultado, RegistroCampoResponse::from));
	}

	@GetMapping("/{id}")
	ApiResponse<RegistroCampoResponse> obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return ApiResponse.ok(RegistroCampoResponse.from(buscar.obtener(id, usuario(jwt), supervisa(jwt))));
	}

	/** Anulación lógica: el registro se conserva con estado inactivo. */
	@DeleteMapping("/{id}")
	ApiResponse<Void> anular(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		anular.anular(id, usuario(jwt), supervisa(jwt));
		return ApiResponse.ok("Registro anulado");
	}

	private static Long usuario(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}

	private static boolean supervisa(Jwt jwt) {
		var roles = jwt.getClaimAsStringList("roles");
		return roles != null && roles.stream().anyMatch(SUPERVISAN::contains);
	}

}
