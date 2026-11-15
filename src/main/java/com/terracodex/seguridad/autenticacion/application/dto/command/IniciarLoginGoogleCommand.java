package com.terracodex.seguridad.autenticacion.application.dto.command;

import com.terracodex.seguridad.usuario.domain.model.Canal;

/** @param desafio SHA-256 en hexadecimal del verificador que guarda la app */
public record IniciarLoginGoogleCommand(Canal canal, String desafio) {
}
