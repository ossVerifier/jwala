package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.ExternalSystemErrorException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmControlServiceLifecycle;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public class JvmControlServiceImpl implements JvmControlService {

    private static final Logger logger = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    public static final String FORCED_STOPPED = "FORCED STOPPED";
    private final JvmService jvmService;
    private final RemoteCommandExecutor<JvmControlOperation> remoteCommandExecutor;
    private final JvmControlServiceLifecycle jvmControlServiceLifecycle;
    private final HistoryService historyService;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final RemoteCommandExecutor<JvmControlOperation> theExecutor,
                                 final JvmControlServiceLifecycle theLifecycle,
                                 final HistoryService historyService) {
        jvmService = theJvmService;
        remoteCommandExecutor = theExecutor;
        jvmControlServiceLifecycle = theLifecycle;
        this.historyService = historyService;
    }

    @Override
    // TODO: Refactor
    public CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser) {
        logger.info("entering controlJvm for command {}", controlJvmRequest);
        long start = System.currentTimeMillis();
        CurrentState<Jvm, JvmState> prevState = null;

        final Identifier<Jvm> jvmId = controlJvmRequest.getJvmId();
        final Jvm jvm = jvmService.getJvm(jvmId);
        try {
            final String event = controlJvmRequest.getControlOperation().getOperationState() == null ?
                    controlJvmRequest.getControlOperation().name() :
                    controlJvmRequest.getControlOperation().getOperationState().toStateString();

            historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), event, EventType.USER_ACTION, aUser.getId());

            controlJvmRequest.validate();

            JvmControlOperation ctrlOp = controlJvmRequest.getControlOperation();

            if (ctrlOp.getOperationState() != null) {
                prevState = jvmControlServiceLifecycle.startState(controlJvmRequest, aUser);
            }

            CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                    jvm.getJvmName(),
                    jvm.getHostName(),
                    controlJvmRequest.getControlOperation(),
                    new WindowsJvmPlatformCommandProvider());
            if (commandOutput != null && (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP))) {
                commandOutput.cleanStandardOutput();
                logger.info("shell command output{}", commandOutput.getStandardOutput());
            }

            if (commandOutput != null && commandOutput.getReturnCode().wasSuccessful()) {
                logger.info("exiting controlJvm for command {}", controlJvmRequest);
                jvmControlServiceLifecycle.completeState(controlJvmRequest, aUser, "");
            } else {
                if (commandOutput != null) {
                    final String result;
                    if (ctrlOp.equals(JvmControlOperation.START) || ctrlOp.equals(JvmControlOperation.STOP)) {
                        result = commandOutput.extractMessageFromStandardOutput();
                    } else {
                        result = commandOutput.standardErrorOrStandardOut();
                    }

                    switch (commandOutput.getReturnCode().getReturnCode()) {
                        case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                            logger.info("exiting controlJvm for ABNORMAL_SUCCESS command {} :: {}", controlJvmRequest, result);
                            commandOutput = new CommandOutput(new ExecReturnCode(0),
                                    commandOutput.getStandardOutput(),
                                    commandOutput.getStandardError());
                            jvmControlServiceLifecycle.completeState(controlJvmRequest, aUser, "");
                            break;
                        case ExecReturnCode.STP_EXIT_CODE_NO_OP:
                            logger.debug("exiting controlJvm with result NOOP for command {}: '{}'", controlJvmRequest, result);
                            jvmControlServiceLifecycle.revertState(prevState, aUser);
                            break;
                        case ExecReturnCode.STP_EXIT_CODE_FAST_FAIL:
                            logger.error("exiting controlJvm FAST FAIL command {} :: {}", controlJvmRequest, result);
                            jvmControlServiceLifecycle.startStateWithMessage(jvmId, ctrlOp.getFailureStateOrPrevious(prevState), result, aUser);
                            throw new ExternalSystemErrorException(AemFaultType.FAST_FAIL, "Remote JVM startup health checks failed for jvm with id " + jvmId.getId() + ": " + result);
                        case ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE:
                            logger.error("exiting controlJVM NO SUCH SERVICE command {} :: {}", controlJvmRequest, result);
                            jvmControlServiceLifecycle.startStateWithMessage(jvmId,
                                    ctrlOp.getFailureStateOrPrevious(prevState),
                                    result,
                                    aUser);
                            throw new ExternalSystemErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                    "Error controlling JVM " + jvm.getJvmName() + ": " + result);
                        case ExecReturnCode.STP_EXIT_PROCESS_KILLED:
                            commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED,
                                    commandOutput.getStandardError());
                            jvmControlServiceLifecycle.completeState(controlJvmRequest, aUser, FORCED_STOPPED);
                            break;
                        default:
                            processDefaultReturnCode(controlJvmRequest, aUser, prevState, ctrlOp, commandOutput, jvm.getJvmName(), result);
                            break;
                    }

                }

            }

            logger.info("exiting controlJvm for command {} elapsedTime:{}", controlJvmRequest, System.currentTimeMillis() - start);
            return commandOutput;
        } catch (final CommandFailureException cfe) {
            /*
             * Even though there was a failure, we don't necessarily want to
             * change the state of the JVM, so if we can we avoid doing so
             * by utilizing prevState. The propagated exception will popup a
             * dialog to the user.
             */
            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
            jvmControlServiceLifecycle.startStateWithMessage(
                    jvmId,
                    JvmState.JVM_FAILED,
                    stackTrace,
                    aUser);

            historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), stackTrace, EventType.APPLICATION_ERROR,
                    aUser.getId());

            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlJvmRequest,
                    cfe);
        }
    }

    @Override
    public CommandOutput secureCopyFile(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath) throws CommandFailureException {
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jvm = jvmService.getJpaJvm(jvmId, true);

        return remoteCommandExecutor.executeRemoteCommand(
                jvm.getName(),
                jvm.getHostName(),
                secureCopyRequest.getControlOperation(),
                new WindowsJvmPlatformCommandProvider(),
                sourcePath,
                destPath);
    }


    private void processDefaultReturnCode(ControlJvmRequest controlJvmRequest, User aUser, CurrentState<Jvm, JvmState> prevState, JvmControlOperation ctrlOp, CommandOutput execData, String jvmName, String result) {
        if (ctrlOp.checkForSuccess(result)) {
            logger.debug("exiting controlJvm for command {}: '{}'", controlJvmRequest, result);
            jvmControlServiceLifecycle.revertState(prevState, aUser);
        } else {
            logger.error("exiting controlJvm for FAILING ({}) command {} :: {}", execData.getReturnCode().getReturnCode(), controlJvmRequest, result);
            jvmControlServiceLifecycle.startStateWithMessage(controlJvmRequest.getJvmId(),
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
        public CurrentState<Jvm, JvmState> startState(final ControlJvmRequest controlJvmRequest, final User aUser) {
            final Identifier<Jvm> jvmId = controlJvmRequest.getJvmId();
            CurrentState<Jvm, JvmState> jvmState = jvmStateService.getCurrentState(jvmId);
            jvmStateService.setCurrentState(createNewSetJvmStateCommand(controlJvmRequest), //TODO send in jvmState to setCurrentState, setCurrentState calls getState - unnecessary hit to the db
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
        public void completeState(final ControlJvmRequest controlJvmRequest, final User aUser, final String msg) {
            JvmState jcs = controlJvmRequest.getControlOperation().getConfirmedState();
            if (jcs != null) {
                JvmSetStateRequest jssc = new JvmSetStateCommandBuilder().setJvmId(controlJvmRequest.getJvmId())
                                                                         .setJvmState(jcs)
                                                                         .setMessage(msg)
                                                                         .build();
                jvmStateService.setCurrentState(jssc, aUser);
            }
        }
    }
}
