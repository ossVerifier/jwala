package com.siemens.cto.aem.service.state.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl;


/**
 * Invoked in response to incoming state changes - jvm or web server
 */
public class GroupStateServiceImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceImpl.class);

    @Autowired 
    private GroupPersistenceService groupPersistenceService;

    @Autowired 
    private JvmPersistenceService jvmPersistenceService;
    
    @Autowired 
    private GroupStateManagerTableImpl groupStateManagerTableImpl;
    
    private User systemUser;
    
    public GroupStateServiceImpl() {
        systemUser = User.getSystemUser();
    }

    @Transactional
    public void stateUpdate(CurrentJvmState cjs) {
        
        LOGGER.info("State Update Received");
        
        // alias
        GroupStateManagerTableImpl gsm = groupStateManagerTableImpl;

        // lookup children
        Identifier<Jvm> jvmId = cjs.getJvmId();
        Jvm jvm = jvmPersistenceService.getJvm(jvmId);
        Set<LiteGroup> groups = jvm.getGroups();
        
        for(LiteGroup group : groups) {
            
            Group fullGroup = groupPersistenceService.getGroup(group.getId());
            
            gsm.initializeGroup(fullGroup, systemUser);
            
            switch(cjs.getJvmState()) { 
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
           
            if(gsm.getCurrentState() != fullGroup.getState()) {
                SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
                groupPersistenceService.updateGroupStatus(Event.create(sgsc, AuditEvent.now(systemUser)));
            }
        }
    }
    
    public void stateUpdate(Object o) {
        
        LOGGER.error("** State Update For Unknown Object Received **");
        
    }
}
