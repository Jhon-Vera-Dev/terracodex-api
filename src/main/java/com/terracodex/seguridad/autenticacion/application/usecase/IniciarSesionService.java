package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.command.IniciarSesionCommand;
import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;
import com.terracodex.seguridad.autenticacion.application.port.input.IniciarSesionUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.EmisorTokenPort;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.RefreshToken;
import com.terracodex.seguridad.autenticacion.domain.model.Sesion;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NoAutenticadoException;

@UseCase
class IniciarSesionService implements IniciarSesionUseCase {

	private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final SesionRepositoryPort sesiones;

	private final CodificadorContrasenaPort codificador;

	private final EmisorTokenPort emisor;

	private final Clock clock;

	private final Duration duracionSesion;

	/** Se compara contra este hash cuando el login no existe, para no revelarlo por el tiempo de respuesta. */
	private final String hashFicticio;

	IniciarSesionService(UsuarioRepositoryPort usuarios, RolRepositoryPort roles, SesionRepositoryPort sesiones,
			CodificadorContrasenaPort codificador, EmisorTokenPort emisor, Clock clock,
			@Value("${app.seguridad.sesion.duracion}") Duration duracionSesion) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.sesiones = sesiones;
		this.codificador = codificador;
		this.emisor = emisor;
		this.clock = clock;
		this.duracionSesion = duracionSesion;
		this.hashFicticio = codificador.cifrar("contrasena-ficticia-0");
	}

	@Override
	@Transactional
	public SesionResult iniciar(IniciarSesionCommand command) {
		var usuario = buscar(command.login());
		var cifrada = usuario == null ? hashFicticio : usuario.contrasenaCifrada();
		if (!codificador.coincide(command.contrasena(), cifrada) || usuario == null) {
			throw new NoAutenticadoException(CREDENCIALES_INVALIDAS);
		}
		usuario.validarAcceso(command.canal());
		var rolesActivos = roles.descripcionesActivas(usuario.rolIds());
		if (rolesActivos.isEmpty()) {
			throw new AccesoDenegadoException("El usuario no tiene roles activos");
		}
		var refresh = RefreshToken.generar();
		var sesion = Sesion.abrir(usuario.id(), command.canal(), refresh.hash(), clock.instant(), duracionSesion);
		sesiones.save(sesion);
		var acceso = emisor.emitir(usuario, rolesActivos, command.canal(), sesion.id());
		return SesionResult.of(acceso, refresh.valor(), sesion, usuario, rolesActivos);
	}

	private Usuario buscar(String login) {
		try {
			return usuarios.findByLogin(Usuario.normalizarLogin(login)).orElse(null);
		}
		catch (DomainException formatoInvalido) {
			return null;
		}
	}

}
