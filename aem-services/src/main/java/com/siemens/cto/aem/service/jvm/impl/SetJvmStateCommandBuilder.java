package com.siemens.cto.aem.service.jvm.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;

class SetJvmStateCommandBuilder {

    private ControlJvmCommand controlCommand;
    private DateTime asOf;

    SetJvmStateCommandBuilder() {
        asOf = DateTime.now();
    }

    SetJvmStateCommandBuilder setControlCommand(final ControlJvmCommand aControlCommand) {
        controlCommand = aControlCommand;
        return this;
    }

    SetJvmStateCommandBuilder setAsOf(final DateTime anAsOf) {
        asOf = anAsOf;
        return this;
    }

    JvmSetStateCommand build() {
        return new JvmSetStateCommand(createCurrentJvmState());
    }

    private CurrentState<Jvm, JvmState> createCurrentJvmState() {
        return new CurrentState<>(controlCommand.getJvmId(),
                                  getJvmState(),
                                  asOf,
                                  StateType.JVM);
    }

    private JvmState getJvmState() {
        return controlCommand.getControlOperation().getOperationState();
    }
}
