package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class WebServerControlServiceImpl implements WebServerControlService {

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;

    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String WEB_SERVER = "Web Server";
    private final WebServerService webServerService;
    private final RemoteCommandExecutor<WebServerControlOperation> commandExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private final HistoryService historyService;
    private final StateNotificationService stateNotificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final RemoteCommandExecutor<WebServerControlOperation> theExecutor,
                                       final Map<Identifier<WebServer>, WebServerReachableState> theWebServerReachableStateMap,
                                       final HistoryService historyService,
                                       final StateNotificationService stateNotificationService,
                                       final SimpMessagingTemplate messagingTemplate) {
        webServerService = theWebServerService;
        commandExecutor = theExecutor;
        webServerReachableStateMap = theWebServerReachableStateMap;
        this.historyService = historyService;
        this.stateNotificationService = stateNotificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser) {
        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        CommandOutput commandOutput;
        try {
            final String event = controlWebServerRequest.getControlOperation().getOperationState() == null ?
                    controlWebServerRequest.getControlOperation().name() : controlWebServerRequest.getControlOperation().getOperationState().toStateLabel();

            historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION,
                    aUser.getId());

            // Send a message to the UI about the control operation.
            // Note: Sending the details of the control operation to a topic will enable the application to display
            //       the control event to all the UI's opened in different browsers.
            messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(webServer.getId(),
                    controlWebServerRequest.getControlOperation().getOperationState(), aUser.getId(), DateTime.now(), StateType.WEB_SERVER));

            commandOutput = commandExecutor.executeRemoteCommand(webServer.getName(), webServer.getHost(),
                    controlWebServerRequest.getControlOperation(), new WindowsWebServerPlatformCommandProvider());

            if (commandOutput != null && StringUtils.isNotEmpty(commandOutput.getStandardOutput()) &&
                    (controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.START) ||
                     controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.STOP))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", commandOutput.getStandardOutput());
            }

            // Process non successful return codes...
            if (commandOutput != null && !commandOutput.getReturnCode().wasSuccessful()) {
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
                        historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), errorMsg, EventType.APPLICATION_ERROR,
                                aUser.getId());
                        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(webServer.getId(), WebServerReachableState.WS_FAILED,
                                DateTime.now(), StateType.WEB_SERVER, errorMsg));

                        break;
                }
            }

        } catch (final CommandFailureException cfe) {
            LOGGER.error("Remote Command Failure: CommandFailureException when attempting to control a Web Server: " +
                    controlWebServerRequest, cfe);


            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
            historyService.createHistory(getServerName(webServer), new ArrayList<>(webServer.getGroups()), stackTrace,
                    EventType.APPLICATION_ERROR, aUser.getId());

            setFailedState(webServer, stackTrace);

            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "CommandFailureException when attempting to control a Web Server: "
                    + controlWebServerRequest, cfe);
        }

        return commandOutput;
    }

    /**
     * Get the server name prefixed by the server type - "Web Server".
     * @param webServer the {@link WebServer} object.
     * @return server name prefixed by "Web Server".
     */
    private String getServerName(WebServer webServer) {
        return WEB_SERVER + " " + webServer.getName();
    }

    @Override
    public CommandOutput secureCopyFileWithBackup(final String aWebServerName, final String sourcePath, final String destPath, final boolean doBackup) throws CommandFailureException {

        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);

        // back up the original file first
        final String host = aWebServer.getHost();
        if (doBackup) {
            String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
            final String destPathBackup = destPath + currentDateSuffix;
            final CommandOutput commandOutput = commandExecutor.executeRemoteCommand(
                    aWebServer.getName(),
                    host,
                    WebServerControlOperation.BACK_UP_HTTP_CONFIG_FILE,
                    new WindowsWebServerPlatformCommandProvider(),
                    destPath,
                    destPathBackup);
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.error("Failed to back up the " + destPath + " for " + aWebServer.getName());
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + aWebServer.getName());
            } else {
                LOGGER.info("Successfully backed up " + destPath + " at " + host);
            }
        }

        // run the scp command
        return commandExecutor.executeRemoteCommand(
                aWebServer.getName(),
                host,
                WebServerControlOperation.DEPLOY_HTTP_CONFIG_FILE,
                new WindowsWebServerPlatformCommandProvider(),
                sourcePath,
                destPath);
    }

    /**
     * Set web server state to failed.
     *
     * @param webServer the web server.
     * @param msg       the message that details the cause of the failed state.
     */
    private void setFailedState(final WebServer webServer, String msg) {
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(webServer.getId(), WebServerReachableState.WS_FAILED);
        webServerService.updateErrorStatus(webServer.getId(), msg);

        // Send the error via JMS so TOC client can display it in the control window.
//        stateNotificationService.notifyStateUpdated(new CurrentState(webServer.getId(), WebServerReachableState.WS_FAILED,
//                DateTime.now(), StateType.WEB_SERVER));

        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(webServer.getId(), WebServerReachableState.WS_FAILED,
                DateTime.now(), StateType.WEB_SERVER, msg));
    }

    /**
     * Sets the web server state.
     *
     * @param controlWebServerRequest {@link ControlWebServerRequest}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final ControlWebServerRequest controlWebServerRequest) {
        return new WebServerSetStateRequest(new CurrentState<>(controlWebServerRequest.getWebServerId(),
                controlWebServerRequest.getControlOperation().getOperationState(),
                DateTime.now(),
                StateType.WEB_SERVER));
    }

    /**
     * Sets the web server state.
     *
     * @param anId     the web server id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param aState   the state {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @param aMessage a message e.g. error message etc.
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState,
                                                                           final String aMessage) {
        return new WebServerSetStateRequest(new CurrentState<>(anId,
                aState,
                DateTime.now(),
                StateType.WEB_SERVER,
                aMessage));
    }

    /**
     * Sets the web server state.
     *
     * @param anId   the web server id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState) {
        return new WebServerSetStateRequest(new CurrentState<>(anId,
                aState,
                DateTime.now(),
                StateType.WEB_SERVER));
    }

}
