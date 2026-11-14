package com.terracodex.seguridad.menu.application.dto.query;

import com.terracodex.seguridad.usuario.domain.model.Canal;

public record ConsultarMiMenuQuery(Long usuarioId, Canal canal) {
}
