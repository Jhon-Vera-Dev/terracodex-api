package com.terracodex.seguridad.usuario.application.dto.command;

import java.util.Set;

public record RegistrarUsuarioCommand(String login, String descripcion, String contrasena, String avatar,
		boolean isWeb, boolean isMovil, Set<Long> rolIds) {
}
