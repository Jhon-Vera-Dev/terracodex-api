package com.terracodex.seguridad.menu.domain.model;

import java.util.Set;

import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.shared.domain.DomainException;

/**
 * Maestro de menús, de dos niveles. Un menú padre agrupa opciones: no tiene path ni roles propios y se muestra si
 * alguno de sus hijos es visible. Las opciones tienen path y los roles que pueden verlas.
 */
public class Menu {

	private final Long id;

	private DatosMenu datos;

	private Menu(Long id, DatosMenu datos) {
		this.id = id;
		this.datos = datos;
	}

	public static Menu crear(DatosMenu datos) {
		return new Menu(null, validar(datos));
	}

	public static Menu reconstruir(Long id, DatosMenu datos) {
		return new Menu(id, datos);
	}

	public void actualizar(DatosMenu nuevos) {
		if (id != null && id.equals(nuevos.padreId())) {
			throw new DomainException("Un menú no puede ser su propio padre");
		}
		if (datos.isPadre() != nuevos.isPadre()) {
			throw new DomainException("No se puede cambiar un menú padre a opción ni al revés; crea uno nuevo");
		}
		this.datos = validar(nuevos);
	}

	public boolean visiblePara(Canal canal, Set<Long> rolesDelUsuario) {
		return datos.estado() && canal.permitidoPor(datos.isWeb(), datos.isMovil())
				&& (datos.isPadre() || datos.rolIds().stream().anyMatch(rolesDelUsuario::contains));
	}

	private static DatosMenu validar(DatosMenu d) {
		if (d.descripcion() == null || d.descripcion().isBlank()) {
			throw new DomainException("La descripción del menú es obligatoria");
		}
		if (!d.isWeb() && !d.isMovil()) {
			throw new DomainException("El menú debe mostrarse en web, móvil o ambos");
		}
		var path = d.path() == null || d.path().isBlank() ? null : d.path().strip();
		if (d.isPadre()) {
			if (path != null || d.padreId() != null) {
				throw new DomainException("Un menú padre no tiene path ni padre");
			}
			return new DatosMenu(d.descripcion().strip(), null, d.icon(), d.orden(), null, true, d.isWeb(),
					d.isMovil(), d.estado(), Set.of());
		}
		if (path == null || !path.startsWith("/")) {
			throw new DomainException("La opción de menú necesita un path que empiece con /");
		}
		if (d.rolIds() == null || d.rolIds().isEmpty()) {
			throw new DomainException("La opción de menú debe tener al menos un rol");
		}
		return new DatosMenu(d.descripcion().strip(), path, d.icon(), d.orden(), d.padreId(), false, d.isWeb(),
				d.isMovil(), d.estado(), Set.copyOf(d.rolIds()));
	}

	/** Borrado lógico: un menú inactivo deja de mostrarse, pero se conserva. */
	public void cambiarEstado(boolean estado) {
		var d = datos;
		this.datos = new DatosMenu(d.descripcion(), d.path(), d.icon(), d.orden(), d.padreId(), d.isPadre(), d.isWeb(),
				d.isMovil(), estado, d.rolIds());
	}

	public Long id() {
		return id;
	}

	public DatosMenu datos() {
		return datos;
	}

	public boolean isPadre() {
		return datos.isPadre();
	}

}
