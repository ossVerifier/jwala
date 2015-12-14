package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.ControlGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;

public interface GroupControlService {

    void controlGroup(ControlGroupRequest aCommand, User aUser);

    CurrentGroupState resetState(Identifier<Group> aGroupId, User fromContext);
}
