package com.terracodex.seguridad.usuario.application.usecase;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.usuario.application.dto.command.ActualizarUsuarioCommand;
import com.terracodex.seguridad.usuario.application.port.input.ActualizarUsuarioUseCase;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.PoliticaContrasena;
import com.terracodex.shared.application.DomainEventPublisher;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NotFoundException;

/**
 * Si cambia el acceso del usuario, publica {@code UsuarioAccesoModificado} y la feature autenticacion cierra sus
 * sesiones en la misma transacción. Esta feature no conoce a autenticacion.
 */
@UseCase
class ActualizarUsuarioService implements ActualizarUsuarioUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final CodificadorContrasenaPort codificador;

	private final DomainEventPublisher events;

	private final Clock clock;

	ActualizarUsuarioService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
			CodificadorContrasenaPort codificador, DomainEventPublisher events, Clock clock) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.codificador = codificador;
		this.events = events;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void actualizar(ActualizarUsuarioCommand command) {
		var usuario = usuarios.findById(command.id())
			.orElseThrow(() -> new NotFoundException("Usuario", command.id()));
		// el administrador no puede dejarse a sí mismo sin acceso
		if (usuario.id().equals(command.solicitanteId())) {
			if (!command.estado()) {
				throw new DomainException("No puedes desactivar tu propio usuario");
			}
			var rolAdmin = roles.findByDescripcion(Rol.ADMIN).map(Rol::id);
			if (rolAdmin.isPresent() && !command.rolIds().contains(rolAdmin.get())) {
				throw new DomainException("No puedes quitarte el rol ADMIN");
			}
		}
		roles.verificarExistentes(command.rolIds());
		var ahora = clock.instant();
		usuario.actualizar(command.descripcion(), command.avatar(), command.isWeb(), command.isMovil(),
				command.estado(), command.rolIds(), ahora);
		if (command.nuevaContrasena() != null && !command.nuevaContrasena().isBlank()) {
			PoliticaContrasena.validar(command.nuevaContrasena());
			usuario.restablecerContrasena(codificador.cifrar(command.nuevaContrasena()), ahora);
		}
		usuarios.save(usuario);
		events.publish(usuario.pullEvents());
	}

}
