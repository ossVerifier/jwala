package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.domain.model.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.service.webserver.WebServerControlService;

public class WebServerCommandExecutorBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
            .getLogger(WebServerCommandExecutorBean.class);

    private WebServerControlService webServerControlService;

    public WebServerCommandExecutorBean(WebServerControlService theWebServerControlService) {
        webServerControlService = theWebServerControlService;
    }

    public WebServerDispatchCommandResult startStop(WebServerDispatchCommand webServerDispatchCommand) {

        GroupWebServerDispatchCommand groupDispatchCommand = webServerDispatchCommand
                .getGroupWebServerDispatchCommand();

        LOGGER.debug("Execute command {}", webServerDispatchCommand);

        WebServerControlHistory webServerControlHistory = null;
        Boolean wasSuccessful = false;

        try {

            ControlWebServerCommand controlWebServerCommand = new ControlWebServerCommand(webServerDispatchCommand
                    .getWebServer().getId(), groupDispatchCommand.getCommand().getControlOperation());

            webServerControlHistory = webServerControlService.controlWebServer(controlWebServerCommand,
                    groupDispatchCommand.getUser());

            wasSuccessful = webServerControlHistory.getExecData().getReturnCode().getWasSuccessful();

        } catch (RuntimeException e) {
            wasSuccessful = false;
            LOGGER.warn("Group dispatch (" + groupDispatchCommand.toString() + "): ControlJvmCommand ("
                    + webServerDispatchCommand.toString() + ") failed: ", e);
        }

        WebServerDispatchCommandResult result = new WebServerDispatchCommandResult(wasSuccessful,
                webServerControlHistory.getId(), groupDispatchCommand);

        return result;
    }
}
