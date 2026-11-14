package com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.entity.MenuEntity;

public interface MenuJpaRepository extends JpaRepository<MenuEntity, Long> {

	List<MenuEntity> findAllByOrderByOrdenAscDescripcionAsc();

}
