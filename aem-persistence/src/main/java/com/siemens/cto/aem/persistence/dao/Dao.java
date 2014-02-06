package com.siemens.cto.aem.persistence.dao;

import java.util.List;

import com.siemens.cto.aem.persistence.domain.AbstractEntity;

public interface Dao<T extends AbstractEntity<?>> {
    T findById(final Object id);

    T findObject(final String queryString, final Object... values);

    List<T> findObjects(final String queryString, final Object... values);

    List<T> findAll();

    T findByName(final String name);

    void add(final T t);

    T update(final T entity);

    void remove(final T entity);

    void flush();

    void removeAllEntities(List<T> entities);

    int count(final String queryString, final Object... values);
}