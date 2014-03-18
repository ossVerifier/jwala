package com.siemens.cto.aem.persistence.dao;

import javax.persistence.EntityManager;

import com.siemens.cto.aem.persistence.domain.JpaJvm;

public class JvmDaoJpa extends AbstractDaoJpa<JpaJvm> {
    public JvmDaoJpa() {
    }

    JvmDaoJpa(final EntityManager entityManager) {
        super.entityManager = entityManager;
    }
}
