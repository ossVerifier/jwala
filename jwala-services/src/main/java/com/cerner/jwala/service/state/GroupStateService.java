package com.cerner.jwala.service.state;

import java.util.List;

import com.cerner.jwala.common.domain.model.group.CurrentGroupState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.group.ControlGroupRequest;
import com.cerner.jwala.common.request.group.SetGroupStateRequest;

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
