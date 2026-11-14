package com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.entity;

import com.terracodex.shared.infrastructure.adapter.output.persistence.EntidadAuditada;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Modelo de persistencia. Se traduce con {@code RolPersistenceMapper}; el dominio nunca ve esta clase. */
@Entity
@Table(name = "roles")
@AttributeOverride(name = "usuarioCreacion", column = @Column(name = "usuario_creacion_rol", updatable = false))
@AttributeOverride(name = "fechaCreacion", column = @Column(name = "fecha_creacion_rol", updatable = false))
@AttributeOverride(name = "usuarioModificacion", column = @Column(name = "usuario_modificacion_rol"))
@AttributeOverride(name = "fechaModificacion", column = @Column(name = "fecha_modificacion_rol"))
public class RolEntity extends EntidadAuditada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_rol")
	private Long id;

	@Column(name = "descripcion_rol")
	private String descripcion;

	@Column(name = "estado_rol")
	private boolean estado;

	protected RolEntity() {
	}

	public RolEntity(Long id, String descripcion, boolean estado) {
		this.id = id;
		this.descripcion = descripcion;
		this.estado = estado;
	}

	public Long getId() {
		return id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public boolean isEstado() {
		return estado;
	}

}
