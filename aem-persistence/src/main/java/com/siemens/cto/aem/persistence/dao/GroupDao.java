package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.persistence.domain.AbstractEntity;
import com.siemens.cto.aem.persistence.domain.Group;

public interface GroupDao extends Dao<Group> {
    AbstractEntity<Group> findByName(String name);
}
