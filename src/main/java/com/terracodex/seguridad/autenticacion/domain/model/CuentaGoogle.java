package com.terracodex.seguridad.autenticacion.domain.model;

import java.time.Instant;

/**
 * Vínculo entre una cuenta de Google y un usuario de terracodex. Se identifica por {@code sub} (el id estable de
 * Google), no por el correo, que el dueño puede cambiar.
 */
public record CuentaGoogle(String googleSub, Long usuarioId, String email, Instant vinculadaEn) {
}
