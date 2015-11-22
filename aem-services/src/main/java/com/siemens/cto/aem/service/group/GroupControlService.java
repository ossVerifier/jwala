package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface GroupControlService {

    void controlGroup(ControlGroupCommand aCommand, User aUser);

    CurrentGroupState resetState(Identifier<Group> aGroupId, User fromContext);
}
