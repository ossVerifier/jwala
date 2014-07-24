package com.siemens.cto.aem.service.state.impl;

import java.util.Collection;
import java.util.Set;

import org.joda.time.DateTime;
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
            StateNotificationService<CurrentState<?, ?>> theNotificationService, StateType theStateType,
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
    public void stateUpdateJvm(CurrentState<Jvm, JvmState> cjs) {

        LOGGER.debug("State Update Received");

        // alias
        GroupStateMachine gsm = groupStateMachine;

        // lookup children
        Identifier<Jvm> jvmId = cjs.getId();
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

            internalHandleJvmStateUpdate(gsm, jvmId, cjs.getState());

            if(gsm.getCurrentState() != groupState) {
                SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
                
                groupPersistenceService.updateGroupStatus(Event.create(sgsc, AuditEvent.now(systemUser)));
                super.setCurrentState(sgsc, systemUser);
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
    public void stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> wsState) {
        LOGGER.debug("State Update Received");

        // alias
        GroupStateMachine gsm = groupStateMachine;

        // lookup children
        Identifier<WebServer> wsId = wsState.getId();
        WebServer ws = webServerDao.getWebServer(wsId);

        if(ws == null) {
            return;
        }
        
        Collection<Group> groups = ws.getGroups();

        for(Group group : groups) {

            Group fullGroup = groupPersistenceService.getGroup(group.getId());
            CurrentGroupState currentGroupState =
                    fullGroup.getCurrentState() == null
                    ? null : fullGroup.getCurrentState();
            GroupState  groupState =
                    currentGroupState == null
                    ? null : currentGroupState.getState();

            gsm.initializeGroup(fullGroup, systemUser);

            internalHandleWebServerStateUpdate(gsm, wsId, wsState.getState());

            if(gsm.getCurrentState() != groupState) {
                SetGroupStateCommand sgsc= new SetGroupStateCommand(group.getId(), gsm.getCurrentState());
                groupPersistenceService.updateGroupStatus(Event.create(sgsc, AuditEvent.now(systemUser)));

                CurrentGroupState groupStateDetail = gsm.getCurrentStateDetail();
                LOGGER.info("Group State Service: " + groupStateDetail.toString());
            }
        }
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

    @Override
    protected CurrentState<Group, GroupState> createUnknown(Identifier<Group> anId) {
        return new CurrentGroupState(anId, GroupState.UNKNOWN, DateTime.now());
    }

    @Override
    protected void sendNotification(CurrentState<Group, GroupState> anUpdatedState) {
        stateNotificationGateway.groupStateChanged(anUpdatedState);
    }
}
