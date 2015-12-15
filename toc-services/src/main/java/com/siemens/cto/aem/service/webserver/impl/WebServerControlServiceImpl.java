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
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

public class WebServerControlServiceImpl implements WebServerControlService {

    private final WebServerService webServerService;
    private final WebServerCommandExecutor webServerCommandExecutor;
    private final StateService<WebServer, WebServerReachableState> webServerStateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private final HistoryService historyService;
    private ClientFactoryHelper clientFactoryHelper;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor,
                                       final StateService<WebServer, WebServerReachableState> theWebServerStateService,
                                       final Map<Identifier<WebServer>, WebServerReachableState> theWebServerReachableStateMap,
                                       final HistoryService historyService,
                                       ClientFactoryHelper clientFactoryHelper) {
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
        webServerStateService = theWebServerStateService;
        webServerReachableStateMap = theWebServerReachableStateMap;
        this.historyService = historyService;
        this.clientFactoryHelper = clientFactoryHelper;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest aCommand,
                                          final User aUser) {

        final JpaWebServer webServer = webServerService.getJpaWebServer(aCommand.getWebServerId().getId(), true);
        try {
            final String event = aCommand.getControlOperation().getOperationState() == null ?
                    aCommand.getControlOperation().name() :
                    aCommand.getControlOperation().getOperationState().toStateString();
            historyService.createHistory(webServer.getName(), webServer.getGroups(), event, EventType.USER_ACTION,
                    aUser.getId());

            aCommand.validate();

            final SetStateRequest<WebServer, WebServerReachableState> setStateCommand = createStateCommand(aCommand);
            webServerReachableStateMap.put(aCommand.getWebServerId(), setStateCommand.getNewState().getState());

            webServerStateService.setCurrentState(setStateCommand, aUser);

            final CommandOutput commandOutput = webServerCommandExecutor.controlWebServer(aCommand,
                    webServerService.getWebServer(aCommand.getWebServerId()));

            if (commandOutput != null &&
                    (aCommand.getControlOperation().equals(WebServerControlOperation.START) ||
                            aCommand.getControlOperation().equals(WebServerControlOperation.STOP))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output{}", commandOutput.getStandardOutput());

                // Set the states after sending out the control command.
                if (commandOutput.getReturnCode().wasSuccessful() || commandOutput.getReturnCode().wasAbnormallySuccessful()) {
                    final WebServerReachableState finalWebServerState =
                            aCommand.getControlOperation().equals(WebServerControlOperation.START) ?
                                    WebServerReachableState.WS_REACHABLE : WebServerReachableState.WS_UNREACHABLE;
                    webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                            finalWebServerState), aUser);
                } else {
                    setFailedState(aCommand, aUser, commandOutput.extractMessageFromStandardOutput());
                }

            }

            return commandOutput;
        } catch (final CommandFailureException cfe) {
            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
            historyService.createHistory(webServer.getName(), webServer.getGroups(), stackTrace,
                    EventType.APPLICATION_ERROR, aUser.getId());

            setFailedState(aCommand, aUser, stackTrace);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a Web Server: " + aCommand,
                    cfe);
        } finally {
            webServerReachableStateMap.remove(aCommand.getWebServerId());
        }
    }

    @Override
    public CommandOutput secureCopyHttpdConf(String aWebServerName, String sourcePath, String destPath) throws CommandFailureException {

        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);

        // ping the web server and return if not stopped
        ClientHttpResponse response = null;
        try {
            response = clientFactoryHelper.requestGet(aWebServer.getStatusUri());
            if (response.getStatusCode() == HttpStatus.OK) {
                return new CommandOutput(new ExecReturnCode(1), "", "The target web server must be stopped before attempting to copy the httpd.conf file to the server");
            }
        } catch (ConnectException e) {
            LOGGER.info("Ignore connect exception when attempting to replace resource files for the web server", e);
        } catch (ConnectTimeoutException e) {
            LOGGER.info("Ignore connect timeout exception when attempting to replace resource files for the web server", e);
        } catch (SocketTimeoutException e) {
            LOGGER.info("Ignore socket timeout exception when attempting to replace resource files for the web server", e);
        } catch (IOException e) {
            LOGGER.error("Failed to ping {} while attempting to copy httpd.config :: ERROR: {}", aWebServerName, e.getMessage());
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION,
                    "Failed to ping " + aWebServerName + " while attempting to copy httpd.config", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return webServerCommandExecutor.secureCopyHttpdConf(aWebServer, sourcePath, destPath);
    }

    /**
     * Set web server state to failed.
     *
     * @param aCommand {@link ControlWebServerRequest} which contains the id of the web server whose status is to be set to failed.
     * @param aUser    the user who issued the control command.
     * @param msg      the message that details the cause of the failed state.
     */
    private void setFailedState(final ControlWebServerRequest aCommand, final User aUser, String msg) {
        final WebServer webServer = webServerService.getWebServer(aCommand.getWebServerId());
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(aCommand.getWebServerId(), WebServerReachableState.WS_FAILED);
        webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                WebServerReachableState.WS_FAILED,
                msg), aUser);
    }

    /**
     * Sets the web server state.
     *
     * @param aCommand {@link ControlWebServerRequest}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final ControlWebServerRequest aCommand) {
        return new WebServerSetStateRequest(new CurrentState<>(aCommand.getWebServerId(),
                aCommand.getControlOperation().getOperationState(),
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
