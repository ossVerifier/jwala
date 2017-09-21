package com.cerner.jwala.dao.impl;

import com.cerner.jwala.dao.PersistenceHelper;
import com.cerner.jwala.dao.exception.PersistenceHelperException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Jedd Cuison on 9/20/2017
 */
@Component
public class PersistenceHelperImpl implements PersistenceHelper {

    @PersistenceContext(unitName = "jwala-unit")
    private EntityManager entityManager;

    @Override
    public void clearCache() {
        try {
            entityManager.getEntityManagerFactory().getCache().evictAll();
        } catch (final RuntimeException e) {
            throw new PersistenceHelperException("Failed to clear the cache!", e);
        }
    }
}
