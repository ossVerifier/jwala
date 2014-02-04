package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Group;

public class GroupDaoJpa extends AbstractDaoJpa<Group> implements GroupDao {

    @Override
    public AbstractEntity<Group> findByName(final String name) {
        return findObject("SELECT g FROM Group g where g.name = ?1", name);
    }
}
