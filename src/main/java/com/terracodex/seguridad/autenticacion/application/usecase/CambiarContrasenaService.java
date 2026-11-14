package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.command.CambiarContrasenaCommand;
import com.terracodex.seguridad.autenticacion.application.port.input.CambiarContrasenaUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.PoliticaContrasena;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NotFoundException;

/** Vive en autenticacion porque depende de la sesión actual: es la única que queda abierta. */
@UseCase
class CambiarContrasenaService implements CambiarContrasenaUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final SesionRepositoryPort sesiones;

	private final CodificadorContrasenaPort codificador;

	private final Clock clock;

	CambiarContrasenaService(UsuarioRepositoryPort usuarios, SesionRepositoryPort sesiones,
			CodificadorContrasenaPort codificador, Clock clock) {
		this.usuarios = usuarios;
		this.sesiones = sesiones;
		this.codificador = codificador;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void cambiar(CambiarContrasenaCommand command) {
		var usuario = usuarios.findById(command.usuarioId())
			.orElseThrow(() -> new NotFoundException("Usuario", command.usuarioId()));
		if (!codificador.coincide(command.actual(), usuario.contrasenaCifrada())) {
			throw new DomainException("La contraseña actual no es correcta");
		}
		PoliticaContrasena.validar(command.nueva());
		usuario.cambiarContrasena(codificador.cifrar(command.nueva()));
		usuarios.save(usuario);
		sesiones.revocarTodas(usuario.id(), command.sesionActual(), clock.instant());
	}

}
