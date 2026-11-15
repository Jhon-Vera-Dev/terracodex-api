package com.terracodex.seguridad.autenticacion.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.result.SesionResult;
import com.terracodex.seguridad.autenticacion.application.port.input.CanjearLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.EmisorTokenPort;
import com.terracodex.seguridad.autenticacion.application.port.output.InicioGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.application.port.output.SesionRepositoryPort;
import com.terracodex.seguridad.autenticacion.domain.model.RefreshToken;
import com.terracodex.seguridad.autenticacion.domain.model.Sesion;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.AccesoDenegadoException;
import com.terracodex.shared.domain.NoAutenticadoException;

/** Mismas reglas que el login con contraseña: usuario activo, canal permitido y al menos un rol activo. */
@UseCase
class CanjearLoginGoogleService implements CanjearLoginGoogleUseCase {

	private static final String NO_VALIDO = "El inicio de sesión con Google no es válido o ya se usó. Vuelve a intentarlo";

	private final InicioGoogleRepositoryPort inicios;

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final SesionRepositoryPort sesiones;

	private final EmisorTokenPort emisor;

	private final Clock clock;

	private final Duration duracionSesion;

	CanjearLoginGoogleService(InicioGoogleRepositoryPort inicios, UsuarioRepositoryPort usuarios,
			RolRepositoryPort roles, SesionRepositoryPort sesiones, EmisorTokenPort emisor, Clock clock,
			@Value("${app.seguridad.sesion.duracion}") Duration duracionSesion) {
		this.inicios = inicios;
		this.usuarios = usuarios;
		this.roles = roles;
		this.sesiones = sesiones;
		this.emisor = emisor;
		this.clock = clock;
		this.duracionSesion = duracionSesion;
	}

	// noRollbackFor: si el canje falla después de marcarlo consumido, igual no se puede reutilizar
	@Override
	@Transactional(noRollbackFor = { NoAutenticadoException.class, AccesoDenegadoException.class })
	public Optional<SesionResult> canjear(String id, String verificador) {
		var inicio = id == null ? null : inicios.findById(id).orElse(null);
		if (inicio == null) {
			throw new NoAutenticadoException(NO_VALIDO);
		}
		var ahora = clock.instant();
		var usuarioId = inicio.canjear(verificador, ahora);
		if (usuarioId == null) {
			return Optional.empty();
		}
		inicios.save(inicio);

		var usuario = usuarios.findById(usuarioId).orElseThrow(() -> new NoAutenticadoException(NO_VALIDO));
		usuario.validarAcceso(inicio.canal());
		var rolesActivos = roles.descripcionesActivas(usuario.rolIds());
		if (rolesActivos.isEmpty()) {
			throw new AccesoDenegadoException("El usuario no tiene roles activos");
		}
		var refresh = RefreshToken.generar();
		var sesion = Sesion.abrir(usuario.id(), inicio.canal(), refresh.hash(), ahora, duracionSesion);
		sesiones.save(sesion);
		var acceso = emisor.emitir(usuario, rolesActivos, inicio.canal(), sesion.id());
		return Optional.of(SesionResult.of(acceso, refresh.valor(), sesion, usuario, rolesActivos));
	}

}
