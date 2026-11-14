package com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence;

import static com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper.containsPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.terracodex.seguridad.usuario.application.port.output.UsuarioRepositoryPort;
import com.terracodex.seguridad.usuario.domain.model.Usuario;
import com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.entity.UsuarioEntity;
import com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.mapper.UsuarioPersistenceMapper;
import com.terracodex.seguridad.usuario.infrastructure.adapter.output.persistence.repository.UsuarioJpaRepository;
import com.terracodex.shared.application.pagination.PageQuery;
import com.terracodex.shared.application.pagination.PageResult;
import com.terracodex.shared.infrastructure.adapter.output.persistence.PageableMapper;

import jakarta.persistence.criteria.Predicate;

@Component
class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

	private final UsuarioJpaRepository jpa;

	UsuarioPersistenceAdapter(UsuarioJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Long save(Usuario usuario) {
		return jpa.save(UsuarioPersistenceMapper.toEntity(usuario)).getId();
	}

	@Override
	public Optional<Usuario> findById(Long id) {
		return jpa.findById(id).map(UsuarioPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Usuario> findByLogin(String login) {
		return jpa.findByLogin(login).map(UsuarioPersistenceMapper::toDomain);
	}

	@Override
	public boolean existsByLogin(String login) {
		return jpa.existsByLogin(login);
	}

	@Override
	public List<Usuario> findAllById(Collection<Long> ids) {
		return jpa.findAllById(ids).stream().map(UsuarioPersistenceMapper::toDomain).toList();
	}

	@Override
	public List<Usuario> findActivos() {
		return jpa.findByEstadoTrueOrderByDescripcion().stream().map(UsuarioPersistenceMapper::toDomain).toList();
	}

	@Override
	public boolean existeAlguno() {
		return jpa.count() > 0;
	}

	@Override
	public PageResult<Usuario> buscar(Boolean estado, PageQuery page) {
		Specification<UsuarioEntity> filtro = (root, query, cb) -> {
			var condiciones = new ArrayList<Predicate>();
			if (estado != null) {
				condiciones.add(cb.equal(root.get("estado"), estado));
			}
			if (page.hasSearch()) {
				var patron = containsPattern(page.search());
				condiciones.add(cb.or(cb.like(cb.lower(root.get("login")), patron, '\\'),
						cb.like(cb.lower(root.get("descripcion")), patron, '\\')));
			}
			return cb.and(condiciones.toArray(Predicate[]::new));
		};
		return PageableMapper.toResult(jpa.findAll(filtro, PageableMapper.toPageable(page)),
				UsuarioPersistenceMapper::toDomain);
	}

}
