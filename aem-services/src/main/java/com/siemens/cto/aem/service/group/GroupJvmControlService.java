package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.command.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.command.group.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.user.User;

import java.util.List;

public interface GroupJvmControlService {

    void controlGroup(final ControlGroupJvmCommand aCommand, final User aUser);
    
    void dispatchCommandComplete(List<JvmDispatchCommandResult> results);
}
