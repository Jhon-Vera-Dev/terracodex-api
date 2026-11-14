package com.terracodex.seguridad.usuario.application.port.output;

/** Cifrado irreversible de contraseñas (BCrypt en infraestructura). */
public interface CodificadorContrasenaPort {

	String cifrar(String contrasena);

	boolean coincide(String contrasena, String cifrada);

}
