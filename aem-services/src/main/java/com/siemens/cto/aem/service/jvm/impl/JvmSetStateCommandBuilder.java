package com.siemens.cto.aem.service.jvm.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;

class JvmSetStateCommandBuilder {

    private Identifier<Jvm> jvmId;
    private JvmState jvmState;
    private DateTime asOf;
    private String message;

    JvmSetStateCommandBuilder() {
        asOf = DateTime.now();
    }

    JvmSetStateCommandBuilder setControlCommandComposite(final ControlJvmCommand aControlCommand) {
        setJvmId(aControlCommand.getJvmId());
        setJvmState(aControlCommand.getControlOperation().getOperationState());
        return this;
    }

    JvmSetStateCommandBuilder setJvmId(final Identifier<Jvm> aJvmId) {
        jvmId = aJvmId;
        return this;
    }

    JvmSetStateCommandBuilder setJvmState(final JvmState aJvmState) {
        jvmState = aJvmState;
        return this;
    }

    JvmSetStateCommandBuilder setAsOf(final DateTime anAsOf) {
        asOf = anAsOf;
        return this;
    }

    JvmSetStateCommandBuilder setMessage(final String aMessage) {
        message = aMessage;
        return this;
    }

    JvmSetStateCommand build() {
        return new JvmSetStateCommand(createCurrentJvmState());
    }

    private CurrentState<Jvm, JvmState> createCurrentJvmState() {
        if (message == null) {
            return new CurrentState<>(jvmId,
                                      jvmState,
                                      asOf,
                                      StateType.JVM);
        } else {
            return new CurrentState<>(jvmId,
                                      jvmState,
                                      asOf,
                                      StateType.JVM,
                                      message);
        }
    }
}
