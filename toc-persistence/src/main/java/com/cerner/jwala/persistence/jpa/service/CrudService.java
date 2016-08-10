package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.persistence.jpa.domain.AbstractEntity;

import java.util.Collection;
import java.util.List;

public interface CrudService<T extends AbstractEntity<T>> {

    T findById(final Long id);

    List<T> findAll();

    T create(final T t);

    T update(final T entity);

    void remove(final T entity);

    void remove(Long id);

    void removeAllEntities(Collection<T> entities);

    T findObject(final String queryString, final Object... values);

    List<?> findObjects(final String queryString, final Object... values);
}

