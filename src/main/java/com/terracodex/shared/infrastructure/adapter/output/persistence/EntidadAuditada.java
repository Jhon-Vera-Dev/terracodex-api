package com.terracodex.shared.infrastructure.adapter.output.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

/**
 * Auditoría de toda tabla de negocio: quién y cuándo creó y modificó el registro. Se llena sola (JPA Auditing):
 * el usuario es el login del token, o {@code SISTEMA} en eventos y procesos automáticos; la hora es la de Lima.
 * <p>
 * Las columnas de creación no se actualizan nunca ({@code updatable = false}). En los maestros, cada entidad las
 * renombra con su sufijo ({@code usuario_creacion_area}) con {@code @AttributeOverride}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadAuditada {

	@CreatedBy
	@Column(name = "usuario_creacion", updatable = false)
	private String usuarioCreacion;

	@CreatedDate
	@Column(name = "fecha_creacion", updatable = false)
	private Instant fechaCreacion;

	@LastModifiedBy
	@Column(name = "usuario_modificacion")
	private String usuarioModificacion;

	@LastModifiedDate
	@Column(name = "fecha_modificacion")
	private Instant fechaModificacion;

	public String getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public Instant getFechaCreacion() {
		return fechaCreacion;
	}

	public String getUsuarioModificacion() {
		return usuarioModificacion;
	}

	public Instant getFechaModificacion() {
		return fechaModificacion;
	}

}
