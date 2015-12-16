package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.persistence.jpa.service.GroupControlCrudService;
import com.siemens.cto.aem.persistence.service.GroupControlPersistenceService;

public class JpaGroupControlPersistenceServiceImpl implements GroupControlPersistenceService {

    private final GroupControlCrudService crudService;

    public JpaGroupControlPersistenceServiceImpl(final GroupControlCrudService theService) {
        crudService = theService;
    }


}
