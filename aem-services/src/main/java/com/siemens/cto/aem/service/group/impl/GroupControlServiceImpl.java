package com.siemens.cto.aem.service.group.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;

public class GroupControlServiceImpl implements GroupControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupControlServiceImpl.class);

    private final GroupJvmControlService groupJvmControlService;

    public GroupControlServiceImpl(final GroupJvmControlService theGroupJvmControlService) {

        groupJvmControlService = theGroupJvmControlService;
    }

    @Transactional
    @Override
    public GroupControlHistory controlGroup(ControlGroupCommand aCommand, User aUser) {

        LOGGER.debug("begin controlGroup operation {} for groupId {}", aCommand.getControlOperation(), aCommand.getGroupId());
        aCommand.validateCommand();
        
        JvmControlOperation jvmControlOperation = aCommand.getControlOperation();  // eventually convert from Group Operation to JvmOperation
        ControlGroupJvmCommand controlGroupJvmCommand = new ControlGroupJvmCommand(aCommand.getGroupId(), jvmControlOperation);
        
        return groupJvmControlService.controlGroup(controlGroupJvmCommand, aUser);
    }

}
