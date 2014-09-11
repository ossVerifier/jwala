package com.siemens.cto.aem.service.state.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.DateTime;

import com.siemens.cto.aem.common.exception.ExceptionUtil;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.state.StateService;

public abstract class AbstractStateServiceFacade<S, T extends OperationalState> {

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

    @Deprecated
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

    public void setStateWithMessageAndException(final Identifier<S> anId,
            final T aNewState,
            final DateTime anAsOf,
            final String aMessage,
            final Throwable anException) throws IOException /*does not really.*/ {

        StringWriter writer = null;
        PrintWriter printer = null;
        Throwable penaCause = null;
        if(anException != null) {
            penaCause = ExceptionUtil.INSTANCE.getPenultimateRootCause(anException);
            if(penaCause == null) { 
                penaCause = anException;
            }
            writer = new StringWriter();
            printer = new PrintWriter(writer);
        } 
        
        String message = "";
        if(penaCause == null && aMessage == null) { 
            message = "";
        } else if(aMessage == null && penaCause != null) { 
            printer.append(penaCause.getLocalizedMessage());
        } else if(penaCause != null) {
            printer.append(aMessage);
        } else { 
            message = aMessage;
        }
        
        if(penaCause != null) {
            printer.append(System.lineSeparator());
            penaCause.printStackTrace(printer);
            printer.flush();
            message = writer.toString();
            printer.close();
            writer.close();
        }
        
        final CurrentState<S, T> newCurrentState = createCurrentStateWithMessage(anId,
                                                                 aNewState,
                                                                 anAsOf,
                                                                 message);
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
