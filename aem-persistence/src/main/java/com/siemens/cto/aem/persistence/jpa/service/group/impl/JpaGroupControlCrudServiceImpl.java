package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class JpaGroupControlCrudServiceImpl implements GroupControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;
    

}
