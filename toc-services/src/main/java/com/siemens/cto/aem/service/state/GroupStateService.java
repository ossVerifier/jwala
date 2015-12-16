package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.request.group.SetGroupStateRequest;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.ControlGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

import java.util.List;

public interface GroupStateService {

    interface Events {
        List<SetGroupStateRequest> stateUpdateJvm(CurrentState<Jvm, JvmState> cjs) throws InterruptedException;

        List<SetGroupStateRequest> stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> state) throws InterruptedException;
        
        SetGroupStateRequest stateUpdateRequest(Group group) throws InterruptedException;
    }

    interface Triggers {
        CurrentGroupState signalReset(Identifier<Group> groupId, User user);

        CurrentGroupState signalStopRequested(Identifier<Group> groupId, User user);

        CurrentGroupState signalStartRequested(Identifier<Group> groupId, User user);

        CurrentGroupState signal(ControlGroupRequest controlGroupRequest, User aUser);

    }

    interface Query {

        boolean canStart(Identifier<Group> groupId, User user);

        boolean canStop(Identifier<Group> groupId, User user);
    }

    interface API extends Events, Triggers, Query {

        SetGroupStateRequest groupStateNotify(SetGroupStateRequest sgsc);

        SetGroupStateRequest groupStatePersist(SetGroupStateRequest sgsc);

        SetGroupStateRequest groupStateUnlock(SetGroupStateRequest sgsc);
    }
}
