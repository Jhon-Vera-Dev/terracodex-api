package com.terracodex.seguridad.usuario.application.dto.query;

import com.terracodex.shared.application.pagination.PageQuery;

/** @param estado {@code null} para todos */
public record BuscarUsuariosQuery(Boolean estado, PageQuery page) {
}
