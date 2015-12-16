package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;
import com.siemens.cto.aem.persistence.service.builder.JvmJpaCurrentStateBuilder;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;

public class JvmJpaStatePersistenceServiceImpl extends JpaStatePersistenceServiceImpl<Jvm, JvmState> implements StatePersistenceService<Jvm, JvmState> {

    public JvmJpaStatePersistenceServiceImpl(final StateCrudService<Jvm, JvmState> theService) {
        super(theService);
    }

    @Override
    protected CurrentState<Jvm, JvmState> build(final JpaCurrentState aCurrentState, JvmState staleState) {
        return new JvmJpaCurrentStateBuilder(aCurrentState).setStaleOption(staleState).build();
    }
}
