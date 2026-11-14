package com.terracodex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Login de punta a punta con el perfil local (H2). El administrador inicial lo crea la aplicación al arrancar:
 * admin / Admin123*.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginTests {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JsonMapper json;

	@Test
	void elLoginDevuelveTokensYDatosDelUsuario() throws Exception {
		var respuesta = login("admin", "Admin123*", "WEB");

		assertThat(respuesta).hasStatus(HttpStatus.OK);
		var body = body(respuesta);
		assertThat(body.get("ok").asBoolean()).isTrue();
		assertThat(body.get("message").asString()).isEqualTo("Bienvenido");
		var data = body.get("data");
		assertThat(data.get("accessToken").asString()).isNotBlank();
		assertThat(data.get("refreshToken").asString()).isNotBlank();
		assertThat(data.get("tokenType").asString()).isEqualTo("Bearer");
		assertThat(data.get("usuario").get("login").asString()).isEqualTo("admin");
		assertThat(data.get("usuario").get("roles").get(0).asString()).isEqualTo("ADMIN");
	}

	@Test
	void contrasenaIncorrectaResponde401SinRevelarSiElUsuarioExiste() throws Exception {
		var malaContrasena = login("admin", "otra-clave-1", "WEB");
		var noExiste = login("nadie", "otra-clave-1", "WEB");

		assertThat(malaContrasena).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(noExiste).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(body(malaContrasena).get("message").asString())
			.isEqualTo(body(noExiste).get("message").asString());
	}

	@Test
	void losErroresDeValidacionIndicanElCampo() throws Exception {
		var respuesta = mvc.post()
			.uri("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(json.writeValueAsString(Map.of("login", "admin")))
			.exchange();

		assertThat(respuesta).hasStatus(HttpStatus.BAD_REQUEST);
		var campos = body(respuesta).get("errors").valueStream().map(e -> e.get("field").asString()).toList();
		assertThat(campos).contains("contrasena", "canal");
	}

	@Test
	void sinTokenResponde401ConElFormatoEstandar() throws Exception {
		var respuesta = mvc.put().uri("/api/seguridad/mi-contrasena").exchange();

		assertThat(respuesta).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(body(respuesta).get("ok").asBoolean()).isFalse();
		assertThat(body(respuesta).get("status").asInt()).isEqualTo(401);
	}

	@Test
	void refreshRotaElTokenYLogoutCortaElAccesoAlInstante() throws Exception {
		var sesion = body(login("admin", "Admin123*", "WEB")).get("data");
		var refresh = sesion.get("refreshToken").asString();

		var renovada = post("/api/auth/refresh", Map.of("refreshToken", refresh));
		assertThat(renovada).hasStatus(HttpStatus.OK);
		var nuevoRefresh = body(renovada).get("data").get("refreshToken").asString();
		var nuevoAcceso = body(renovada).get("data").get("accessToken").asString();
		assertThat(post("/api/auth/refresh", Map.of("refreshToken", refresh))).as("el refresh anterior ya no sirve")
			.hasStatus(HttpStatus.UNAUTHORIZED);

		assertThat(post("/api/auth/logout", Map.of("refreshToken", nuevoRefresh))).hasStatus(HttpStatus.OK);
		var conTokenCerrado = mvc.post()
			.uri("/api/seguridad/usuarios/1/revocacion")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + nuevoAcceso)
			.exchange();
		assertThat(conTokenCerrado).as("el access token de una sesión cerrada se rechaza")
			.hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private MvcTestResult login(String login, String contrasena, String canal) throws Exception {
		return post("/api/auth/login", Map.of("login", login, "contrasena", contrasena, "canal", canal));
	}

	private MvcTestResult post(String uri, Object body) throws Exception {
		return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))
			.exchange();
	}

	private JsonNode body(MvcTestResult resultado) throws Exception {
		return json.readTree(resultado.getResponse().getContentAsString());
	}

}
