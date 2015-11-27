package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.domain.command.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.command.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.domain.command.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.webserver.ControlWebServerCommand;
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

            ControlWebServerCommand controlWebServerCommand = new ControlWebServerCommand(webServerDispatchCommand
                    .getWebServer().getId(), groupDispatchCommand.getCommand().getControlOperation());

            CommandOutput output = webServerControlService.controlWebServer(controlWebServerCommand,
                    groupDispatchCommand.getUser());

            wasSuccessful = output.getReturnCode().getWasSuccessful();

        } catch (RuntimeException e) {
            wasSuccessful = false;
            LOGGER.warn("Group dispatch (" + groupDispatchCommand.toString() + "): ControlJvmCommand ("
                    + webServerDispatchCommand.toString() + ") failed: ", e);
        }

        return new WebServerDispatchCommandResult(wasSuccessful, groupDispatchCommand);
    }
}
