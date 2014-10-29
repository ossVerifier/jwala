package com.siemens.cto.aem.service.jvm.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;

public class AlternateJvmStateServiceImpl extends StateServiceImpl<Jvm, JvmState> implements StateService<Jvm, JvmState> {

    public AlternateJvmStateServiceImpl(final StatePersistenceService<Jvm, JvmState> thePersistenceService,
                                        final StateNotificationService theNotificationService,
                                        final StateNotificationGateway theStateNotificationGateway) {
        super(thePersistenceService,
              theNotificationService,
              StateType.JVM,
              theStateNotificationGateway);
    }

    @Override
    protected CurrentState<Jvm, JvmState> createUnknown(final Identifier<Jvm> anId) {
        return new CurrentState<>(anId,
                                  JvmState.UNKNOWN,
                                  DateTime.now(),
                                  StateType.JVM);
    }

    @Override
    protected void sendNotification(final CurrentState<Jvm, JvmState> anUpdatedState) {
        stateNotificationGateway.jvmStateChanged(anUpdatedState);
    }
}
