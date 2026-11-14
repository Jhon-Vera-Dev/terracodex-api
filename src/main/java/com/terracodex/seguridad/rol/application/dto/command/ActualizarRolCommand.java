package com.terracodex.seguridad.rol.application.dto.command;

public record ActualizarRolCommand(Long id, String descripcion, boolean estado) {
}
