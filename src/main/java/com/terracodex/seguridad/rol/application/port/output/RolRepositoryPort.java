package com.terracodex.seguridad.rol.application.port.output;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;
import com.terracodex.shared.domain.DomainException;

/** Puerto de salida de roles. Lo usan también las features usuario, menu y autenticacion. */
public interface RolRepositoryPort {

	/** @return el id asignado */
	Long save(Rol rol);

	Optional<Rol> findById(Long id);

	Optional<Rol> findByDescripcion(String descripcion);

	List<Rol> findAllById(Collection<Long> ids);

	/** Activos ordenados por descripción: para listas desplegables. */
	List<Rol> findActivos();

	/**
	 * @param estado {@code null} para todos
	 * @param page {@code search} busca en la descripción
	 */
	PageResult<Rol> buscar(Boolean estado, PageQuery page);

	/** Descripciones de los roles activos entre {@code ids}, ordenadas. Es lo que viaja en el token. */
	default List<String> descripcionesActivas(Collection<Long> ids) {
		return findAllById(ids).stream().filter(Rol::estado).map(Rol::descripcion).sorted().toList();
	}

	default void verificarExistentes(Collection<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		var encontrados = findAllById(ids).stream().map(Rol::id).collect(Collectors.toSet());
		var faltantes = ids.stream().filter(id -> !encontrados.contains(id)).sorted().toList();
		if (!faltantes.isEmpty()) {
			throw new DomainException("No existen los roles " + faltantes);
		}
	}

}
