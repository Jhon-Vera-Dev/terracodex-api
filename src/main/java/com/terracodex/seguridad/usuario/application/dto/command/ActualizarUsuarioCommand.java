package com.terracodex.seguridad.usuario.application.dto.command;

import java.util.Set;

/**
 * @param nuevaContrasena opcional: si viene, el administrador restablece la contraseña
 * @param solicitanteId usuario autenticado que hace el cambio
 */
public record ActualizarUsuarioCommand(Long id, String descripcion, String avatar, boolean isWeb, boolean isMovil,
		boolean estado, Set<Long> rolIds, String nuevaContrasena, Long solicitanteId) {
}
