package com.siemens.cto.aem.service.state.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Splitter;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;


/**
 * Invoked in response to incoming state changes - jvm or web server
 */
public class GroupStateServiceImpl extends StateServiceImpl<Group, GroupState> implements StateService<Group, GroupState>, GroupStateService.API {

    public GroupStateServiceImpl(StatePersistenceService<Group, GroupState> thePersistenceService,
            StateNotificationService theNotificationService, StateType theStateType,
            StateNotificationGateway theStateNotificationGateway) {
        super(thePersistenceService, theNotificationService, theStateType, theStateNotificationGateway);

        systemUser = User.getSystemUser();
    }

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceImpl.class);

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private WebServerDao webServerDao;

    @Autowired
    private GroupStateMachine groupStateMachine;
   
    private User systemUser;

    @Transactional
    @Override
    @Splitter
    public List<SetGroupStateCommand> stateUpdateJvm(CurrentState<Jvm, JvmState> cjs) {

        LOGGER.debug("Recalculating group state due to jvm update: " + cjs.toString());

        // alias
        GroupStateMachine gsm = groupStateMachine;

        // lookup children
        Identifier<Jvm> jvmId = cjs.getId();
        Jvm jvm = jvmPersistenceService.getJvm(jvmId);

        if(jvm == null) {
            return Collections.<SetGroupStateCommand>emptyList();
        }

        Set<LiteGroup> groups = jvm.getGroups();
        
        if(groups == null || groups.isEmpty()) {
            return Collections.<SetGroupStateCommand>emptyList();
        }
        
        List<SetGroupStateCommand> result = null;
        result = new ArrayList<>(groups.size());

        for(LiteGroup group : groups) {

            Group fullGroup = groupPersistenceService.getGroup(group.getId());

            gsm.initializeGroup(fullGroup, systemUser);

            internalHandleJvmStateUpdate(gsm, jvmId, cjs.getState());

            // could check for changes and only persist/notify on changes
            SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
            
            result.add(sgsc);
        }
        
        return result;
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

    @Transactional
    @Override
    @Splitter
    public List<SetGroupStateCommand>  stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> wsState) {
        LOGGER.debug("Recalculating group state due to web server update: " + wsState.toString());

        // alias
        GroupStateMachine gsm = groupStateMachine;

        // lookup children
        Identifier<WebServer> wsId = wsState.getId();
        WebServer ws = webServerDao.getWebServer(wsId);

        if(ws == null) {
            return Collections.<SetGroupStateCommand>emptyList();
        }

        Collection<Group> groups = ws.getGroups();
        
        if(groups == null || groups.isEmpty()) {
            return Collections.<SetGroupStateCommand>emptyList();
        }
        
        List<SetGroupStateCommand> result = new ArrayList<>(groups.size());

        for(Group group : groups) {

            Group fullGroup = groupPersistenceService.getGroup(group.getId());

            gsm.initializeGroup(fullGroup, systemUser);

            internalHandleWebServerStateUpdate(gsm, wsId, wsState.getState());

            // could check for changes and only persist/notify on changes
            SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
            
            result.add(sgsc);
        }
        
        return result;
    }

    private void internalHandleWebServerStateUpdate(GroupStateMachine gsm, Identifier<WebServer> wsId, WebServerReachableState webServerReachableState) {

        switch(webServerReachableState) {
        case REACHABLE:
            gsm.wsReachable(wsId);
            break;
        case UNREACHABLE:
            gsm.wsUnreachable(wsId);
            break;
        case START_REQUESTED:
        case STOP_REQUESTED:
        case UNKNOWN:
        default:
            // no action needed for these states
            break;
        }

        // note - error is not supported.
    }

    /**
     * @param groupId group to get a state machine for.
     * @return the state machine
     */
    private GroupStateMachine getGsmById(Identifier<Group> groupId, User user) {
        GroupStateMachine gsm = groupStateMachine;
        Group group = groupPersistenceService.getGroup(groupId);
        gsm.initializeGroup(group, user);
        return gsm;
    }
    @Override
    public CurrentGroupState signalReset(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).signalReset(user);
    }

    @Override
    public CurrentGroupState signalStopRequested(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).signalStopRequested(user);
    }

    @Override
    public CurrentGroupState signalStartRequested(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).signalStartRequested(user);
    }

    @Override
    public boolean canStart(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).canStart();
    }

    @Override
    public boolean canStop(Identifier<Group> groupId, User user) {
        return getGsmById(groupId, user).canStop();
    }

    @Override
    protected CurrentState<Group, GroupState> createUnknown(Identifier<Group> anId) {
        return new CurrentGroupState(anId, GroupState.UNKNOWN, DateTime.now());
    }

    @Override
    public CurrentGroupState signal(ControlGroupCommand aCommand, User aUser) {
        switch(aCommand.getControlOperation()) {
        case START:
            return signalStartRequested(aCommand.getGroupId(), aUser);
        case STOP:
            return signalStopRequested(aCommand.getGroupId(), aUser);
        default:
            return null;
        }
    }
    
    @Override
    @Transactional
    public SetGroupStateCommand groupStatePersist(SetGroupStateCommand sgsc) {
        // If an empty list is returned by the splitter, it will be treated as single null item, so check
        if(sgsc != null) {
            groupPersistenceService.updateGroupStatus(Event.create(sgsc, AuditEvent.now(systemUser)));
        }
        return sgsc;
    }

    @Override
    public SetGroupStateCommand groupStateNotify(SetGroupStateCommand sgsc) {
        // If an empty list is returned by the splitter, it will be treated as single null item, so check
        if(sgsc != null) {
            getNotificationService().notifyStateUpdated(sgsc.getNewState());
        }
        return sgsc;
    }

    @Override
    protected void sendNotification(CurrentState<Group, GroupState> anUpdatedState) {
        // Do NOT forward the notification on, since we are the ones who created it, it would come right back in.
        // stateNotificationGateway.groupStateChanged(anUpdatedState);
    }
}
