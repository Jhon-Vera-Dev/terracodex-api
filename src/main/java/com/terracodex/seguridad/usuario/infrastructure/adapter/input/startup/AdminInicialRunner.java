package com.terracodex.seguridad.usuario.infrastructure.adapter.input.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.usuario.application.port.input.CrearAdminInicialUseCase;

/** Adaptador de entrada: el arranque de la aplicación dispara el caso de uso. */
@Component
class AdminInicialRunner implements ApplicationRunner {

	private final CrearAdminInicialUseCase crearAdmin;

	AdminInicialRunner(CrearAdminInicialUseCase crearAdmin) {
		this.crearAdmin = crearAdmin;
	}

	@Override
	public void run(ApplicationArguments args) {
		crearAdmin.crearSiNoHayUsuarios();
	}

}
