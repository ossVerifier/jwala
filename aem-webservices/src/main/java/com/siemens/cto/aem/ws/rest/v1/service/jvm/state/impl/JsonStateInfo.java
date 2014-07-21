package com.siemens.cto.aem.ws.rest.v1.service.jvm.state.impl;

import java.util.Set;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;

/**
 * Intended for serialization of state notifications
 */
@Deprecated
public class JsonStateInfo {

    public class JsonGroupState {

        final Identifier<Group>   id;
        final GroupState          state;
        final DateTime            asOf;
        
        public JsonGroupState(Group group) {
            id = group.getId();
            state = group.getCurrentState().getState();
            asOf = group.getCurrentState().getAsOf();
        }
    }
    
    final Set<CurrentJvmState>    jvms;
    final Set<JsonGroupState>     groups;
    
    public JsonStateInfo(final Set<CurrentJvmState> jvms, final Set<JsonGroupState> groups) {
        this.jvms =  jvms;
        this.groups = groups;
    }
}
