package com.terracodex.seguridad.autenticacion.application.dto.result;

import java.time.Instant;

/**
 * @param id lo que la app usa para canjear la sesión
 * @param url página de Google que la app abre en el navegador
 */
public record InicioGoogleResult(String id, String url, Instant expiraEn) {
}
