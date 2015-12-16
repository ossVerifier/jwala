package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;

public class JvmStateCrudServiceImpl extends StateCrudServiceImpl<Jvm, JvmState> implements StateCrudService<Jvm, JvmState> {

    public JvmStateCrudServiceImpl() {
        super(StateType.JVM);
    }
}
