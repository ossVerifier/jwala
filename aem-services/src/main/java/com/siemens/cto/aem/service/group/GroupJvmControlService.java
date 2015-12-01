package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.request.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.domain.model.user.User;

import java.util.List;

public interface GroupJvmControlService {

    void controlGroup(final ControlGroupJvmRequest aCommand, final User aUser);
    
    void dispatchCommandComplete(List<JvmDispatchCommandResult> results);
}
