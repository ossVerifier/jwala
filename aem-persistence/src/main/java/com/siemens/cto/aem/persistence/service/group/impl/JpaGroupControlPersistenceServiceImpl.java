package com.siemens.cto.aem.persistence.service.group.impl;

import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;

public class JpaGroupControlPersistenceServiceImpl implements GroupControlPersistenceService {

    private final GroupControlCrudService crudService;

    public JpaGroupControlPersistenceServiceImpl(final GroupControlCrudService theService) {
        crudService = theService;
    }


}
