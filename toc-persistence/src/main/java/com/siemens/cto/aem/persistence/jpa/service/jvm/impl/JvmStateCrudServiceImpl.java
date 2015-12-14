package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.state.impl.StateCrudServiceImpl;

public class JvmStateCrudServiceImpl extends StateCrudServiceImpl<Jvm, JvmState> implements StateCrudService<Jvm, JvmState> {

    public JvmStateCrudServiceImpl() {
        super(StateType.JVM);
    }
}
