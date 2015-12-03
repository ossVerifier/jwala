package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.exception.ExternalSystemErrorException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmControlServiceLifecycle;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class JvmControlServiceImpl implements JvmControlService {

    private static final Logger logger = LoggerFactory.getLogger(JvmControlServiceImpl.class);
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
    public CommandOutput controlJvm(final ControlJvmRequest aCommand, final User aUser) {
        logger.info("entering controlJvm for command {}", aCommand);
        long start = System.currentTimeMillis();
        CurrentState<Jvm, JvmState> prevState = null;

        final Identifier<Jvm> jvmId = aCommand.getJvmId();
        final Jvm jvm;
        try {
            aCommand.validate();
            jvm = jvmService.getJvm(jvmId);
            String jvmName = jvm.getJvmName();
            prevState = null;

            JvmControlOperation ctrlOp = aCommand.getControlOperation();

            if (ctrlOp.getOperationState() != null) {
                prevState = jvmControlServiceLifecycle.startState(aCommand, aUser);
            }

            CommandOutput commandOutput = jvmCommandExecutor.controlJvm(aCommand, jvm);
            if (commandOutput != null && (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP))) {
                commandOutput.cleanStandardOutput();
                logger.info("shell command output{}", commandOutput.getStandardOutput());
            }

            if (commandOutput != null && commandOutput.getReturnCode().wasSuccessful()) {
                logger.info("exiting controlJvm for command {}", aCommand);
                jvmControlServiceLifecycle.completeState(aCommand, aUser);
            } else {
                String result;
                if (commandOutput == null) {
                    result = "";
                } else {
                    result = commandOutput.standardErrorOrStandardOut();
                }
                if (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP)) {
                    result = commandOutput.extractMessageFromStandardOutput();
                }
                switch (commandOutput.getReturnCode().getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        logger.info("exiting controlJvm for ABNORMAL_SUCCESS command {} :: {}", aCommand, result);
                        final CommandOutput newExecData = new CommandOutput(new ExecReturnCode(0),
                                                                  commandOutput.getStandardOutput(),
                                                                  commandOutput.getStandardError());
                        commandOutput = newExecData;
                        jvmControlServiceLifecycle.completeState(aCommand, aUser);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_NO_OP:
                        logger.debug("exiting controlJvm with result NOOP for command {}: '{}'", aCommand, result);
                        jvmControlServiceLifecycle.revertState(prevState, aUser);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_FAST_FAIL:
                        logger.error("exiting controlJvm FAST FAIL command {} :: {}", aCommand, result);
                        jvmControlServiceLifecycle.startStateWithMessage(jvmId, ctrlOp.getFailureStateOrPrevious(prevState), result, aUser);
                        throw new ExternalSystemErrorException(AemFaultType.FAST_FAIL, "Remote JVM startup health checks failed for jvm with id " + jvmId.getId() + ": " + result);
                    case ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE:
                        logger.error("exiting controlJVM NO SUCH SERVICE command {} :: {}", aCommand, result);
                        jvmControlServiceLifecycle.startStateWithMessage(jvmId,
                                ctrlOp.getFailureStateOrPrevious(prevState),
                                result,
                                aUser);
                        throw new ExternalSystemErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                "Error controlling JVM " + jvmName + ": " + result);
                    default:
                        processDefaultReturnCode(aCommand, aUser, prevState, ctrlOp, commandOutput, jvmName, result);
                        break;
                }
            }

            logger.info("exiting controlJvm for command {} elapsedTime:{}", aCommand, System.currentTimeMillis() - start);
            return commandOutput;
        } catch (final CommandFailureException cfe) {
            /*
             * Even though there was a failure, we don't necessarily want to 
             * change the state of the JVM, so if we can we avoid doing so
             * by utilizing prevState. The propagated exception will popup a 
             * dialog to the user.
             */
            jvmControlServiceLifecycle.startStateWithMessage(
                    jvmId,
                    prevState != null ? prevState.getState() : JvmState.JVM_FAILED,
                    cfe.getMessage(),
                    aUser);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + aCommand,
                    cfe);
        }
    }

    private void processDefaultReturnCode(ControlJvmRequest aCommand, User aUser, CurrentState<Jvm, JvmState> prevState, JvmControlOperation ctrlOp, CommandOutput execData, String jvmName, String result) {
        if (ctrlOp.checkForSuccess(result)) {
            logger.debug("exiting controlJvm for command {}: '{}'", aCommand, result);
            jvmControlServiceLifecycle.revertState(prevState, aUser);
        } else {
            logger.error("exiting controlJvm for FAILING ({}) command {} :: {}", execData.getReturnCode().getReturnCode(), aCommand, result);
            jvmControlServiceLifecycle.startStateWithMessage(aCommand.getJvmId(),
                    ctrlOp.getFailureStateOrPrevious(prevState),
                    "Return code: " + execData.getReturnCode().getReturnCode() + "; " + result,
                    aUser);
            throw new ExternalSystemErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "Error controlling JVM " + jvmName + ": " + result);
        }
    }

    public static class LifecycleImpl implements JvmControlServiceLifecycle {

        private final StateService<Jvm, JvmState> jvmStateService;

        public LifecycleImpl(StateService<Jvm, JvmState> theJvmStateService) {
            jvmStateService = theJvmStateService;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public CurrentState<Jvm, JvmState> startState(final ControlJvmRequest aCommand, final User aUser) {
            final Identifier<Jvm> jvmId = aCommand.getJvmId();
            CurrentState<Jvm, JvmState> jvmState = jvmStateService.getCurrentState(jvmId);
            jvmStateService.setCurrentState(createNewSetJvmStateCommand(aCommand), //TODO send in jvmState to setCurrentState, setCurrentState calls getState - unnecessary hit to the db
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

        protected JvmSetStateRequest createNewSetJvmStateCommand(final ControlJvmRequest aControlCommand) {
            return new JvmSetStateCommandBuilder().setControlCommandComposite(aControlCommand)
                    .build();
        }

        protected JvmSetStateRequest createNewSetJvmStateCommand(final Identifier<Jvm> aJvmId,
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
            JvmSetStateRequest command = new JvmSetStateCommandBuilder()
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
        public void completeState(ControlJvmRequest aCommand, User aUser) {
            JvmState jcs = aCommand.getControlOperation().getConfirmedState();
            if (jcs != null) {
                JvmSetStateRequest jssc = new JvmSetStateCommandBuilder()
                        .setJvmId(aCommand.getJvmId())
                        .setJvmState(jcs)
                        .build();
                jvmStateService.setCurrentState(jssc, aUser);
            }
        }
    }
}
