package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.group.ControlGroupRequest;

public interface GroupControlService {

    void controlGroup(ControlGroupRequest controlGroupRequest, User aUser);

}
