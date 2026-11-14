package com.terracodex.seguridad.autenticacion.infrastructure.adapter.input.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.autenticacion.application.port.input.RevocarSesionesUseCase;
import com.terracodex.seguridad.usuario.domain.events.UsuarioAccesoModificado;

/**
 * Adaptador de entrada por evento. Es síncrono a propósito: la revocación ocurre en la misma transacción que el
 * cambio del usuario, así no queda ni un instante con sesiones que tienen permisos viejos.
 */
@Component
class UsuarioAccesoModificadoListener {

	private final RevocarSesionesUseCase revocar;

	UsuarioAccesoModificadoListener(RevocarSesionesUseCase revocar) {
		this.revocar = revocar;
	}

	@EventListener
	void on(UsuarioAccesoModificado evento) {
		revocar.revocarTodas(evento.usuarioId());
	}

}
