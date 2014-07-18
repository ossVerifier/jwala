package com.siemens.cto.aem.service.webserver.impl;

import java.util.List;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.GroupWebServerControlService;

public class GroupWebServerControlServiceImpl implements GroupWebServerControlService {

    private final GroupControlPersistenceService persistenceService;
    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupWebServerControlServiceImpl(final GroupControlPersistenceService thePersistenceService,
            final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {

        persistenceService = thePersistenceService;
        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }
    
    @Override
    public GroupControlHistory controlGroup(ControlGroupWebServerCommand aCommand, User aUser) {

        aCommand.validateCommand();

        GroupControlHistory controlHistoryEvent = persistenceService.addIncompleteControlHistoryEvent(new Event<GroupCommand>(
                aCommand, AuditEvent.now(aUser)));

        Group group = groupService.getGroup(aCommand.getGroupId());

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(group, aCommand, aUser,
                controlHistoryEvent.getId());
        
        commandDispatchGateway.asyncDispatchCommand(dispatchCommand);

        return controlHistoryEvent;
    }

    @Override
    public GroupControlHistory dispatchCommandComplete(List<WebServerDispatchCommandResult> results) {
        return null;
    }

}
