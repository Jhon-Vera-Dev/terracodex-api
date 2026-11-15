package com.terracodex.campo.recomendacion.domain.model;

/**
 * @param codigo código del cultivo en el modelo (mze, wpo, cof…)
 * @param puntaje aptitud estimada de 0 a 100
 */
public record CultivoRecomendado(String codigo, String descripcion, double puntaje) {
}
