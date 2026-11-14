package com.terracodex.shared.infrastructure.adapter.output.persistence;

import java.util.Locale;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;

/** Traduce la paginación de la aplicación (Java puro) a Spring Data y de vuelta. Lo usan los adaptadores JPA. */
public final class PageableMapper {

	private PageableMapper() {
	}

	/** El campo de orden debe llamarse igual en la entidad JPA; el adaptador de entrada ya lo validó. */
	public static Pageable toPageable(PageQuery query) {
		var sort = query.sortBy() == null ? Sort.unsorted()
				: Sort.by(query.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, query.sortBy());
		return PageRequest.of(query.page(), query.size(), sort);
	}

	/**
	 * Patrón para {@code cb.like(cb.lower(campo), patron, '\\')}: busca el texto en cualquier parte, sin distinguir
	 * mayúsculas, y escapa los comodines que escriba el usuario.
	 */
	public static String containsPattern(String search) {
		var escapado = search.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escapado + "%";
	}

	public static <E, T> PageResult<T> toResult(Page<E> page, Function<? super E, ? extends T> mapper) {
		return new PageResult<>(page.getContent().stream().<T>map(mapper).toList(), page.getNumber(), page.getSize(),
				page.getTotalElements());
	}

}
