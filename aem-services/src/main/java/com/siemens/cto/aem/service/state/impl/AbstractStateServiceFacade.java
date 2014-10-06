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

    public CurrentState<S, T> setState(final Identifier<S> anId,
                         final T aNewState,
                         final DateTime anAsOf) {

        final CurrentState<S, T> newCurrentState = createCurrentState(anId,
                                                                      aNewState,
                                                                      anAsOf);
        setState(newCurrentState);
        
        return newCurrentState;
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

    public CurrentState<S, T> setStateWithMessageAndException(final Identifier<S> anId,
            final T aNewState,
            final DateTime anAsOf,
            final String aMessage,
            final Throwable anException) throws IOException /*does not really.*/ {

        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        Throwable penultimateRootCause = null;
        if(anException != null) {
            penultimateRootCause = ExceptionUtil.INSTANCE.getPenultimateRootCause(anException);
            if(penultimateRootCause == null) { 
                penultimateRootCause = anException;
            }
        } 
        
        if(penultimateRootCause == null && aMessage == null) { 
            printer.append(ExceptionUtil.NO_EXCEPTION_MESSAGE);
        } else if(aMessage == null && penultimateRootCause != null) { 
            printer.append(penultimateRootCause.getLocalizedMessage());
        } else if(aMessage != null && penultimateRootCause != null) {
            printCustomizedStateMessageFromException(printer, anId, aNewState, anAsOf, aMessage, penultimateRootCause);
        } else {
            printCustomizedStateMessage(printer, anId, aNewState, anAsOf, aMessage);
        }
        
        if(penultimateRootCause != null) {
            printer.append(System.lineSeparator());
            penultimateRootCause.printStackTrace(printer);
        }

        printer.flush();
        String message = writer.toString();
        printer.close();
        writer.close();
        
        final CurrentState<S, T> newCurrentState = createCurrentStateWithMessage(anId,
                                                                 aNewState,
                                                                 anAsOf,
                                                                 message);
        setState(newCurrentState);
        
        return newCurrentState;
    }

    /**
     * This API lets you manipulate the message returned in the state string
     * Default implementation writes aMessage to the PrintWriter
     * 
     * @param printer Implementors must write aMessage output to this PrintWriter
     * @param anId the entity id 
     * @param aNewState the state being changed to
     * @param anAsOf the date of the state change
     * @param aMessage the string to start from 
     * 
     */
    protected void printCustomizedStateMessage(PrintWriter printer, Identifier<S> anId, T aNewState, DateTime anAsOf, String aMessage) {
        printer.print(aMessage);
    }

    /**
     * This API lets you manipulate the message returned in the state string
     * Default implementation writes aMessage to the PrintWriter
     * 
     * @param printer Implementors must write aMessage output to this PrintWriter
     * @param anId the entity id 
     * @param aNewState the state being changed to
     * @param anAsOf the date of the state change
     * @param aMessage the string to start from 
     * @param penultimateRootCause the exception that was thrown 
     */
    protected void printCustomizedStateMessageFromException(PrintWriter printer, Identifier<S> anId, T aNewState, DateTime anAsOf,
            String aMessage, Throwable penultimateRootCause) {        
        printer.print(aMessage);
    }

    protected SetStateCommand<S, T> setState(final CurrentState<S, T> aNewCurrentState) {
        final SetStateCommand<S, T> command = createCommand(aNewCurrentState);
        service.setCurrentState(command,
                                User.getSystemUser());
        return command;
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
