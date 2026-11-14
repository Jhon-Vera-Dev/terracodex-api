package com.terracodex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
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
 * Mantenimiento de usuarios, roles y menús, menú por rol y canal, permisos y revocación de sesiones. El administrador
 * inicial lo crea la aplicación al arrancar (perfil local: admin / Admin123*).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTests {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JsonMapper json;

	@Test
	void losErroresDeValidacionIndicanElCampo() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var respuesta = body(post("/api/seguridad/usuarios", admin, Map.of("login", "", "descripcion", "Sin login")));

		assertThat(respuesta.get("ok").asBoolean()).isFalse();
		assertThat(respuesta.get("status").asInt()).isEqualTo(400);
		assertThat(valores(respuesta.get("errors"), "field")).contains("login", "contrasena", "rolIds");
	}

	@Test
	void losListadosSePaginanBuscanYOrdenan() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		for (var i = 1; i <= 3; i++) {
			crearOperador("pagina.op" + i);
		}

		var pagina = data(get("/api/seguridad/usuarios?search=pagina.op&size=2&page=0&sort=login,desc", admin));
		assertThat(pagina.get("totalElements").asLong()).isEqualTo(3);
		assertThat(pagina.get("totalPages").asInt()).isEqualTo(2);
		assertThat(pagina.get("first").asBoolean()).isTrue();
		assertThat(pagina.get("last").asBoolean()).isFalse();
		assertThat(valores(pagina.get("content"), "login")).containsExactly("pagina.op3", "pagina.op2");

		var segunda = data(get("/api/seguridad/usuarios?search=PAGINA.OP&size=2&page=1&sort=login,desc", admin));
		assertThat(valores(segunda.get("content"), "login")).containsExactly("pagina.op1");

		assertThat(get("/api/seguridad/usuarios?sort=contrasena,asc", admin)).as("campo de orden no permitido")
			.hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/seguridad/usuarios?size=500", admin)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elMenuDependeDelRolYDelCanal() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var menuAdmin = data(get("/api/seguridad/mi-menu", admin));
		assertThat(valores(menuAdmin, "descripcion")).containsExactly("Configuración");
		assertThat(valores(menuAdmin.get(0).get("hijos"), "path")).containsExactly("/seguridad/usuarios",
				"/seguridad/roles", "/seguridad/menus");
		assertThat(data(get("/api/seguridad/mi-menu?canal=MOVIL", admin))).as("configuración es solo web").isEmpty();

		// Una opción nueva para OPERADOR en web y móvil
		var grupo = data(post("/api/seguridad/menus", admin, Map.of("descripcion", "Operación", "orden", 1,
				"isPadre", true, "isWeb", true, "isMovil", true, "estado", true, "rolIds", List.of())))
			.get("id")
			.asLong();
		assertThat(post("/api/seguridad/menus", admin,
				Map.of("descripcion", "Tareas", "path", "/tareas", "orden", 1, "padreId", grupo, "isPadre", false,
						"isWeb", true, "isMovil", true, "estado", true, "rolIds", List.of(rolId(admin, "OPERADOR")))))
			.hasStatus(HttpStatus.CREATED);

		crearOperador("operador.menu");
		var menuOperador = data(get("/api/seguridad/mi-menu", token("operador.menu", "Clave2026", "MOVIL")));
		assertThat(valores(menuOperador, "descripcion")).containsExactly("Operación");
		assertThat(valores(menuOperador.get(0).get("hijos"), "path")).containsExactly("/tareas");
	}

	@Test
	void elCanalYElEstadoDelUsuarioControlanElAcceso() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var alta = post("/api/seguridad/usuarios", admin,
				Map.of("login", "operador.web", "descripcion", "Solo web", "contrasena", "Clave2026", "isWeb", true,
						"isMovil", false, "rolIds", List.of(rolId(admin, "OPERADOR"))));
		assertThat(body(alta).get("message").asString()).isEqualTo("Usuario creado");
		var id = data(alta).get("id").asLong();

		assertThat(login("operador.web", "Clave2026", "MOVIL")).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(login("operador.web", "Clave2026", "WEB")).hasStatus(HttpStatus.OK);

		assertThat(delete("/api/seguridad/usuarios/" + id, admin)).hasStatus(HttpStatus.OK);
		assertThat(login("operador.web", "Clave2026", "WEB")).as("usuario inactivo").hasStatus(HttpStatus.FORBIDDEN);

		assertThat(post("/api/seguridad/usuarios/" + id + "/activacion", admin, Map.of())).hasStatus(HttpStatus.OK);
		assertThat(login("operador.web", "Clave2026", "WEB")).hasStatus(HttpStatus.OK);
	}

	@Test
	void losEndpointsSeRestringenPorRol() throws Exception {
		crearOperador("operador.rol");
		var operador = token("operador.rol", "Clave2026", "MOVIL");

		var prohibido = get("/api/seguridad/usuarios", operador);
		assertThat(prohibido).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(body(prohibido).get("message").asString()).isEqualTo("No tienes permiso para esta acción");
		assertThat(get("/api/seguridad/roles", operador)).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(get("/api/seguridad/menus", operador)).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(get("/api/seguridad/mi-menu", operador)).as("cualquier usuario autenticado")
			.hasStatus(HttpStatus.OK);
	}

	@Test
	void losRolesSeCreanNormalizadosYAdminEsIntocable() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var alta = post("/api/seguridad/roles", admin, Map.of("descripcion", "jefe de area"));
		assertThat(alta).hasStatus(HttpStatus.CREATED);
		assertThat(rolId(admin, "JEFE_DE_AREA")).isPositive();

		assertThat(put("/api/seguridad/roles/" + rolId(admin, "ADMIN"), admin,
				Map.of("descripcion", "ADMIN", "estado", false)))
			.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		assertThat(delete("/api/seguridad/roles/" + rolId(admin, "ADMIN"), admin))
			.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void elAdministradorNoPuedeDejarseSinAcceso() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var adminId = data(get("/api/seguridad/usuarios?search=admin", admin)).get("content")
			.get(0)
			.get("id")
			.asLong();

		var sinAdmin = put("/api/seguridad/usuarios/" + adminId, admin, Map.of("descripcion", "Administrador",
				"isWeb", true, "isMovil", true, "estado", true, "rolIds", List.of(rolId(admin, "OPERADOR"))));
		assertThat(sinAdmin).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		assertThat(body(sinAdmin).get("message").asString()).isEqualTo("No puedes quitarte el rol ADMIN");
		assertThat(delete("/api/seguridad/usuarios/" + adminId, admin)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void elAdministradorRevocaLasSesionesAlInstante() throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var id = crearOperador("operador.revocado");
		var web = token("operador.revocado", "Clave2026", "WEB");
		var movil = token("operador.revocado", "Clave2026", "MOVIL");

		var revocacion = post("/api/seguridad/usuarios/" + id + "/revocacion", admin, Map.of());
		assertThat(revocacion).hasStatus(HttpStatus.OK);
		assertThat(data(revocacion).get("sesionesCerradas").asInt()).isEqualTo(2);
		assertThat(get("/api/seguridad/mi-menu", web)).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(get("/api/seguridad/mi-menu", movil)).hasStatus(HttpStatus.UNAUTHORIZED);

		// Cambiar los roles también cierra sus sesiones: usuario publica el evento y autenticacion revoca
		var otra = token("operador.revocado", "Clave2026", "WEB");
		assertThat(put("/api/seguridad/usuarios/" + id, admin,
				Map.of("descripcion", "Operador", "isWeb", true, "isMovil", true, "estado", true, "rolIds",
						List.of(rolId(admin, "OPERADOR"), rolId(admin, "SUPERVISOR")))))
			.hasStatus(HttpStatus.OK);
		assertThat(get("/api/seguridad/mi-menu", otra)).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void cambiarLaContrasenaPropiaCierraLasDemasSesiones() throws Exception {
		crearOperador("operador.clave");
		var actual = token("operador.clave", "Clave2026", "WEB");
		var otra = token("operador.clave", "Clave2026", "MOVIL");

		assertThat(put("/api/seguridad/mi-contrasena", actual, Map.of("actual", "Clave2026", "nueva", "Nueva2026x")))
			.hasStatus(HttpStatus.OK);
		assertThat(get("/api/seguridad/mi-menu", actual)).as("la sesión actual sigue").hasStatus(HttpStatus.OK);
		assertThat(get("/api/seguridad/mi-menu", otra)).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(login("operador.clave", "Nueva2026x", "WEB")).hasStatus(HttpStatus.OK);
	}

	// Ayudas

	private long crearOperador(String login) throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var alta = post("/api/seguridad/usuarios", admin, Map.of("login", login, "descripcion", "Operador",
				"contrasena", "Clave2026", "isWeb", true, "isMovil", true, "rolIds", List.of(rolId(admin, "OPERADOR"))));
		assertThat(alta).hasStatus(HttpStatus.CREATED);
		return data(alta).get("id").asLong();
	}

	private MvcTestResult login(String usuario, String contrasena, String canal) {
		return post("/api/auth/login", null, Map.of("login", usuario, "contrasena", contrasena, "canal", canal));
	}

	private String token(String usuario, String contrasena, String canal) throws Exception {
		var respuesta = login(usuario, contrasena, canal);
		assertThat(respuesta).hasStatus(HttpStatus.OK);
		return data(respuesta).get("accessToken").asString();
	}

	private long rolId(String token, String descripcion) throws Exception {
		for (var rol : data(get("/api/seguridad/roles/opciones", token))) {
			if (rol.get("descripcion").asString().equals(descripcion)) {
				return rol.get("id").asLong();
			}
		}
		throw new AssertionError("No existe el rol " + descripcion);
	}

	private static List<String> valores(JsonNode lista, String campo) {
		var resultado = new ArrayList<String>();
		lista.forEach(nodo -> resultado.add(nodo.get(campo).asString()));
		return resultado;
	}

	private MvcTestResult get(String uri, String token) {
		return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange();
	}

	private MvcTestResult delete(String uri, String token) {
		return mvc.delete().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange();
	}

	private MvcTestResult post(String uri, String token, Object body) {
		var request = mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
		if (token != null) {
			request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}
		return request.exchange();
	}

	private MvcTestResult put(String uri, String token, Object body) {
		return mvc.put()
			.uri(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json.writeValueAsString(body))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.exchange();
	}

	private JsonNode body(MvcTestResult result) throws Exception {
		return json.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode data(MvcTestResult result) throws Exception {
		return body(result).get("data");
	}

}
