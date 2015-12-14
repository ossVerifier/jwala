package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import org.joda.time.DateTime;

class JvmSetStateCommandBuilder {

    private Identifier<Jvm> jvmId;
    private JvmState jvmState;
    private DateTime asOf;
    private String message;

    JvmSetStateCommandBuilder() {
        asOf = DateTime.now();
    }

    JvmSetStateCommandBuilder setControlCommandComposite(final ControlJvmRequest aControlCommand) {
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

    JvmSetStateRequest build() {
        return new JvmSetStateRequest(createCurrentJvmState());
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
