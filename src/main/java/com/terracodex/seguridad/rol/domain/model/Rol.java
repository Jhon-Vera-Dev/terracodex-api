package com.terracodex.seguridad.rol.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

import com.terracodex.shared.domain.DomainException;

/**
 * Maestro de roles. La descripción es también el código que viaja en el token (ROLE_&lt;descripcion&gt;), por eso
 * se guarda en mayúsculas y sin espacios.
 */
public class Rol {

	public static final String ADMIN = "ADMIN";

	private static final Pattern FORMATO = Pattern.compile("[A-Z][A-Z0-9_]{1,59}");

	private final Long id;

	private String descripcion;

	private boolean estado;

	private Rol(Long id, String descripcion, boolean estado) {
		this.id = id;
		this.descripcion = descripcion;
		this.estado = estado;
	}

	public static Rol crear(String descripcion) {
		return new Rol(null, normalizar(descripcion), true);
	}

	public static Rol reconstruir(Long id, String descripcion, boolean estado) {
		return new Rol(id, descripcion, estado);
	}

	public void actualizar(String descripcion, boolean estado) {
		var nueva = normalizar(descripcion);
		if (ADMIN.equals(this.descripcion) && (!ADMIN.equals(nueva) || !estado)) {
			throw new DomainException("El rol ADMIN no se puede renombrar ni desactivar");
		}
		this.descripcion = nueva;
		this.estado = estado;
	}

	public static String normalizar(String descripcion) {
		var valor = descripcion == null ? "" : descripcion.strip().toUpperCase(Locale.ROOT).replace(' ', '_');
		if (!FORMATO.matcher(valor).matches()) {
			throw new DomainException("La descripción del rol debe tener de 2 a 60 letras, números o guion bajo, "
					+ "por ejemplo SUPERVISOR");
		}
		return valor;
	}

	/** Borrado lógico. El rol ADMIN no se puede desactivar. */
	public void cambiarEstado(boolean estado) {
		if (ADMIN.equals(descripcion) && !estado) {
			throw new DomainException("El rol ADMIN no se puede desactivar");
		}
		this.estado = estado;
	}

	public Long id() {
		return id;
	}

	public String descripcion() {
		return descripcion;
	}

	public boolean estado() {
		return estado;
	}

}
