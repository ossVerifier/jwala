package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.command.group.ControlGroupCommand;
import com.siemens.cto.aem.domain.command.group.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

import java.util.List;

public interface GroupStateService {

    interface Events {
        List<SetGroupStateCommand> stateUpdateJvm(CurrentState<Jvm, JvmState> cjs) throws InterruptedException;

        List<SetGroupStateCommand> stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> state) throws InterruptedException;
        
        SetGroupStateCommand stateUpdateRequest(Group group) throws InterruptedException;
    }

    interface Triggers {
        CurrentGroupState signalReset(Identifier<Group> groupId, User user);

        CurrentGroupState signalStopRequested(Identifier<Group> groupId, User user);

        CurrentGroupState signalStartRequested(Identifier<Group> groupId, User user);

        CurrentGroupState signal(ControlGroupCommand aCommand, User aUser);

    }

    interface Query {

        boolean canStart(Identifier<Group> groupId, User user);

        boolean canStop(Identifier<Group> groupId, User user);
    }

    interface API extends Events, Triggers, Query {

        SetGroupStateCommand groupStateNotify(SetGroupStateCommand sgsc);

        SetGroupStateCommand groupStatePersist(SetGroupStateCommand sgsc);

        SetGroupStateCommand groupStateUnlock(SetGroupStateCommand sgsc);
    }
}
