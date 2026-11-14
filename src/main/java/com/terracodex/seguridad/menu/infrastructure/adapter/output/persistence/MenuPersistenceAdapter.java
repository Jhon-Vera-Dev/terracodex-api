package com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.terracodex.seguridad.menu.application.port.output.MenuRepositoryPort;
import com.terracodex.seguridad.menu.domain.model.Menu;
import com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.mapper.MenuPersistenceMapper;
import com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.repository.MenuJpaRepository;

@Component
class MenuPersistenceAdapter implements MenuRepositoryPort {

	private final MenuJpaRepository jpa;

	MenuPersistenceAdapter(MenuJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Long save(Menu menu) {
		return jpa.save(MenuPersistenceMapper.toEntity(menu)).getId();
	}

	@Override
	public Optional<Menu> findById(Long id) {
		return jpa.findById(id).map(MenuPersistenceMapper::toDomain);
	}

	@Override
	public List<Menu> findAll() {
		return jpa.findAllByOrderByOrdenAscDescripcionAsc().stream().map(MenuPersistenceMapper::toDomain).toList();
	}

}
