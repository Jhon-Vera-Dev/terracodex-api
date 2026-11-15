package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.terracodex.seguridad.autenticacion.application.port.input.CanjearLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.application.port.input.CompletarLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.application.port.input.IniciarLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request.CanjeGoogleRequest;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.request.InicioGoogleRequest;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response.InicioGoogleResponse;
import com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.rest.response.SesionResponse;
import com.terracodex.shared.infrastructure.adapter.input.rest.ApiResponse;

import jakarta.validation.Valid;

/**
 * Login con Google (públicos). La app:
 * <ol>
 * <li>{@code POST /api/auth/google/inicio {"canal": "MOVIL", "desafio": sha256hex(verificador)}} → abre {@code url}
 * en el navegador.</li>
 * <li>Google redirige a {@code GET /api/auth/google/callback}, que responde una página de "vuelve a la app".</li>
 * <li>{@code POST /api/auth/google/sesion {"id", "verificador"}} cada 2 s: 202 mientras está pendiente, 200 con la
 * sesión (igual que {@code /api/auth/login}) cuando terminó.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/auth/google")
class GoogleAuthController {

	private final IniciarLoginGoogleUseCase iniciar;

	private final CompletarLoginGoogleUseCase completar;

	private final CanjearLoginGoogleUseCase canjear;

	GoogleAuthController(IniciarLoginGoogleUseCase iniciar, CompletarLoginGoogleUseCase completar,
			CanjearLoginGoogleUseCase canjear) {
		this.iniciar = iniciar;
		this.completar = completar;
		this.canjear = canjear;
	}

	@PostMapping("/inicio")
	ApiResponse<InicioGoogleResponse> inicio(@Valid @RequestBody InicioGoogleRequest request) {
		return ApiResponse.ok(InicioGoogleResponse.from(iniciar.iniciar(request.toCommand())));
	}

	/** Lo abre el navegador del usuario: responde HTML, nunca datos de la sesión. */
	@GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
	String callback(@RequestParam(required = false) String state, @RequestParam(required = false) String code,
			@RequestParam(required = false) String error) {
		var ok = completar.completar(state, code, error);
		return ok ? pagina("¡Listo!", "Ya iniciaste sesión con Google. Vuelve a la app TerraCode.")
				: pagina("No se pudo iniciar sesión",
						"Vuelve a la app TerraCode para ver el motivo e intentarlo de nuevo.");
	}

	@PostMapping("/sesion")
	ResponseEntity<ApiResponse<SesionResponse>> sesion(@Valid @RequestBody CanjeGoogleRequest request) {
		return canjear.canjear(request.id(), request.verificador())
			.map(s -> ResponseEntity.ok(ApiResponse.ok("Bienvenido", SesionResponse.from(s))))
			.orElseGet(() -> ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new ApiResponse<>(true, HttpStatus.ACCEPTED.value(), "Esperando a Google", null, null,
						Instant.now())));
	}

	/** Textos fijos: no se refleja nada de la petición en la página. */
	private static String pagina(String titulo, String mensaje) {
		return """
				<!doctype html>
				<html lang="es"><head><meta charset="utf-8">
				<meta name="viewport" content="width=device-width, initial-scale=1">
				<title>TerraCode</title>
				<style>
				  body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;
				       background:#F8FAF9;font-family:system-ui,sans-serif;color:#1F2937}
				  .c{background:#fff;border:1px solid #E5E7EB;border-radius:16px;padding:32px;max-width:360px;
				     margin:16px;text-align:center}
				  h1{color:#1B4332;font-size:22px;margin:0 0 12px} p{color:#6B7280;margin:0;line-height:1.5}
				</style></head>
				<body><div class="c"><h1>%s</h1><p>%s</p></div></body></html>
				""".formatted(titulo, mensaje);
	}

}
