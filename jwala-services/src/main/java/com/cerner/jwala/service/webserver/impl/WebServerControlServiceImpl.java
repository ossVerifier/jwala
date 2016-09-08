package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.message.WebServerHistoryEvent;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.cerner.jwala.control.webserver.command.windows.WindowsWebServerNetOperation;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WebServerControlServiceImpl implements WebServerControlService {

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;

    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String WEB_SERVER = "Web Server";
    private final WebServerService webServerService;
    private final RemoteCommandExecutor<WebServerControlOperation> commandExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final HistoryService historyService;
    private final MessagingService messagingService;
    private final RemoteCommandExecutorService remoteCommandExecutorService;
    private final SshConfiguration sshConfig;

    public WebServerControlServiceImpl(final WebServerService webServerService,
                                       final RemoteCommandExecutor<WebServerControlOperation> commandExecutor,
                                       final HistoryService historyService,
                                       final MessagingService messagingService,
                                       final RemoteCommandExecutorService remoteCommandExecutorService,
                                       final SshConfiguration sshConfig) {
        this.webServerService = webServerService;
        this.commandExecutor = commandExecutor;
        this.historyService = historyService;
        this.messagingService = messagingService;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        this.sshConfig = sshConfig;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser) {
        final WebServerControlOperation controlOperation = controlWebServerRequest.getControlOperation();
        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        try {
            final String event = controlWebServerRequest.getControlOperation().getOperationState() == null ?
                    controlWebServerRequest.getControlOperation().name() : controlWebServerRequest.getControlOperation().getOperationState().toStateLabel();

            historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION,
                    aUser.getId());

            // Send a message to the UI about the control operation.
            if (controlOperation.getOperationState() != null) {
                messagingService.send(new CurrentState<>(webServer.getId(), controlWebServerRequest.getControlOperation().getOperationState(),
                        aUser.getId(), DateTime.now(), StateType.WEB_SERVER));
            } else if (controlOperation.equals(WebServerControlOperation.DELETE_SERVICE)
                    || controlOperation.equals(WebServerControlOperation.INVOKE_SERVICE)
                    || controlOperation.equals(WebServerControlOperation.SECURE_COPY)){
                messagingService.send(new WebServerHistoryEvent(webServer.getId(), controlOperation.name(), aUser.getId(), DateTime.now(), controlOperation));
            }

            final WindowsWebServerPlatformCommandProvider windowsJvmPlatformCommandProvider = new WindowsWebServerPlatformCommandProvider();
            final ServiceCommandBuilder serviceCommandBuilder = windowsJvmPlatformCommandProvider.getServiceCommandBuilderFor(controlOperation);
            final ExecCommand execCommand = serviceCommandBuilder.buildCommandForService(webServer.getName());
            final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                    sshConfig.getPassword(), webServer.getHost(), sshConfig.getPort()), execCommand);

            RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            final String standardOutput = commandOutput.getStandardOutput();
            if (StringUtils.isNotEmpty(standardOutput) && (WebServerControlOperation.START.equals(controlOperation) ||
                    WebServerControlOperation.STOP.equals(controlOperation))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", standardOutput);
            }

            // Process non successful return codes...
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                switch (commandOutput.getReturnCode().getReturnCode()) {
                    case ExecReturnCode.STP_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        webServerService.updateState(webServer.getId(), WebServerReachableState.FORCED_STOPPED, "");
                        break;
                    case ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS:
                        LOGGER.warn(CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
                        break;
                    default:
                        final String errorMsg = "Web Server control command was not successful! Return code = "
                                + commandOutput.getReturnCode().getReturnCode() + ", description = " +
                                CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc();

                        LOGGER.error(errorMsg);
                        historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), errorMsg,
                                EventType.APPLICATION_ERROR, aUser.getId());
                        messagingService.send(new CurrentState<>(webServer.getId(), WebServerReachableState.WS_FAILED,
                                DateTime.now(), StateType.WEB_SERVER, errorMsg));
                        break;
                }
            }
            return commandOutput;
        } catch (final RemoteCommandExecutorServiceException e) {
            LOGGER.error(e.getMessage(), e);
            historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), e.getMessage(),
                    EventType.APPLICATION_ERROR, aUser.getId());
            messagingService.send(new CurrentState<>(webServer.getId(), WebServerReachableState.WS_FAILED, DateTime.now(),
                    StateType.WEB_SERVER, e.getMessage()));
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlWebServerRequest, e);
        }
    }

    /**
     * Get the server name prefixed by the server type - "Web Server".
     *
     * @param webServer the {@link WebServer} object.
     * @return server name prefixed by "Web Server".
     */
    private String getServerName(WebServer webServer) {
        return WEB_SERVER + " " + webServer.getName();
    }

    @Override
    public CommandOutput secureCopyFile(final String aWebServerName, final String sourcePath, final String destPath, String userId) throws CommandFailureException {

        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);
        final int beginIndex = destPath.lastIndexOf('/');
        final String fileName = destPath.substring(beginIndex + 1, destPath.length());
        if (!AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue().endsWith(fileName)) {
            final String eventDescription = WindowsWebServerNetOperation.SECURE_COPY.name() + " " + fileName;
            historyService.createHistory(getServerName(aWebServer), new ArrayList<>(aWebServer.getGroups()), eventDescription, EventType.USER_ACTION, userId);
            messagingService.send(new WebServerHistoryEvent(aWebServer.getId(), eventDescription, userId, DateTime.now(), WebServerControlOperation.SECURE_COPY));
        }

        // back up the original file first
        final String host = aWebServer.getHost();
        CommandOutput commandOutput = commandExecutor.executeRemoteCommand(aWebServerName,
                host,
                WebServerControlOperation.CHECK_FILE_EXISTS,
                new WindowsWebServerPlatformCommandProvider(),
                destPath);
        if (commandOutput.getReturnCode().wasSuccessful()) {
            String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
            final String destPathBackup = destPath + currentDateSuffix;
            commandOutput = commandExecutor.executeRemoteCommand(
                    aWebServerName,
                    host,
                    WebServerControlOperation.BACK_UP_CONFIG_FILE,
                    new WindowsWebServerPlatformCommandProvider(),
                    destPath,
                    destPathBackup);
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Failed to back up the " + destPath + " for " + aWebServerName + ". Continuing with secure copy.");
            } else {
                LOGGER.info("Successfully backed up " + destPath + " at " + host);
            }
        }

        // run the scp command
        return commandExecutor.executeRemoteCommand(
                aWebServer.getName(),
                host,
                WebServerControlOperation.SECURE_COPY,
                new WindowsWebServerPlatformCommandProvider(),
                sourcePath,
                destPath);
    }

    @Override
    public CommandOutput createDirectory(WebServer webServer, String dirAbsolutePath) throws CommandFailureException {
        return commandExecutor.executeRemoteCommand(
                webServer.getName(),
                webServer.getHost(),
                WebServerControlOperation.CREATE_DIRECTORY,
                new WindowsWebServerPlatformCommandProvider(),
                dirAbsolutePath);

    }

    @Override
    public CommandOutput changeFileMode(WebServer webServer, String fileMode, String targetDirPath, String targetFile) throws CommandFailureException {
        return commandExecutor.executeRemoteCommand(
                webServer.getName(),
                webServer.getHost(),
                WebServerControlOperation.CHANGE_FILE_MODE,
                new WindowsWebServerPlatformCommandProvider(),
                fileMode,
                targetDirPath,
                targetFile);

    }

}
