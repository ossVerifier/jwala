package com.siemens.cto.aem.service.jvm.impl;

import java.util.List;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.service.jvm.JvmService;

public class JvmServiceFacade {

    private final JvmService service;

    public JvmServiceFacade(final JvmService theService) {
        service = theService;
    }

    public List<Jvm> getAllJvms() {
        return service.getJvms(PaginationParameter.all());
    }
}
