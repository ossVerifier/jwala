package com.siemens.cto.aem.service.state.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerState;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.GroupStateService;


/**
 * Invoked in response to incoming state changes - jvm or web server
 */
public class GroupStateServiceImpl implements GroupStateService.API {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceImpl.class);

    @Autowired 
    private GroupPersistenceService groupPersistenceService;

    @Autowired 
    private JvmPersistenceService jvmPersistenceService;
    
    @Autowired 
    private GroupStateMachine groupStateMachine;
    
    private User systemUser;
    
    public GroupStateServiceImpl() {
        systemUser = User.getSystemUser();
    }

    @Transactional
    @Override
    public void stateUpdate(CurrentJvmState cjs) {
        
        LOGGER.debug("State Update Received");
        
        // alias
        GroupStateMachine gsm = groupStateMachine;

        // lookup children
        Identifier<Jvm> jvmId = cjs.getJvmId();
        Jvm jvm = jvmPersistenceService.getJvm(jvmId);
        
        if(jvm == null) {
            return;
        }
        Set<LiteGroup> groups = jvm.getGroups();
        
        for(LiteGroup group : groups) {
            
            Group fullGroup = groupPersistenceService.getGroup(group.getId());
            CurrentGroupState currentGroupState = 
                    fullGroup.getCurrentState() == null 
                    ? null : fullGroup.getCurrentState();
            GroupState  groupState = 
                    currentGroupState == null 
                    ? null : currentGroupState.getState();
            
            gsm.initializeGroup(fullGroup, systemUser);
            
            internalHandleJvmStateUpdate(gsm, jvmId, cjs.getJvmState());            
           
            if(gsm.getCurrentState() != groupState) {
                SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
                groupPersistenceService.updateGroupStatus(Event.create(sgsc, AuditEvent.now(systemUser)));
                
                CurrentGroupState groupStateDetail = gsm.getCurrentStateDetail();
                LOGGER.info("Group State Service: " + groupStateDetail.toString());
            }
        }
    }
    
    private void internalHandleJvmStateUpdate(GroupStateMachine gsm, Identifier<Jvm> jvmId, JvmState jvmState) {

        switch(jvmState) { 
        case STARTED: 
            gsm.jvmStarted(jvmId);
            break;
        case STOPPED:
            gsm.jvmStopped(jvmId);
            break;
        case FAILED:
            gsm.jvmError(jvmId);
            break;
        case INITIALIZED:
        case START_REQUESTED:
        case STOP_REQUESTED:
        case UNKNOWN:
        default:
            // no action needed for these states
            break;
        }
    }

    @Override
    public void stateUpdate(WebServerState wsState) {
        LOGGER.error("** State Update For WebServerState Received - not implemented **");        
    }

    /**
     * @param group group to get a state machine for.
     * @return the state machine
     */
    private GroupStateMachine getGsmById(Identifier<Group> groupId, User user) { 
        GroupStateMachine gsm = groupStateMachine;
        Group group = groupPersistenceService.getGroup(groupId);
        gsm.initializeGroup(group, user);
        return gsm;
    }
    @Override
    public void signalReset(Identifier<Group> groupId, User user) {
        getGsmById(groupId, user).signalReset(user);
    }

    @Override
    public void signalStopRequested(Identifier<Group> groupId, User user) {
        getGsmById(groupId, user).signalStopRequested(user);
    }

    @Override
    public void signalStartRequested(Identifier<Group> groupId, User user) {
        getGsmById(groupId, user).signalStartRequested(user);
    }

    @Override
    public boolean canStart(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).canStart();
    }

    @Override
    public boolean canStop(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).canStop();
    }
}
