package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.persistence.jpa.service.GroupControlCrudService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class JpaGroupControlCrudServiceImpl implements GroupControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;
    

}
