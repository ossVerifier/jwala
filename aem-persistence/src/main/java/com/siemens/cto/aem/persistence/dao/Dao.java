package com.siemens.cto.aem.persistence.dao;

import java.util.Collection;
import java.util.List;

import com.siemens.cto.aem.persistence.domain.AbstractEntity;

public interface Dao<T extends AbstractEntity<?>> {
    T findById(final Object id);

    T findObject(final String queryString, final Object... values);

    List<T> findObjects(final String queryString, final Object... values);

    List<T> findAll();

    void add(final T t);

    T update(final T entity);

    void remove(final T entity);

    void flush();

    void removeAllEntities(Collection<T> entities);

    int count(final String queryString, final Object... values);
}