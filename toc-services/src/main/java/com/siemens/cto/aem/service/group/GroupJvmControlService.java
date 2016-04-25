package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.domain.model.user.User;

import java.util.List;

public interface GroupJvmControlService {

    void controlGroup(final ControlGroupJvmRequest controlGroupJvmRequest, final User aUser);
    
    void dispatchCommandComplete(List<JvmDispatchCommandResult> results);

    /**
     * Control all JVMs.
     * @param controlGroupJvmRequest {@link ControlGroupJvmRequest}
     * @param user the user who's executing this method
     */
    void controlAllJvms(ControlGroupJvmRequest controlGroupJvmRequest, User user);
}
