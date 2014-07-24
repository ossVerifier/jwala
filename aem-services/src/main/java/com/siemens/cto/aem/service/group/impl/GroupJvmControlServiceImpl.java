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
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;

public class GroupJvmControlServiceImpl implements GroupJvmControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmControlServiceImpl.class);

    private final GroupControlPersistenceService persistenceService;
    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupJvmControlServiceImpl(final GroupControlPersistenceService thePersistenceService,
            final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {

        persistenceService = thePersistenceService;
        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }

    @Transactional
    @Override
    public GroupControlHistory controlGroup(ControlGroupJvmCommand aCommand, User aUser) {

        aCommand.validateCommand();

        GroupControlHistory controlHistoryEvent = persistenceService.addIncompleteControlHistoryEvent(new Event<GroupCommand>(
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

        if (results != null && !results.isEmpty()) {

            GroupJvmDispatchCommand aCommand = null;
            long successCount = 0;
            long totalCount = 0;
            
            for (JvmDispatchCommandResult jvmDispatchCommandResult : results) {
                aCommand = jvmDispatchCommandResult.getGroupJvmDispatchCommand();
                if (jvmDispatchCommandResult.wasSuccessful()) {
                    successCount++;
                }
                ++totalCount;
            }
            
            completeHistory = persistenceService.completeControlHistoryEvent(new Event<>(
                    new CompleteControlGroupCommand(aCommand.getGroupControlHistoryId(), totalCount, successCount), AuditEvent.now(aCommand
                            .getUser())));

            String logMsg = "Group Dispatch: Command Complete: " + successCount + " of " + totalCount + " succeeded.";
            if(successCount == results.size()) {
                LOGGER.info(logMsg);
            } else {
                LOGGER.warn(logMsg);
            }
        }
        return completeHistory;
    }

}
