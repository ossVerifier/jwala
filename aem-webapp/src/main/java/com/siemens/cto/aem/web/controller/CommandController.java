package com.siemens.cto.aem.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.JvmControlService;

@Controller
public class CommandController {

    @Autowired
    @Qualifier("jvmControlService")
    private JvmControlService jvmControlService;

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
