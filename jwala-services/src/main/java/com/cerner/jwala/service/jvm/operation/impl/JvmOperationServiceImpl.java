package com.cerner.jwala.service.jvm.operation.impl;

import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.service.jvm.operation.JvmOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements {@link JvmOperationService}
 *
 * Created by Jedd Cuison on 12/16/2016
 */
@Service
public class JvmOperationServiceImpl implements JvmOperationService {

    @Autowired
    private JvmCrudService jvmCrudService;

    @Override
    public void start(final String jvmName) {
        Operations.START.run(jvmCrudService.findJvmByExactName(jvmName));
    }

    @Override
    public void stop(final String jvmName) {
        Operations.STOP.run(jvmCrudService.findJvmByExactName(jvmName));
    }

}
