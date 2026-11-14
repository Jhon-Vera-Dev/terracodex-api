package com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.mapper;

import java.util.Set;

import com.terracodex.seguridad.menu.domain.model.DatosMenu;
import com.terracodex.seguridad.menu.domain.model.Menu;
import com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.entity.MenuEntity;

public final class MenuPersistenceMapper {

	private MenuPersistenceMapper() {
	}

	public static MenuEntity toEntity(Menu menu) {
		var d = menu.datos();
		return new MenuEntity(menu.id(), d.descripcion(), d.path(), d.icon(), d.orden(), d.padreId(), d.isPadre(),
				d.isWeb(), d.isMovil(), d.estado(), d.rolIds());
	}

	public static Menu toDomain(MenuEntity e) {
		return Menu.reconstruir(e.getId(), new DatosMenu(e.getDescripcion(), e.getPath(), e.getIcon(), e.getOrden(),
				e.getPadreId(), e.isPadre(), e.isWeb(), e.isMovil(), e.isEstado(), Set.copyOf(e.getRolIds())));
	}

}
