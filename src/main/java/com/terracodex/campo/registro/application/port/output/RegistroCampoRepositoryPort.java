package com.terracodex.campo.registro.application.port.output;

import java.util.Optional;

import com.terracodex.campo.registro.domain.model.RegistroCampo;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;

public interface RegistroCampoRepositoryPort {

	/** @return el id asignado */
	Long save(RegistroCampo registro);

	Optional<RegistroCampo> findById(Long id);

	/**
	 * @param idUsuario {@code null} para los de todos los usuarios
	 * @param estado {@code null} para activos y anulados
	 * @param page {@code search} busca en la zona y el cultivo
	 */
	PageResult<RegistroCampo> buscar(Long idUsuario, Boolean estado, PageQuery page);

}
