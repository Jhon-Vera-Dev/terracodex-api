package com.terracodex.seguridad.usuario.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.terracodex.seguridad.usuario.domain.events.UsuarioAccesoModificado;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.AggregateRoot;
import com.terracodex.shared.domain.DomainException;

/**
 * Maestro de usuarios. Guarda la contraseña ya cifrada: el cifrado es un puerto de salida.
 * Los roles se referencian por id, no se cargan como objetos.
 */
public class Usuario extends AggregateRoot {

	private static final Pattern FORMATO_LOGIN = Pattern.compile("[a-z0-9._-]{3,60}");

	private final Long id;

	private final String login;

	private String descripcion;

	private String contrasenaCifrada;

	private String avatar;

	private boolean isWeb;

	private boolean isMovil;

	private boolean estado;

	private Set<Long> rolIds;

	private Usuario(Long id, String login, String contrasenaCifrada) {
		this.id = id;
		this.login = login;
		this.contrasenaCifrada = contrasenaCifrada;
	}

	public static Usuario crear(String login, String descripcion, String contrasenaCifrada, String avatar,
			boolean isWeb, boolean isMovil, Set<Long> rolIds) {
		var usuario = new Usuario(null, normalizarLogin(login), contrasenaCifrada);
		usuario.asignar(descripcion, avatar, isWeb, isMovil, true, rolIds);
		return usuario;
	}

	public static Usuario reconstruir(Long id, String login, String descripcion, String contrasenaCifrada,
			String avatar, boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {
		var usuario = new Usuario(id, login, contrasenaCifrada);
		usuario.descripcion = descripcion;
		usuario.avatar = avatar;
		usuario.isWeb = isWeb;
		usuario.isMovil = isMovil;
		usuario.estado = estado;
		usuario.rolIds = Set.copyOf(rolIds);
		return usuario;
	}

	public static String normalizarLogin(String login) {
		var valor = login == null ? "" : login.strip().toLowerCase(Locale.ROOT);
		if (!FORMATO_LOGIN.matcher(valor).matches()) {
			throw new DomainException(
					"El login debe tener de 3 a 60 caracteres: letras, números, punto, guion o guion bajo");
		}
		return valor;
	}

	/** Si cambian estado, canales o roles, genera {@link UsuarioAccesoModificado}. */
	public void actualizar(String descripcion, String avatar, boolean isWeb, boolean isMovil, boolean estado,
			Set<Long> rolIds, Instant ahora) {
		var cambiaAcceso = this.estado != estado || this.isWeb != isWeb || this.isMovil != isMovil
				|| !this.rolIds.equals(rolIds == null ? Set.of() : Set.copyOf(rolIds));
		asignar(descripcion, avatar, isWeb, isMovil, estado, rolIds);
		if (cambiaAcceso) {
			raise(new UsuarioAccesoModificado(id, ahora));
		}
	}

	/** El administrador pone una contraseña nueva: se cierran todas las sesiones del usuario. */
	public void restablecerContrasena(String nuevaCifrada, Instant ahora) {
		this.contrasenaCifrada = nuevaCifrada;
		raise(new UsuarioAccesoModificado(id, ahora));
	}

	/** El propio usuario la cambia; la feature autenticacion decide qué sesiones cerrar. */
	public void cambiarContrasena(String nuevaCifrada) {
		this.contrasenaCifrada = nuevaCifrada;
	}

	/** Reglas para iniciar sesión por un canal. Se llama después de comprobar la contraseña. */
	public void validarAcceso(Canal canal) {
		if (!estado) {
			throw new AccesoDenegadoException("El usuario está inactivo");
		}
		if (!canal.permitidoPor(isWeb, isMovil)) {
			throw new AccesoDenegadoException("El usuario no tiene acceso por el canal " + canal);
		}
	}

	private void asignar(String descripcion, String avatar, boolean isWeb, boolean isMovil, boolean estado,
			Set<Long> rolIds) {
		if (descripcion == null || descripcion.isBlank()) {
			throw new DomainException("La descripción del usuario es obligatoria");
		}
		if (!isWeb && !isMovil) {
			throw new DomainException("El usuario debe tener acceso web, móvil o ambos");
		}
		if (rolIds == null || rolIds.isEmpty()) {
			throw new DomainException("El usuario debe tener al menos un rol");
		}
		this.descripcion = descripcion.strip();
		this.avatar = avatar == null || avatar.isBlank() ? null : avatar.strip();
		this.isWeb = isWeb;
		this.isMovil = isMovil;
		this.estado = estado;
		this.rolIds = Set.copyOf(rolIds);
	}

	/** Borrado lógico. Desactivar o activar cambia el acceso: genera {@link UsuarioAccesoModificado}. */
	public void cambiarEstado(boolean estado, Instant ahora) {
		if (this.estado != estado) {
			this.estado = estado;
			raise(new UsuarioAccesoModificado(id, ahora));
		}
	}

	public Long id() {
		return id;
	}

	public String login() {
		return login;
	}

	public String descripcion() {
		return descripcion;
	}

	public String contrasenaCifrada() {
		return contrasenaCifrada;
	}

	public String avatar() {
		return avatar;
	}

	public boolean isWeb() {
		return isWeb;
	}

	public boolean isMovil() {
		return isMovil;
	}

	public boolean estado() {
		return estado;
	}

	public Set<Long> rolIds() {
		return rolIds;
	}

}
