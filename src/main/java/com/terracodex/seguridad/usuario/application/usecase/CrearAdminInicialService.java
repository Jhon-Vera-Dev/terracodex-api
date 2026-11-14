package com.terracodex.seguridad.usuario.application.usecase;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.usuario.application.port.input.CrearAdminInicialUseCase;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.PoliticaContrasena;
import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.shared.application.UseCase;

/**
 * La contraseña viene de ADMIN_PASSWORD, nunca de una migración, para no dejar un hash conocido en el repositorio.
 */
@UseCase
class CrearAdminInicialService implements CrearAdminInicialUseCase {

	private static final Logger log = LoggerFactory.getLogger(CrearAdminInicialService.class);

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final CodificadorContrasenaPort codificador;

	private final String login;

	private final String contrasena;

	CrearAdminInicialService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
			CodificadorContrasenaPort codificador, @Value("${app.seguridad.admin-inicial.login}") String login,
			@Value("${app.seguridad.admin-inicial.contrasena}") String contrasena) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.codificador = codificador;
		this.login = login;
		this.contrasena = contrasena;
	}

	@Override
	@Transactional
	public void crearSiNoHayUsuarios() {
		if (usuarios.existeAlguno()) {
			return;
		}
		if (contrasena == null || contrasena.isBlank()) {
			log.warn("No hay usuarios y ADMIN_PASSWORD no está definido: no se creó el administrador inicial");
			return;
		}
		PoliticaContrasena.validar(contrasena);
		var rolAdmin = roles.findByDescripcion(Rol.ADMIN)
			.orElseThrow(() -> new IllegalStateException("Falta el rol ADMIN (migración V1__seguridad.sql)"));
		usuarios.save(Usuario.crear(login, "Administrador", codificador.cifrar(contrasena), null, true, true,
				Set.of(rolAdmin.id())));
		log.info("Administrador inicial '{}' creado", login);
	}

}
