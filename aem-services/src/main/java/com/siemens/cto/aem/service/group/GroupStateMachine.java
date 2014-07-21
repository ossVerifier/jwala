package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface GroupStateMachine {

    void initializeGroup(Group group, User user);

    void signalReset(User user);

    void signalStopRequested(User user);

    void signalStartRequested(User user);

    void jvmError(Identifier<Jvm> jvmId);

    void jvmStopped(Identifier<Jvm> jvmId);

    void jvmStarted(Identifier<Jvm> jvmId);

    boolean canStart();

    boolean canStop();
    
    GroupState getCurrentState();

    Group getCurrentGroup();

}
