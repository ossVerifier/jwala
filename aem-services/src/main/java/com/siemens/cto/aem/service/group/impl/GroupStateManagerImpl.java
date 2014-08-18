package com.siemens.cto.aem.service.group.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public class GroupStateManagerImpl extends AbstractGroupStateManager {

    GroupState readPerceivedState() {

        // Consider that we might be a long lived instance, so reload.
        Group group = groupPersistenceService.getGroup(getCurrentGroup().getId());
        
        // Some of the servers in question may actually be in a STARTING or START REQUESTED state. 
        // or stopping or stop requested state
        // in these cases, this web server should return that specific state
        // if that state is returned, then we should not transition to the state 
        // identified by the other elements, but we should stay where we are.

        GroupState jvmState = readPerceivedStateJvms(group);
        GroupState webState = readPerceivedStateWebServers(group);

        if(webState == GroupState.INITIALIZED) {
            return jvmState;
        }
        if(jvmState == GroupState.INITIALIZED) {
            return webState;
        }

        if(jvmState == GroupState.ERROR || webState == GroupState.ERROR) {
            return GroupState.ERROR;
        }
        
        if(jvmState.equals(GroupState.STARTING)
                || jvmState.equals(GroupState.STOPPING)) {
            return jvmState;
        }

        if(webState.equals(GroupState.STARTING)
                || webState.equals(GroupState.STOPPING)) {
            return webState;
        }

        if(!(jvmState.equals(webState))) { 
            return GroupState.PARTIAL;
        }

        return jvmState; // both are the same at this point.
    }

    GroupState readPerceivedStateJvms(Group group) { 
        
        int started = 0, unstarted = 0, errors = 0, starting = 0, stopping = 0;
        for(Jvm jvm : group.getJvms()) {
            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId());
            if(jvmState == null) { 
                ++unstarted;
            } else {
                switch(jvmState.getState()) {
                case FAILED:
                    ++errors;
                    break;
                case INITIALIZED: 
                case UNKNOWN:
                    default: break;
                    case START_REQUESTED:
                        ++unstarted;
                        ++starting;
                        break;
                    case STOP_REQUESTED:
                        ++started;
                        ++stopping;
                        break;
                    case STOPPED:
                        ++unstarted;
                        break;
                    case STARTED:
                        ++started;
                        break;
                
                }
            }
        }

        jvmsDetail.setStarted(started);
        jvmsDetail.setTotal(unstarted + started);
        
        return progressToState(starting, stopping, started, unstarted, errors); 
    }
    
    GroupState progressToState(int starting, int stopping, int started, int unstarted, int errors) {
        if(errors > 0) return GroupState.ERROR;
        
        if(starting > 0 && stopping == 0 && currentState == GroupState.STARTING) {
            return GroupState.STARTING;
        }
       
        if(stopping > 0 && starting == 0 && currentState == GroupState.STOPPING) {
            return GroupState.STOPPING;
        }
        
        if(started == 0 && unstarted == 0) { 
            return GroupState.INITIALIZED;
        } else if(started == 0) {
            return GroupState.STOPPED;
        } else if(started > 0 && unstarted > 0) {
            return GroupState.PARTIAL;
        } else {
            return GroupState.STARTED;
        }
    }

    GroupState readPerceivedStateWebServers(Group group) {

        int started = 0, unstarted = 0, errors = 0 /*unsupported for web servers*/, starting = 0, stopping = 0;

        List<WebServer> webServers = webServerDao.findWebServersBelongingTo(group.getId(), PaginationParameter.all());

        if(!webServers.isEmpty()) {

            Set<Identifier<WebServer>> webServerSet = new HashSet<>();
            for(WebServer webServer : webServers) {
                webServerSet.add(webServer.getId());
            }
            Set<CurrentState<WebServer,WebServerReachableState>> results = webServerStateService.getCurrentStates(webServerSet);

            for(CurrentState<WebServer, WebServerReachableState> wsState : results) {
                switch(wsState.getState()) {
                case UNKNOWN:
                    break;
                default: 
                    break;
                case START_REQUESTED:
                    ++unstarted;
                    ++starting;
                    break;
                case STOP_REQUESTED:
                    ++started;
                    ++stopping;
                    break;
                case UNREACHABLE:
                    ++unstarted;
                    break;
                case REACHABLE:
                    ++started;
                    break;                
                }
            }
        }
        
        webServersDetail.setStarted(started);
        webServersDetail.setTotal(unstarted + started);
        
        return progressToState(starting, stopping, started, unstarted, errors); 
    }
}
