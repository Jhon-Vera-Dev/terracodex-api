package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.terracodex.seguridad.usuario.domain.model.Canal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sesiones")
public class SesionEntity {

	@Id
	private UUID id;

	@Column(name = "id_usuario")
	private Long usuarioId;

	@Enumerated(EnumType.STRING)
	private Canal canal;

	private String refreshHash;

	private Instant creadaEn;

	private Instant expiraEn;

	private Instant revocadaEn;

	protected SesionEntity() {
	}

	public SesionEntity(UUID id, Long usuarioId, Canal canal, String refreshHash, Instant creadaEn, Instant expiraEn,
			Instant revocadaEn) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.canal = canal;
		this.refreshHash = refreshHash;
		this.creadaEn = creadaEn;
		this.expiraEn = expiraEn;
		this.revocadaEn = revocadaEn;
	}

	public UUID getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public Canal getCanal() {
		return canal;
	}

	public String getRefreshHash() {
		return refreshHash;
	}

	public Instant getCreadaEn() {
		return creadaEn;
	}

	public Instant getExpiraEn() {
		return expiraEn;
	}

	public Instant getRevocadaEn() {
		return revocadaEn;
	}

}
