package com.terracodex.shared.application.pagination;

/**
 * Qué página pide un caso de uso de listado. Java puro: lo usan los puertos sin depender de Spring Data.
 *
 * @param page número de página, desde 0
 * @param size registros por página, de 1 a {@link #MAX_SIZE}
 * @param search texto libre opcional; cada adaptador decide en qué campos buscar
 * @param sortBy campo de orden ya validado por el adaptador de entrada, o {@code null}
 */
public record PageQuery(int page, int size, String search, String sortBy, boolean ascending) {

	public static final int DEFAULT_SIZE = 20;

	public static final int MAX_SIZE = 100;

	public PageQuery {
		if (page < 0) {
			throw new IllegalArgumentException("page no puede ser negativo");
		}
		if (size < 1 || size > MAX_SIZE) {
			throw new IllegalArgumentException("size debe estar entre 1 y " + MAX_SIZE);
		}
		search = search == null || search.isBlank() ? null : search.strip();
	}

	public static PageQuery of(int page, int size) {
		return new PageQuery(page, size, null, null, true);
	}

	public boolean hasSearch() {
		return search != null;
	}

}
