package com.terracodex.campo.registro.infrastructure.adapter.output.persistence;

import static com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper.containsPattern;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.terracodex.campo.registro.application.port.output.RegistroCampoRepositoryPort;
import com.terracodex.campo.registro.domain.model.RegistroCampo;
import com.terracodex.campo.registro.infrastructure.adapter.output.persistence.entity.RegistroCampoEntity;
import com.terracodex.campo.registro.infrastructure.adapter.output.persistence.mapper.RegistroCampoPersistenceMapper;
import com.terracodex.campo.registro.infrastructure.adapter.output.persistence.repository.RegistroCampoJpaRepository;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;
import com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper;

import jakarta.persistence.criteria.Predicate;

@Component
class RegistroCampoPersistenceAdapter implements RegistroCampoRepositoryPort {

	private final RegistroCampoJpaRepository jpa;

	RegistroCampoPersistenceAdapter(RegistroCampoJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Long save(RegistroCampo registro) {
		return jpa.save(RegistroCampoPersistenceMapper.toEntity(registro)).getId();
	}

	@Override
	public Optional<RegistroCampo> findById(Long id) {
		return jpa.findById(id).map(RegistroCampoPersistenceMapper::toDomain);
	}

	@Override
	public PageResult<RegistroCampo> buscar(Long idUsuario, Boolean estado, PageQuery page) {
		Specification<RegistroCampoEntity> filtro = (root, query, cb) -> {
			var condiciones = new ArrayList<Predicate>();
			if (idUsuario != null) {
				condiciones.add(cb.equal(root.get("idUsuario"), idUsuario));
			}
			if (estado != null) {
				condiciones.add(cb.equal(root.get("estado"), estado));
			}
			if (page.hasSearch()) {
				var patron = containsPattern(page.search());
				condiciones.add(cb.or(cb.like(cb.lower(root.get("cultivoDescripcion")), patron, '\\'),
						cb.like(cb.lower(root.get("zona")), patron, '\\')));
			}
			return cb.and(condiciones.toArray(Predicate[]::new));
		};
		return PageableMapper.toResult(jpa.findAll(filtro, PageableMapper.toPageable(page)),
				RegistroCampoPersistenceMapper::toDomain);
	}

}
