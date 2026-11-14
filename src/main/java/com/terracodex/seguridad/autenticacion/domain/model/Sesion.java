package com.terracodex.seguridad.autenticacion.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.shared.domain.NoAutenticadoException;

/**
 * Sesión abierta por un inicio de sesión. Su id viaja en el access token (claim {@code sid}) y cada petición
 * comprueba que siga activa: revocarla corta el acceso al instante, sin esperar a que el token expire.
 * Guarda solo el hash del refresh token, que rota en cada renovación.
 */
public class Sesion {

	private final UUID id;

	private final Long usuarioId;

	private final Canal canal;

	private String refreshHash;

	private final Instant creadaEn;

	private Instant expiraEn;

	private Instant revocadaEn;

	private Sesion(UUID id, Long usuarioId, Canal canal, String refreshHash, Instant creadaEn, Instant expiraEn,
			Instant revocadaEn) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.canal = canal;
		this.refreshHash = refreshHash;
		this.creadaEn = creadaEn;
		this.expiraEn = expiraEn;
		this.revocadaEn = revocadaEn;
	}

	public static Sesion abrir(Long usuarioId, Canal canal, String refreshHash, Instant ahora, Duration duracion) {
		return new Sesion(UUID.randomUUID(), usuarioId, canal, refreshHash, ahora, ahora.plus(duracion), null);
	}

	public static Sesion reconstruir(UUID id, Long usuarioId, Canal canal, String refreshHash, Instant creadaEn,
			Instant expiraEn, Instant revocadaEn) {
		return new Sesion(id, usuarioId, canal, refreshHash, creadaEn, expiraEn, revocadaEn);
	}

	/** Rota el refresh token: el anterior deja de servir. La sesión se extiende desde ahora. */
	public void renovar(String nuevoRefreshHash, Instant ahora, Duration duracion) {
		if (!activaEn(ahora)) {
			throw new NoAutenticadoException("La sesión expiró o fue cerrada");
		}
		this.refreshHash = nuevoRefreshHash;
		this.expiraEn = ahora.plus(duracion);
	}

	public void revocar(Instant ahora) {
		if (revocadaEn == null) {
			revocadaEn = ahora;
		}
	}

	public boolean activaEn(Instant ahora) {
		return revocadaEn == null && ahora.isBefore(expiraEn);
	}

	public UUID id() {
		return id;
	}

	public Long usuarioId() {
		return usuarioId;
	}

	public Canal canal() {
		return canal;
	}

	public String refreshHash() {
		return refreshHash;
	}

	public Instant creadaEn() {
		return creadaEn;
	}

	public Instant expiraEn() {
		return expiraEn;
	}

	public Instant revocadaEn() {
		return revocadaEn;
	}

}
