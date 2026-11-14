package com.terracodex.seguridad.usuario.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.usuario.application.dto.command.RegistrarUsuarioCommand;
import com.terracodex.seguridad.usuario.application.port.input.RegistrarUsuarioUseCase;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.PoliticaContrasena;
import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;

@UseCase
class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final CodificadorContrasenaPort codificador;

	RegistrarUsuarioService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
			CodificadorContrasenaPort codificador) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.codificador = codificador;
	}

	@Override
	@Transactional
	public Long registrar(RegistrarUsuarioCommand command) {
		var login = Usuario.normalizarLogin(command.login());
		if (usuarios.existsByLogin(login)) {
			throw new DomainException("Ya existe el usuario " + login);
		}
		PoliticaContrasena.validar(command.contrasena());
		roles.verificarExistentes(command.rolIds());
		var usuario = Usuario.crear(login, command.descripcion(), codificador.cifrar(command.contrasena()),
				command.avatar(), command.isWeb(), command.isMovil(), command.rolIds());
		return usuarios.save(usuario);
	}

}
