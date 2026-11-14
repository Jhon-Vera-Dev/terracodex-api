package com.terracodex.seguridad.rol.application.dto.query;

import com.terracodex.shared.application.pagination.PageQuery;

/** @param estado {@code null} para todos */
public record BuscarRolesQuery(Boolean estado, PageQuery page) {
}
