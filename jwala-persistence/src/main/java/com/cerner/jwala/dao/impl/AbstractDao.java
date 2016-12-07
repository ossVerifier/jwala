package com.cerner.jwala.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
public class AbstractDao<T> {

    private Class<T> entityClass;

    private final EntityManager em;

    @SuppressWarnings("unchecked")
    public AbstractDao(final EntityManager em) {
        this.em = em;
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    public T find(final String name) {
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<T> criteria = builder.createQuery(entityClass);
        final Root root = criteria.from(entityClass);
        criteria.where(builder.equal(root.get("name"), name));
        criteria.select(root);

        return em.createQuery(criteria).getSingleResult();
    }

    public List<T> findAll() {
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<T> criteria = builder.createQuery(entityClass);
        final Root<T> root = criteria.from(entityClass);
        criteria.select(root);

        final TypedQuery<T> query = em.createQuery(criteria);

        return query.getResultList();
    }

    public void create(final T t) {
        em.persist(t);
    }

    public void remove(final T t) {
        em.remove(t);
    }

}
