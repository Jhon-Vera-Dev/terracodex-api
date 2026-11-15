package com.terracodex.seguridad.autenticacion.infrastructure.adapter.output.persistence.entity;

import java.time.Instant;

import com.terracodex.seguridad.usuario.domain.model.Canal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inicios_google")
public class InicioGoogleEntity {

	@Id
	private String id;

	private String desafio;

	@Enumerated(EnumType.STRING)
	private Canal canal;

	@Column(name = "creado_en")
	private Instant creadoEn;

	@Column(name = "expira_en")
	private Instant expiraEn;

	@Column(name = "id_usuario")
	private Long usuarioId;

	private String error;

	@Column(name = "consumido_en")
	private Instant consumidoEn;

	protected InicioGoogleEntity() {
	}

	public InicioGoogleEntity(String id, String desafio, Canal canal, Instant creadoEn, Instant expiraEn,
			Long usuarioId, String error, Instant consumidoEn) {
		this.id = id;
		this.desafio = desafio;
		this.canal = canal;
		this.creadoEn = creadoEn;
		this.expiraEn = expiraEn;
		this.usuarioId = usuarioId;
		this.error = error;
		this.consumidoEn = consumidoEn;
	}

	public String getId() {
		return id;
	}

	public String getDesafio() {
		return desafio;
	}

	public Canal getCanal() {
		return canal;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public Instant getExpiraEn() {
		return expiraEn;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public String getError() {
		return error;
	}

	public Instant getConsumidoEn() {
		return consumidoEn;
	}

}
