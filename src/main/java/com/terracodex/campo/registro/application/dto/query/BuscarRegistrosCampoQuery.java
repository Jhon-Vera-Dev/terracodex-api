package com.terracodex.campo.registro.application.dto.query;

import com.terracodex.shared.application.pagination.PageQuery;

/**
 * @param solicitante id del usuario que consulta
 * @param todos {@code true} para ver los de todos los usuarios; solo lo puede pedir quien supervisa
 * @param supervisa ADMIN o SUPERVISOR
 * @param estado {@code null} para activos y anulados
 */
public record BuscarRegistrosCampoQuery(Long solicitante, boolean todos, boolean supervisa, Boolean estado,
		PageQuery page) {
}
