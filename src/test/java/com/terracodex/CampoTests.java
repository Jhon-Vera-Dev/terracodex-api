package com.terracodex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.terracodex.campo.recomendacion.application.port.output.RecomendadorCultivoPort;
import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.recomendacion.domain.model.CultivoRecomendado;
import com.terracodex.campo.recomendacion.domain.model.Recomendacion;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Módulo campo con el perfil local (H2). El servicio-ml se reemplaza por un mock. */
@SpringBootTest
@AutoConfigureMockMvc
class CampoTests {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JsonMapper json;

	@MockitoBean
	RecomendadorCultivoPort recomendador;

	@Test
	void laRecomendacionDevuelveZonaYCultivosOrdenados() throws Exception {
		given(recomendador.recomendar(any(Coordenada.class), any(TipoRiego.class)))
			.willAnswer(inv -> new Recomendacion(inv.getArgument(0), inv.getArgument(1),
					new Zona("Ica, Ica", "Ica", null, "Ica", "Perú"),
					List.of(new CultivoRecomendado("tom", "Tomate", 72.5), new CultivoRecomendado("oni", "Cebolla", 60)),
					"1981-2010", 9.3));
		var movil = tokenOperador("operador.campo1");

		var respuesta = post("/api/campo/recomendaciones", movil,
				Map.of("latitud", -14.06, "longitud", -75.73, "tipoRiego", "GOTEO"));

		assertThat(respuesta).hasStatus(HttpStatus.OK);
		var data = data(respuesta);
		assertThat(data.get("tipoRiego").asString()).isEqualTo("GOTEO");
		assertThat(data.get("zona").get("departamento").asString()).isEqualTo("Ica");
		assertThat(data.get("cultivos").get(0).get("descripcion").asString()).isEqualTo("Tomate");
	}

	@Test
	void siElServicioMlNoRespondeSeContesta503() throws Exception {
		given(recomendador.recomendar(any(), any())).willThrow(new ServicioNoDisponibleException("caído"));

		var respuesta = post("/api/campo/recomendaciones", tokenOperador("operador.campo2"),
				Map.of("latitud", -14.06, "longitud", -75.73));

		assertThat(respuesta).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(body(respuesta).get("ok").asBoolean()).isFalse();
	}

	@Test
	void coordenadasFueraDeRangoSeRechazan() throws Exception {
		var respuesta = post("/api/campo/recomendaciones", tokenOperador("operador.campo3"),
				Map.of("latitud", 95, "longitud", -75.73));

		assertThat(respuesta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void cadaUsuarioVeSusRegistrosYElAdminLosDeTodos() throws Exception {
		var ana = tokenOperador("operador.ana");
		var beto = tokenOperador("operador.beto");
		var admin = token("admin", "Admin123*", "WEB");

		var alta = post("/api/campo/registros", ana, registro("Tomate", "tom", 72.5));
		assertThat(alta).hasStatus(HttpStatus.CREATED);
		var id = data(alta).get("id").asLong();
		assertThat(post("/api/campo/registros", beto, registro("Papa", "wpo", null))).hasStatus(HttpStatus.CREATED);

		var deAna = data(get("/api/campo/registros", ana)).get("content");
		assertThat(deAna).hasSize(1);
		assertThat(deAna.get(0).get("cultivoDescripcion").asString()).isEqualTo("Tomate");
		assertThat(deAna.get(0).get("zona").get("descripcion").asString()).isEqualTo("Ica, Ica");
		assertThat(deAna.get(0).get("sugerido").asBoolean()).isTrue();

		assertThat(get("/api/campo/registros/" + id, beto)).as("beto no ve el registro de ana")
			.hasStatus(HttpStatus.NOT_FOUND);
		assertThat(get("/api/campo/registros?todos=true", beto)).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(data(get("/api/campo/registros?todos=true&search=papa", admin)).get("content")).hasSize(1);
		assertThat(get("/api/campo/registros/" + id, admin)).hasStatus(HttpStatus.OK);
	}

	@Test
	void unRegistroSeAnulaSinBorrarse() throws Exception {
		var carla = tokenOperador("operador.carla");
		var id = data(post("/api/campo/registros", carla, registro("Maíz", "mze", 40.0))).get("id").asLong();

		assertThat(delete("/api/campo/registros/" + id, carla)).hasStatus(HttpStatus.OK);
		assertThat(delete("/api/campo/registros/" + id, carla)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);

		var anulado = data(get("/api/campo/registros/" + id, carla));
		assertThat(anulado.get("estado").asBoolean()).isFalse();
		assertThat(data(get("/api/campo/registros?estado=true", carla)).get("content")).isEmpty();
	}

	@Test
	void elRegistroValidaLosCamposObligatorios() throws Exception {
		var respuesta = post("/api/campo/registros", tokenOperador("operador.campo4"), Map.of("latitud", -14.0));

		assertThat(respuesta).hasStatus(HttpStatus.BAD_REQUEST);
		var campos = body(respuesta).get("errors").valueStream().map(e -> e.get("field").asString()).toList();
		assertThat(campos).contains("longitud", "tipoRiego", "cultivoDescripcion");
	}

	@Test
	void sinTokenNoSeRegistra() throws Exception {
		assertThat(post("/api/campo/registros", null, registro("Papa", "wpo", null)))
			.hasStatus(HttpStatus.UNAUTHORIZED);
	}

	// Ayudas

	private static Map<String, Object> registro(String cultivo, String codigo, Double puntaje) {
		var body = new HashMap<String, Object>();
		body.put("latitud", -14.0634);
		body.put("longitud", -75.7291);
		body.put("precisionMetros", 8.5);
		body.put("zona", Map.of("descripcion", "Ica, Ica", "distrito", "Ica", "departamento", "Ica", "pais", "Perú"));
		body.put("tipoRiego", "GOTEO");
		body.put("cultivoCodigo", codigo);
		body.put("cultivoDescripcion", cultivo);
		body.put("puntaje", puntaje);
		body.put("sugerido", puntaje != null);
		body.put("observacion", "Parcela de prueba");
		return body;
	}

	private String tokenOperador(String login) throws Exception {
		var admin = token("admin", "Admin123*", "WEB");
		var alta = post("/api/seguridad/usuarios", admin, Map.of("login", login, "descripcion", "Operador de campo",
				"contrasena", "Clave2026", "isWeb", false, "isMovil", true, "rolIds", List.of(rolId(admin, "OPERADOR"))));
		assertThat(alta).hasStatus(HttpStatus.CREATED);
		return token(login, "Clave2026", "MOVIL");
	}

	private String token(String usuario, String contrasena, String canal) throws Exception {
		var respuesta = post("/api/auth/login", null, Map.of("login", usuario, "contrasena", contrasena, "canal", canal));
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

	private JsonNode body(MvcTestResult result) throws Exception {
		return json.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode data(MvcTestResult result) throws Exception {
		return body(result).get("data");
	}

}
