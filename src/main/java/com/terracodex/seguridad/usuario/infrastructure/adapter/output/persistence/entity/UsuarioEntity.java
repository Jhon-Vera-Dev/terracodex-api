package com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.entity;

import java.util.HashSet;
import java.util.Set;

import com.terracodex.shared.infrastructure.adapter.output.persistence.EntidadAuditada;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** Los roles se guardan como ids en usuario_roles: Usuario y Rol son agregados distintos. */
@Entity
@Table(name = "usuarios")
@AttributeOverride(name = "usuarioCreacion", column = @Column(name = "usuario_creacion_usuario", updatable = false))
@AttributeOverride(name = "fechaCreacion", column = @Column(name = "fecha_creacion_usuario", updatable = false))
@AttributeOverride(name = "usuarioModificacion", column = @Column(name = "usuario_modificacion_usuario"))
@AttributeOverride(name = "fechaModificacion", column = @Column(name = "fecha_modificacion_usuario"))
public class UsuarioEntity extends EntidadAuditada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_usuario")
	private Long id;

	@Column(name = "login_usuario")
	private String login;

	@Column(name = "descripcion_usuario")
	private String descripcion;

	@Column(name = "contrasena_usuario")
	private String contrasena;

	@Column(name = "avatar_usuario")
	private String avatar;

	@Column(name = "is_web_usuario")
	private boolean isWeb;

	@Column(name = "is_movil_usuario")
	private boolean isMovil;

	@Column(name = "estado_usuario")
	private boolean estado;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "id_usuario"))
	@Column(name = "id_rol")
	private Set<Long> rolIds = new HashSet<>();

	protected UsuarioEntity() {
	}

	public UsuarioEntity(Long id, String login, String descripcion, String contrasena, String avatar, boolean isWeb,
			boolean isMovil, boolean estado, Set<Long> rolIds) {
		this.id = id;
		this.login = login;
		this.descripcion = descripcion;
		this.contrasena = contrasena;
		this.avatar = avatar;
		this.isWeb = isWeb;
		this.isMovil = isMovil;
		this.estado = estado;
		this.rolIds = new HashSet<>(rolIds);
	}

	public Long getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getContrasena() {
		return contrasena;
	}

	public String getAvatar() {
		return avatar;
	}

	public boolean isWeb() {
		return isWeb;
	}

	public boolean isMovil() {
		return isMovil;
	}

	public boolean isEstado() {
		return estado;
	}

	public Set<Long> getRolIds() {
		return rolIds;
	}

}
