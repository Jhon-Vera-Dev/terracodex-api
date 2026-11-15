package com.terracodex.seguridad.autenticacion.application.dto.result;

import com.terracodex.seguridad.usuario.domain.model.Canal;

/**
 * Resultado del callback de Google.
 *
 * @param completado {@code true} si el usuario quedó identificado; si no, el motivo lo recibe la app al canjear
 * @param canal canal del inicio, o {@code null} si el {@code state} no corresponde a ningún inicio
 */
public record CallbackGoogleResult(boolean completado, Canal canal) {

}
