package com.siemens.cto.aem.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;

@Controller
public class CommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

    private JvmControlService jvmControlService;

    private WebServerCommandService webServerCommandService;

    @Autowired
    public CommandController(JvmControlService aJvmControlService, WebServerCommandService aWebServerCommandService) {
        jvmControlService = aJvmControlService;
        webServerCommandService = aWebServerCommandService;
    }

    @RequestMapping(value = "/jvmCommand")
    public ModelAndView jvmCommand(HttpServletRequest request, HttpServletResponse response) {

        Identifier<Jvm> jvmIdentifier = getJvmIdParameter(request);
        ControlJvmCommand aCommand = getControlOperation(request, jvmIdentifier);

        JvmControlHistory jvmControlHistory = jvmControlService.controlJvm(aCommand, User.getSystemUser());

        ModelAndView mv = new ModelAndView("cmd/textOutput");
        mv.addObject("stdErr", jvmControlHistory.getExecData().getStandardError());
        mv.addObject("stdOut", jvmControlHistory.getExecData().getStandardOutput());

        return mv;
    }

    @RequestMapping(value = "/webServerCommand")
    public void webServerCommand(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Identifier<WebServer> id = new Identifier<>(request.getParameter("webServerId"));
        final String ERROR_MSG_PREFIX = "Error reading HTTPD Conf: ";
        response.setContentType("text/plain");
        try {
            final ExecData execData = webServerCommandService.getHttpdConf(id);
            if (execData.getReturnCode().wasSuccessful()) {
                response.getWriter().print(execData.getStandardOutput());
            } else {
                response.getWriter().print(ERROR_MSG_PREFIX + execData.getStandardError());
            }
        } catch (CommandFailureException cmdFailEx) {
            LOGGER.warn("Command Failure occurred", cmdFailEx);
            response.getWriter().print(ERROR_MSG_PREFIX + cmdFailEx.getMessage());
        }
    }

    protected ControlJvmCommand getControlOperation(HttpServletRequest request, Identifier<Jvm> jvmIdentifier) {
        String operation = request.getParameter("operation");
        JvmControlOperation theControlOperation = JvmControlOperation.convertFrom(operation);
        ControlJvmCommand jvmCommand = new ControlJvmCommand(jvmIdentifier, theControlOperation);
        return jvmCommand;
    }

    protected Identifier<Jvm> getJvmIdParameter(HttpServletRequest request) {
        String parameter = request.getParameter("jvmId");
        long id = Long.parseLong(parameter);
        return new Identifier<>(id);
    }

}