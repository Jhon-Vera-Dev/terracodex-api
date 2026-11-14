package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;
import com.terracodex.seguridad.autenticacion.application.port.input.RenovarSesionUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.EmisorTokenPort;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.RefreshToken;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.NoAutenticadoException;

/**
 * Vuelve a revisar el usuario: si lo desactivaron o le quitaron el canal o los roles, la renovación falla y la
 * sesión se revoca.
 */
@UseCase
class RenovarSesionService implements RenovarSesionUseCase {

	private static final String SESION_TERMINADA = "La sesión expiró o fue cerrada, vuelve a ingresar";

	private final SesionRepositoryPort sesiones;

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final EmisorTokenPort emisor;

	private final Clock clock;

	private final Duration duracionSesion;

	RenovarSesionService(SesionRepositoryPort sesiones, UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
			EmisorTokenPort emisor, Clock clock, @Value("${app.seguridad.sesion.duracion}") Duration duracionSesion) {
		this.sesiones = sesiones;
		this.usuarios = usuarios;
		this.roles = roles;
		this.emisor = emisor;
		this.clock = clock;
		this.duracionSesion = duracionSesion;
	}

	// noRollbackFor: si el usuario ya no tiene acceso, la revocación de la sesión debe quedar guardada
	@Override
	@Transactional(noRollbackFor = AccesoDenegadoException.class)
	public SesionResult renovar(String refreshToken) {
		var ahora = clock.instant();
		var sesion = sesiones.findByRefreshHash(RefreshToken.hashDe(refreshToken))
			.filter(s -> s.activaEn(ahora))
			.orElseThrow(() -> new NoAutenticadoException(SESION_TERMINADA));
		var usuario = usuarios.findById(sesion.usuarioId())
			.orElseThrow(() -> new NoAutenticadoException(SESION_TERMINADA));
		var rolesActivos = roles.descripcionesActivas(usuario.rolIds());
		try {
			usuario.validarAcceso(sesion.canal());
			if (rolesActivos.isEmpty()) {
				throw new AccesoDenegadoException("El usuario no tiene roles activos");
			}
		}
		catch (AccesoDenegadoException ex) {
			sesion.revocar(ahora);
			sesiones.save(sesion);
			throw ex;
		}
		var nuevo = RefreshToken.generar();
		sesion.renovar(nuevo.hash(), ahora, duracionSesion);
		sesiones.save(sesion);
		var acceso = emisor.emitir(usuario, rolesActivos, sesion.canal(), sesion.id());
		return SesionResult.of(acceso, nuevo.valor(), sesion, usuario, rolesActivos);
	}

}
