package com.terracodex.seguridad.autenticacion.application.dto.command;

import com.terracodex.seguridad.usuario.domain.model.Canal;

public record IniciarSesionCommand(String login, String contrasena, Canal canal) {
}
