package com.siemens.cto.aem.persistence.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.persistence.domain.AbstractEntity;

public abstract class AbstractDaoJpa<T extends AbstractEntity<?>> implements Dao<T> {

    @PersistenceContext(unitName = "aem-unit")
    EntityManager entityManager;

    private final Class<?> entityClass = getEntityClass();
    private final String simpleName = entityClass.getSimpleName();

    private Class<?> getEntityClass() {
        Class<?> result = null;
        final Class<? extends AbstractDaoJpa> theClass = getClass();
        final Type type = theClass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            final Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            for (final Type argument : arguments) {
                if (argument instanceof Class) {
                    result = (Class<?>) argument;
                }
            }
        }
        return result;
    }

    public AbstractDaoJpa() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return entityManager.createQuery("SELECT e FROM " + simpleName + " e").getResultList();
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public List<T> findObjects(final String queryString, final Object... values) {
        List<T> result = null;
        final Query queryObject = entityManager.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }
        result = queryObject.getResultList();

        return result;
    }

    @Override
    public int count(final String queryString, final Object... values) {
        final Query queryObject = entityManager.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }
        return ((Long) queryObject.getSingleResult()).intValue();
    }

    @Override
    public void removeAllEntities(final List<T> entities) {
        for (final T t : entities) {
            entityManager.remove(t);
        }
        entityManager.flush();
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public void add(final T t) {
        entityManager.persist(t);
        entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T findById(final Object id) {
        return (T) entityManager.find(entityClass, id);
    }

    @Override
    public T findByName(final String name) {
        return findObject("SELECT e FROM " + simpleName + " e where e.name = ?1", name);
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public T findObject(final String queryString, final Object... values) {
        final Query queryObject = entityManager.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i + 1, values[i]);
            }
        }

        T singleResult = null;
        try {
            singleResult = (T) queryObject.getSingleResult();
        } catch (final NoResultException e) {
            // intentionally swallowed
        }
        return singleResult;
    }

    @Override
    public T update(final T entity) {
        entityManager.merge(entity);
        entityManager.flush();
        return entity;
    }

    @Override
    public void remove(final T entity) {
        entityManager.remove(entity);
        entityManager.flush();
    }
}
