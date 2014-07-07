package com.siemens.cto.aem.service.group.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupService;

public class GroupControlServiceImpl implements GroupControlService {

    private final GroupControlPersistenceService persistenceService;
    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupControlServiceImpl(final GroupControlPersistenceService thePersistenceService,
            final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {

        persistenceService = thePersistenceService;
        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }

    @Transactional
    @Override
    public GroupControlHistory controlGroup(ControlGroupCommand aCommand, User aUser) {

        aCommand.validateCommand();

        GroupControlHistory controlHistoryEvent = persistenceService.addIncompleteControlHistoryEvent(new Event<>(
                aCommand, AuditEvent.now(aUser)));

        Group group = groupService.getGroup(aCommand.getGroupId());

        GroupJvmDispatchCommand dispatchCommand = new GroupJvmDispatchCommand(group, aCommand, aUser,
                controlHistoryEvent.getId());
        commandDispatchGateway.asyncDispatchCommand(dispatchCommand);

        return controlHistoryEvent;
    }

    @Transactional
    public GroupControlHistory dispatchCommandComplete(List<JvmDispatchCommandResult> results) {

        GroupControlHistory completeHistory = null;

        if (results.size() != 0) {

            GroupJvmDispatchCommand aCommand = results.get(0).getGroupJvmDispatchCommand();

            long successCount = 0;
            
            for (JvmDispatchCommandResult jvmDispatchCommandResult : results) {
                if (jvmDispatchCommandResult.wasSuccessful()) {
                    successCount++;
                }
            }
            
            completeHistory = persistenceService.completeControlHistoryEvent(new Event<>(
                    new CompleteControlGroupCommand(aCommand.getGroupControlHistoryId(), results.size(), successCount), AuditEvent.now(aCommand
                            .getUser())));

            // notify that the command is complete

        }
        return completeHistory;
    }

}
