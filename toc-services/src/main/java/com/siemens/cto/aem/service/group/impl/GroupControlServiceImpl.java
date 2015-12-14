package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.request.group.ControlGroupRequest;
import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.state.GroupStateService;
import org.springframework.transaction.annotation.Transactional;

public class GroupControlServiceImpl implements GroupControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupControlServiceImpl.class);

    private final GroupJvmControlService groupJvmControlService;

    private final GroupStateService.API groupStateService; 
    
    private final GroupWebServerControlService groupWebServerControlService;
    
    public GroupControlServiceImpl(
            final GroupWebServerControlService theGroupWebServerControlService,
            final GroupJvmControlService theGroupJvmControlService,
            final GroupStateService.API theGroupStateService) {
        groupWebServerControlService = theGroupWebServerControlService;
        groupJvmControlService = theGroupJvmControlService;
        groupStateService = theGroupStateService;
    }

    @Transactional
    @Override
    public void controlGroup(ControlGroupRequest aCommand, User aUser) {

        LOGGER.info("begin controlGroup operation {} for groupId {}", aCommand.getControlOperation(),
                aCommand.getGroupId());

        aCommand.validateCommand(
                groupStateService.canStart(aCommand.getGroupId(), aUser), 
                groupStateService.canStop(aCommand.getGroupId(), aUser));
        
        groupStateService.signal(aCommand, aUser);

        controlWebServers(aCommand, aUser);
        controlJvms(aCommand, aUser);
    }

    private void controlWebServers(ControlGroupRequest aCommand, User aUser) {

        WebServerControlOperation wsControlOperation = WebServerControlOperation.convertFrom(aCommand
                .getControlOperation().getExternalValue());

        ControlGroupWebServerRequest controlGroupWebServerCommand = new ControlGroupWebServerRequest(
                aCommand.getGroupId(), wsControlOperation);

        groupWebServerControlService.controlGroup(controlGroupWebServerCommand, aUser);
    }

    private void controlJvms(ControlGroupRequest aCommand, User aUser) {
  
        JvmControlOperation jvmControlOperation = JvmControlOperation.convertFrom(aCommand.getControlOperation()
                .getExternalValue()); // TODO address this mapping between
                                      // operations

        ControlGroupJvmRequest controlGroupJvmCommand = new ControlGroupJvmRequest(aCommand.getGroupId(),
                jvmControlOperation);

        groupJvmControlService.controlGroup(controlGroupJvmCommand, aUser);
    }

    @Transactional
    @Override
    public CurrentGroupState resetState(Identifier<Group> aGroupId, User aUser) {
        return groupStateService.signalReset(aGroupId, aUser);
    }
}
