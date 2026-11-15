package com.terracodex.shared.infrastructure.adapter.input.rest;

import java.util.Locale;
import java.util.Set;

import com.terracodex.shared.application.pagination.PageQuery;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Parámetros de paginación de cualquier listado: {@code ?page=0&size=20&search=texto&sort=campo,asc}.
 * Uso en un controlador:
 * <pre>
 * &#64;GetMapping
 * ApiResponse&lt;PageResponse&lt;X&gt;&gt; listar(&#64;Valid PageRequestParams pagina, ...) {
 *     var query = pagina.toQuery("descripcion", Set.of("descripcion", "estado"));
 *     ...
 * }
 * </pre>
 */
public record PageRequestParams(@Min(0) Integer page, @Min(1) @Max(PageQuery.MAX_SIZE) Integer size,
		@Size(max = 100) String search, String sort) {

	/**
	 * @param defaultSort campo por el que se ordena si no se envía {@code sort}
	 * @param allowedSorts campos permitidos; evita ordenar por columnas internas o inexistentes
	 */
	public PageQuery toQuery(String defaultSort, Set<String> allowedSorts) {
		var campo = defaultSort;
		var ascendente = true;
		if (sort != null && !sort.isBlank()) {
			var partes = sort.split(",");
			campo = partes[0].strip();
			ascendente = partes.length < 2 || !partes[1].strip().toLowerCase(Locale.ROOT).equals("desc");
			if (!allowedSorts.contains(campo)) {
				throw new IllegalArgumentException(
						"No se puede ordenar por '" + campo + "'. Opciones: " + String.join(", ", allowedSorts.stream().sorted().toList()));
			}
		}
		return new PageQuery(page == null ? 0 : page, size == null ? PageQuery.DEFAULT_SIZE : size, search, campo,
				ascendente);
	}

}
