package com.siemens.cto.aem.service.group;

import java.util.List;

import com.siemens.cto.aem.domain.model.dispatch.GroupDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface GroupControlService {

    GroupControlHistory controlGroup(final ControlGroupCommand aCommand, final User aUser);
    
    GroupControlHistory dispatchCommandComplete(GroupDispatchCommand aCommand, List<JvmDispatchCommandResult> results);

}
