package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.webserver.command.impl.LinuxWebServerPlatformCommandProvider;
import com.cerner.jwala.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.cerner.jwala.control.webserver.command.windows.WindowsWebServerNetOperation;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.host.HostService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation.START;
import static com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation.STOP;

public class WebServerControlServiceImpl implements WebServerControlService {

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;
    @Autowired
    private HostService hostService;

    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String WEB_SERVER = "Web Server";
    private final WebServerService webServerService;
    private final RemoteCommandExecutor<WebServerControlOperation> commandExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final RemoteCommandExecutorService remoteCommandExecutorService;
    private final HistoryFacadeService historyFacadeService;
    private final SshConfiguration sshConfig;
    private static final int SLEEP_DURATION = 1000;

    private static final String MSG_SERVICE_ALREADY_STARTED = "Service already started";
    private static final String MSG_SERVICE_ALREADY_STOPPED = "Service already stopped";

    public WebServerControlServiceImpl(final WebServerService webServerService,
                                       final RemoteCommandExecutor<WebServerControlOperation> commandExecutor,
                                       final RemoteCommandExecutorService remoteCommandExecutorService,
                                       final SshConfiguration sshConfig, HistoryFacadeService historyFacadeService) {
        this.webServerService = webServerService;
        this.commandExecutor = commandExecutor;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        this.sshConfig = sshConfig;
        this.historyFacadeService = historyFacadeService;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser) {
        final WebServerControlOperation controlOperation = controlWebServerRequest.getControlOperation();
        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        try {
            final String event = controlOperation.getOperationState() == null ?
                    controlOperation.name() : controlOperation.getOperationState().toStateLabel();

            historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION_INFO, aUser.getId());

            final PlatformCommandProvider platformCommandProvider = getPlatformCommandProvider(webServer);
            final ServiceCommandBuilder serviceCommandBuilder = platformCommandProvider.getServiceCommandBuilderFor(controlOperation);
            final ExecCommand execCommand = serviceCommandBuilder.buildCommandForService(webServer.getName());
            final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                    sshConfig.getPassword(), webServer.getHost(), sshConfig.getPort()), execCommand);

            RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            final String standardOutput = commandOutput.getStandardOutput();
            if (StringUtils.isNotEmpty(standardOutput) && (START.equals(controlOperation) ||
                    STOP.equals(controlOperation))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", standardOutput);
            }

            // Process non successful return codes...
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                final Integer returnCode = commandOutput.getReturnCode().getReturnCode();
                String commandOutputReturnDescription = CommandOutputReturnCode.fromReturnCode(returnCode).getDesc();
                switch (returnCode) {
                    case ExecReturnCode.JWALA_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        webServerService.updateState(webServer.getId(), WebServerReachableState.FORCED_STOPPED, "");
                        break;
                    case ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS:
                        int retCode = 0;
                        switch (controlOperation) {
                            case START:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STARTED;
                                break;
                            case STOP:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STOPPED;
                                break;
                            default:
                                retCode = returnCode;
                                break;
                        }

                        sendMessageToActionEventLogs(aUser, webServer, commandOutputReturnDescription);

                        if (retCode == 0) {
                            commandOutput = new CommandOutput(new ExecReturnCode(retCode), commandOutputReturnDescription, null);
                        }
                        break;
                    case ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE:
                        if (controlOperation.equals(START) || controlOperation.equals(STOP)) {
                            sendMessageToActionEventLogs(aUser, webServer, commandOutputReturnDescription);
                        } else {
                            final String errorMsg = createCommandErrorMessage(commandOutput, returnCode, controlOperation.getExternalValue());
                            sendMessageToActionEventLogs(aUser, webServer, errorMsg);
                        }

                        break;
                    default:
                        final String errorMsg = createCommandErrorMessage(commandOutput, returnCode, controlOperation.getExternalValue());
                        sendMessageToActionEventLogs(aUser, webServer, errorMsg);
                        break;
                }
            }
            return commandOutput;
        } catch (final RemoteCommandExecutorServiceException e) {
            LOGGER.error(e.getMessage(), e);

            historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), e.getMessage(),
                    EventType.SYSTEM_ERROR, aUser.getId());

            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlWebServerRequest, e);
        }
    }

    private String createCommandErrorMessage(CommandOutput commandOutput, Integer returnCode, String commandName) {
        return "Web Server control command [" + commandName + "] was not successful! Return code = "
                + returnCode + ", description = " +
                CommandOutputReturnCode.fromReturnCode(returnCode).getDesc() +
                ", message = " + commandOutput.standardErrorOrStandardOut();
    }

    private void sendMessageToActionEventLogs(User aUser, WebServer webServer, String commandOutputReturnDescription) {
        LOGGER.error(commandOutputReturnDescription);
        historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), commandOutputReturnDescription,
                EventType.SYSTEM_ERROR, aUser.getId());
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
        final String fileName = new File(destPath).getName();
        if (!ApplicationProperties.get("remote.commands.user-scripts").endsWith(fileName)) {
            historyFacadeService.write(getServerName(aWebServer), new ArrayList<>(aWebServer.getGroups()),
                    WindowsWebServerNetOperation.SECURE_COPY.name() + " " + fileName, EventType.USER_ACTION_INFO, userId);
        }

        // back up the original file first
        final String host = aWebServer.getHost();
        CommandOutput commandOutput = commandExecutor.executeRemoteCommand(
                aWebServerName,
                host,
                WebServerControlOperation.CHECK_FILE_EXISTS,
                new WindowsWebServerPlatformCommandProvider(),
                destPath
        );
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("Found the file {}", destPath);
        } else {
            final String parentDir;
            if (destPath.startsWith("~")) {
                parentDir = destPath.substring(0, destPath.lastIndexOf("/"));
            } else {
                parentDir = new File(destPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
            }
            commandOutput = commandExecutor.executeRemoteCommand(
                    aWebServerName,
                    host,
                    WebServerControlOperation.CREATE_DIRECTORY,
                    new WindowsWebServerPlatformCommandProvider(),
                    parentDir
            );
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Successfully created parent directory {} on host {}", parentDir, host);
            } else {
                final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("create command failed with error trying to create parent directory {} on {} :: ERROR: {}", parentDir, host, standardError);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
            }
        }
        commandOutput = commandExecutor.executeRemoteCommand(aWebServerName,
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
                    WebServerControlOperation.BACK_UP,
                    new WindowsWebServerPlatformCommandProvider(),
                    destPath,
                    destPathBackup);
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                final String standardError = "Failed to back up the " + destPath + " for " + aWebServerName + ". Continuing with secure copy.";
                LOGGER.error(standardError);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
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

    @Override
    public boolean waitForState(ControlWebServerRequest controlWebServerRequest, final Long waitTimeout) {
        final Long startTime = DateTime.now().getMillis();
        final WebServerControlOperation webServerControlOperation = controlWebServerRequest.getControlOperation();
        while (true) {
            final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
            LOGGER.info("Retrieved web server: {}", webServer);
            switch (webServerControlOperation) {
                case START:
                    if (webServer.getState() == WebServerReachableState.WS_REACHABLE) {
                        return true;
                    }
                    break;
                case STOP:
                    if (webServer.getState() == WebServerReachableState.WS_UNREACHABLE ||
                            webServer.getState() == WebServerReachableState.FORCED_STOPPED) {
                        return true;
                    }
                    break;
                default:
                    throw new InternalErrorException(FaultType.SERVICE_EXCEPTION, "Command: " + webServerControlOperation.toString() + " not supported");
            }
            if (DateTime.now().getMillis() - startTime > waitTimeout) {
                LOGGER.warn("Timeout reached to get the state for webserver: {}", webServer.getName());
                break;
            }
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                LOGGER.error("Error with Thread.sleep", e);
                throw new InternalErrorException(FaultType.SERVICE_EXCEPTION, "Error with waiting for state for WebServer: " + webServer.getName(), e);
            }
        }
        return false;
    }


    /**
     * @param aWebServer
     * @return
     */
    private PlatformCommandProvider<WebServerControlOperation> getPlatformCommandProvider(WebServer aWebServer) throws ApplicationServiceException {
        //Determine Host OS
        if (HostService.UNAME_LINUX.equals(getHostOS(aWebServer.getHost()))) {
            LinuxWebServerPlatformCommandProvider linuxWindowsPlatformCommandProvider = new LinuxWebServerPlatformCommandProvider();
            return linuxWindowsPlatformCommandProvider;
        } else if (HostService.UNAME_CYGWIN.equals(getHostOS(aWebServer.getHost()))) {
            WindowsWebServerPlatformCommandProvider windowsWindowsPlatformCommandProvider = new WindowsWebServerPlatformCommandProvider();
            return windowsWindowsPlatformCommandProvider;
        }
        return null;
    }

    /**
     * @param hostName
     * @return
     */
    private String getHostOS(String hostName) throws ApplicationServiceException {
        String uName = hostService.getUName(hostName);
        if (uName != null && uName.indexOf(hostService.UNAME_LINUX) > -1) {
            return HostService.UNAME_LINUX;
        } else if (uName != null && uName.indexOf(hostService.UNAME_CYGWIN) > -1) {
            return HostService.UNAME_CYGWIN;
        } else {
            throw new ApplicationServiceException("Unknown host OS");
        }
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }
}