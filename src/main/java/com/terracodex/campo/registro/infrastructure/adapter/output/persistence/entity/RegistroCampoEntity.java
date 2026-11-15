package com.terracodex.campo.registro.infrastructure.adapter.output.persistence.entity;

import java.time.Instant;

import com.terracodex.shared.infrastructure.adapter.output.persistence.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.terracodex.campo.recomendacion.domain.model.TipoRiego;

/** Tabla transaccional: columnas sin prefijo de entidad, salvo la FK {@code id_usuario}. */
@Entity
@Table(name = "registros_campo")
public class RegistroCampoEntity extends EntidadAuditada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "id_usuario")
	private Long idUsuario;

	private double latitud;

	private double longitud;

	@Column(name = "precision_metros")
	private Double precisionMetros;

	private String zona;

	private String distrito;

	private String provincia;

	private String departamento;

	private String pais;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_riego")
	private TipoRiego tipoRiego;

	@Column(name = "cultivo_codigo")
	private String cultivoCodigo;

	@Column(name = "cultivo_descripcion")
	private String cultivoDescripcion;

	private Double puntaje;

	private boolean sugerido;

	private String observacion;

	@Column(name = "fecha_registro")
	private Instant fechaRegistro;

	private boolean estado;

	protected RegistroCampoEntity() {
	}

	public RegistroCampoEntity(Long id, Long idUsuario, double latitud, double longitud, Double precisionMetros,
			String zona, String distrito, String provincia, String departamento, String pais, TipoRiego tipoRiego,
			String cultivoCodigo, String cultivoDescripcion, Double puntaje, boolean sugerido, String observacion,
			Instant fechaRegistro, boolean estado) {
		this.id = id;
		this.idUsuario = idUsuario;
		this.latitud = latitud;
		this.longitud = longitud;
		this.precisionMetros = precisionMetros;
		this.zona = zona;
		this.distrito = distrito;
		this.provincia = provincia;
		this.departamento = departamento;
		this.pais = pais;
		this.tipoRiego = tipoRiego;
		this.cultivoCodigo = cultivoCodigo;
		this.cultivoDescripcion = cultivoDescripcion;
		this.puntaje = puntaje;
		this.sugerido = sugerido;
		this.observacion = observacion;
		this.fechaRegistro = fechaRegistro;
		this.estado = estado;
	}

	public Long getId() {
		return id;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public double getLatitud() {
		return latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public Double getPrecisionMetros() {
		return precisionMetros;
	}

	public String getZona() {
		return zona;
	}

	public String getDistrito() {
		return distrito;
	}

	public String getProvincia() {
		return provincia;
	}

	public String getDepartamento() {
		return departamento;
	}

	public String getPais() {
		return pais;
	}

	public TipoRiego getTipoRiego() {
		return tipoRiego;
	}

	public String getCultivoCodigo() {
		return cultivoCodigo;
	}

	public String getCultivoDescripcion() {
		return cultivoDescripcion;
	}

	public Double getPuntaje() {
		return puntaje;
	}

	public boolean isSugerido() {
		return sugerido;
	}

	public String getObservacion() {
		return observacion;
	}

	public Instant getFechaRegistro() {
		return fechaRegistro;
	}

	public boolean isEstado() {
		return estado;
	}

}
