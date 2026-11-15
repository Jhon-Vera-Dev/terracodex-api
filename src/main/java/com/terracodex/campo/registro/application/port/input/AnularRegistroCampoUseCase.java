package com.terracodex.campo.registro.application.port.input;

public interface AnularRegistroCampoUseCase {

	void anular(Long id, Long solicitante, boolean supervisa);

}
