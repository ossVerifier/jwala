package com.siemens.cto.aem.service.jvm.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.state.StateService;

public class JvmStateServiceFacade {

    private final StateService<Jvm, JvmState> service;

    public JvmStateServiceFacade(final StateService<Jvm, JvmState> theService) {
        service = theService;
    }

    public void setState(final Identifier<Jvm> aJvmId,
                         final JvmState aNewState,
                         final DateTime anAsOf) {

        service.setCurrentState(createStateCommand(aJvmId,
                                                   aNewState,
                                                   anAsOf),
                                User.getSystemUser());
    }

    JvmSetStateCommand createStateCommand(final Identifier<Jvm> aJvmId,
                                          final JvmState aNewState,
                                          final DateTime anAsOf) {
        final JvmSetStateCommand command = new JvmSetStateCommand(createCurrentState(aJvmId,
                                                                                     aNewState,
                                                                                     anAsOf));
        return command;
    }

    CurrentState<Jvm, JvmState> createCurrentState(final Identifier<Jvm> aJvmId,
                                                   final JvmState aNewState,
                                                   final DateTime anAsOf) {
        final CurrentState<Jvm, JvmState> state = new CurrentState<>(aJvmId,
                                                                     aNewState,
                                                                     anAsOf,
                                                                     StateType.JVM);
        return state;
    }
}
