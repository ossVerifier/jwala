package com.siemens.cto.aem.persistence.dao;

import javax.persistence.EntityManager;

import com.siemens.cto.aem.persistence.domain.JpaGroup;

public class GroupDaoJpa extends AbstractDaoJpa<JpaGroup> {

    public GroupDaoJpa() {
    }

    GroupDaoJpa(final EntityManager entityManager) {
        super.entityManager = entityManager;
    }
}
