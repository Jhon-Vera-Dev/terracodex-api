package com.terracodex.campo.registro.infrastructure.adapter.output.persistence.mapper;

import com.terracodex.campo.recomendacion.domain.model.Coordenada;
import com.terracodex.campo.recomendacion.domain.model.Zona;
import com.terracodex.campo.registro.domain.model.Cultivo;
import com.terracodex.campo.registro.domain.model.RegistroCampo;
import com.terracodex.campo.registro.infrastructure.adapter.output.persistence.entity.RegistroCampoEntity;

public final class RegistroCampoPersistenceMapper {

	private RegistroCampoPersistenceMapper() {
	}

	public static RegistroCampoEntity toEntity(RegistroCampo r) {
		var z = r.zona();
		var c = r.cultivo();
		return new RegistroCampoEntity(r.id(), r.idUsuario(), r.coordenada().latitud(), r.coordenada().longitud(),
				r.precisionMetros(), z.descripcion(), z.distrito(), z.provincia(), z.departamento(), z.pais(),
				r.tipoRiego(), c.codigo(), c.descripcion(), c.puntaje(), c.sugerido(), r.observacion(),
				r.fechaRegistro(), r.estado());
	}

	public static RegistroCampo toDomain(RegistroCampoEntity e) {
		return RegistroCampo.reconstruir(e.getId(), e.getIdUsuario(), new Coordenada(e.getLatitud(), e.getLongitud()),
				e.getPrecisionMetros(),
				new Zona(e.getZona(), e.getDistrito(), e.getProvincia(), e.getDepartamento(), e.getPais()),
				e.getTipoRiego(),
				new Cultivo(e.getCultivoCodigo(), e.getCultivoDescripcion(), e.getPuntaje(), e.isSugerido()),
				e.getObservacion(), e.getFechaRegistro(), e.isEstado());
	}

}
