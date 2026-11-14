package com.terracodex.seguridad.autenticacion.application.dto.command;

import java.util.UUID;

/** @param sesionActual sesión desde la que se hace el cambio; es la única que queda abierta */
public record CambiarContrasenaCommand(Long usuarioId, UUID sesionActual, String actual, String nueva) {
}
