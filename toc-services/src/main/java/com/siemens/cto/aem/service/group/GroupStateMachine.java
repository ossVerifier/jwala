package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.user.User;

public interface GroupStateMachine {

    void synchronizedInitializeGroup(Group group, User user);

    CurrentGroupState signalStopRequested(User user);

    CurrentGroupState signalStartRequested(User user);

    boolean canStart();

    boolean canStop();
    
    GroupState getCurrentState();

    Group getCurrentGroup();

    CurrentGroupState getCurrentStateDetail();

}
