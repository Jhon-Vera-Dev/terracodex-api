package com.terracodex.campo.registro.domain.model;

import com.terracodex.shared.domain.DomainException;

/**
 * Cultivo de la parcela.
 *
 * @param codigo código del modelo (mze, wpo…) o {@code null} si el cultivo no está en el modelo
 * @param puntaje aptitud que le dio el modelo (0-100), o {@code null} si no se consultó
 * @param sugerido {@code true} si es el primero de la recomendación
 */
public record Cultivo(String codigo, String descripcion, Double puntaje, boolean sugerido) {

	public Cultivo {
		codigo = codigo == null || codigo.isBlank() ? null : codigo.strip();
		descripcion = descripcion == null ? "" : descripcion.strip();
		if (descripcion.isEmpty() || descripcion.length() > 100) {
			throw new DomainException("El nombre del cultivo debe tener de 1 a 100 caracteres");
		}
		if (codigo != null && codigo.length() > 20) {
			throw new DomainException("El código del cultivo es demasiado largo");
		}
		if (puntaje != null && (!Double.isFinite(puntaje) || puntaje < 0 || puntaje > 100)) {
			throw new DomainException("El puntaje del cultivo debe estar entre 0 y 100");
		}
	}

}
