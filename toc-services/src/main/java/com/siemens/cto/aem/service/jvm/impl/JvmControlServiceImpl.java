package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JvmControlServiceImpl implements JvmControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String TOPIC_SERVER_STATES = "/topic/server-states";
    private final JvmService jvmService;
    private final RemoteCommandExecutor<JvmControlOperation> remoteCommandExecutor;
    private final HistoryService historyService;
    private final SimpMessagingTemplate messagingTemplate;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final RemoteCommandExecutor<JvmControlOperation> theExecutor,
                                 final HistoryService historyService,
                                 final SimpMessagingTemplate messagingTemplate) {
        jvmService = theJvmService;
        remoteCommandExecutor = theExecutor;
        this.historyService = historyService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser) {
        final Jvm jvm = jvmService.getJvm(controlJvmRequest.getJvmId());
        try {
            final JvmControlOperation ctrlOp = controlJvmRequest.getControlOperation();
            final String event = ctrlOp.getOperationState() == null ?
                    ctrlOp.name() : ctrlOp.getOperationState().toStateLabel();

            historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), event, EventType.USER_ACTION, aUser.getId());

            // Send a message to the UI about the control operation.
            // Note: Sending the details of the control operation to a topic will enable the application to display
            //       the control event to all the UI's opened in different browsers.
            messagingTemplate.convertAndSend(TOPIC_SERVER_STATES, new CurrentState<>(jvm.getId(), ctrlOp.getOperationState(),
                    aUser.getId(), DateTime.now(), StateType.JVM));

            CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(jvm.getJvmName(), jvm.getHostName(),
                    ctrlOp, new WindowsJvmPlatformCommandProvider());

            if (commandOutput != null && StringUtils.isNotEmpty(commandOutput.getStandardOutput()) &&
                    (controlJvmRequest.getControlOperation().equals(JvmControlOperation.START) ||
                     controlJvmRequest.getControlOperation().equals(JvmControlOperation.STOP))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", commandOutput.getStandardOutput());
            }

            // Process non successful return codes...
            if (commandOutput != null && !commandOutput.getReturnCode().wasSuccessful()) {
                switch (commandOutput.getReturnCode().getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        jvmService.updateState(jvm.getId(), JvmState.FORCED_STOPPED);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        LOGGER.warn(CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
                        break;
                    default:
                        final String errorMsg = "JVM control command was not successful! Return code = "
                                + commandOutput.getReturnCode().getReturnCode() + ", description = " +
                                CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc();

                        LOGGER.error(errorMsg);
                        historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), errorMsg, EventType.APPLICATION_ERROR,
                                aUser.getId());
                        messagingTemplate.convertAndSend(TOPIC_SERVER_STATES, new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED,
                                DateTime.now(), StateType.JVM, errorMsg));

                        break;
                }
            }

            return commandOutput;
        } catch (final CommandFailureException cfe) {
            LOGGER.error(cfe.getMessage(), cfe);
            historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), cfe.getMessage(), EventType.APPLICATION_ERROR,
                    aUser.getId());
            messagingTemplate.convertAndSend(TOPIC_SERVER_STATES, new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED,
                    DateTime.now(), StateType.JVM, cfe.getMessage()));

            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlJvmRequest, cfe);
        }
    }

    @Override
    public CommandOutput secureCopyFile(final ControlJvmRequest secureCopyRequest, final String sourcePath,
                                        final String destPath) throws CommandFailureException {
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jvm = jvmService.getJpaJvm(jvmId, true);
        return remoteCommandExecutor.executeRemoteCommand(jvm.getName(), jvm.getHostName(), secureCopyRequest.getControlOperation(),
                new WindowsJvmPlatformCommandProvider(), sourcePath, destPath);
    }

    @Override
    public CommandOutput secureCopyFileWithBackup(final ControlJvmRequest secureCopyRequest, final String sourcePath,
                                                  final String destPath) throws CommandFailureException {
        // back up the original file first
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jvm = jvmService.getJpaJvm(jvmId, true);

        String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
        final String destPathBackup = destPath + currentDateSuffix;
        final CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                jvm.getName(),
                jvm.getHostName(),
                JvmControlOperation.BACK_UP_FILE,
                new WindowsJvmPlatformCommandProvider(),
                destPath,
                destPathBackup);
        if (!commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.error("Remote Command Failure: Failed to back up " + destPath + " for " + jvm + "::" + commandOutput.getStandardError());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + jvm);
        }
        return secureCopyFile(secureCopyRequest, sourcePath, destPath);
    }

}
