package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.exception.ExternalSystemErrorException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmControlServiceLifecycle;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class JvmControlServiceImpl implements JvmControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    private final JvmService jvmService;
    private final JvmCommandExecutor jvmCommandExecutor;
    private final JvmControlServiceLifecycle jvmControlServiceLifecycle;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final JvmCommandExecutor theExecutor,
                                 final JvmControlServiceLifecycle theLifecycle) {
        jvmService = theJvmService;
        jvmCommandExecutor = theExecutor;
        jvmControlServiceLifecycle = theLifecycle;
    }

    @Override
    public JvmControlHistory controlJvm(final ControlJvmCommand aCommand,
                                        final User aUser) {

        LOGGER.debug("entering controlJvm for command {}", aCommand);
        CurrentState<Jvm, JvmState> prevState = null;

        try {
            aCommand.validateCommand();

            final JvmControlHistory incompleteHistory = jvmControlServiceLifecycle.startHistory(aCommand,
                    aUser);
            final Jvm jvm = jvmService.getJvm(aCommand.getJvmId());

            prevState = null;

            JvmControlOperation ctrlOp = aCommand.getControlOperation();

            if (ctrlOp.getOperationState() != null) {
                prevState = jvmControlServiceLifecycle.startState(aCommand,
                        aUser);
            }

            final ExecData execData = jvmCommandExecutor.controlJvm(aCommand,
                    jvm);
            if (execData != null && (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP))) {
                execData.cleanStandardOutput();
                LOGGER.info("shell command output{}", execData.getStandardOutput());
            }

            final JvmControlHistory completeHistory = jvmControlServiceLifecycle.completeHistory(incompleteHistory,
                    aCommand,
                    execData,
                    aUser);
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.debug("exiting controlJvm for command {}", aCommand);
                jvmControlServiceLifecycle.completeState(
                        aCommand,
                        aUser);
            } else {
                String result = execData.standardErrorOrStandardOut();
                if (execData != null && (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP))) {
                    result = execData.extractMessageFromStandardOutput();
                }
                switch (execData.getReturnCode().getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        LOGGER.error("exiting controlJvm for ABNORMAL_SUCCESS command {} :: {}", aCommand, result);
                        jvmControlServiceLifecycle.startStateWithMessage(aCommand.getJvmId(),
                                ctrlOp.getConfirmedState(),
                                result,
                                aUser);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_NO_OP:
                        LOGGER.debug("exiting controlJvm with result NOOP for command {}: '{}'", aCommand, result);
                        jvmControlServiceLifecycle.revertState(prevState, aUser);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_FAST_FAIL:
                        LOGGER.error("exiting controlJvm FAST FAIL command {} :: {}", aCommand, result);
                        jvmControlServiceLifecycle.startStateWithMessage(aCommand.getJvmId(),
                                ctrlOp.getFailureStateOrPrevious(prevState),
                                result,
                                aUser);
                        throw new ExternalSystemErrorException(AemFaultType.FAST_FAIL,
                                "Remote JVM startup health checks failed for jvm with id " + aCommand.getJvmId().getId() + ": " + result);
                    default:
                        if (aCommand.getControlOperation().checkForSuccess(result)) {
                            LOGGER.debug("exiting controlJvm for command {}: '{}'", aCommand, result);
                            jvmControlServiceLifecycle.revertState(prevState, aUser);
                        } else {
                            LOGGER.error("exiting controlJvm for FAILING ({}) command {} :: {}", execData.getReturnCode().getReturnCode(), aCommand, result);
                            jvmControlServiceLifecycle.startStateWithMessage(aCommand.getJvmId(),
                                    ctrlOp.getFailureStateOrPrevious(prevState),
                                    "Return code: " + execData.getReturnCode().getReturnCode() + "; " + result,
                                    aUser);
                            throw new ExternalSystemErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                    "Error controlling a JVM with id " + aCommand.getJvmId().getId() + ": " + result);
                        }
                        break;
                }
            }

            return completeHistory;
        } catch (final CommandFailureException cfe) {
            /*
             * Even though there was a failure, we don't necessarily want to 
             * change the state of the JVM, so if we can we avoid doing so
             * by utilizing prevState. The propagated exception will popup a 
             * dialog to the user.
             */
            jvmControlServiceLifecycle.startStateWithMessage(
                    aCommand.getJvmId(),
                    prevState != null ? prevState.getState() : JvmState.JVM_FAILED,
                    cfe.getMessage(),
                    aUser);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + aCommand,
                    cfe);
        }
    }

    public static class LifecycleImpl implements JvmControlServiceLifecycle {

        private final JvmControlPersistenceService persistenceService;
        private final StateService<Jvm, JvmState> jvmStateService;

        public LifecycleImpl(JvmControlPersistenceService thePersistenceService,
                             StateService<Jvm, JvmState> theJvmStateService) {
            jvmStateService = theJvmStateService;
            persistenceService = thePersistenceService;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public JvmControlHistory startHistory(final ControlJvmCommand aCommand, final User aUser) {
            return persistenceService.addIncompleteControlHistoryEvent(new Event<>(aCommand,
                    AuditEvent.now(aUser)));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public CurrentState<Jvm, JvmState> startState(final ControlJvmCommand aCommand, final User aUser) {
            CurrentState<Jvm, JvmState> jvmState = jvmStateService.getCurrentState(aCommand.getJvmId());
            jvmStateService.setCurrentState(createNewSetJvmStateCommand(aCommand),
                    aUser);
            return jvmState;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public void startStateWithMessage(final Identifier<Jvm> aJvmId,
                                          final JvmState aJvmState,
                                          final String aMessage,
                                          final User aUser) {
            if (aJvmState != null) {
                jvmStateService.setCurrentState(createNewSetJvmStateCommand(aJvmId,
                                aJvmState,
                                aMessage),
                        aUser);
            }
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public JvmControlHistory completeHistory(JvmControlHistory incompleteHistory, ControlJvmCommand aCommand, ExecData execData, final User aUser) {
            return persistenceService.completeControlHistoryEvent(new Event<>(new CompleteControlJvmCommand(incompleteHistory.getId(),
                    execData),
                    AuditEvent.now(aUser)));
        }

        protected JvmSetStateCommand createNewSetJvmStateCommand(final ControlJvmCommand aControlCommand) {
            return new JvmSetStateCommandBuilder().setControlCommandComposite(aControlCommand)
                    .build();
        }

        protected JvmSetStateCommand createNewSetJvmStateCommand(final Identifier<Jvm> aJvmId,
                                                                 final JvmState aJvmState,
                                                                 final String aMessage) {
            return new JvmSetStateCommandBuilder().setJvmId(aJvmId)
                    .setJvmState(aJvmState)
                    .setMessage(aMessage)
                    .build();
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public void revertState(CurrentState<Jvm, JvmState> aJvmState, User aUser) {
            JvmSetStateCommand command = new JvmSetStateCommandBuilder()
                    .setJvmId(aJvmState.getId())
                    .setJvmState(aJvmState.getState())
                    .setAsOf(aJvmState.getAsOf())
                    .build();

            jvmStateService.setCurrentState(command,
                    aUser);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public void notifyMessageOnly(Identifier<Jvm> aJvmId, String aMessage, User aUser) {

            CurrentState<Jvm, JvmState> aJvmState = jvmStateService.getCurrentState(aJvmId);
            jvmStateService.setCurrentState(createNewSetJvmStateCommand(aJvmId,
                    aJvmState.getState(),
                    aMessage), aUser);
        }

        /**
         * Success:
         * Sets the state to the confirmed state
         * for the control operation without additional messaging.
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public void completeState(ControlJvmCommand aCommand, User aUser) {
            JvmState jcs = aCommand.getControlOperation().getConfirmedState();
            if (jcs != null) {
                JvmSetStateCommand jssc = new JvmSetStateCommandBuilder()
                        .setJvmId(aCommand.getJvmId())
                        .setJvmState(jcs)
                        .build();
                jvmStateService.setCurrentState(jssc, aUser);
            }
        }
    }
}
