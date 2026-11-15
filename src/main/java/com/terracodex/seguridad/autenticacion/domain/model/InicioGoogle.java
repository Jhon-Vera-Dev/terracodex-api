package com.terracodex.seguridad.autenticacion.domain.model;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import com.terracodex.seguridad.usuario.domain.model.Canal;
import com.terracodex.shared.domain.NoAutenticadoException;

/**
 * Un intento de inicio de sesión con Google, de la app al navegador y de vuelta.
 * <ol>
 * <li>La app genera un verificador secreto y envía solo su SHA-256 ({@code desafio}); recibe el {@code id} (el
 * {@code state} de OAuth) y abre el navegador.</li>
 * <li>Google redirige al backend con {@code state} y {@code code}: el inicio queda completado con el usuario.</li>
 * <li>La app canjea {@code id} + verificador por la sesión, una sola vez. Quien solo vea la URL no tiene el
 * verificador y no puede llevarse la sesión.</li>
 * </ol>
 */
public class InicioGoogle {

	public enum Estado {

		PENDIENTE, COMPLETADO, FALLIDO, CONSUMIDO

	}

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String NO_VALIDO = "El inicio de sesión con Google no es válido o ya se usó. Vuelve a intentarlo";

	private final String id;

	private final String desafio;

	private final Canal canal;

	private final Instant creadoEn;

	private final Instant expiraEn;

	private Long usuarioId;

	private String error;

	private Instant consumidoEn;

	private InicioGoogle(String id, String desafio, Canal canal, Instant creadoEn, Instant expiraEn, Long usuarioId,
			String error, Instant consumidoEn) {
		this.id = id;
		this.desafio = desafio;
		this.canal = canal;
		this.creadoEn = creadoEn;
		this.expiraEn = expiraEn;
		this.usuarioId = usuarioId;
		this.error = error;
		this.consumidoEn = consumidoEn;
	}

	/** @param desafio SHA-256 en hexadecimal (64 caracteres) del verificador que guarda la app */
	public static InicioGoogle crear(String desafio, Canal canal, Instant ahora, Duration vigencia) {
		var valor = desafio == null ? "" : desafio.strip().toLowerCase();
		if (!valor.matches("[0-9a-f]{64}")) {
			throw new IllegalArgumentException("desafio debe ser el SHA-256 en hexadecimal del verificador");
		}
		if (canal == null) {
			throw new IllegalArgumentException("canal es obligatorio");
		}
		var bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		var id = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return new InicioGoogle(id, valor, canal, ahora, ahora.plus(vigencia), null, null, null);
	}

	public static InicioGoogle reconstruir(String id, String desafio, Canal canal, Instant creadoEn, Instant expiraEn,
			Long usuarioId, String error, Instant consumidoEn) {
		return new InicioGoogle(id, desafio, canal, creadoEn, expiraEn, usuarioId, error, consumidoEn);
	}

	public Estado estado() {
		if (consumidoEn != null) {
			return Estado.CONSUMIDO;
		}
		if (error != null) {
			return Estado.FALLIDO;
		}
		return usuarioId != null ? Estado.COMPLETADO : Estado.PENDIENTE;
	}

	public boolean vigenteEn(Instant ahora) {
		return ahora.isBefore(expiraEn);
	}

	/** Google confirmó la cuenta. Volver a completar (el usuario recargó la página) no cambia nada. */
	public void completar(Long usuarioId) {
		if (estado() == Estado.PENDIENTE) {
			this.usuarioId = usuarioId;
		}
	}

	public void fallar(String mensaje) {
		if (estado() == Estado.PENDIENTE) {
			this.error = mensaje == null || mensaje.isBlank() ? "No se pudo iniciar sesión con Google" : mensaje;
		}
	}

	/**
	 * Comprueba el verificador. Lo llama la app mientras espera.
	 *
	 * @return el usuario si Google ya confirmó, o {@code null} si todavía está pendiente
	 * @throws NoAutenticadoException si el verificador no coincide, venció, falló o ya se canjeó
	 */
	public Long canjear(String verificador, Instant ahora) {
		if (verificador == null || !coincide(RefreshToken.hashDe(verificador), desafio)) {
			throw new NoAutenticadoException(NO_VALIDO);
		}
		switch (estado()) {
			case CONSUMIDO -> throw new NoAutenticadoException(NO_VALIDO);
			case FALLIDO -> throw new NoAutenticadoException(error);
			case PENDIENTE -> {
				if (!vigenteEn(ahora)) {
					throw new NoAutenticadoException("Se agotó el tiempo para iniciar sesión con Google. Vuelve a intentarlo");
				}
				return null;
			}
			case COMPLETADO -> {
				this.consumidoEn = ahora;
				return usuarioId;
			}
		}
		throw new IllegalStateException();
	}

	private static boolean coincide(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}

	public String id() {
		return id;
	}

	public String desafio() {
		return desafio;
	}

	public Canal canal() {
		return canal;
	}

	public Instant creadoEn() {
		return creadoEn;
	}

	public Instant expiraEn() {
		return expiraEn;
	}

	public Long usuarioId() {
		return usuarioId;
	}

	public String error() {
		return error;
	}

	public Instant consumidoEn() {
		return consumidoEn;
	}

}
