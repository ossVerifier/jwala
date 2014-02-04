package com.siemens.cto.aem.persistence.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.common.ApplicationException;
import com.siemens.cto.aem.persistence.domain.AbstractEntity;

public abstract class AbstractDaoJpa<T extends AbstractEntity<?>> implements Dao<T> {

    @PersistenceContext(unitName = "persistence-unit")
    protected EntityManager em;

    private Class<?> entityClass;

    public AbstractDaoJpa() {
    }

    @SuppressWarnings("unchecked")
    protected synchronized Class<?> getEntityClass() {
        // return Assignment.class
        if (entityClass == null) {
            final Type type = getClass().getGenericSuperclass();
            loop: while (true) {
                if (type instanceof ParameterizedType) {
                    final Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
                    for (final Type argument : arguments) {
                        if (argument instanceof Class) {
                            entityClass = (Class<?>) argument;
                            break loop;
                        }
                    }
                }
            }
        }
        return entityClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + getEntityClass().getSimpleName() + " e").getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T findById(final Object id) {
        return (T) em.find(getEntityClass(), id);
    }

    @Override
    public void remove(final T entity) {
        em.remove(entity);
        em.flush();
    }

    @Override
    public void flush() {
        em.flush();
    }

    @Override
    public void add(final T t) {
        try {
            em.persist(t);
            em.flush();
        } catch (final Exception e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public T update(final T entity) {
        em.merge(entity);
        em.flush();
        return entity;
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public T findObject(final String queryString, final Object... values) {
        final Query queryObject = em.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }

        try {
            return (T) queryObject.getSingleResult();
        } catch (final NoResultException e) {
            // intentionally swallowed
        }
        return null;
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public List<T> findObjects(final String queryString, final Object... values) {
        final Query queryObject = em.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }
        try {
            return queryObject.getResultList();
        } catch (final NoResultException e) {
            // intentionally swallowed
        }
        return null;
    }

    @Override
    public void removeAllEntities(final Collection<T> entities) {
        for (final T t : entities) {
            em.remove(t);
        }
    }

    @Override
    public int count(final String queryString, final Object... values) {
        final Query queryObject = em.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }
        return ((Long) queryObject.getSingleResult()).intValue();
    }
}
