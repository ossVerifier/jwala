package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

public interface GroupStateMachine {

    void synchronizedInitializeGroup(Group group, User user);

    CurrentGroupState signalReset(User user);

    CurrentGroupState signalStopRequested(User user);

    CurrentGroupState signalStartRequested(User user);
    
    boolean refreshState();

    void jvmError(Identifier<Jvm> jvmId);

    void jvmStopped(Identifier<Jvm> jvmId);

    void jvmStarted(Identifier<Jvm> jvmId);
    
    void wsError(Identifier<WebServer> wsId);

    void wsReachable(Identifier<WebServer> wsId);

    void wsUnreachable(Identifier<WebServer> wsId);

    boolean canStart();

    boolean canStop();
    
    GroupState getCurrentState();

    Group getCurrentGroup();

    CurrentGroupState getCurrentStateDetail();

}
