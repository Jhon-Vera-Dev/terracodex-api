package com.terracodex.campo.recomendacion.domain.model;

/**
 * División administrativa del punto (OpenStreetMap). Cualquier campo puede faltar.
 *
 * @param descripcion texto para mostrar, por ejemplo "Ica, Ica, Ica"
 */
public record Zona(String descripcion, String distrito, String provincia, String departamento, String pais) {

	public static final Zona DESCONOCIDA = new Zona(null, null, null, null, null);

}
