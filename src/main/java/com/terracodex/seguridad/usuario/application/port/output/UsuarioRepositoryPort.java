package com.terracodex.seguridad.usuario.application.port.output;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;

/** Puerto de salida de usuarios. Lo usan también las features menu y autenticacion. */
public interface UsuarioRepositoryPort {

	/** @return el id asignado */
	Long save(Usuario usuario);

	Optional<Usuario> findById(Long id);

	Optional<Usuario> findByLogin(String login);

	boolean existsByLogin(String login);

	/** Los que existan entre {@code ids}. */
	List<Usuario> findAllById(Collection<Long> ids);

	/** Activos ordenados por descripción. */
	List<Usuario> findActivos();

	boolean existeAlguno();

	/**
	 * @param estado {@code null} para todos
	 * @param page {@code search} busca en login y descripción
	 */
	PageResult<Usuario> buscar(Boolean estado, PageQuery page);

}
