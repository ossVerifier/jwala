package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GroupJvmControlServiceImpl implements GroupJvmControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmControlServiceImpl.class);

    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupJvmControlServiceImpl(final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {

        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }

    @Transactional
    @Override
    public void controlGroup(ControlGroupJvmRequest controlGroupJvmRequest, User aUser) {

        controlGroupJvmRequest.validate();

        Group group = groupService.getGroup(controlGroupJvmRequest.getGroupId());

        GroupJvmDispatchCommand dispatchCommand = new GroupJvmDispatchCommand(group, controlGroupJvmRequest, aUser);

        // TODO: Deprecate spring integration stuff in the future!
        commandDispatchGateway.asyncDispatchCommand(dispatchCommand);
    }

    @Transactional
    public void dispatchCommandComplete(List<JvmDispatchCommandResult> results) {

        LOGGER.debug("entering dispatchCommandComplete with results {}", results);
        
        if (results != null && !results.isEmpty()) {

            long successCount = 0;
            long totalCount = 0;
            
            for (JvmDispatchCommandResult jvmDispatchCommandResult : results) {
                jvmDispatchCommandResult.getGroupJvmDispatchCommand();
                if (jvmDispatchCommandResult.wasSuccessful()) {
                    successCount++;
                }
                ++totalCount;
            }

            String logMsg = "Group Dispatch : Command Complete: " + successCount + " of " + totalCount + " succeeded.";
            if(successCount == results.size()) {
                LOGGER.info(logMsg);
            } else {
                LOGGER.warn(logMsg);
            }
        }
    }

    @Override
    public void controlAllJvms(final ControlGroupJvmRequest controlGroupJvmRequest, final User user) {
        commandDispatchGateway.asyncDispatchCommand(new GroupJvmDispatchCommand(null, controlGroupJvmRequest, user));
    }
}
