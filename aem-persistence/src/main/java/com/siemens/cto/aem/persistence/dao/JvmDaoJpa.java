package com.siemens.cto.aem.persistence.dao;

import javax.persistence.EntityManager;

import com.siemens.cto.aem.persistence.domain.Jvm;

public class JvmDaoJpa extends AbstractDaoJpa<Jvm> {
    public JvmDaoJpa() {
    }

    JvmDaoJpa(final EntityManager entityManager) {
        super.entityManager = entityManager;
    }
}
