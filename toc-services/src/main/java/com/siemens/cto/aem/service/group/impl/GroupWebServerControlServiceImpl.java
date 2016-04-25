package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.common.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GroupWebServerControlServiceImpl implements GroupWebServerControlService {

    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupWebServerControlServiceImpl(final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {
        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }
    
    @Transactional
    @Override
    public void controlGroup(ControlGroupWebServerRequest controlGroupWebServerRequest, User aUser) {

        controlGroupWebServerRequest.validate();

        Group group = groupService.getGroup(controlGroupWebServerRequest.getGroupId());

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(group, controlGroupWebServerRequest, aUser);
        
        commandDispatchGateway.asyncDispatchCommand(dispatchCommand);
    }

    @Override
    public void controlAllWebSevers(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User user) {
        commandDispatchGateway.asyncDispatchCommand(new GroupWebServerDispatchCommand(null, controlGroupWebServerRequest,
                user));
    }

    @Transactional
    @Override
    public void dispatchCommandComplete(final List<WebServerDispatchCommandResult> results) {
        // We need to have this or else Spring integration will complain that this service
        // does not have any eligible methods for handling Messages.
    }
}
