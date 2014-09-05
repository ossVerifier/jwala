package com.siemens.cto.aem.service.state.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.state.StateService;

public abstract class AbstractStateServiceFacade<S, T extends ExternalizableState> {

    private final StateType stateType;
    private final StateService<S, T> service;

    public AbstractStateServiceFacade(final StateService<S, T> theService,
                                      final StateType theStateType) {
        service = theService;
        stateType = theStateType;
    }

    public void setState(final Identifier<S> anId,
                         final T aNewState,
                         final DateTime anAsOf) {

        final CurrentState<S, T> newCurrentState = createCurrentState(anId,
                                                                      aNewState,
                                                                      anAsOf);
        setState(newCurrentState);
    }

    public void setStateWithMessage(final Identifier<S> anId,
                                    final T aNewState,
                                    final DateTime anAsOf,
                                    final String aMessage) {
        final CurrentState<S, T> newCurrentState = createCurrentStateWithMessage(anId,
                                                                                 aNewState,
                                                                                 anAsOf,
                                                                                 aMessage);
        setState(newCurrentState);
    }

    void setState(final CurrentState<S, T> aNewCurrentState) {
        final SetStateCommand<S, T> command = createCommand(aNewCurrentState);
        service.setCurrentState(command,
                                User.getSystemUser());
    }

    CurrentState<S, T> createCurrentState(final Identifier<S> anId,
                                                   final T aNewState,
                                                   final DateTime anAsOf) {
        final CurrentState<S, T> state = new CurrentState<>(anId,
                                                            aNewState,
                                                            anAsOf,
                                                            stateType);
        return state;
    }

    CurrentState<S, T> createCurrentStateWithMessage(final Identifier<S> aJvmId,
                                                     final T aNewState,
                                                     final DateTime anAsOf,
                                                     final String aMessage) {
        final CurrentState<S, T> state = new CurrentState<>(aJvmId,
                                                            aNewState,
                                                            anAsOf,
                                                            stateType,
                                                            aMessage);
        return state;
    }

    protected abstract SetStateCommand<S, T> createCommand(final CurrentState<S, T> aNewCurrentState);
}
