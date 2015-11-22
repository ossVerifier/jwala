package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

import java.util.List;

public interface GroupJvmControlService {

    void controlGroup(final ControlGroupJvmCommand aCommand, final User aUser);
    
    void dispatchCommandComplete(List<JvmDispatchCommandResult> results);
}
