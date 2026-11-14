package com.terracodex.seguridad.menu.infrastructure.adapter.output.persistence.entity;

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

@Entity
@Table(name = "menus")
@AttributeOverride(name = "usuarioCreacion", column = @Column(name = "usuario_creacion_menu", updatable = false))
@AttributeOverride(name = "fechaCreacion", column = @Column(name = "fecha_creacion_menu", updatable = false))
@AttributeOverride(name = "usuarioModificacion", column = @Column(name = "usuario_modificacion_menu"))
@AttributeOverride(name = "fechaModificacion", column = @Column(name = "fecha_modificacion_menu"))
public class MenuEntity extends EntidadAuditada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_menu")
	private Long id;

	@Column(name = "descripcion_menu")
	private String descripcion;

	@Column(name = "path_menu")
	private String path;

	@Column(name = "icon_menu")
	private String icon;

	@Column(name = "orden_menu")
	private int orden;

	@Column(name = "id_menu_padre")
	private Long padreId;

	@Column(name = "is_padre_menu")
	private boolean isPadre;

	@Column(name = "is_web_menu")
	private boolean isWeb;

	@Column(name = "is_movil_menu")
	private boolean isMovil;

	@Column(name = "estado_menu")
	private boolean estado;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "menu_roles", joinColumns = @JoinColumn(name = "id_menu"))
	@Column(name = "id_rol")
	private Set<Long> rolIds = new HashSet<>();

	protected MenuEntity() {
	}

	public MenuEntity(Long id, String descripcion, String path, String icon, int orden, Long padreId,
			boolean isPadre, boolean isWeb, boolean isMovil, boolean estado, Set<Long> rolIds) {
		this.id = id;
		this.descripcion = descripcion;
		this.path = path;
		this.icon = icon;
		this.orden = orden;
		this.padreId = padreId;
		this.isPadre = isPadre;
		this.isWeb = isWeb;
		this.isMovil = isMovil;
		this.estado = estado;
		this.rolIds = new HashSet<>(rolIds);
	}

	public Long getId() {
		return id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getPath() {
		return path;
	}

	public String getIcon() {
		return icon;
	}

	public int getOrden() {
		return orden;
	}

	public Long getPadreId() {
		return padreId;
	}

	public boolean isPadre() {
		return isPadre;
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
