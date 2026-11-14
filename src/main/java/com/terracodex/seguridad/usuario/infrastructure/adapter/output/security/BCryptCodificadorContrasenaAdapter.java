package com.terracodex.seguridad.usuario.infrastructure.adapter.output.security;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;

/** Guarda el hash con prefijo ({bcrypt}...), así se puede migrar de algoritmo sin invalidar contraseñas. */
@Component
class BCryptCodificadorContrasenaAdapter implements CodificadorContrasenaPort {

	private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	@Override
	public String cifrar(String contrasena) {
		return encoder.encode(contrasena);
	}

	@Override
	public boolean coincide(String contrasena, String cifrada) {
		return encoder.matches(contrasena, cifrada);
	}

}
