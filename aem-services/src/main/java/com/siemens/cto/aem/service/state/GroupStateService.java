package com.siemens.cto.aem.service.state;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public interface GroupStateService {

    interface Events {
        List<SetGroupStateCommand> stateUpdateJvm(CurrentState<Jvm, JvmState> cjs);

        List<SetGroupStateCommand> stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> state);

        Set<Identifier<Group>> stateUpdateJvmSplitOnly(CurrentState<Jvm, JvmState> cjs);

        Collection<Identifier<Group>> stateUpdateWebServerSplitOnly(CurrentState<WebServer, WebServerReachableState> cjs);

        SetGroupStateCommand coalescedGroupRefresh(Identifier<Group> groupId);
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
    }
}
