package com.terracodex.seguridad.menu.application.port.output;

import java.util.List;
import java.util.Optional;

import com.terracodex.seguridad.menu.domain.model.Menu;
import com.terracodex.shared.domain.DomainException;

public interface MenuRepositoryPort {

	/** @return el id asignado */
	Long save(Menu menu);

	Optional<Menu> findById(Long id);

	/** Ordenados por orden y descripción. Son pocos registros: el árbol y los filtros se arman en memoria. */
	List<Menu> findAll();

	default void verificarPadre(Long padreId) {
		if (padreId != null && findById(padreId).filter(Menu::isPadre).isEmpty()) {
			throw new DomainException("El menú padre " + padreId + " no existe o no es un menú padre");
		}
	}

}
