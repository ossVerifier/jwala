package com.siemens.cto.aem.service.group.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class GroupStateManagerImpl {

    Group       currentGroup;
    GroupState  currentState;
    States      fsmState;
    Triggers    triggers = new Triggers();
    
    GroupStateManagerImpl() {}
    
    void setState(GroupState state) { 
        currentState = state;
    }
    
    boolean canStart() {
        return States.LOOKUP_MAP.get(currentState).canStart();
    }
    
    void jvmStarted(Identifier<Jvm> jvmId) {
        triggers.jvms.add(jvmId);
        fsmState = States.LOOKUP_MAP.get(currentState).changeState(fsmState, this);
    }
    
    private class Triggers { 
        public boolean reset;
        public List<Identifier<Jvm>> jvms;
    }
    
    private interface StateTransition  { 
        States changeState(States fsmState, GroupStateManagerImpl self);
        boolean canStart();
    }
    
    private enum States implements StateTransition { 
        ANY_UP(null) {

            @Override
            public States changeState(States fsmState, GroupStateManagerImpl self) {
                return fsmState;
            }

            @Override
            public boolean canStart() {
                return false;
            }
        },
        ERROR(GroupState.ERROR) {

            @Override
            public States changeState(States fsmState, GroupStateManagerImpl self) {
                if(self.triggers.reset) { 
                    return fsmState.changeState(States.ANY_UP, self);
                } else { 
                    return self.fsmState;
                }
            }

            @Override
            public boolean canStart() {
                return false;
            }
            
        };
        
        private GroupState state;
        private static final Map<GroupState, States> LOOKUP_MAP = new HashMap<>();

        static {
            for (final States state : values()) {
                LOOKUP_MAP.put(state.state, state);
            }
        }
        States(GroupState mappedState) { 
            state = mappedState; 
        }
    }
    
}
