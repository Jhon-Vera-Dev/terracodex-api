package com.terracodex.shared.infrastructure.adapter.input.rest;

import java.util.List;
import java.util.function.Function;

import com.terracodex.shared.application.pagination.PageResult;

/**
 * {@code data} de un listado paginado:
 * <pre>
 * { "content": [...], "page": 0, "size": 20, "totalElements": 57, "totalPages": 3, "first": true, "last": false }
 * </pre>
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean first,
		boolean last) {

	public static <R, T> PageResponse<T> from(PageResult<R> result, Function<? super R, ? extends T> mapper) {
		var totalPages = result.totalPages();
		return new PageResponse<>(result.content().stream().<T>map(mapper).toList(), result.page(), result.size(),
				result.totalElements(), totalPages, result.page() == 0, result.page() + 1 >= totalPages);
	}

}
