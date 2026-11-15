package com.terracodex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.terracodex.seguridad.autenticacion.application.port.output.ProveedorGooglePort;
import com.terracodex.seguridad.autenticacion.domain.model.PerfilGoogle;
import com.terracodex.shared.domain.NoAutenticadoException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Login con Google de punta a punta (H2). Google se reemplaza por un mock: el code "code-X" es la cuenta X. */
@SpringBootTest
@AutoConfigureMockMvc
class GoogleLoginTests {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JsonMapper json;

	@MockitoBean
	ProveedorGooglePort google;

	@BeforeEach
	void google() {
		given(google.configurado()).willReturn(true);
		given(google.urlAutorizacion(anyString()))
			.willAnswer(inv -> "https://accounts.google.com/o/oauth2/v2/auth?state=" + inv.getArgument(0));
		given(google.perfil(anyString())).willAnswer(inv -> {
			String code = inv.getArgument(0);
			if (code.equals("code-malo")) {
				throw new NoAutenticadoException("Google rechazó el inicio de sesión. Vuelve a intentarlo");
			}
			var cuenta = code.substring("code-".length());
			return new PerfilGoogle("sub-" + cuenta, cuenta + "@gmail.com", "Persona " + cuenta,
					"https://lh3.googleusercontent.com/" + cuenta);
		});
	}

	@Test
	void elPrimerLoginCreaUnOperadorMovilYLaSesionSeCanjeaUnaSolaVez() throws Exception {
		var verificador = verificador();
		var inicio = inicio("MOVIL", verificador);
		assertThat(inicio).hasStatus(HttpStatus.OK);
		var id = data(inicio).get("id").asString();
		assertThat(data(inicio).get("url").asString()).contains("state=" + id);

		assertThat(canje(id, verificador)).as("todavía no vuelve de Google").hasStatus(HttpStatus.ACCEPTED);

		var callback = callback(id, "code-ana.flores");
		assertThat(callback).hasStatus(HttpStatus.OK);
		assertThat(callback.getResponse().getContentAsString()).contains("Vuelve a la app");

		assertThat(canje(id, verificador() /* otro */)).as("sin el verificador no hay sesión")
			.hasStatus(HttpStatus.UNAUTHORIZED);

		var sesion = canje(id, verificador);
		assertThat(sesion).hasStatus(HttpStatus.OK);
		var usuario = data(sesion).get("usuario");
		assertThat(data(sesion).get("accessToken").asString()).isNotBlank();
		assertThat(usuario.get("login").asString()).isEqualTo("ana.flores");
		assertThat(usuario.get("descripcion").asString()).isEqualTo("Persona ana.flores");
		assertThat(usuario.get("isMovil").asBoolean()).isTrue();
		assertThat(usuario.get("isWeb").asBoolean()).isFalse();
		assertThat(usuario.get("roles").get(0).asString()).isEqualTo("OPERADOR");

		assertThat(canje(id, verificador)).as("se canjea una sola vez").hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void elSegundoLoginReutilizaElUsuarioYLosLoginsRepetidosLlevanSufijo() throws Exception {
		var primero = login("MOVIL", "code-luis");
		var segundo = login("MOVIL", "code-luis");
		assertThat(segundo.get("usuario").get("id").asLong()).isEqualTo(primero.get("usuario").get("id").asLong());

		// Otra cuenta de Google cuyo correo da el mismo login
		var v = verificador();
		var id = data(inicio("MOVIL", v)).get("id").asString();
		given(google.perfil("code-otro-luis"))
			.willReturn(new PerfilGoogle("sub-otro-luis", "luis@empresa.com", "Luis de la empresa", null));
		callback(id, "code-otro-luis");
		assertThat(data(canje(id, v)).get("usuario").get("login").asString()).isEqualTo("luis-2");
	}

	@Test
	void unUsuarioNuevoDeGoogleNoEntraPorWeb() throws Exception {
		var v = verificador();
		var id = data(inicio("WEB", v)).get("id").asString();
		var callback = callback(id, "code-carla");
		assertThat(callback).as("la app web abrió Google en la misma pestaña: vuelve a ella")
			.hasStatus(HttpStatus.SEE_OTHER);
		assertThat(callback.getResponse().getHeader("Location")).isEqualTo("/app/?google=ok");

		assertThat(canje(id, v)).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(canje(id, v)).as("el intento ya se consumió").hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void siSeCancelaOGoogleRechazaLaAppRecibeElMotivo() throws Exception {
		var v1 = verificador();
		var cancelado = data(inicio("MOVIL", v1)).get("id").asString();
		var pagina = mvc.get().uri("/api/auth/google/callback?state={s}&error=access_denied", cancelado).exchange();
		assertThat(pagina.getResponse().getContentAsString()).contains("No se pudo iniciar sesión");
		var r1 = canje(cancelado, v1);
		assertThat(r1).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(body(r1).get("message").asString()).contains("Cancelaste");

		var v2 = verificador();
		var rechazado = data(inicio("MOVIL", v2)).get("id").asString();
		callback(rechazado, "code-malo");
		assertThat(body(canje(rechazado, v2)).get("message").asString()).contains("Google rechazó");
	}

	@Test
	void sinConfiguracionDeGoogleResponde503() throws Exception {
		given(google.configurado()).willReturn(false);

		assertThat(inicio("MOVIL", verificador())).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void elDesafioDebeSerUnSha256() throws Exception {
		var r = post("/api/auth/google/inicio", Map.of("canal", "MOVIL", "desafio", "corto"));

		assertThat(r).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void unCallbackConStateDesconocidoNoRompe() throws Exception {
		var r = callback("no-existe", "code-x");

		assertThat(r).hasStatus(HttpStatus.OK);
		assertThat(r.getResponse().getContentAsString()).contains("No se pudo iniciar sesión");
	}

	// Ayudas

	private JsonNode login(String canal, String code) throws Exception {
		var v = verificador();
		var id = data(inicio(canal, v)).get("id").asString();
		callback(id, code);
		var sesion = canje(id, v);
		assertThat(sesion).hasStatus(HttpStatus.OK);
		return data(sesion);
	}

	private static String verificador() {
		return UUID.randomUUID() + "-" + UUID.randomUUID();
	}

	private static String sha256(String valor) throws Exception {
		return HexFormat.of()
			.formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
	}

	private MvcTestResult inicio(String canal, String verificador) throws Exception {
		return post("/api/auth/google/inicio", Map.of("canal", canal, "desafio", sha256(verificador)));
	}

	private MvcTestResult callback(String state, String code) {
		return mvc.get().uri("/api/auth/google/callback?state={s}&code={c}", state, code).exchange();
	}

	private MvcTestResult canje(String id, String verificador) {
		return post("/api/auth/google/sesion", Map.of("id", id, "verificador", verificador));
	}

	private MvcTestResult post(String uri, Object body) {
		return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))
			.exchange();
	}

	private JsonNode body(MvcTestResult r) throws Exception {
		return json.readTree(r.getResponse().getContentAsString());
	}

	private JsonNode data(MvcTestResult r) throws Exception {
		return body(r).get("data");
	}

}
