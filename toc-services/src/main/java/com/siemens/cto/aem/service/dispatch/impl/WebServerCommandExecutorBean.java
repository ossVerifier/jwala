package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.request.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.request.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.request.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.request.webserver.ControlWebServerRequest;
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

        Boolean wasSuccessful = false;

        try {

            ControlWebServerRequest controlWebServerCommand = new ControlWebServerRequest(webServerDispatchCommand
                    .getWebServer().getId(), groupDispatchCommand.getRequest().getControlOperation());

            CommandOutput output = webServerControlService.controlWebServer(controlWebServerCommand,
                    groupDispatchCommand.getUser());

            wasSuccessful = output.getReturnCode().getWasSuccessful();

        } catch (RuntimeException e) {
            wasSuccessful = false;
            LOGGER.warn("Group dispatch (" + groupDispatchCommand.toString() + "): ControlJvmRequest ("
                    + webServerDispatchCommand.toString() + ") failed: ", e);
        }

        return new WebServerDispatchCommandResult(wasSuccessful, groupDispatchCommand);
    }
}
