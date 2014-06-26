package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupControlService;

public class GroupControlServiceImpl implements GroupControlService {

    @Override
    public GroupControlHistory controlJvm(ControlGroupCommand aCommand, User aUser) {
        return null;
    }

}
