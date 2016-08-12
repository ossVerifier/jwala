package com.cerner.jwala.service.jvm.impl;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.state.JvmSetStateRequest;

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
