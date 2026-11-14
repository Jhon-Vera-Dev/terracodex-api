package com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence;

import static com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper.containsPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.rol.application.port.output.RolRepositoryPort;
import com.terracodex.seguridad.rol.domain.model.Rol;
import com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.entity.RolEntity;
import com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.mapper.RolPersistenceMapper;
import com.terracodex.seguridad.rol.infrastructure.adapter.output.persistence.repository.RolJpaRepository;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;
import com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper;

import jakarta.persistence.criteria.Predicate;

/** Adaptador de salida: implementa el puerto con Spring Data JPA. */
@Component
class RolPersistenceAdapter implements RolRepositoryPort {

	private final RolJpaRepository jpa;

	RolPersistenceAdapter(RolJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Long save(Rol rol) {
		return jpa.save(RolPersistenceMapper.toEntity(rol)).getId();
	}

	@Override
	public Optional<Rol> findById(Long id) {
		return jpa.findById(id).map(RolPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Rol> findByDescripcion(String descripcion) {
		return jpa.findByDescripcion(descripcion).map(RolPersistenceMapper::toDomain);
	}

	@Override
	public List<Rol> findAllById(Collection<Long> ids) {
		return jpa.findAllById(ids).stream().map(RolPersistenceMapper::toDomain).toList();
	}

	@Override
	public List<Rol> findActivos() {
		return jpa.findByEstadoTrueOrderByDescripcion().stream().map(RolPersistenceMapper::toDomain).toList();
	}

	@Override
	public PageResult<Rol> buscar(Boolean estado, PageQuery page) {
		Specification<RolEntity> filtro = (root, query, cb) -> {
			var condiciones = new ArrayList<Predicate>();
			if (estado != null) {
				condiciones.add(cb.equal(root.get("estado"), estado));
			}
			if (page.hasSearch()) {
				condiciones.add(cb.like(cb.lower(root.get("descripcion")), containsPattern(page.search()), '\\'));
			}
			return cb.and(condiciones.toArray(Predicate[]::new));
		};
		return PageableMapper.toResult(jpa.findAll(filtro, PageableMapper.toPageable(page)),
				RolPersistenceMapper::toDomain);
	}

}
