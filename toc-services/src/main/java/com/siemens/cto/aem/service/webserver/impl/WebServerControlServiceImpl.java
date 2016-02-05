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
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class WebServerControlServiceImpl implements WebServerControlService {

    private final WebServerService webServerService;
    private final RemoteCommandExecutor<WebServerControlOperation> commandExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private final HistoryService historyService;
    private final StateNotificationService stateNotificationService;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final RemoteCommandExecutor<WebServerControlOperation> theExecutor,
                                       final Map<Identifier<WebServer>, WebServerReachableState> theWebServerReachableStateMap,
                                       final HistoryService historyService,
                                       final StateNotificationService stateNotificationService) {
        webServerService = theWebServerService;
        commandExecutor = theExecutor;
        webServerReachableStateMap = theWebServerReachableStateMap;
        this.historyService = historyService;
        this.stateNotificationService = stateNotificationService;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser) {


        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        CommandOutput commandOutput = null;
        try {
            final String event = controlWebServerRequest.getControlOperation().getOperationState() == null ?
                    controlWebServerRequest.getControlOperation().name() : controlWebServerRequest.getControlOperation().getOperationState().toStateLabel();
            historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION,
                    aUser.getId());

            commandOutput = commandExecutor.executeRemoteCommand(webServer.getName(), webServer.getHost(),
                    controlWebServerRequest.getControlOperation(), new WindowsWebServerPlatformCommandProvider());
        } catch (final CommandFailureException cfe) {
            LOGGER.error("Remote Command Failure: CommandFailureException when attempting to control a Web Server: " +
                    controlWebServerRequest, cfe);


            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
            historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), stackTrace,
                    EventType.APPLICATION_ERROR, aUser.getId());

            setFailedState(webServer, stackTrace);

            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "CommandFailureException when attempting to control a Web Server: "
                    + controlWebServerRequest, cfe);
        }

        return commandOutput;
    }

    //    @Override
//    @Transactional
//    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest,
//                                          final User aUser) {
//
////        final JpaWebServer webServer = webServerService.getJpaWebServer(controlWebServerRequest.getWebServerId().getId(), true);
//        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
//        try {
//            final String event = controlWebServerRequest.getControlOperation().getOperationState() == null ?
//                    controlWebServerRequest.getControlOperation().name() :
//                    controlWebServerRequest.getControlOperation().getOperationState().toStateLabel();
//            historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION,
//                    aUser.getId());
//
//            controlWebServerRequest.validate();
//
//            final SetStateRequest<WebServer, WebServerReachableState> setStateCommand = createStateCommand(controlWebServerRequest);
//            webServerReachableStateMap.put(controlWebServerRequest.getWebServerId(), setStateCommand.getNewState().getState());
//
//            webServerStateService.setCurrentState(setStateCommand, aUser);
//            webServerService.updateState();
//
//            CommandOutput commandOutput = commandExecutor.executeRemoteCommand(
//                    webServer.getName(),
//                    webServer.getHost(),
//                    controlWebServerRequest.getControlOperation(),
//                    new WindowsWebServerPlatformCommandProvider());
//
//            if (commandOutput != null &&
//                    (controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.START) ||
//                            controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.STOP))) {
//                commandOutput.cleanStandardOutput();
//                LOGGER.info("shell command output{}", commandOutput.getStandardOutput());
//
//                // Set the states after sending out the control command.
//                if (commandOutput.getReturnCode().wasSuccessful() || commandOutput.getReturnCode().wasAbnormallySuccessful()) {
//                    final WebServerReachableState finalWebServerState =
//                            controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.START) ?
//                                    WebServerReachableState.WS_REACHABLE : WebServerReachableState.WS_UNREACHABLE;
//                    webServerStateService.setCurrentState(createStateCommand(controlWebServerRequest.getWebServerId(),
//                            finalWebServerState), aUser);
//                } else if (commandOutput.getReturnCode().getReturnCode() == ExecReturnCode.STP_EXIT_PROCESS_KILLED) {
//                    webServerStateService.setCurrentState(createStateCommand(controlWebServerRequest.getWebServerId(),
//                            WebServerReachableState.WS_UNREACHABLE, FORCED_STOPPED), aUser);
//                    commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, "");
//                } else {
//                    setFailedState(controlWebServerRequest, aUser, commandOutput.extractMessageFromStandardOutput());
//                }
//
//            }
//
//            return commandOutput;
//        } catch (final CommandFailureException cfe) {
//            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
//            historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), stackTrace,
//                    EventType.APPLICATION_ERROR, aUser.getId());
//
//            setFailedState(controlWebServerRequest, aUser, stackTrace);
//            LOGGER.error("Remote Command Failure: CommandFailureException when attempting to control a Web Server: " + controlWebServerRequest, cfe);
//            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
//                    "CommandFailureException when attempting to control a Web Server: " + controlWebServerRequest,
//                    cfe);
//        } finally {
//            webServerReachableStateMap.remove(controlWebServerRequest.getWebServerId());
//        }
//    }

    @Override
    public CommandOutput secureCopyHttpdConf(String aWebServerName, String sourcePath, String destPath) throws CommandFailureException {

        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);

        // back up the original file first
        String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
        final String destPathBackup = destPath + currentDateSuffix;
        final CommandOutput commandOutput = commandExecutor.executeRemoteCommand(
                aWebServer.getName(),
                aWebServer.getHost(),
                WebServerControlOperation.BACK_UP_HTTP_CONFIG_FILE,
                new WindowsWebServerPlatformCommandProvider(),
                destPath,
                destPathBackup);
        if (!commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to back up the httpd.conf for " + aWebServer);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up the httpd.conf for " + aWebServer);
        }

        // run the scp command
        return commandExecutor.executeRemoteCommand(
                aWebServer.getName(),
                aWebServer.getHost(),
                WebServerControlOperation.DEPLOY_HTTP_CONFIG_FILE,
                new WindowsWebServerPlatformCommandProvider(),
                sourcePath,
                destPath);
    }

    /**
     * Set web server state to failed.
     *
     * @param webServer the web server.
     * @param msg the message that details the cause of the failed state.
     */
    private void setFailedState(final WebServer webServer, String msg) {
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(webServer.getId(), WebServerReachableState.WS_FAILED);
        webServerService.updateErrorStatus(webServer.getId(), msg);

        // TODO: We need to find out whether we should still inform the UI of a failed web server control attempt via JMS
        stateNotificationService.notifyStateUpdated(new CurrentState(webServer.getId(), WebServerReachableState.WS_FAILED,
                DateTime.now(), StateType.WEB_SERVER));
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
