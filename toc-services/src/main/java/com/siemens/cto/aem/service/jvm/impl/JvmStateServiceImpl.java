package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;

public class JvmStateServiceImpl extends StateServiceImpl<Jvm, JvmState> implements StateService<Jvm, JvmState> {

    private ArrayList<JvmState> jvmStatesToCheck = new ArrayList<>(10);
    private ArrayList<JvmState> jvmStoppingStatesToCheck = new ArrayList<>(1);
    

    @Value("${states.stopped-check.jvm.max-stop-time.millis}")
    private int serviceStoppedMillis;

    private final GroupStateService.API groupStateService;

    public JvmStateServiceImpl(final StatePersistenceService<Jvm, JvmState> thePersistenceService,
                               final StateNotificationService theNotificationService,
                               final GroupStateService.API groupStateService,
                               final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        super(thePersistenceService, theNotificationService, StateType.JVM,
                grpStateComputationAndNotificationSvc);

        jvmStatesToCheck.add(JvmState.JVM_INITIALIZING);
        jvmStatesToCheck.add(JvmState.JVM_INITIALIZED);
        jvmStatesToCheck.add(JvmState.JVM_START);
        jvmStatesToCheck.add(JvmState.JVM_STARTING);
        jvmStatesToCheck.add(JvmState.JVM_STARTED);
        jvmStatesToCheck.add(JvmState.JVM_STOP);
        jvmStatesToCheck.add(JvmState.JVM_STOPPING);
        jvmStatesToCheck.add(JvmState.JVM_DESTROYING);
        jvmStatesToCheck.add(JvmState.JVM_UNKNOWN);

        jvmStoppingStatesToCheck.add(JvmState.JVM_STOPPED);

        this.groupStateService = groupStateService;
    }

    @Override
    protected CurrentState<Jvm, JvmState> createUnknown(final Identifier<Jvm> anId) {
        return new CurrentState<>(anId,
                                  JvmState.JVM_UNKNOWN,
                                  DateTime.now(),
                                  StateType.JVM);
    }

}
