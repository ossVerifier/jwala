package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
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
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    // TODO: Initialize with the constructor!
    private RemoteCommandExecutorService remoteCommandExecutorService;

    @Autowired
    // TODO: Initialize with the constructor!
    private SshConfiguration sshConfig;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final RemoteCommandExecutor<JvmControlOperation> theExecutor,
                                 final HistoryService historyService,
                                 final MessagingService messagingService,
                                 final JvmStateService jvmStateService) {
        jvmService = theJvmService;
        remoteCommandExecutor = theExecutor;
        this.historyService = historyService;
        this.messagingService = messagingService;
        this.jvmStateService = jvmStateService;
    }

    @Override
    public CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser) {
        LOGGER.debug("Control JVM request operation = {}", controlJvmRequest.getControlOperation().toString());
        final Jvm jvm = jvmService.getJvm(controlJvmRequest.getJvmId());
        try {
            final JvmControlOperation ctrlOp = controlJvmRequest.getControlOperation();
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

            // CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(jvm.getJvmName(), jvm.getHostName(),
            //    ctrlOp, new WindowsJvmPlatformCommandProvider());

            // The code above was replaced by the code below which eliminate's a lot of java class layers just to execute
            // a command via JSCH. Once the said code has be tested thoroughly, then the commented out code above will
            // be deleted along with its class and supporting files.
            final WindowsJvmPlatformCommandProvider windowsJvmPlatformCommandProvider = new WindowsJvmPlatformCommandProvider();
            final ServiceCommandBuilder serviceCommandBuilder = windowsJvmPlatformCommandProvider.getServiceCommandBuilderFor(controlJvmRequest.getControlOperation());
            final ExecCommand execCommand = serviceCommandBuilder.buildCommandForService(jvm.getJvmName());
            final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                    sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort()) , execCommand);

            RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

            // TODO: Decide whether we keep CommandOuput or RemoteCommandReturnInfo!
            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            if (StringUtils.isNotEmpty(commandOutput.getStandardOutput()) && (JvmControlOperation.START
                    .equals(controlJvmRequest.getControlOperation()) || JvmControlOperation.STOP
                    .equals(controlJvmRequest.getControlOperation()))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", commandOutput.getStandardOutput());
            }

            LOGGER.debug("Command output return code = {}", commandOutput.getReturnCode());
            if (commandOutput.getReturnCode().wasSuccessful()) {
                if (JvmControlOperation.STOP.equals(controlJvmRequest.getControlOperation())) {
                    jvmStateService.updateState(jvm.getId(), JvmState.JVM_STOPPED);
                }
            } else {
                // Process non successful return codes...
                final String commandOutputReturnDescription = CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc();
                switch (commandOutput.getReturnCode().getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        jvmStateService.updateState(jvm.getId(), JvmState.FORCED_STOPPED);
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        LOGGER.warn(commandOutputReturnDescription);
                        historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), commandOutputReturnDescription, EventType.APPLICATION_ERROR, aUser.getId());
                        messagingService.send(new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM,
                                commandOutputReturnDescription));
                        break;
                    default:
                        final String errorMsg = "JVM control command was not successful! Return code = "
                                + commandOutput.getReturnCode().getReturnCode() + ", description = " +
                                commandOutputReturnDescription;

                        LOGGER.error(errorMsg);
                        historyService.createHistory(getServerName(jvm), new ArrayList<>(jvm.getGroups()), errorMsg, EventType.APPLICATION_ERROR,
                                aUser.getId());
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

    @Override
    public CommandOutput changeFileMode(Jvm jvm, String modifiedPermissions, String targetAbsoluteDir, String targetFile) throws CommandFailureException {
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
