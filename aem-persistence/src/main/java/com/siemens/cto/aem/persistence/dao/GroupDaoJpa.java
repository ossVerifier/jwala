package com.siemens.cto.aem.persistence.dao;

import javax.persistence.EntityManager;

import com.siemens.cto.aem.persistence.domain.Group;

public class GroupDaoJpa extends AbstractDaoJpa<Group> {

    public GroupDaoJpa() {
    }

    GroupDaoJpa(final EntityManager entityManager) {
        super.entityManager = entityManager;
    }
}
