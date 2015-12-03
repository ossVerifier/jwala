package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.request.group.ControlGroupRequest;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.user.User;

public interface GroupControlService {

    void controlGroup(ControlGroupRequest aCommand, User aUser);

    CurrentGroupState resetState(Identifier<Group> aGroupId, User fromContext);
}
