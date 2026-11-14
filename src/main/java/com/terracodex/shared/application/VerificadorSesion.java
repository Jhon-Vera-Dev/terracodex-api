package com.terracodex.shared.application;

/**
 * Puerto que la validación del JWT consulta en cada petición. Lo implementa el módulo seguridad; shared solo define
 * el contrato, así no conoce a ningún módulo.
 */
public interface VerificadorSesion {

	boolean estaActiva(String sesionId);

}
