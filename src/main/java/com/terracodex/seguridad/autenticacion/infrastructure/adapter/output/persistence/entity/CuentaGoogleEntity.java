package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas_google")
public class CuentaGoogleEntity {

	@Id
	@Column(name = "google_sub")
	private String googleSub;

	@Column(name = "id_usuario")
	private Long usuarioId;

	private String email;

	@Column(name = "vinculada_en")
	private Instant vinculadaEn;

	protected CuentaGoogleEntity() {
	}

	public CuentaGoogleEntity(String googleSub, Long usuarioId, String email, Instant vinculadaEn) {
		this.googleSub = googleSub;
		this.usuarioId = usuarioId;
		this.email = email;
		this.vinculadaEn = vinculadaEn;
	}

	public String getGoogleSub() {
		return googleSub;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public String getEmail() {
		return email;
	}

	public Instant getVinculadaEn() {
		return vinculadaEn;
	}

}
