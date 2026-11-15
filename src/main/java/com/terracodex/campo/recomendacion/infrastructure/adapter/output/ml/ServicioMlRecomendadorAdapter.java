package com.terracodex.campo.recomendacion.infrastructure.adapter.output.ml;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.terracodex.campo.recomendacion.application.port.output.RecomendadorCultivoPort;
import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.recomendacion.domain.model.CultivoRecomendado;
import com.terracodex.campo.recomendacion.domain.model.Recomendacion;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

/** Adaptador de salida HTTP a servicio-ml ({@code POST /recomendar}). */
@Component
class ServicioMlRecomendadorAdapter implements RecomendadorCultivoPort {

	private static final Map<TipoRiego, String> ESCENARIO = Map.of(TipoRiego.SECANO, "rainfed", TipoRiego.GRAVEDAD,
			"irrigated_gravity", TipoRiego.GOTEO, "irrigated_drip");

	private final RestClient http;

	ServicioMlRecomendadorAdapter(@Value("${app.ml.url}") String url, @Value("${app.ml.api-key:}") String apiKey,
			@Value("${app.ml.timeout:90s}") Duration timeout) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setReadTimeout(timeout);
		var builder = RestClient.builder().baseUrl(url).requestFactory(factory);
		if (!apiKey.isBlank()) {
			builder.defaultHeader("X-Api-Key", apiKey);
		}
		this.http = builder.build();
	}

	@Override
	public Recomendacion recomendar(Coordenada coordenada, TipoRiego tipoRiego) {
		RespuestaMl respuesta;
		try {
			respuesta = http.post()
				.uri("/recomendar")
				.body(Map.of("latitud", coordenada.latitud(), "longitud", coordenada.longitud(), "tipo_riego",
						ESCENARIO.get(tipoRiego)))
				.retrieve()
				.body(RespuestaMl.class);
		}
		catch (RestClientException ex) {
			throw new ServicioNoDisponibleException(
					"El servicio de recomendación no está disponible. Intenta nuevamente en unos minutos", ex);
		}
		if (respuesta == null || respuesta.recomendaciones() == null) {
			throw new ServicioNoDisponibleException("El servicio de recomendación devolvió una respuesta vacía");
		}
		var cultivos = respuesta.recomendaciones()
			.stream()
			.map(c -> new CultivoRecomendado(c.codigo(), c.cultivo(), c.puntaje()))
			.toList();
		var z = respuesta.zona();
		var zona = z == null ? Zona.DESCONOCIDA
				: new Zona(z.descripcion(), z.distrito(), z.provincia(), z.departamento(), z.pais());
		return new Recomendacion(coordenada, tipoRiego, zona, cultivos, respuesta.periodoReferencia(),
				respuesta.errorMedio() == null ? 0 : respuesta.errorMedio());
	}

	record RespuestaMl(List<CultivoMl> recomendaciones, ZonaMl zona,
			@JsonProperty("periodo_referencia") String periodoReferencia,
			@JsonProperty("error_medio") Double errorMedio) {
	}

	record CultivoMl(String codigo, String cultivo, double puntaje) {
	}

	record ZonaMl(String descripcion, String distrito, String provincia, String departamento, String pais) {
	}

}
