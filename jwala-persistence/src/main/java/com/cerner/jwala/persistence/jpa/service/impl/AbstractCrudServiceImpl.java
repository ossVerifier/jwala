package com.cerner.jwala.persistence.jpa.service.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.persistence.jpa.domain.AbstractEntity;
import com.cerner.jwala.persistence.jpa.service.CrudService;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

public abstract class AbstractCrudServiceImpl<T extends AbstractEntity<T>> implements CrudService<T> {

    @PersistenceContext(unitName = "jwala-unit")
    protected EntityManager entityManager;

    private Class<T> entityClass;

    public AbstractCrudServiceImpl() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }


    public List<T> findAll() {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> criteria = builder.createQuery(entityClass);
        final Root<T> root = criteria.from(entityClass);
        criteria.select(root);

        final TypedQuery<T> query = entityManager.createQuery(criteria);

        return query.getResultList();
    }

    @Override
    public T findById(Long id) {
        T t = entityManager.find(entityClass, id);

        if (t == null) {
            throw new NotFoundException(AemFaultType.ENTITY_NOT_FOUND,
                    "Entity with id " + id + " not found");
        }

        return t;
    }

    @Override
    @Transactional
    public void remove(final T entity) {
        entityManager.remove(entityManager.find(entityClass, entity.getId()));
        entityManager.flush();
        entityManager.getEntityManagerFactory().getCache().evict(entityClass, entity.getId());
    }

    @Override
    @Transactional
    public void remove(Long id) {
        entityManager.remove(entityManager.find(entityClass, id));
        entityManager.flush();
        entityManager.getEntityManagerFactory().getCache().evict(entityClass, id);
    }

    @Override
    @Transactional
    public T create(final T t) {
        entityManager.persist(t);
        entityManager.flush();
        return t;
    }

    @Override
    @Transactional
    public T update(final T entity) {
        entityManager.merge(entity);
        entityManager.flush();
        return entity;
    }

    @Override
    @Transactional
    public void removeAllEntities(final Collection<T> entities) {
        for (final T t : entities) {
            entityManager.remove(t);
        }
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Override
    public T findObject(final String queryString,
                        final Object... values) {
        final Query queryObject = entityManager.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }

        return (T) queryObject.getSingleResult();
    }

    @Override
    public List<?> findObjects(final String queryString,
                               final Object... values) {
        final Query queryObject = entityManager.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }
        return (List<?>) queryObject.getResultList();
    }
}

