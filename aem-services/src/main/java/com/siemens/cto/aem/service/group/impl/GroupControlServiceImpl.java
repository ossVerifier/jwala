package com.siemens.cto.aem.service.group.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.webserver.GroupWebServerControlService;

public class GroupControlServiceImpl implements GroupControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupControlServiceImpl.class);

    private final GroupJvmControlService groupJvmControlService;

    private final GroupWebServerControlService groupWebServerControlService;

    public GroupControlServiceImpl(final GroupWebServerControlService theGroupWebServerControlService,
            final GroupJvmControlService theGroupJvmControlService) {
        groupWebServerControlService = theGroupWebServerControlService;
        groupJvmControlService = theGroupJvmControlService;
    }

    @Transactional
    @Override
    public GroupControlHistory controlGroup(ControlGroupCommand aCommand, User aUser) {

        LOGGER.debug("begin controlGroup operation {} for groupId {}", aCommand.getControlOperation(),
                aCommand.getGroupId());

        // TODO: incomplete controlHistory 
        
        aCommand.validateCommand();
        controlWebServers(aCommand, aUser);
        controlJvms(aCommand, aUser);

        // TODO: complete control history - really should be a callback after all commands. 
        
        return null;
    }

    protected void controlWebServers(ControlGroupCommand aCommand, User aUser) {

        WebServerControlOperation wsControlOperation = WebServerControlOperation.convertFrom(aCommand
                .getControlOperation().getExternalValue());

        ControlGroupWebServerCommand controlGroupWebServerCommand = new ControlGroupWebServerCommand(
                aCommand.getGroupId(), wsControlOperation);

        groupWebServerControlService.controlGroup(controlGroupWebServerCommand, aUser);
    }

    protected void controlJvms(ControlGroupCommand aCommand, User aUser) {
  
        JvmControlOperation jvmControlOperation = JvmControlOperation.convertFrom(aCommand.getControlOperation()
                .getExternalValue()); // TODO address this mapping between
                                      // operations

        ControlGroupJvmCommand controlGroupJvmCommand = new ControlGroupJvmCommand(aCommand.getGroupId(),
                jvmControlOperation);

        groupJvmControlService.controlGroup(controlGroupJvmCommand, aUser);
    }
}
