package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public interface GroupStateService {

    interface Events {
        void stateUpdateJvm(CurrentState<Jvm, JvmState> cjs);

        void stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> state);
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
    }
}
