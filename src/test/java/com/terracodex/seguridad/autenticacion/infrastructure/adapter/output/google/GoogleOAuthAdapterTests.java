package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.google;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

class GoogleOAuthAdapterTests {

	@Test
	void laUrlDeAutorizacionLlevaLosParametrosDeOpenIdConnect() {
		var adapter = new GoogleOAuthAdapter("cliente.apps.googleusercontent.com", "secreto",
				"https://api.ejemplo.com/api/auth/google/callback");

		var url = UriComponentsBuilder.fromUriString(adapter.urlAutorizacion("estado-123_abc")).build();

		assertThat(url.getHost()).isEqualTo("accounts.google.com");
		var p = url.getQueryParams();
		assertThat(p.getFirst("client_id")).isEqualTo("cliente.apps.googleusercontent.com");
		// ':' y '/' son válidos dentro de un parámetro (RFC 3986): Spring no los codifica y Google lo acepta
		assertThat(p.getFirst("redirect_uri")).isEqualTo("https://api.ejemplo.com/api/auth/google/callback");
		assertThat(p.getFirst("response_type")).isEqualTo("code");
		assertThat(p.getFirst("scope")).isEqualTo("openid%20email%20profile");
		assertThat(p.getFirst("state")).isEqualTo("estado-123_abc");
		assertThat(adapter.configurado()).isTrue();
	}

	@Test
	void sinCredencialesNoEstaConfigurado() {
		assertThat(new GoogleOAuthAdapter("", "", "https://x/callback").configurado()).isFalse();
	}

}
