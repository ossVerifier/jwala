package com.siemens.cto.aem.service.jvm.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;

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

    SetJvmStateCommand build() {
        return new SetJvmStateCommand(createCurrentJvmState());
    }

    private CurrentJvmState createCurrentJvmState() {
        return new CurrentJvmState(controlCommand.getJvmId(),
                                   getJvmState(),
                                   asOf);
    }

    private JvmState getJvmState() {
        return controlCommand.getControlOperation().getOperationState();
    }
}
