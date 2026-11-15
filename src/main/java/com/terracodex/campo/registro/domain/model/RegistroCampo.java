package com.terracodex.campo.registro.domain.model;

import java.time.Instant;

import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.recomendacion.domain.model.TipoRiego;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.DomainException;

/**
 * Lo que un usuario registra en campo desde el celular: dónde está la parcela, en qué zona, cómo se riega y qué
 * cultivo tiene. Si eligió el cultivo de la recomendación, se guarda el puntaje que le dio el modelo.
 * <p>
 * Transaccional: no se borra, se anula ({@code estado = false}).
 */
public class RegistroCampo {

	private final Long id;

	private final Long idUsuario;

	private final Coordenada coordenada;

	private final Double precisionMetros;

	private final Zona zona;

	private final TipoRiego tipoRiego;

	private final Cultivo cultivo;

	private final String observacion;

	private final Instant fechaRegistro;

	private boolean estado;

	private RegistroCampo(Long id, Long idUsuario, Coordenada coordenada, Double precisionMetros, Zona zona,
			TipoRiego tipoRiego, Cultivo cultivo, String observacion, Instant fechaRegistro, boolean estado) {
		this.id = id;
		this.idUsuario = idUsuario;
		this.coordenada = coordenada;
		this.precisionMetros = precisionMetros;
		this.zona = zona == null ? Zona.DESCONOCIDA : zona;
		this.tipoRiego = tipoRiego;
		this.cultivo = cultivo;
		this.observacion = observacion;
		this.fechaRegistro = fechaRegistro;
		this.estado = estado;
	}

	public static RegistroCampo registrar(Long idUsuario, Coordenada coordenada, Double precisionMetros, Zona zona,
			TipoRiego tipoRiego, Cultivo cultivo, String observacion, Instant ahora) {
		if (idUsuario == null) {
			throw new DomainException("El registro debe tener un usuario");
		}
		if (coordenada == null) {
			throw new DomainException("Indica la ubicación de la parcela");
		}
		if (tipoRiego == null) {
			throw new DomainException("Indica el tipo de riego");
		}
		if (cultivo == null) {
			throw new DomainException("Indica el cultivo");
		}
		if (precisionMetros != null && (!Double.isFinite(precisionMetros) || precisionMetros < 0)) {
			throw new DomainException("La precisión del GPS no es válida");
		}
		var nota = observacion == null || observacion.isBlank() ? null : observacion.strip();
		return new RegistroCampo(null, idUsuario, coordenada, precisionMetros, zona, tipoRiego, cultivo, nota, ahora,
				true);
	}

	public static RegistroCampo reconstruir(Long id, Long idUsuario, Coordenada coordenada, Double precisionMetros,
			Zona zona, TipoRiego tipoRiego, Cultivo cultivo, String observacion, Instant fechaRegistro, boolean estado) {
		return new RegistroCampo(id, idUsuario, coordenada, precisionMetros, zona, tipoRiego, cultivo, observacion,
				fechaRegistro, estado);
	}

	/** Solo quien lo registró o un supervisor puede anularlo. */
	public void anular(Long solicitante, boolean supervisa) {
		if (!supervisa && !idUsuario.equals(solicitante)) {
			throw new AccesoDenegadoException("Solo puedes anular tus propios registros");
		}
		if (!estado) {
			throw new DomainException("El registro ya está anulado");
		}
		this.estado = false;
	}

	public boolean visiblePara(Long solicitante, boolean supervisa) {
		return supervisa || idUsuario.equals(solicitante);
	}

	public Long id() {
		return id;
	}

	public Long idUsuario() {
		return idUsuario;
	}

	public Coordenada coordenada() {
		return coordenada;
	}

	public Double precisionMetros() {
		return precisionMetros;
	}

	public Zona zona() {
		return zona;
	}

	public TipoRiego tipoRiego() {
		return tipoRiego;
	}

	public Cultivo cultivo() {
		return cultivo;
	}

	public String observacion() {
		return observacion;
	}

	public Instant fechaRegistro() {
		return fechaRegistro;
	}

	public boolean estado() {
		return estado;
	}

}
