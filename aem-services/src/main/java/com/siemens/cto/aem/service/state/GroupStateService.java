package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerState;

public interface GroupStateService {

    public interface Events {
        void stateUpdate(CurrentJvmState cjs);
    
        void stateUpdate(WebServerState state);
    }    
    
    public interface Triggers {
        void signalReset(Identifier<Group> groupId, User user);

        void signalStopRequested(Identifier<Group> groupId, User user);

        void signalStartRequested(Identifier<Group> groupId, User user);
    }
    
    public interface Query { 

        boolean canStart(Identifier<Group> groupId, User user); 

        boolean canStop(Identifier<Group> groupId, User user);        
    }
    
    public interface API extends Events, Triggers, Query {
        
    }
}
