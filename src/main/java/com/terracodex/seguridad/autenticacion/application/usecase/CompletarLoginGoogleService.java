package com.terracodex.seguridad.autenticacion.application.usecase;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.terracodex.seguridad.autenticacion.application.dto.result.CallbackGoogleResult;
import com.terracodex.seguridad.autenticacion.application.port.input.CompletarLoginGoogleUseCase;
import com.terracodex.seguridad.autenticacion.application.port.output.CuentaGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.application.port.output.InicioGoogleRepositoryPort;
import com.terracodex.seguridad.autenticacion.application.port.output.ProveedorGooglePort;
import com.terracodex.seguridad.autenticacion.domain.model.CuentaGoogle;
import com.terracodex.seguridad.autenticacion.domain.model.InicioGoogle;
import com.terracodex.seguridad.autenticacion.domain.model.PerfilGoogle;
import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.usuario.application.port.output.CodificadorContrasenaPort;
import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.shared.application.UseCase;
import com.terracodex.shared.domain.DomainException;
import com.terracodex.shared.domain.NoAutenticadoException;
import com.terracodex.shared.domain.ServicioNoDisponibleException;

/**
 * Callback de Google. Los fallos no se lanzan: se guardan en el inicio para que la app los muestre al canjear (el
 * navegador solo ve una página de "vuelve a la app").
 */
@UseCase
class CompletarLoginGoogleService implements CompletarLoginGoogleUseCase {

	/** Rol de quien se registra solo con Google. El administrador puede cambiarlo después. */
	static final String ROL_NUEVOS = "OPERADOR";

	private static final Logger log = LoggerFactory.getLogger(CompletarLoginGoogleService.class);

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ProveedorGooglePort google;

	private final InicioGoogleRepositoryPort inicios;

	private final CuentaGoogleRepositoryPort cuentas;

	private final UsuarioRepositoryPort usuarios;

	private final RolRepositoryPort roles;

	private final CodificadorContrasenaPort codificador;

	private final Clock clock;

	CompletarLoginGoogleService(ProveedorGooglePort google, InicioGoogleRepositoryPort inicios,
			CuentaGoogleRepositoryPort cuentas, UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
			CodificadorContrasenaPort codificador, Clock clock) {
		this.google = google;
		this.inicios = inicios;
		this.cuentas = cuentas;
		this.usuarios = usuarios;
		this.roles = roles;
		this.codificador = codificador;
		this.clock = clock;
	}

	@Override
	@Transactional
	public CallbackGoogleResult completar(String state, String code, String errorGoogle) {
		var inicio = state == null ? null : inicios.findById(state).orElse(null);
		if (inicio == null) {
			return new CallbackGoogleResult(false, null);
		}
		return new CallbackGoogleResult(procesar(inicio, code, errorGoogle), inicio.canal());
	}

	private boolean procesar(InicioGoogle inicio, String code, String errorGoogle) {
		if (inicio.estado() != InicioGoogle.Estado.PENDIENTE) {
			// Recargó la página del callback: no se vuelve a canjear el código
			return inicio.estado() == InicioGoogle.Estado.COMPLETADO;
		}
		if (!inicio.vigenteEn(clock.instant())) {
			return fallar(inicio, "Se agotó el tiempo para iniciar sesión con Google. Vuelve a intentarlo");
		}
		if (errorGoogle != null || code == null || code.isBlank()) {
			return fallar(inicio, "Cancelaste el inicio de sesión con Google");
		}
		try {
			var perfil = google.perfil(code);
			inicio.completar(usuarioDe(perfil));
			inicios.save(inicio);
			return true;
		}
		catch (NoAutenticadoException | DomainException | ServicioNoDisponibleException ex) {
			log.warn("Login con Google fallido: {}", ex.getMessage());
			return fallar(inicio, ex.getMessage());
		}
	}

	private Long usuarioDe(PerfilGoogle perfil) {
		var existente = cuentas.findByGoogleSub(perfil.sub());
		if (existente.isPresent()) {
			return existente.get().usuarioId();
		}
		var rol = roles.findByDescripcion(ROL_NUEVOS)
			.filter(r -> r.estado())
			.orElseThrow(() -> new DomainException("No se pueden registrar usuarios nuevos: falta el rol " + ROL_NUEVOS));
		// Contraseña aleatoria que nadie conoce: la cuenta solo entra con Google hasta que el admin le ponga una
		var contrasena = codificador.cifrar(Base64.getEncoder().encodeToString(aleatorio()));
		// Web y móvil: cualquier cuenta de Google entra por los dos canales
		var usuario = Usuario.crear(loginDisponible(perfil), perfil.descripcion(), contrasena, perfil.avatar(), true,
				true, Set.of(rol.id()));
		var id = usuarios.save(usuario);
		cuentas.save(new CuentaGoogle(perfil.sub(), id, perfil.email(), clock.instant()));
		log.info("Usuario {} creado con Google ({})", usuario.login(), perfil.email());
		return id;
	}

	private String loginDisponible(PerfilGoogle perfil) {
		var base = perfil.loginSugerido();
		var login = base;
		for (var n = 2; usuarios.existsByLogin(login); n++) {
			login = base + "-" + n;
		}
		return login;
	}

	private boolean fallar(InicioGoogle inicio, String mensaje) {
		inicio.fallar(mensaje);
		inicios.save(inicio);
		return false;
	}

	private static byte[] aleatorio() {
		var bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

}
