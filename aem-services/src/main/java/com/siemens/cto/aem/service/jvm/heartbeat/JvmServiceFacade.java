package com.siemens.cto.aem.service.jvm.heartbeat;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.JvmService;

import java.util.List;

public class JvmServiceFacade {

    private final JvmService service;

    public JvmServiceFacade(final JvmService theService) {
        service = theService;
    }

    public List<Jvm> getAllJvms() {
        return service.getJvms();
    }
}
