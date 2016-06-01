package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmHistoryEvent;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import com.siemens.cto.aem.service.exception.RemoteCommandExecutorServiceException;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JvmControlServiceImpl implements JvmControlService {

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String JVM = "JVM";
    private final JvmService jvmService;
    private final RemoteCommandExecutor<JvmControlOperation> remoteCommandExecutor;
    private final HistoryService historyService;
    private final MessagingService messagingService;
    private final JvmStateService jvmStateService;
    private final RemoteCommandExecutorService remoteCommandExecutorService;
    private final SshConfiguration sshConfig;
    private final GroupStateNotificationService groupStateNotificationService;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final RemoteCommandExecutor<JvmControlOperation> theExecutor,
                                 final HistoryService historyService,
                                 final MessagingService messagingService,
                                 final JvmStateService jvmStateService,
                                 final RemoteCommandExecutorService remoteCommandExecutorService,
                                 final SshConfiguration sshConfig,
                                 final GroupStateNotificationService groupStateNotificationService) {
        jvmService = theJvmService;
        remoteCommandExecutor = theExecutor;
        this.historyService = historyService;
        this.messagingService = messagingService;
        this.jvmStateService = jvmStateService;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        this.sshConfig = sshConfig;
        this.groupStateNotificationService = groupStateNotificationService;
    }

    @Override
    public CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser) {
        final JvmControlOperation controlOperation = controlJvmRequest.getControlOperation();
        LOGGER.debug("Control JVM request operation = {}", controlOperation.toString());
        final Jvm jvm = jvmService.getJvm(controlJvmRequest.getJvmId());
        try {
            final JvmControlOperation ctrlOp = controlOperation;
            final String event = ctrlOp.getOperationState() == null ? ctrlOp.name() : ctrlOp.getOperationState().toStateLabel();

            historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), event, EventType.USER_ACTION, aUser.getId());

            // Send a message to the UI about the control operation.
            // Note: Sending the details of the control operation to a topic will enable the application to display
            //       the control event to all the UI's opened in different browsers.
            // TODO: We should also be able to send info to the UI about the other control operations e.g. thread dump, heap dump etc...
            if (ctrlOp.getOperationState() != null) {
                messagingService.send(new CurrentState<>(jvm.getId(), ctrlOp.getOperationState(), aUser.getId(), DateTime.now(),
                        StateType.JVM));
            }

            final WindowsJvmPlatformCommandProvider windowsJvmPlatformCommandProvider = new WindowsJvmPlatformCommandProvider();
            final ServiceCommandBuilder serviceCommandBuilder = windowsJvmPlatformCommandProvider.getServiceCommandBuilderFor(controlOperation);
            final ExecCommand execCommand = serviceCommandBuilder.buildCommandForService(jvm.getJvmName(), jvm.getUserName(), jvm.getEncryptedPassword());
            final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                    sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort()) , execCommand);

            RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

            // TODO: Decide whether we keep CommandOuput or RemoteCommandReturnInfo!
            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            final String standardOutput = commandOutput.getStandardOutput();
            final ExecReturnCode returnCode = commandOutput.getReturnCode();
            if (StringUtils.isNotEmpty(standardOutput) && (JvmControlOperation.START.equals(controlOperation) ||
                JvmControlOperation.STOP.equals(controlOperation))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", standardOutput);
            } else if (StringUtils.isNoneBlank(standardOutput) && JvmControlOperation.HEAP_DUMP.equals(controlOperation)
                    && returnCode.wasSuccessful()){
                commandOutput.cleanHeapDumpStandardOutput();
            }

            LOGGER.debug("Command output return code = {}", returnCode);
            if (returnCode.wasSuccessful()) {
                if (JvmControlOperation.STOP.equals(controlOperation)) {
                    LOGGER.debug("Updating state to {}...", JvmState.JVM_STOPPED);
                    jvmStateService.updateState(jvm.getId(), JvmState.JVM_STOPPED);
                    LOGGER.debug("State successfully updated to {}", JvmState.JVM_STOPPED);
                }
            } else {
                // Process non successful return codes...
                final String commandOutputReturnDescription = CommandOutputReturnCode.fromReturnCode(returnCode.getReturnCode()).getDesc();
                switch (returnCode.getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        jvmStateService.updateState(jvm.getId(), JvmState.FORCED_STOPPED);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        LOGGER.warn(commandOutputReturnDescription);
                        historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), commandOutputReturnDescription,
                                EventType.APPLICATION_ERROR, aUser.getId());
                        messagingService.send(new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM,
                                commandOutputReturnDescription));
                        break;
                    default:
                        final String errorMsg = "JVM control command was not successful! Return code = "
                                + returnCode.getReturnCode() + ", description = " +
                                commandOutputReturnDescription;

                        LOGGER.error(errorMsg);
                        historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), errorMsg,
                                EventType.APPLICATION_ERROR, aUser.getId());
                        messagingService.send(new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM,
                                errorMsg));

                        break;
                }
            }

            return commandOutput;
        } catch (final RemoteCommandExecutorServiceException e) {
            LOGGER.error(e.getMessage(), e);
            historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), e.getMessage(), EventType.APPLICATION_ERROR,
                    aUser.getId());
            messagingService.send(new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM, e.getMessage()));
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlJvmRequest, e);
        }
    }

    /**
     * Get the server name prefixed by the server type - "JVM".
     * @param jvm the {@link Jvm} object.
     * @return server name prefixed by "JVM".
     */
    private String getServerName(final Jvm jvm) {
        return JVM + " " + jvm.getJvmName();
    }

    @Override
    public CommandOutput secureCopyFile(final ControlJvmRequest secureCopyRequest, final String sourcePath,
                                        final String destPath, String userId) throws CommandFailureException {
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jpaJvm = jvmService.getJpaJvm(jvmId, true);

        final String event = secureCopyRequest.getControlOperation().name();
        final Jvm jvm = jvmService.getJvm(jvmId);
        final int beginIndex = destPath.lastIndexOf("/");
        final String fileName = destPath.substring(beginIndex + 1, destPath.length());
        // don't add any usage of the toc user internal directory to the history
        if (!AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue().endsWith(fileName)) {
            final String eventDescription = event + " " + fileName;
            historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), eventDescription, EventType.USER_ACTION, userId);
            messagingService.send(new JvmHistoryEvent(jvm.getId(), eventDescription, userId, DateTime.now(), JvmControlOperation.SECURE_COPY));
        }
        CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                jpaJvm.getName(),
                jpaJvm.getHostName(),
                JvmControlOperation.CHECK_FILE_EXISTS,
                new WindowsJvmPlatformCommandProvider(),
                destPath
        );
        if (commandOutput.getReturnCode().wasSuccessful()){
            String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
            final String destPathBackup = destPath + currentDateSuffix;
            remoteCommandExecutor.executeRemoteCommand(
                    jpaJvm.getName(),
                    jpaJvm.getHostName(),
                    JvmControlOperation.BACK_UP_FILE,
                    new WindowsJvmPlatformCommandProvider(),
                    destPath,
                    destPathBackup);
        }

        return remoteCommandExecutor.executeRemoteCommand(jpaJvm.getName(), jpaJvm.getHostName(), secureCopyRequest.getControlOperation(),
                new WindowsJvmPlatformCommandProvider(), sourcePath, destPath);
    }

    @Override
    public CommandOutput changeFileMode(Jvm jvm, String modifiedPermissions, String targetAbsoluteDir, String targetFile)
            throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                jvm.getJvmName(),
                jvm.getHostName(),
                JvmControlOperation.CHANGE_FILE_MODE,
                new WindowsJvmPlatformCommandProvider(),
                modifiedPermissions,
                targetAbsoluteDir,
                targetFile);
    }

    @Override
    public CommandOutput createDirectory(Jvm jvm, String dirAbsolutePath) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                jvm.getJvmName(),
                jvm.getHostName(),
                JvmControlOperation.CREATE_DIRECTORY,
                new WindowsJvmPlatformCommandProvider(),
                dirAbsolutePath);
    }
}
